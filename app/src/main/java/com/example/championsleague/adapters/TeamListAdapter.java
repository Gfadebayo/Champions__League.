package com.example.championsleague.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.R;
import com.example.championsleague.database.LeagueRepository;
import com.example.championsleague.databinding.TeamSelectButtonBinding;
import com.example.championsleague.databinding.TeamTextViewBinding;
import com.example.championsleague.fragments.TeamSelectionFragment;
import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.utils.LeagueUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

public class TeamListAdapter extends RecyclerView.Adapter<TeamListAdapter.ViewHolder> {
    private final String TAG = getClass().getSimpleName();
    private final List<String> mTeams;
    private final TeamSelectionFragment mFragment;
    private final LeagueRepository mRepository;
    private final LayoutInflater mInflater;

    public TeamListAdapter(List<String> teams, Fragment frag){
        mTeams = teams;
        mFragment = (TeamSelectionFragment) frag;
        mInflater = LayoutInflater.from(frag.requireContext());
        mRepository = LeagueRepository.getInstance(frag.getActivity().getApplication());
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding mBinding;

        if(viewType == 0) mBinding = DataBindingUtil.bind(mInflater.inflate(R.layout.team_text_view, parent, false));
        else mBinding = DataBindingUtil.bind(mInflater.inflate(R.layout.team_select_button, parent, false));

        return new ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(!mTeams.isEmpty() && position != mTeams.size()) {
            String team = null;
            if (position < mTeams.size()) team = mTeams.get(position);

            TeamTextViewBinding bind = ((TeamTextViewBinding) holder.mBinding);
            bind.editText.setText(team);

        }else ((TeamSelectButtonBinding) holder.mBinding).setFragment(mFragment);
    }

    @Override
    public long getItemId(int position) {
        return position >= mTeams.size() ? R.id.constraint_buttons : mTeams.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position >= mTeams.size() ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return mTeams.size() + 1;
    }

    /**
     * Change the number of Edit Text shown.
     * @param newSize The number used to determine the amount of Edit Text shown
     */
    public void updateTeamSize(int newSize){

            if(mTeams.size() > newSize) {
                List<String> obsoleteTeams = mTeams.subList(newSize, mTeams.size());
                eraseObsoleteTeams(new ArrayList<>(obsoleteTeams));
                mTeams.removeAll(obsoleteTeams);
                notifyItemRangeRemoved(newSize, mTeams.size() - newSize);
                notifyDataSetChanged();

            } else if(mTeams.size() < newSize) {
                notifyItemRangeInserted(mTeams.size(), newSize - mTeams.size());
                insertDefaultText(newSize - mTeams.size());
                notifyItemRangeChanged(0, mTeams.size());
            }
    }

    private void eraseObsoleteTeams(final List<String> obsoleteTeams) {
        new Thread(() -> {
            List<String> obs = new ArrayList<>(obsoleteTeams);
            obs.forEach(team -> {
                List<FixtureInfo> fixtures = mRepository.getCompletedFixtures(team);
                LeagueUtils.batchUpdateDb(fixtures, mRepository, false);
            });

            mRepository.deleteTeam(obsoleteTeams.toArray(new String[0]));
            Snackbar.make(mFragment.getView(), "Removed " + obs.size() + " teams", Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.bottom_nav_view).show();
        }).start();
    }

    /**
     * Pass if available teams which will then be put into a corresponding Edit Text
     * @param teams Teams to use to populate the Edit Text
     */
    public void addTeams(List<String> teams){

            if(validateTeams(teams)) return;

            notifyItemRangeInserted(mTeams.size(), teams.size());

            mTeams.addAll(teams);

            notifyItemRangeChanged(0, mTeams.size());
    }

    /**
     * Checks thoroughly the teams to be added and removes duplicates if found
     * @param teams The teams to be added
     * @return True if the parameter is empty as a result of the checks made
     */
    private boolean validateTeams(List<String> teams){
        for(String t : teams){
            for(int i = 0; i < mTeams.size(); i++){
                if(mTeams.get(i).equalsIgnoreCase(t)){
                    teams.remove(t);
                    break;
                }
            }
        }

        return teams.isEmpty();
    }

    private void insertDefaultText(int amount){
        int size = mTeams.size();
        for(int i = 0; i < size+amount; i++) {

            String text = "Team " + (i+1);
            if(i < 9) text = new StringBuilder(text).insert(text.length()-1, '0').toString();


            if(!mTeams.isEmpty() && mTeams.size() > i &&
                    (mTeams.get(i).equals(text) || !mTeams.get(i).contains("Team"))) continue;
            mTeams.add(i, text);
        }
    }

    public List<String> getList(){
        return mTeams;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewDataBinding mBinding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            binding.executePendingBindings();

            if(mBinding instanceof TeamTextViewBinding) initCallbacks((TeamTextViewBinding) binding);
        }

        private void initCallbacks(TeamTextViewBinding binding){


            binding.imageTeamLogo.setOnClickListener((v) -> {
                Intent fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                fileIntent.setType("image/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

                mFragment.startActivityForResult(fileIntent, getAdapterPosition());
            });

            binding.teamTextLayout.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                String prevName;
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.i("Teams", "Focus changed to " + hasFocus);
                    if(!hasFocus){
                        String newName = ((TextInputEditText) v).getText().toString();
                        if(mTeams.contains(prevName) && !prevName.equalsIgnoreCase(newName)){
                            mTeams.set(mTeams.indexOf(prevName), newName);
                            mRepository.updateTeamNames(prevName, newName);
                            Snackbar.make(binding.getRoot(), "Team Renamed Successfully", Snackbar.LENGTH_SHORT)
                                    .setAnchorView(R.id.bottom_nav_view).show();
                        }
                    }else{
                        prevName = ((TextInputEditText) v).getText().toString();
                    }

                    View view1 = binding.teamTextLayout.getEditText().focusSearch(View.FOCUS_DOWN);
                    if(view1 == null) binding.teamTextLayout.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
            });


            binding.buttonRemoveTeam.setOnClickListener(v -> {
                //TODO: Dont forget to delete the image path too
                int pos = getAdapterPosition();
                String removedTeam = binding.editText.getText().toString();
//                FileUtils.deleteImage(removedTeam, mFragment.requireContext().getExternalFilesDir(FileUtils.TEAMS_LOGO_DIR));
                List<FixtureInfo> removedFixtures = mRepository.getCompletedFixtures(removedTeam);

                LeagueUtils.batchUpdateDb(removedFixtures, mRepository, false);

                mRepository.deleteTeam(removedTeam);

                notifyItemRemoved(pos);
                Snackbar.make(mFragment.getView(), removedTeam + " has been removed", Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.bottom_nav_view).show();
                if (mTeams.size() >= pos) {
                    mTeams.remove(pos);

                    List<String> ada = mFragment.mItems;

                    int indexPos = ada.indexOf(String.valueOf(mTeams.size()));
                    ((PowerSpinnerView) mFragment.mRootBinding.getRoot().findViewById(R.id.spinner_teams)).selectItemByIndex(indexPos);
                }
            });

        }
    }
}
