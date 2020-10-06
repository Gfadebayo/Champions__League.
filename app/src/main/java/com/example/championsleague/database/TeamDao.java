package com.example.championsleague.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.championsleague.models.TeamInfo;

import java.util.List;

@Dao
public interface TeamDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTeam(List<TeamInfo> teams);

    @Delete
    void deleteTeams(TeamInfo teams);

    @Query("SELECT * FROM `Teams` ORDER BY points DESC, goal_diff DESC, name")
    LiveData<List<TeamInfo>> getCompleteLiveData();

    @Query("SELECT `name` FROM `Teams` ORDER BY position")
    List<String> allTeamNames();

    @Query("SELECT `name` FROM Teams ORDER BY name")
    List<String> allTeamsByName();

    @Query("SELECT * FROM `Teams`")
    List<TeamInfo> getTeams();

    @RawQuery
    List<TeamInfo> getTeams(SupportSQLiteQuery query);

    @Update
    void updateTeams(List<TeamInfo> teams);

    @Query("DELETE FROM `Teams`")
    void removeEverything();

    @Query("SELECT * FROM `Teams` WHERE `name` = :name")
    TeamInfo selectedTeams(String name);

    @Query("SELECT COUNT(*) FROM `Teams`")
    int teamCount();

    @Query("UPDATE `Teams` SET `name` = :newName WHERE `name` = :oldName")
    void updateTeamName(String oldName, String newName);
}
