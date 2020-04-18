package com.example.championsleague;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

@SuppressWarnings("ALL")
public class TeamResultActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button nextButton = findViewById(R.id.final_button);

        populateLayout();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                League.getInstance().setAllResult(getFixtureResult());

                Intent intent = new Intent(TeamResultActivity.this, championActivity.class);
                startActivity(intent);
            }
        });
    }

    public void populateLayout() {

        mRecyclerView = findViewById(R.id.fixture_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        String[] fixtures = League.getInstance().generateFixtures();
        FixtureRecyclerAdapter fixtureRecyclerAdapter = new FixtureRecyclerAdapter(this, fixtures);
        mRecyclerView.setAdapter(fixtureRecyclerAdapter);


    }

    public String[] getFixtureResult() {
        List<Integer> viewIds = FixtureRecyclerAdapter.viewIds;
        String[] result = new String[mRecyclerView.getChildCount()];

        for (int i = 0; i < result.length; i++) {


            View childView = findViewById(viewIds.get(i));

            FixtureRecyclerAdapter.ViewHolder holder = (FixtureRecyclerAdapter.ViewHolder) mRecyclerView.getChildViewHolder(childView);

            result[i] = holder.toString();
        }

        return result;
    }
}

