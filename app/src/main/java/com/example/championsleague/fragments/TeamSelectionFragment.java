package com.example.championsleague.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.championsleague.databinding.TeamTextViewBinding;
import com.example.championsleague.utils.DialogUtils;
import com.example.championsleague.viewmodels.SelectionViewModel;
import com.example.championsleague.R;
import com.example.championsleague.utils.FileUtils;
import com.example.championsleague.adapters.TeamListAdapter;
import com.example.championsleague.databinding.FragmentTeamSelectBinding;
import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.LeagueInfo;
import com.example.championsleague.models.TeamInfo;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TeamSelectionFragment extends Fragment implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();

    //Bindings
    public FragmentTeamSelectBinding mRootBinding;

    private SelectionViewModel mViewModel;
    private TeamListAdapter mAdapter;
    public List<Integer> mItems;
    private NiceSpinner mSpinner;

    private SharedPreferences mPref;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(
                requireActivity().getApplication(), this)).get(SelectionViewModel.class);
        mViewModel.initialHandleValues();
        mViewModel.watchTeamChanges(requireActivity());

        setRetainInstance(true);
        mPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mPref.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> setSpinnerValues(sharedPreferences));
    }

    private void setSpinnerValues(SharedPreferences pref){
        mItems = IntStream.range(4, Integer.parseInt(pref.getString("KEY_MAX_TEAM",  "100"))+1).boxed().collect(Collectors.toList());
        mItems.add(0, 0);

        mSpinner.attachDataSource(mItems);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_team_select, container, false);
        mRootBinding = FragmentTeamSelectBinding.bind(root);
        mRootBinding.setFragment(this);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mAdapter = new TeamListAdapter(mViewModel.getTeamNames(), this);

        mRootBinding.listTeams.setAdapter(mAdapter);
        mSpinner = mRootBinding.getRoot().findViewById(R.id.spinner_teams);
        setSpinnerValues(mPref);

        int count = mItems.indexOf(mAdapter.getItemCount()-1);
        count = count == -1 ? mItems.indexOf(mItems.size()-1) : count;
        Log.i("Selection", String.valueOf(mItems.indexOf(count)));
        mSpinner.setSelectedIndex(mItems.indexOf(count < 4 ? 0 : count));

        setSpinnerVariables();
    }

    private void setSpinnerVariables() {

        mSpinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            int value = (Integer) parent.getItemAtPosition(position);
            mAdapter.updateTeamSize(value);
            mViewModel.setSpinnerPosition(position);
            mViewModel.setNewTeamName(mAdapter.getList());
        });
    }

    /**
     * Add the chosen existing teams to the recycler view
     * @param teamList The existing teams
     */
    void addToList(Collection<String> teamList) {

        mAdapter.addTeams(new ArrayList<>(teamList));

        int index = mItems.indexOf(mAdapter.getItemCount()-1);
        mSpinner.setSelectedIndex(index);
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null) return;

        String name = ((TeamTextViewBinding) ((TeamListAdapter.ViewHolder) mRootBinding.listTeams.
                findViewHolderForAdapterPosition(requestCode)).mBinding).editText.getText().toString();

        try{
            Uri uri = data.getData();
            InputStream pathStream = requireContext().getContentResolver().openInputStream(uri);

            FileUtils.saveImage(pathStream, name, requireContext().getExternalFilesDir(FileUtils.TEAMS_LOGO_DIR));

            Snackbar.make(getView(), name + " Logo added", BaseTransientBottomBar.LENGTH_SHORT)
                    .setAnchorView(R.id.bottom_nav_view).show();

        }catch(IOException e){e.printStackTrace();}
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.button_create_league || v.getId() == R.id.button_update_league){
            int num = (Integer) mSpinner.getSelectedItem();
            if(num == 0){
                Toast.makeText(requireContext(), "At least 4 teams need to be Selected", Toast.LENGTH_SHORT).show();
                mSpinner.setSelectedIndex(1);
                return;
            }
        }

        switch(v.getId()){
            case R.id.button_create_league:
                mViewModel.clearDb();
                AlertDialog waitDialog = DialogUtils.pleaseWaitDialog(TeamSelectionFragment.this);
                waitDialog.show();

                List<TeamInfo> teams = LeagueInfo.createTeams(mAdapter.getList(), null);

                List<String> teamNames = teams.stream().map(TeamInfo::getName).collect(Collectors.toList());
                List<FixtureInfo> fixtures = LeagueInfo.createFixtures(teamNames, null);

                mViewModel.insertDbData(waitDialog, teams, fixtures, this);
                mViewModel.addToTeamsDb(teams);
                mViewModel.addToFixturesDb(fixtures);
                break;

            case R.id.button_existing_teams:
                ExistingTeamDialogFragment etd = ExistingTeamDialogFragment.getInstance();
                etd.show(getChildFragmentManager(), ExistingTeamDialogFragment.TAG);
//                DialogUtils.existingTeamsDialog(TeamSelectionFragment.this);
                break;

            case R.id.button_update_league:
                updateDatabase();
                DialogUtils.saveTeamsDialog(requireActivity(), mAdapter.getList());
                break;
        }
    }

    private void updateDatabase() {

        requireActivity().runOnUiThread(() -> {
            List<String> dbTeams;

            dbTeams = mViewModel.dbTeamNames();

            //TODO: Fix this, so that instead of removing all the fixtures and teams, you simply update with the new ones
            List<TeamInfo> newTeams = LeagueInfo.createTeams(mAdapter.getList(), dbTeams);
            mViewModel.addToTeamsDb(newTeams);

            List<String> allTeams = new ArrayList<>(dbTeams);
            allTeams.addAll(newTeams.parallelStream().map(TeamInfo::getName).distinct().collect(Collectors.toList()));

            List<FixtureInfo> newFixtures = LeagueInfo.createFixtures(allTeams, dbTeams);

            mViewModel.addToFixturesDb(newFixtures);
        });
    }
}
