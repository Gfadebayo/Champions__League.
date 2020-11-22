package com.example.championsleague.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.championsleague.models.FixtureInfo;

import java.util.List;

@Dao
public interface FixtureDao {

    @Update()
    int updateFixture(List<FixtureInfo> fixture);

    @Insert()
    void insertFixtures(List<FixtureInfo> fixtures);

    @Query("DELETE FROM Fixtures")
    void emptyFixtureDb();

    @Query("SELECT * FROM Fixtures")
    LiveData<List<FixtureInfo>> getCompleteLiveData();

    @Query("UPDATE `Fixtures` SET `home_team` = CASE WHEN `home_team` = :oldName THEN :newName ELSE home_team END")
    void updateHomeName(String oldName, String newName);

    @Query("UPDATE Fixtures SET `away_team` = CASE WHEN `away_team` = :oldName THEN :newName ELSE away_team END")
    void updateAwayName(String oldName, String newName);

    @Query("DELETE FROM Fixtures WHERE (`home_team` = :name OR `away_team`= :name)")
    void deleteTeamFixtures(String name);

    @Query("SELECT * FROM Fixtures WHERE home_score > -1")
    List<FixtureInfo> getCompletedFixtures();

    @Query("SELECT * FROM Fixtures WHERE (home_team= :name OR away_team= :name) AND home_score > -1")
    List<FixtureInfo> getCompletedFixturesForTeam(String name);

    @RawQuery
    List<FixtureInfo> getFixtures(SupportSQLiteQuery query);

    @Query("SELECT COUNT(*) FROM Fixtures WHERE home_score > -1")
    int completedFixturesCount();

    @Query("SELECT * FROM Fixtures WHERE home_score > -1 ORDER BY date DESC LIMIT 1")
    FixtureInfo lastFixtureSubmitted();

    @Query("SELECT COUNT(*) FROM Fixtures")
    int totalFixturesCount();
}
