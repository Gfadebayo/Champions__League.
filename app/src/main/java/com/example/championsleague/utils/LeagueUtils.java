package com.example.championsleague.utils;

import androidx.annotation.Nullable;

import com.example.championsleague.database.LeagueRepository;
import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.TeamInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LeagueUtils {

    /**
     * creates FixtureInfo objects from the given TeamInfo objects. The objects created are given wrong fixture numbers
     * so ensure the fixture numbers are corrected
     * @param newTeams The TeamInfo objects to create Fixtures for
     * @param presentTeams The Teams already present in the database in order to prevent fixture recreation
     * @return A list of Fixture Info objects
     */
    public static List<FixtureInfo> createFixtures(List<String> newTeams, @Nullable List<String> presentTeams) {

                    int noOfFixtures = newTeams.size() * (newTeams.size() - 1);
                    int fixtureStart = 0;
                    FixtureInfo[] turesArr = new FixtureInfo[noOfFixtures];
                    for (int j = 0; j < newTeams.size() - 1; j++) {

                        for (int k = j + 1; k < newTeams.size(); k++) {

                            if (k == j) continue;

                            String home = newTeams.get(j);
                            String away = newTeams.get(k);

                            if(presentTeams != null && presentTeams.contains(home) && presentTeams.contains(away)) continue;

                            turesArr[fixtureStart] = new FixtureInfo(home, away, 1, fixtureStart + 1);
                            turesArr[(noOfFixtures - 1)] = new FixtureInfo(away, home, 2, noOfFixtures);

                            noOfFixtures--;
                            fixtureStart++;
                        }
                    }
                    List<FixtureInfo> tures = Stream.of(turesArr).filter(Objects::nonNull).collect(Collectors.toList());

                    setFixtureNo(tures, presentTeams != null ? presentTeams.size() : 0);
                    return tures;
                }

    private static void setFixtureNo(List<FixtureInfo> fixtures, int startPosition){
        Collections.sort(fixtures, (o1, o2) -> Integer.compare(o1.getLeg(), o2.getLeg()));

        Collections.shuffle(fixtures.subList(0, (fixtures.size() / 2)));
        Collections.shuffle(fixtures.subList(fixtures.size() / 2, fixtures.size()));

        startPosition = startPosition * (startPosition - 1);
        for(int i = startPosition, j = 0; j < fixtures.size(); i++, j++){
            fixtures.get(j).setFixtureNo(i+1);
        }
    }

    /**
     * Creates Team Info objects from the given string list. The infos created are already given their starting
     * position in the table
     * @param teams The teams to create.
     * @param oldTeamNames Old teams from the database if present in order to prevent recreation
     * @return A list of team info created from @params teams
     */
    public static List<TeamInfo> createTeams(final List<String> teams, @Nullable final List<String> oldTeamNames){

               Collections.sort(teams);
               List<TeamInfo> leagueTeams = new ArrayList<>();
               int startPosition = oldTeamNames != null ? oldTeamNames.size() + 1 : 1;

                  for (String teamName : teams) {

                       if(oldTeamNames != null && oldTeamNames.contains(teamName)) continue;

                       TeamInfo team = new TeamInfo(teamName, startPosition);
                       startPosition++;

                       leagueTeams.add(team);
                   }

                   return leagueTeams;
               }


    private static void evaluateResult(FixtureInfo result, List<TeamInfo> involvedTeams, boolean isEvaluating){

        TeamInfo homeTeam;
        TeamInfo awayTeam;

        if(result.getHomeTeam().equals(involvedTeams.get(0).getName())) {
            homeTeam = involvedTeams.get(0);
            awayTeam = involvedTeams.get(1);
        }else{
            homeTeam = involvedTeams.get(1);
            awayTeam = involvedTeams.get(0);
        }

        //A positive goal diff indicates the home team's victory
        int goalDiff = result.getHomeScore() - result.getAwayScore();

        if(isEvaluating) {
            if (goalDiff > 0) {
                homeTeam.incrementPoint(3);
                homeTeam.setWins(homeTeam.getWins() + 1);
                awayTeam.setLosses(awayTeam.getLosses() + 1);
            } else if (goalDiff < 0) {
                awayTeam.incrementPoint(3);
                awayTeam.setWins(awayTeam.getWins() + 1);
                homeTeam.setLosses(homeTeam.getLosses() + 1);
            } else {
                homeTeam.incrementPoint(1);
                awayTeam.incrementPoint(1);
                homeTeam.setDraws(homeTeam.getDraws() + 1);
                awayTeam.setDraws(awayTeam.getDraws() + 1);
            }

            homeTeam.incrementGoalDiff(goalDiff);
            awayTeam.incrementGoalDiff(-goalDiff);

            homeTeam.setPlayed(homeTeam.getPlayed() + 1);
            awayTeam.setPlayed(awayTeam.getPlayed() + 1);
        }
        else{
            if(goalDiff > 0){
                homeTeam.incrementPoint(-3);
                homeTeam.setWins(homeTeam.getWins() - 1);
                awayTeam.setLosses(awayTeam.getLosses() - 1);
            }else if(goalDiff < 0){
                awayTeam.incrementPoint(-3);
                awayTeam.setWins(awayTeam.getWins() - 1);
                homeTeam.setLosses(homeTeam.getLosses() - 1);
            }else{
                homeTeam.incrementPoint(-1);
                awayTeam.incrementPoint(-1);
                homeTeam.setDraws(homeTeam.getDraws() - 1);
                awayTeam.setDraws(awayTeam.getDraws() - 1);
            }
            homeTeam.setPlayed(homeTeam.getPlayed() - 1);
            awayTeam.setPlayed(awayTeam.getPlayed() - 1);

            homeTeam.incrementGoalDiff(-goalDiff);
            awayTeam.incrementGoalDiff(goalDiff);
        }
    }

    public static List<int[]> generateRandomScores(int noOfFixtures){
        List<int[]> scores = new ArrayList<>(noOfFixtures);

        for(int i = 0; i < noOfFixtures; i++){

            int[] randomScores = {((int)Math.ceil(Math.random() * 5)), ((int)Math.ceil(Math.random() * 5))};
            scores.add(randomScores);
        }

        return scores;
    }

    public static void updateDb(FixtureInfo fixture, LeagueRepository repo,  boolean which){
        new Thread(() -> {
            List<TeamInfo> teams = repo.getTeams();
            List<TeamInfo> fixtureTeams = new ArrayList<>(2);

            fixture.updateDate();
            for(int i = 0; i < teams.size(); i++){
                String name = teams.get(i).getName();
                if(name.equals(fixture.getHomeTeam()) || name.equals(fixture.getAwayTeam())) {
                    fixtureTeams.add(teams.remove(i));
                    i--;
                }
            }

            evaluateResult(fixture, fixtureTeams, which);

            teams.addAll(fixtureTeams);

            if(!which) {
                fixture.setHomeScore(-1);
                fixture.setAwayScore(-1);
            }

            repo.updateFixtureDb(Collections.singletonList(fixture));
            repo.updateTeamsDb(teams);
        }).start();
    }

    public static void batchUpdateDb(List<FixtureInfo> fixtures, LeagueRepository repo, boolean which){
        new Thread(() -> {
            List<TeamInfo> teams = repo.getTeams();
            List<TeamInfo> fixInfo = new ArrayList<>(2);

            for (FixtureInfo f : fixtures) {
                f.updateDate();

                for(int i = 0; i < teams.size(); i++) {

                    String name = teams.get(i).getName();
                    if(name.equals(f.getHomeTeam()) || name.equals(f.getAwayTeam())){
                        fixInfo.add(teams.remove(i));
                        i--;
                    }
                }

                if(fixInfo.size() == 2) evaluateResult(f, fixInfo, which);
                teams.addAll(fixInfo);

                fixInfo.clear();
            }

            repo.updateTeamsDb(teams);
            repo.updateFixtureDb(fixtures);
        }).start();
    }
}
