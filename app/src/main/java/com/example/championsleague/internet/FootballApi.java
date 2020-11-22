package com.example.championsleague.internet;

import com.example.championsleague.models.League;
import com.example.championsleague.models.TeamEmpty;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FootballApi {

    @GET("/v2/competitions/")
    Observable<List<League>> getLeagues();

    @GET("/v2/competitions/{id}/teams")
    Observable<List<TeamEmpty>> getTeams(@Path(value = "id", encoded = true) int id);
}