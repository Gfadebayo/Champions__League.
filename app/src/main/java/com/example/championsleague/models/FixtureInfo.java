package com.example.championsleague.models;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "Fixtures")
public class FixtureInfo extends BaseObservable implements Comparable {

    @ColumnInfo(name = "home_team")
    private String homeTeam;

    @PrimaryKey
    @ColumnInfo(name = "fixture_no")
    private int fixtureNo;

    @ColumnInfo(name = "home_score")
    public int homeScore;

    @ColumnInfo(name = "away_score")
    public int awayScore;

    @ColumnInfo(name = "away_team")
    private String awayTeam;

    @ColumnInfo(name = "leg")
    private int leg;

    @Ignore
    private Bitmap homeLogo;

    @Ignore
    private Bitmap awayLogo;

    private long date;


    public FixtureInfo(){}

    @Ignore
    public FixtureInfo(String home, String away, int fixtureLeg, int no){
        homeTeam = home;
        awayTeam = away;
        homeScore = -1;
        awayScore = -1;
        fixtureNo = no;
        leg = fixtureLeg;
        date = System.currentTimeMillis();


//        this.fixtureNo = fixtureNo;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    @Bindable
    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        if(this.homeScore == homeScore) return;
        this.homeScore = homeScore;
        notifyPropertyChanged(BR.homeScore);
    }

    @Bindable
    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        if(this.awayScore == awayScore) return;
        this.awayScore = awayScore;
        notifyPropertyChanged(BR.awayScore);
    }

    public int getFixtureNo() {
        return fixtureNo;
    }

    public void setFixtureNo(int fixtureNo) {
        this.fixtureNo = fixtureNo;

//        this.fixtureNo++;
    }

    public int getLeg() {
        return leg;
    }

    public void setLeg(int leg) {
        this.leg = leg;
    }

    public Bitmap getHomeLogo(){
        return homeLogo; }

    public Bitmap getAwayLogo(){ return awayLogo; }

    public void setHomeLogo(Bitmap homeLogo) {
        this.homeLogo = homeLogo;
    }

    public void setAwayLogo(Bitmap awayLogo) {
        this.awayLogo = awayLogo;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return homeTeam + "\t" + awayTeam;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;

        if(!(obj instanceof FixtureInfo)){ return false;}

        FixtureInfo o = (FixtureInfo) obj;

        return this.getFixtureNo() == o.getFixtureNo() &&
                this.getHomeTeam().equals(o.getHomeTeam()) &&
                this.getAwayTeam().equals(o.getAwayTeam()) &&
                this.getHomeScore() == o.getHomeScore() &&
                this.getAwayScore() == o.getAwayScore();
    }

    @Override
    public int compareTo(Object o) {
        FixtureInfo obj = (FixtureInfo) o;

        return Integer.compare(this.getFixtureNo(), obj.getFixtureNo());
    }

    public void updateDate() {
        date = System.currentTimeMillis();
    }
}
