package com.example.championsleague.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class WebRecyclerAdapter extends RecyclerView.Adapter<WebRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final List<String> mNames = new ArrayList<>(189);
    private final Map<String, String> mChosenTeams = new WeakHashMap<>();

    public WebRecyclerAdapter(Fragment fragment) {
        mContext = fragment.requireContext();
        setHasStableIds(true);
    }

    public void addCountry(String country) {
        if(mNames.contains(country)) return;

        int pos = mNames.isEmpty() ? 0 : mNames.size()-1;
        Log.i("Web", "Addint in Position " + pos);

        mNames.add(pos, country);
        notifyItemInserted(pos);
        notifyItemChanged(pos);
//            notifyItemRangeChanged(pos, 1);
    }

    public int pathPosition(String name) {
        //Note: France is an exception to this as Monaco
        // though plays in Ligue 1 is regarded as a country on its own

        if(name.equalsIgnoreCase("monaco")) name = "France";

        Log.i("Adapter", name);
        for(String c : mNames){
            String split = name.split("\\s+")[0];
            Log.i("Adapter", split + " vs " + c);
            if(c.contains(split)){
                return mNames.indexOf(c);
            }
        }

        return -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_teams, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = mNames.get(position);
        holder.mText.setText(name);
    }

    @Override
    public int getItemCount() {
        return mNames.isEmpty() ? 0 : mNames.size();
    }

    public Map<String, String> getChosenTeams(){return mChosenTeams;}

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView mText;
        LinearLayout mLayout;
        Map<String, String> mTeams = new HashMap<>(20);

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            mText = itemView.findViewById(R.id.text_country);
            mLayout = itemView.findViewById(R.id.linear_country);

            itemView.setOnClickListener(v -> {
                mLayout.setVisibility(mLayout.getVisibility() == View.VISIBLE
                        ? View.GONE : View.VISIBLE);
                animate(itemView.findViewById(R.id.image_arrow));
            });
        }

        public void addTeam(String team, String logoLink){

            if(mTeams.containsKey(team)) return;
            final MaterialCheckBox box = new MaterialCheckBox(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            box.setText(team);
            box.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) mChosenTeams.put(box.getText().toString(), mTeams.get(box.getText().toString()));
                else mChosenTeams.remove(box.getText().toString());
            });
            mLayout.addView(box, params);

            mTeams.put(team, logoLink);
        }
    }

    private void animate(View view){
        ViewPropertyAnimator animate = view.animate();
        animate.rotationBy(180f).setDuration(200).start();
    }
}
