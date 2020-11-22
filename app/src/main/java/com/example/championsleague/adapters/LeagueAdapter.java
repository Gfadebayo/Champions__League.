package com.example.championsleague.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.R;
import com.example.championsleague.models.League;
import com.example.championsleague.models.TeamEmpty;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LeagueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private League mLeague;
    private boolean isHidden = true;
    private List<TeamEmpty> mTeams;
    private boolean isFetched = false;
    private Consumer<Integer> onHeaderClicked;
    private List<TeamEmpty> mSelectedTeams = new ArrayList<>();
    private BiConsumer<TeamEmpty, Boolean> onTeamChecked;

    public LeagueAdapter(Fragment fragment, League league) {
        mContext = fragment.requireContext();
        mLeague = league;
        mTeams = new ArrayList<>();
        setHasStableIds(true);
        onTeamChecked = (chosen, isChecked) -> {
            if(isChecked) mSelectedTeams.add(chosen);
            else mSelectedTeams.remove(chosen);
        };
    }

    public void setTeams(List<TeamEmpty> teams){
        mTeams = teams;
        isFetched = true;
        notifyItemChanged(1);
    }

    public List<TeamEmpty> getSelectedTeams(){return mSelectedTeams;}

    @Override
    public long getItemId(int position) {
        return position == 0 ? mLeague.getId() : position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if(viewType == R.layout.header) return new HeaderViewHolder(inflater.inflate(viewType, parent, false));
        else return new BodyViewHolder(inflater.inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof BodyViewHolder){
            ((BodyViewHolder) holder).mAdapter.setTeams(mTeams);
            if(mTeams.size() > 0) ((BodyViewHolder) holder).mProgress.setVisibility(View.GONE);
//            holder.itemView.setVisibility(isHidden ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return isHidden ? 1 : 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? R.layout.header : R.layout.loading_recycler_view;
    }

    private void animate(View view){
        ViewPropertyAnimator animate = view.animate();
        animate.cancel();

        animate.rotationBy(180f).setDuration(200).start();
    }

    public void setOnHeaderClickedAction(Consumer<Integer> consumer) {
        onHeaderClicked = consumer;
    }

    public class BodyViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgress;
        RecyclerView mRecyclerView;
        TeamResponseAdapter mAdapter;

        BodyViewHolder(@NonNull View itemView) {
            super(itemView);

            mProgress = itemView.findViewById(R.id.progress_loading);
            mRecyclerView = itemView.findViewById(R.id.recycler_loading);

            mAdapter = new TeamResponseAdapter(mContext);
            mRecyclerView.setAdapter(mAdapter);

            mAdapter.setMCheckboxAction(onTeamChecked);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{

        public MaterialTextView mHeaderTitle;
        public ShapeableImageView mHeaderImage;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            mHeaderTitle = itemView.findViewById(R.id.header_text);
            mHeaderImage = itemView.findViewById(R.id.header_image);

            String ok = mLeague.getLeagueName() + '(' + mLeague.getCountryName() + ')';

            mHeaderTitle.setText(ok);

            mHeaderImage.setOnClickListener(null);

            itemView.setOnClickListener(onParentClicked());

        }

        private View.OnClickListener onParentClicked(){
            return b -> {
                if(!isFetched){
                    onHeaderClicked.accept(mLeague.getId());
                }
                if(isHidden){
                    notifyItemInserted(1);
                    isHidden = false;
                }else{
                    notifyItemRemoved(1);
                    isHidden = true;
                }
                animate(mHeaderImage);
            };
        }
    }
}
