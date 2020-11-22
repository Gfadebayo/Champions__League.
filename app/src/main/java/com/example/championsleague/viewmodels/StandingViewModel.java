package com.example.championsleague.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.championsleague.database.LeagueRepository;
import com.example.championsleague.models.TeamInfo;

import java.util.List;

public class StandingViewModel extends AndroidViewModel {

    private final LeagueRepository mLeagueRepo;
    private Observer<List<TeamInfo>> positionObserver;

    public StandingViewModel(@NonNull Application application) {
        super(application);
        mLeagueRepo = LeagueRepository.getInstance(application);
    }
    
    public LiveData<List<TeamInfo>> getLatestTeamInfo(LifecycleOwner o){

        return mLeagueRepo.getCurrentStanding();
    }

    public List<TeamInfo> getTeams(){
        return mLeagueRepo.getTeams();
    }
}
