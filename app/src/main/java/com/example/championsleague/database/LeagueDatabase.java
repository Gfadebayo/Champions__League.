package com.example.championsleague.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.TeamInfo;

@Database(entities = {FixtureInfo.class, TeamInfo.class}, version = 32, exportSchema = false)
public abstract class LeagueDatabase extends RoomDatabase {

    public abstract FixtureDao getFixtureDao();

    public abstract TeamDao getTeamDao();
}
