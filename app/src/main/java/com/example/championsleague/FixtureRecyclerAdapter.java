package com.example.championsleague;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.League;
import com.example.championsleague.models.LeagueInfo;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


public class FixtureRecyclerAdapter extends RecyclerView.Adapter<FixtureRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater layoutInflater;
    private List<FixtureInfo> mFixtures;
    public static int currentSubmittedFixture;
    private Map<Integer, int[]> holderFixtureView = new WeakHashMap<>();
    private List<Integer> mFixtureNos;
    private Map<Integer, int[]> predictedFixtures;
    private Map<String, Bitmap> mTeamLogos;
    private SharedPreferences.OnSharedPreferenceChangeListener listen;

    public FixtureRecyclerAdapter(Context context, List<FixtureInfo> fixtures, Map<Integer, int[]> predictedFix) {
        this.mContext = context;
        this.mFixtures = fixtures;
        this.predictedFixtures = predictedFix;
        fixtureNos();


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
        final ViewHolder finalHold = holder;
        final FixtureInfo fixture = mFixtures.get(position);

        if(!holderFixtureView.isEmpty()) {
            Set<Integer> goneFixtures = holderFixtureView.keySet();

            for (Integer in : goneFixtures) {
                if (mFixtureNos.contains(in)) mFixtureNos.remove(in);
            }
        }

//        Set<String> teamKeys = mTeamLogos.keySet();
//        if(teamKeys.contains(fixture.getHomeTeam())){
//        }
//
//        if(teamKeys.contains(fixture.getAwayTeam())){
//        }


//        holder.homeText.setText(fixture.getHomeTeam());
//        holder.awayText.setText(fixture.getAwayTeam());
        holder.fixtureNo.setText("Fixture " + fixture.getFixtureNo());
        holder.homeImage.setImageBitmap(fixture.getHomeLogo());
        holder.awayImage.setImageBitmap(fixture.getAwayLogo());
        holder.homeText.setText(fixture.getHomeTeam());
        holder.awayText.setText(fixture.getAwayTeam());

        int ok = fixture.getFixtureNo();

        if(holderFixtureView.containsKey(ok)){
                holder.awayScore.getEditText().setText(Integer.toString(holderFixtureView.get(ok)[1]));
                holder.homeScore.getEditText().setText(Integer.toString(holderFixtureView.get(ok)[0]));
                holder.submitButton.setVisibility(View.VISIBLE);
        }else{
            holder.awayScore.getEditText().setText("");
            holder.homeScore.getEditText().setText("");
            holder.submitButton.setVisibility(View.GONE);
        }

        if(predictedFixtures != null){
            if(predictedFixtures.keySet().contains(ok)) {
                holder.awayScore.getEditText().setText(Integer.toString(predictedFixtures.get(ok)[1]));
                holder.homeScore.getEditText().setText(Integer.toString(predictedFixtures.get(ok)[0]));
            }
        }

        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(listen = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("team_length")){

                    finalHold.postForText(sharedPreferences.getString(key, "3"));
                }else if(key.equals("image_toggle")){

                    finalHold.postForImage(sharedPreferences.getBoolean(key, false));
                }
            }
        });


    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public boolean updateFixture(boolean isEval, ViewHolder hold, FixtureInfo outcast, Integer viewCurrentPosition){
        int no;
        if(viewCurrentPosition == null) {
            no = Integer.parseInt(hold.fixtureNo.getText().toString().split("\\s+")[1]) - 1;
        }else no = viewCurrentPosition;
        if(isEval) {
            hold.cardView.setVisibility(View.GONE);
            notifyItemRemoved(no);
            notifyItemRangeRemoved(no, 1);
        }else{
            hold.cardView.setVisibility(View.VISIBLE);
            notifyItemInserted(no);
            notifyItemRangeInserted(no, 1);
            return true;
        }

        notifyItemChanged(no);
        return false;
    }

    public List<FixtureInfo> getFixtures(){
        return mFixtures;
    }

    @Override
    public int getItemCount() {

        if(mFixtures == null) return 0;

        return mFixtures.size();
    }

    public void fixtureNos(){
        List<Integer> extractedFixtureNo = new ArrayList<>(mFixtures.size());

        for(int i = 0; i < mFixtures.size(); i++){
            extractedFixtureNo.add(mFixtures.get(i).getFixtureNo());
        }

        mFixtureNos = extractedFixtureNo;
    }


    public boolean changeFixtures(FixtureInfo toBeRemoved){
        return mFixtures.remove(toBeRemoved);
    }

    public boolean setNewFixtures(List<FixtureInfo> newFixtures){

//        int changedSize = mFixtures.size() - newFixtures.size();

        if(newFixtures.size() == 0) return false;

        boolean isNotSameSet = false;

        if(mFixtures.size() == newFixtures.size()) {
            for (FixtureInfo f : newFixtures) {

                if (!mFixtures.contains(f)) isNotSameSet = true;
                if (isNotSameSet) break;
            }
        }else isNotSameSet = true;


        mFixtures.clear();
        mFixtures.addAll(newFixtures);
        notifyDataSetChanged();

        return isNotSameSet;
        }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView homeText;
        public final TextView awayText;
        public final TextInputLayout homeScore;
        public final TextInputLayout awayScore;
        private final TextView vsText;
        private final Button submitButton;
        private final CardView cardView;
        public final TextView fixtureNo;
        public final ImageView homeImage;
        public final ImageView awayImage;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            homeText = itemView.findViewById(R.id.text_home);
            awayText = itemView.findViewById(R.id.text_away);
            homeScore = itemView.findViewById(R.id.edit_score_home_layout);
            awayScore = itemView.findViewById(R.id.edit_score_away_layout);
            vsText = itemView.findViewById(R.id.vs_text);
            submitButton = itemView.findViewById(R.id.submit_butt);
            cardView = itemView.findViewById(R.id.card_view_fixture);
            fixtureNo = itemView.findViewById(R.id.text_fixture_number);
            homeImage = itemView.findViewById(R.id.image_home);
            awayImage = itemView.findViewById(R.id.image_away);

            itemView.setId(View.generateViewId());
            submitButton.setId(View.generateViewId());
            awayScore.getEditText().setId(View.generateViewId());

            textListeners();

            bothScoresEntered();

        }

        @Override
        public String toString() {

            return fixtureNo.getText().toString();
        }

        private void bothScoresEntered(){

            awayScore.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if(!v.getText().toString().trim().isEmpty() && !homeScore.getEditText().getText().toString().trim().isEmpty()){

                        ViewGroup editLay = ((ViewGroup)((ViewGroup)v.getParent()).getParent());

                        ViewGroup absParent =(ViewGroup) editLay.getParent();

                        TextView fixNo = absParent.findViewById(R.id.text_fixture_number);

                        int vFixtureNo = Integer.parseInt(fixNo.getText().toString().split("\\s+")[1]);
                        int itemFixtureNo = Integer.parseInt(ViewHolder.this.fixtureNo.getText().toString().split("\\s+")[1]);

                        if(vFixtureNo == itemFixtureNo) {

                            ViewHolder.this.submitButton.setVisibility(View.VISIBLE);
                            LeagueInfo.currentFrameLayout.add(ViewHolder.this.itemView.getId());

                            int[] initScores = {Integer.parseInt(ViewHolder.this.homeScore.getEditText().getText().toString()),
                                    Integer.parseInt(ViewHolder.this.awayScore.getEditText().getText().toString())};

                            holderFixtureView.put(itemFixtureNo, initScores);
                            return true;
                        }
                        return false;
                    }
                    else return false;
                }
            });

        }

        private void textListeners(){

            homeScore.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.isSelected()) ((EditText)v).setText("");
                }
            });

            awayScore.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.isSelected()) {
                        ((EditText)v).setText("");}
                }
            });

        }

        public void postForText(final String newLengths){
            final int newLength = Integer.parseInt(newLengths);
            awayText.post(new Runnable() {
                @Override
                public void run() {
                    awayText.setMaxLines(newLength);
                }
            });

            homeText.post(new Runnable() {
                @Override
                public void run() {
                    homeText.setMaxLines(newLength);
                }
            });
        }

        public void postForImage(final boolean isShowing){
            Handler handler = homeImage.getHandler();
                    handler.post(new Runnable() {
                @Override
                public void run() {
                    if(isShowing) homeImage.setVisibility(View.VISIBLE);
                    else homeImage.setVisibility(View.GONE);
                }
            });

            awayImage.getHandler()
                    .postAtTime(new Runnable() {
                @Override
                public void run() {
                    if(isShowing) awayImage.setVisibility(View.VISIBLE);
                    else awayImage.setVisibility(View.GONE);
                }
            }, SystemClock.uptimeMillis());

        }
    }
}
