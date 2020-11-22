package com.example.championsleague.database;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.TeamInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeagueRepository {

    private final String TAG = getClass().getSimpleName();
    private static LeagueRepository leagueInstance;
    private final TeamDao mTeamDao;
    private final FixtureDao mFixtureDao;
    private final ScheduledExecutorService mExecutor;

    private LeagueRepository(Application app) {
        String DB_NAME = "db_league";
        LeagueDatabase db = Room.databaseBuilder(app.getApplicationContext(), LeagueDatabase.class, DB_NAME)
                .addMigrations(MIGRATE_2_8, MIGRATE_8_14, MIGRATE_14_17, MIGRATE_17_25, MIGRATE_25_32)
                .build();

        mTeamDao = db.getTeamDao();
        mFixtureDao = db.getFixtureDao();

        mExecutor = Executors.newScheduledThreadPool(20);
    }

    public static LeagueRepository getInstance(Application app) {
        if (leagueInstance == null) {
            leagueInstance = new LeagueRepository(app);
        }
        return leagueInstance;
    }

    public void updateFixtureDb(final List<FixtureInfo> fixture){
        Integer answer = -1;
        try{
            answer =
                    mExecutor.submit(() -> mFixtureDao.updateFixture(fixture)).get();
        }catch(InterruptedException | ExecutionException e){e.printStackTrace();}

    }

    public void updateTeamsDb(final List<TeamInfo> teams){
        mExecutor.submit(() -> mTeamDao.updateTeams(teams));
    }

    public void clearDb(){
        mExecutor.submit(() -> {
            mFixtureDao.emptyFixtureDb();
            mTeamDao.removeEverything();
            Log.i(TAG, "Database clearing done");
        });
    }

    /**
     * Update the names of the team
     * in both database
     * @param oldName The name of the team in the database
     * @param newName The name to be changed to
     * */
    public void updateTeamNames(final String oldName, final String newName){
        mExecutor.submit(() -> {
            mTeamDao.updateTeamName(oldName, newName);
            mFixtureDao.updateHomeName(oldName, newName);
            mFixtureDao.updateAwayName(oldName, newName);
        });
    }

    /**
     * Remove a team from both database
     * @param names The team name to remove*/
    public void deleteTeam(final String... names){
        mExecutor.submit(() -> {
            Arrays.stream(names).forEach(c -> mTeamDao.deleteTeams(new TeamInfo(c, -1)));
            Arrays.stream(names).forEach(mFixtureDao::deleteTeamFixtures);
        });
    }

    /**
     *
     * @return The names of the teams currently in the database
     * ordered by their position
     */
    public List<String> teamNames() {
        List<String> names = null;

        try {
            names = mExecutor.submit(() -> mTeamDao.allTeamsByName()).get();
        }catch(InterruptedException | ExecutionException e){e.printStackTrace();}

        return names;
    }

    public void insertTeams(final List<TeamInfo> teams) {
        try {
            mExecutor.schedule(() -> {
                mTeamDao.insertTeam(teams);
                Log.i(TAG, "Teams inserted successfully");
            }, 300, TimeUnit.MILLISECONDS).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void insertFixtures(final List<FixtureInfo> sendFixtures) {
        try {
            mExecutor.schedule(() -> {
                mFixtureDao.insertFixtures(sendFixtures);
                Log.i(TAG, "Fixtures inserted successfully");

            }, 300, TimeUnit.MILLISECONDS).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Provide a query and get a live data using SimpleSQLiteQuery
     * By default, this method returns uncompleted fixtures.
     * @param query The query to pass to Room
     * @param forComplete if to return the completed fixtures (true) or not (false)
     * @return A LiveData matching table information presented in the query
     */
    public List<FixtureInfo> getFixtures(final String query, boolean forComplete){
        List<FixtureInfo> monitoredFixtures = null;

        try {
            monitoredFixtures =
                    mExecutor.submit(() -> {

                        int index = query.lastIndexOf("WHERE");
                        StringBuilder build = new StringBuilder(query);

                        if(!forComplete) {
                            if (index == -1) build.append(" WHERE `home_score` < 0");
                            else build.append(" AND `home_score` < 0");
                        }else{
                            if(index == -1) build.append(" WHERE `home_score` >= 0");
                            else build.append(" AND `home_score` >= 0");
                        }

                        SimpleSQLiteQuery simpleQuery = new SimpleSQLiteQuery(build.toString());

                        return mFixtureDao.getFixtures(simpleQuery);
                    }).get();

        }catch(InterruptedException | ExecutionException e){e.printStackTrace();}

        return monitoredFixtures;
    }

    public FixtureInfo getLastCompletedFixture() {
        FixtureInfo f = null;

        try {
            f = mExecutor.submit(() -> mFixtureDao.lastFixtureSubmitted()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return f;
    }

    public List<FixtureInfo> getCompletedFixtures(String teamName){
        List<FixtureInfo> fixtures = null;

        try{
            if(teamName == null) fixtures = (mExecutor.submit(() -> mFixtureDao.getCompletedFixtures()).get());
            else fixtures = (mExecutor.submit(() -> mFixtureDao.getCompletedFixturesForTeam(teamName)).get());

        }catch (ExecutionException | InterruptedException e){ e.printStackTrace();}

        return fixtures;
    }

    /**
     * Same as {@link #getCompletedFixtures(String)} except this returns a Live Data of all the Fixtures
     * without any querying
     * @return A Live Data of all the fixtures in the database.
     */
    public LiveData<List<FixtureInfo>> getAllFixtures(){
        LiveData<List<FixtureInfo>> info = null;

        try {
            info = mExecutor.submit(() -> mFixtureDao.getCompleteLiveData()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return info;
    }

    public LiveData<List<TeamInfo>> getCurrentStanding() {
        LiveData<List<TeamInfo>> res = null;
        try {
            res =
                    mExecutor.submit(() -> mTeamDao.getCompleteLiveData()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public List<TeamInfo> getTeams(final String ...names){
        final List<TeamInfo> teams = new ArrayList<>();

        try {
            mExecutor.submit(() -> {
                if(names.length == 0) teams.addAll(mTeamDao.getTeams());
                else{
                    StringBuilder query = new StringBuilder("SELECT * FROM Teams WHERE name= NULL");
                    for(String n : names){
                        String args = String.format(" OR name= \"%s\"" , n);
                        query.append(args);
                    }
                    teams.addAll(mTeamDao.getTeams(new SimpleSQLiteQuery(query.toString())));
                }
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return teams;
    }

    public int teamCount() {
        int resu = 0;

        Future<Integer> res = mExecutor.submit(() -> mTeamDao.teamCount());
        try {
            resu = res.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resu;
    }


    public void nukeAllTeams() {
        mExecutor.execute(() -> mTeamDao.removeEverything());
    }

    public void nukeAllFixtures() {
        mExecutor.execute(() -> mFixtureDao.emptyFixtureDb());
    }

    public int getCompletedFixturesCount(){
        int complete = 0;
        try {
            complete =
            mExecutor.submit(() -> mFixtureDao.completedFixturesCount()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return complete;
    }

    public int totalFixtures(){
        int total = 0;
        try {
            total = mExecutor.submit(() -> mFixtureDao.totalFixturesCount()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return total;
    }

    private static final Migration MIGRATE_2_8 = new Migration(2, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("CREATE TABLE fixinfos(`Home Team` TEXT, `Away Team` TEXT, `Home Score` INTEGER NOT NULL," +
                    " `Away Score` INTEGER NOT NULL, `Fixture no` INTEGER NOT NULL PRIMARY KEY)");

            database.execSQL("INSERT INTO fixinfos(`Home Team`, `Away Team`, `Home Score`, `Away Score`) SELECT `Home Team`, `Away Team`, `Home Score`, `Away Score` FROM `Fixtures`");

            database.execSQL("DROP TABLE `Fixtures`");

            database.execSQL("ALTER TABLE fixinfos RENAME TO `Fixtures`");

            database.execSQL("CREATE TABLE infos(`Team Name` TEXT NOT NULL PRIMARY KEY DEFAULT 0, `Points` INTEGER NOT NULL, `Position` INTEGER NOT NULL, `Goal Difference` INTEGER NOT NULL, id INTEGER NOT NULL)");

            database.execSQL("INSERT INTO infos(`Team Name`, Points, Position, `Goal Difference`, id) SELECT `Team Name`, Points, Position, `Goal Difference`, id FROM `Team Information`");

            database.execSQL("DROP TABLE `Team Information`");

            database.execSQL("ALTER TABLE infos RENAME TO `Team Information`");
        }
    };

    private static final Migration MIGRATE_8_14 = new Migration(8, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("CREATE TABLE fixinfos(`Home Team` TEXT, `Away Team` TEXT," +
                    " `Home Score` INTEGER NOT NULL, `Away Score` INTEGER NOT NULL, `Fixture no` INTEGER NOT NULL PRIMARY KEY, `Leg` INTEGER NOT NULL DEFAULT 0)");

            database.execSQL("INSERT INTO fixinfos(`Home Team`, `Away Team`, `Home Score`, `Away Score`, `Fixture no`) " +
                    "SELECT `Home Team`, `Away Team`, `Home Score`, `Away Score`, `Fixture no`FROM `Fixtures`");

            database.execSQL("DROP TABLE `Fixtures`");

            database.execSQL("ALTER TABLE fixinfos RENAME TO `Fixtures`");


            database.execSQL("CREATE TABLE infos(`Team Name` TEXT NOT NULL PRIMARY KEY, `Points` INTEGER NOT NULL," +
                    " `Position` INTEGER NOT NULL, `Goal Difference` INTEGER NOT NULL, id INTEGER NOT NULL," +
                    " Wins INTEGER NOT NULL DEFAULT 0, Losses INTEGER NOT NULL DEFAULT 0, Draws INTEGER NOT NULL DEFAULT 0)");

            database.execSQL("INSERT INTO infos(`Team Name`, Points, Position, `Goal Difference`, id)" +
                    " SELECT `Team Name`, Points, Position, `Goal Difference`, id FROM `Team Information`");

            database.execSQL("DROP TABLE `Team Information`");

            database.execSQL("ALTER TABLE infos RENAME TO `Team Information`");
        }
    };

    private static final Migration MIGRATE_14_17 = new Migration(14, 17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `Team Information` ADD COLUMN Logo TEXT");
            database.execSQL("ALTER TABLE `Fixtures` ADD COLUMN homeLogo TEXT");
            database.execSQL("ALTER TABLE Fixtures ADD COLUMN awayLogo TEXT");
        }
    };

    private static final Migration MIGRATE_17_25 = new Migration(17, 25) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE fixinfos(`Home Team` TEXT, `Away Team` TEXT," +
                    " `Home Score` INTEGER NOT NULL, `Away Score` INTEGER NOT NULL," +
                    " `Fixture no` INTEGER NOT NULL PRIMARY KEY, `Leg` INTEGER NOT NULL DEFAULT 0)" +
                    " ");

            database.execSQL("INSERT INTO fixinfos(`Home Team`, `Away Team`, `Home Score`, `Away Score`, `Fixture no`) " +
                    "SELECT `Home Team`, `Away Team`, `Home Score`, `Away Score`, `Fixture no`FROM `Fixtures`");

            database.execSQL("DROP TABLE `Fixtures`");

            database.execSQL("ALTER TABLE fixinfos RENAME TO `Fixtures`");

            database.execSQL("CREATE TABLE infos(`Team Name` TEXT NOT NULL PRIMARY KEY, `Points` INTEGER NOT NULL," +
                    " `Position` INTEGER NOT NULL, `Goal Difference` INTEGER NOT NULL, id INTEGER NOT NULL," +
                    " Wins INTEGER NOT NULL DEFAULT 0, Losses INTEGER NOT NULL DEFAULT 0, Draws INTEGER NOT NULL DEFAULT 0, " +
                    " `Logo` TEXT)");

            database.execSQL("INSERT INTO infos(`Team Name`, Points, Position, `Goal Difference`, id, Wins, Losses, Draws)" +
                    " SELECT `Team Name`, Points, Position, `Goal Difference`, id, Wins, Losses, Draws FROM `Team Information`");

            database.execSQL("DROP TABLE `Team Information`");

            database.execSQL("ALTER TABLE infos RENAME TO `Team Information`");        }
    };

    private static final Migration MIGRATE_25_32 = new Migration(25, 32) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Teams(`position` INTEGER NOT NULL, `name` TEXT NOT NULL PRIMARY KEY, " +
                    "`played` INTEGER NOT NULL, `goal_diff` INTEGER NOT NULL, `wins` INTEGER NOT NULL, `losses`" +
                    " INTEGER NOT NULL, `draws` INTEGER NOT NULL, `points` INTEGER NOT NULL)");

            database.execSQL("INSERT INTO Teams(`position`, `name`, `played`, `goal_diff`, `wins`, `losses`, `draws`, `points`)" +
                    " SELECT `Position`, `Team Name`, `id`, `Goal Difference`, `Wins`, `Losses`, `Draws`, `Points` FROM `Team Information`");

            database.execSQL("DROP TABLE `Team Information`");

            database.execSQL("CREATE TABLE fixinfos(`fixture_no` INTEGER NOT NULL PRIMARY KEY, `home_team` TEXT, " +
                    " `home_score` INTEGER NOT NULL, `away_team` TEXT, `away_score` INTEGER NOT NULL," +
                    " `leg` INTEGER NOT NULL, `date` INTEGER NOT NULL DEFAULT 0)");

            database.execSQL("INSERT INTO fixinfos(`home_team`, `away_team`, `home_score`, `away_score`, `fixture_no`, `leg`) " +
                    "SELECT `Home Team`, `Away Team`, `Home Score`, `Away Score`, `Fixture no`, `Leg` FROM `Fixtures`");

            database.execSQL("DROP TABLE `Fixtures`");

            database.execSQL("ALTER TABLE fixinfos RENAME TO Fixtures");

        }
    };
}