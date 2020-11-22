package com.example.championsleague.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.adapters.FixtureAdapter;
import com.example.championsleague.R;
import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.utils.LeagueUtils;
import com.example.championsleague.viewmodels.FixtureViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FixtureFragment extends Fragment implements FilterDialogFragment.OnDialogItemClicked {


    private FixtureViewModel mViewModel;
    private FixtureAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(requireActivity()
                .getApplication(), requireActivity())).get(FixtureViewModel.class);
        mViewModel.initHandle();
        mViewModel.registerPrefObserver(requireActivity());
        mViewModel.loadTeamLogos();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fixture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<FixtureInfo> fixtures = mViewModel.getFixtures();

        mAdapter = new FixtureAdapter(this, fixtures, mViewModel.getComplete());
        RecyclerView mRecyclerView = view.findViewById(R.id.fixture_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);

        view.findViewById(R.id.fab_mass_submit).setOnClickListener(v -> {
            if(!mViewModel.getComplete()) startMassSubmit();
        });
    }

    private void startMassSubmit() {
        final List<FixtureInfo> size = mAdapter.getCurrentList();

        new Thread(() -> {
            List<FixtureInfo> validFixtures = new ArrayList<>(size.size());
            validFixtures.addAll(size.stream().filter(fixtureInfo -> fixtureInfo.getHomeScore() > -1
                    && fixtureInfo.getAwayScore() > -1).collect(Collectors.toList()));

            mViewModel.performSubmission(validFixtures);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.i("Fixture", "Getting new Fixtures");
                List<FixtureInfo> newFixtures = mViewModel.getFixtures();
                mAdapter.submitList(newFixtures);
            }, 200);
        }).start();
    }

    private Snackbar makeSnackBar(View anchor, final FixtureInfo fixNo){

        String fix = String.format("Fixture %s has no Score", fixNo.getFixtureNo());

        return Snackbar.make(anchor, fix, Snackbar.LENGTH_SHORT).setAction("Predict", v -> {
            List<int[]> scores = LeagueUtils.generateRandomScores(1);
            fixNo.setHomeScore(scores.get(0)[0]);
            fixNo.setAwayScore(scores.get(0)[1]);
            startMassSubmit();
        }).setAnchorView(R.id.bottom_nav_view);
    }

    public void updateToolbar(int count, int total){

        Toolbar bar = requireActivity().findViewById(R.id.toolbar);
        String subTitle = "Showing " + count + "/" + total + " Fixtures";
        bar.setSubtitle(subTitle);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fixture_menu, menu);

        int color = requireContext().getResources().getColor(R.color.actionBarTint, null);
        IntStream.range(0, menu.size()).forEach(i -> {
            Drawable icon = menu.getItem(i).getIcon();
            if(icon != null) icon.setTint(color);
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {

        boolean com = mViewModel.hasCompletedFixtures();
        menu.findItem(R.id.action_undo).setEnabled(com);

        menu.findItem(R.id.action_fixtures).setChecked(mViewModel.getComplete());

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean isRightMenu = false;
        int menuId = item.getItemId();

        switch (menuId) {
            case (R.id.action_undo):
                mViewModel.undo(mAdapter);
                isRightMenu = true;
                break;
            case (R.id.action_predict):
                predictScores();
                isRightMenu = true;
                break;
            case (R.id.action_filter):
                FilterDialogFragment.dialogInstance().show(getChildFragmentManager(), null);
                break;

            case (R.id.action_fixtures):
                boolean val = item.isChecked();
                mViewModel.setComplete(!val);
                item.setChecked(!val);
                mAdapter.setSubmittable(!val);
                mAdapter.submitList(mViewModel.getFixtures());
                isRightMenu = true;
                break;
        }

        return isRightMenu;
    }

    private void predictScores() {

        List<FixtureInfo> currentFixtures = new ArrayList<>(mAdapter.getCurrentList());
        List<int[]> predictedScores = LeagueUtils.generateRandomScores(currentFixtures.size());
        SparseArray<int[]> sparseScores = new SparseArray<>(currentFixtures.size());
        for(int i = 0; i < currentFixtures.size(); i++) {
            FixtureInfo f = currentFixtures.get(i);

            sparseScores.put(f.getFixtureNo(), predictedScores.get(i));
            f.setHomeScore(predictedScores.get(i)[0]);
            f.setAwayScore(predictedScores.get(i)[1]);
        }

        mViewModel.setPredictions(sparseScores);
        mAdapter.submitList(currentFixtures);
    }

    @Override
    public void onItemClicked(String s, int id, Boolean checked) {
        mViewModel.resolveQuery(s, id, checked);
        mAdapter.submitList(mViewModel.getFixtures());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((Toolbar) requireActivity().findViewById(R.id.toolbar)).setSubtitle(null);
    }
}
