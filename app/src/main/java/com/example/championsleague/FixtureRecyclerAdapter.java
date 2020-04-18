package com.example.championsleague;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class FixtureRecyclerAdapter extends RecyclerView.Adapter<FixtureRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater layoutInflater;
    public static final List<Integer> viewIds = new ArrayList<>();
    private final String[] mFixtures;

    public FixtureRecyclerAdapter(Context context, String[] fixtures) {
        this.mContext = context;
        this.mFixtures = fixtures;

        layoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.list_fixtures, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String fixture = mFixtures[position];

        holder.homeText.setText(fixture.split("\\s+")[0]);
        holder.awayText.setText(fixture.split("\\s+")[1]);
    }

    @Override
    public int getItemCount() {
        return mFixtures.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView homeText;
        public final TextView awayText;
        public final EditText homeScore;
        public final EditText awayScore;
        private final TextView vsText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            homeText = itemView.findViewById(R.id.text_home);
            awayText = itemView.findViewById(R.id.text_away);
            homeScore = itemView.findViewById(R.id.edit_score_home);
            awayScore = itemView.findViewById(R.id.edit_score_away);
            vsText = itemView.findViewById(R.id.vs_text);

            itemView.setId(View.generateViewId());
            viewIds.add(itemView.getId());
        }

        @Override
        public String toString() {

            return homeText.getText().toString() + "\t" + homeScore.getText().toString()
                    + "\t" + vsText.getText().toString() + "\t" + awayScore.getText().toString()
                    + "\t" + awayText.getText().toString();
        }
    }
}
