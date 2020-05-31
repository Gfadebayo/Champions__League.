package com.example.championsleague.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ALL")
public class League {

    private static League leagueInstance;
    private int[] TeamPoints;
    private int[] GoalDifference;
    private static final List<String> TeamName = new ArrayList<>();
    private String[] fixtureResult;
    private final List<String> winners = new ArrayList<>(3);


    private League() {

    }

    public static League getInstance() {

        if (leagueInstance == null) {
            leagueInstance = new League();
        }
        return leagueInstance;
    }

    public void setTeamNames(String[] teamNames) {

        Collections.addAll(TeamName, teamNames);

        TeamPoints = new int[teamNames.length];
        GoalDifference = new int[teamNames.length];
    }

    public void teamResults() {

        for (String result : fixtureResult) {

            String[] splt = result.split("\tVS\t");
            String[] first = splt[0].split("\t");
            String[] second = splt[1].split("\t");
            Arrays.sort(second, Collections.<String>reverseOrder());
            //Adds the first and second teams of fixtures to first and second respectively


            pointAndGoaldiff(first, second);

        }
        winner();
    }

    public void pointAndGoaldiff(String[] first, String[] second) {
        //home team wins if goal diff is > 0
        int goal_diff = Integer.parseInt(first[1]) - Integer.parseInt(second[1]);

        if (goal_diff > 0) {
            int currPoint = TeamPoints[TeamName.indexOf(first[0])];
            currPoint += 3;
            TeamPoints[TeamName.indexOf(first[0])] = currPoint;
        } else if (goal_diff < 0) {
            int currPoint = TeamPoints[TeamName.indexOf(second[0])];
            currPoint += 3;
            TeamPoints[TeamName.indexOf(second[0])] = currPoint;
        } else {
            int currPoint = TeamPoints[TeamName.indexOf(first[0])];
            currPoint += 1;
            TeamPoints[TeamName.indexOf(first[0])] = currPoint;

            currPoint = TeamPoints[TeamName.indexOf(second[0])];
            currPoint += 1;
            TeamPoints[TeamName.indexOf(second[0])] = currPoint;
        }
        int currGoalDiff = GoalDifference[TeamName.indexOf(first[0])];
        currGoalDiff += goal_diff;
        GoalDifference[TeamName.indexOf(first[0])] = currGoalDiff;

        currGoalDiff = GoalDifference[TeamName.indexOf(second[0])];
        currGoalDiff -= goal_diff;
        GoalDifference[TeamName.indexOf(second[0])] = currGoalDiff;

    }

    public int arrIndex(int[] arr, int value) {
        int index = 0;
        for (int t = 0; t < arr.length; t++) {
            if (arr[t] == value) {
                index = t;
                break;
            }
        }
        return index;
    }


    public String[] generateFixtures() {
        String[] newTeam = new String[TeamName.size()];
        newTeam = TeamName.toArray(newTeam);
        List<String> fixtures = new ArrayList<>(12);
        int counter = 0;
        String fix;

        for (int j = 0; j < newTeam.length - 1; j++) {

            for (int k = j + 1; k < newTeam.length; k++) {

                if (k == j) continue;

                fix = newTeam[j] + " " + newTeam[k];

                fixtures.add(counter, fix);


                fix = newTeam[k] + " " + newTeam[j];

                fixtures.add(fixtures.size() - 1, fix);


                counter++;
            }
        }
        String[] fixture = new String[fixtures.size()];
        return fixtures.toArray(fixture);
    }

    public void setAllResult(String[] getResult) {

        fixtureResult = getResult;
    }


    public void winner() {
        String twoChamps;
        int currentHighestPoint;
        List<Integer> tPoints = new ArrayList<>(TeamPoints.length);
        for (int poo : TeamPoints) {

            tPoints.add(poo);
        }
        Collections.sort(tPoints, Collections.<Integer>reverseOrder());


        for (int i = 0; i < 3; i++) {
            currentHighestPoint = Collections.max(tPoints);

            //remove all occurrence of the highest point
            for (int j = 0; j < tPoints.size(); j++) {
                tPoints.remove((Object) currentHighestPoint);
            }

            if (checkPoints(currentHighestPoint) != 1) {

                winners.addAll(winnerByGoalDiff(currentHighestPoint, checkPoints(currentHighestPoint)));
            } else {
                twoChamps = TeamName.get(arrIndex(TeamPoints, currentHighestPoint)) + "\t" + currentHighestPoint;
                winners.add(twoChamps);
            }

            if (winners.size() > 3) break;
        }
    }

    public List<String> getWinner() {

        return winners;
    }


    public int checkPoints(int highPoint) {
        int not1 = 0;

        for (int point : TeamPoints) {
            if (point == highPoint) not1++;
        }

        return not1;
    }

    public List<String> winnerByGoalDiff(int point, int noOfTeams) {
        List<String> others = new ArrayList<>();

        List<Integer> goalDiffs = new ArrayList<>(noOfTeams);

        for (int teamPoint : TeamPoints) {
            if (teamPoint == point) goalDiffs.add(GoalDifference[arrIndex(TeamPoints, teamPoint)]);
        }
        Collections.sort(goalDiffs, Collections.<Integer>reverseOrder());

        for (int i = 0; i < goalDiffs.size(); i++) {
            int goalDiffIndex = arrIndex(GoalDifference, goalDiffs.get(i));

            String team = TeamName.get(goalDiffIndex) + "\t" + TeamPoints[goalDiffIndex];
            others.add(team);
        }

        return others;
    }
}
