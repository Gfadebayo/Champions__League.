package com.example.championsleague.models;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.Collator;
import java.util.Locale;

@Entity(tableName = "Teams")
public class TeamInfo implements Comparable {

    @NonNull
    @ColumnInfo(name = "name")
    @PrimaryKey
    private String name;

   // @PrimaryKey(autoGenerate = true)

    private int played;

    @ColumnInfo(name = "points")
    private int points;

    @ColumnInfo(name = "position")
    private int position;

    @ColumnInfo(name = "goal_diff")
    private int goal_diff;

    @ColumnInfo(name = "wins")
    private int wins;

    @ColumnInfo(name = "losses")
    private int losses;

    @ColumnInfo(name = "draws")
    private int draws;

    @Ignore
    private Bitmap Logo;

    public TeamInfo(){}

    @Ignore
    public TeamInfo(@NonNull String name, int startingPosition){

        this.name = name;
        goal_diff = 0;
        position = startingPosition;
        points = 0;
        played = 0;
        wins = 0;
        losses = 0;
        draws = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int point) {
        points = point;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getGoal_diff() {
        return goal_diff;
    }

    public void setGoal_diff(int goalDifference) {
        goal_diff = goalDifference;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public void incrementPoint(int amount){

        points +=amount;
    }

    public void incrementGoalDiff(int amount){

        goal_diff +=amount;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public Bitmap getLogo(){return Logo;}

    public void setLogo(Bitmap newLogo){
        Logo = newLogo;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        if(!(obj instanceof TeamInfo)) return false;
        TeamInfo o = (TeamInfo) obj;

        return this.name.compareToIgnoreCase(o.name) == 0
                && (this.position == o.position)
                && (this.draws == o.getDraws())
                && (this.points == o.points)
                && (this.wins == o.wins)
                && (this.losses == o.losses)
                && (this.goal_diff == o.goal_diff);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        TeamInfo team = (TeamInfo) o;
        Collator englishCollator = Collator.getInstance(Locale.ENGLISH);
        int comp;

        if(this.points != team.points){

            comp = -Integer.compare(this.points, team.points);
        }else if(this.goal_diff != team.goal_diff){

            comp = -Integer.compare(this.goal_diff, team.goal_diff);
        }else{
            comp = englishCollator.compare(this.name, team.name);
        }

        return comp;
    }

}
