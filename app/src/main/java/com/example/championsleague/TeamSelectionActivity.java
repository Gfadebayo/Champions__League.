package com.example.championsleague;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

@SuppressWarnings("ALL")
public class TeamSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                League.getInstance().setTeamNames(organizeNames());
                Intent nextIntent = new Intent(TeamSelectionActivity.this, TeamResultActivity.class);
                startActivity(nextIntent);

            }
        });
    }

    public String[] organizeNames() {

        TextView team1 = findViewById(R.id.firstTeam);
        TextView team2 = findViewById(R.id.secondTeam);
        TextView team3 = findViewById(R.id.thirdTeam);
        TextView team4 = findViewById(R.id.fourthTeam);

        String[] allTeam = new String[4];

        allTeam[0] = team1.getText().toString();
        allTeam[1] = team2.getText().toString();
        allTeam[2] = team3.getText().toString();
        allTeam[3] = team4.getText().toString();

        return allTeam;
    }

}
