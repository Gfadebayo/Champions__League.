package com.example.championsleague;

import android.app.usage.ConfigurationStats;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class TeamSelectionActivity extends AppCompatActivity {

    private ConstraintLayout rootLayout;
    private Button nextButton;
    private FrameLayout frameLayout;
    private List<Integer> teamIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootLayout = findViewById(R.id.layout_select_team);
        final EditText noTeams = findViewById(R.id.team_number_edit);
        Button submitButton = findViewById(R.id.submit_area);
        frameLayout = findViewById(R.id.next_button_frame);
        nextButton = findViewById(R.id.next);
        final FrameLayout otherFrame = findViewById(R.id.scores_frame);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeTeamViews(Integer.parseInt(noTeams.getText().toString()));
                otherFrame.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
            }
        });


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
        String[] allTeam = new String[teamIds.size()];

        for (int i = 0; i < allTeam.length; i++) {
            EditText teamText = findViewById(teamIds.get(i));

            allTeam[i] = teamText.getText().toString();
        }

        return allTeam;
    }

    public void initializeTeamViews(int noOfViewsNeeded) {

        Resources resources = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.getDisplayMetrics());

        float pxx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, resources.getDisplayMetrics());


        teamIds = new ArrayList<>(noOfViewsNeeded);
        for (int i = 0; i < noOfViewsNeeded; i++) {

            String hint = "Team " + (i + 1);

            EditText textView = new EditText(this);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins((int) pxx, (int) px, (int) pxx, (int) pxx);
            textView.setLayoutParams(layoutParams);

            textView.setId(textView.generateViewId());
            teamIds.add(textView.getId());

            textView.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
            textView.setGravity(Gravity.START);
            textView.setEms(5);
            textView.setHint(hint);

            rootLayout.addView(textView);
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        EditText edit;
        for (int i = 0; i < noOfViewsNeeded; i++) {

            edit = findViewById(teamIds.get(i));

            if (i == 0) {
                constraintSet.connect(teamIds.get(i), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(teamIds.get(i), ConstraintSet.BOTTOM, teamIds.get(i + 1), ConstraintSet.TOP);
            } else if (i == noOfViewsNeeded - 1) {
                constraintSet.connect(teamIds.get(i), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                constraintSet.connect(teamIds.get(i), ConstraintSet.TOP, teamIds.get(i - 1), ConstraintSet.BOTTOM);
            } else {
                constraintSet.connect(teamIds.get(i), ConstraintSet.TOP, teamIds.get(i - 1), ConstraintSet.BOTTOM);
                constraintSet.connect(teamIds.get(i), ConstraintSet.BOTTOM, teamIds.get(i + 1), ConstraintSet.TOP);
            }

            constraintSet.connect(teamIds.get(i), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(teamIds.get(i), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }

        constraintSet.connect(frameLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(frameLayout.getId(), ConstraintSet.TOP, teamIds.get(teamIds.size() - 1), ConstraintSet.BOTTOM);

        constraintSet.applyTo(rootLayout);

    }

}
