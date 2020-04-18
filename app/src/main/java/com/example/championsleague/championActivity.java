package com.example.championsleague;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class championActivity extends AppCompatActivity {
    private TextView thirdScore;
    private TextView secondScore;
    private TextView firstScore;
    private TextView thirdText;
    private TextView secondText;
    private TextView firstText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champion);

        firstText = findViewById(R.id.text_champion_first);
        secondText = findViewById(R.id.text_champion_second);
        thirdText = findViewById(R.id.text_champion_third);
        firstScore = findViewById(R.id.text_point_first);
        secondScore = findViewById(R.id.text_point_second);
        thirdScore = findViewById(R.id.text_point_third);


        viewResult();


    }

    private void viewResult() {
        League league = League.getInstance();

        league.teamResults();

        List<String> leagueWinners = league.getWinner();

        firstText.setText(leagueWinners.get(0).split("\t")[0]);
        firstScore.setText(leagueWinners.get(0).split("\t")[1]);
        secondText.setText(leagueWinners.get(1).split("\t")[0]);
        secondScore.setText(leagueWinners.get(1).split("\t")[1]);
        thirdText.setText(leagueWinners.get(2).split("\t")[0]);
        thirdScore.setText(leagueWinners.get(2).split("\t")[1]);

    }


}
