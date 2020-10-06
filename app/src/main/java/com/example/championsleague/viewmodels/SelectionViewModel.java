package com.example.championsleague.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;

import com.example.championsleague.database.LeagueRepository;
import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.TeamInfo;
import com.example.championsleague.utils.DialogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class SelectionViewModel extends AndroidViewModel {

    private final LeagueRepository mLeagueRepo;
    private final SavedStateHandle mHandle;
    private static final String KEY_SPIN = "spinner_position";
    private static final String KEY_TEAM_NAME = "team_names";

    private Observer<List<TeamInfo>> mTeamDbObserver;
    private Observer<List<FixtureInfo>> mFixtureDbObserver;


    public SelectionViewModel(Application app, SavedStateHandle handle) {
        super(app);
        mLeagueRepo = LeagueRepository.getInstance(app);
        mHandle = handle;
    }

    public void initialHandleValues() {
        if (mHandle.get(KEY_TEAM_NAME) == null)
            setNewTeamName(mLeagueRepo.teamNames());

        if (mHandle.get(KEY_SPIN) == null) setSpinnerPosition(0);
    }

    public List<String> getTeamNames() {
        return mHandle.get(KEY_TEAM_NAME);
    }

    public void setSpinnerPosition(int newValue) {
        mHandle.set(KEY_SPIN, newValue);
    }

    public void setNewTeamName(List<String> newName) {
        mHandle.set(KEY_TEAM_NAME, newName);
    }

    public List<String> dbTeamNames(){
        return mLeagueRepo.teamNames();
    }

    public void clearDb(){
       mLeagueRepo.clearDb();
    }

    public void addToTeamsDb(List<TeamInfo> teamList){
        mLeagueRepo.insertTeams(teamList);
    }

    public void addToFixturesDb(List<FixtureInfo> fixtureList){
        mLeagueRepo.insertFixtures(fixtureList);
    }

    private LiveData<List<FixtureInfo>> getLiveFixture(){
        return mLeagueRepo.getAllFixtures();
    }

    /**
     * A simple method to check and wait till every data is put in the database before proceeding
     * @param dialog A wait dialog to display
     * @param teams The teams to be entered
     * @param fixtures The fixtures to be entered
     */
    public void insertDbData(final AlertDialog dialog, final List<TeamInfo> teams, final List<FixtureInfo> fixtures, final Fragment caller){
        //fixture is index 0 and team is index 1
        final boolean[] done = new boolean[2];

        caller.requireActivity().runOnUiThread(() -> {

            getLiveFixture().observe(caller.requireActivity(), fixtureInfos -> done[0] = fixtures.equals(fixtureInfos));

            mLeagueRepo.getCurrentStanding().observe(caller.requireActivity(), teamInfos -> done[1] = teams.equals(teamInfos));

            new Thread(() -> {
                while(true){
                    if(done[0] && done[1]) {
                        dialog.dismiss();
                        new Handler(Looper.getMainLooper()).post(() -> DialogUtils.saveTeamsDialog(caller.requireActivity(), dbTeamNames()));
                        break;
                    }
                }
            }).start();
        });
    }

    public void watchTeamChanges(LifecycleOwner o){
        if(mTeamDbObserver == null){
            Log.i("Table", "Registering observer");
            mTeamDbObserver = (teamInfos -> new Thread(() -> {
                boolean checker = false;

                for(int i = 0; i < teamInfos.size(); i++)
                    if (teamInfos.get(i).getPosition() != i + 1) {
                        checker = true;
                        break;
                    }

                if(checker) {
                    ArrayList<TeamInfo> newOne = new ArrayList<>(teamInfos);
                    Log.i("Fixtures", "Changing team Positions");
                    //noinspection unchecked
                    Collections.sort(newOne);
                    newOne.parallelStream().forEach(teamInfo -> teamInfo.setPosition(newOne.indexOf(teamInfo) + 1));

                    mLeagueRepo.updateTeamsDb(newOne);
                }
            }).start());
            mLeagueRepo.getCurrentStanding().observe(o, mTeamDbObserver);
        }

        if (mFixtureDbObserver == null){
            mFixtureDbObserver = (fixtureInfos -> new Thread(() -> {
                boolean checker = false;

                for(int i = 0; i < fixtureInfos.size(); i++) {
                    if (fixtureInfos.get(i).getFixtureNo() != i + 1) {
                        checker = true;
                        break;
                    }
                }

                if(checker) {
                    ArrayList<FixtureInfo> upFix = new ArrayList<>(fixtureInfos);
                    IntStream.range(1, upFix.size() + 1).forEach(a -> upFix.get(a - 1).setFixtureNo(a));

                    //TODO: URGENT Fix this by changing the primary key from the fixture number so it can be updated
//                    mLeagueRepo.updateFixtureDb(upFix);


                    mLeagueRepo.nukeAllFixtures();
                    mLeagueRepo.insertFixtures(upFix);
                }
            }).start());

            mLeagueRepo.getAllFixtures().observe(o, mFixtureDbObserver);
        }
    }
}
