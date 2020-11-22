package com.example.championsleague.adapters;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.R;
import com.example.championsleague.database.LeagueRepository;
import com.example.championsleague.databinding.ListFixturesBinding;
import com.example.championsleague.fragments.FixtureFragment;
import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.utils.LeagueUtils;

import java.util.ArrayList;
import java.util.List;


public class FixtureAdapter extends ListAdapter<FixtureInfo, FixtureAdapter.ViewHolder> {

    private final LeagueRepository mRepository;
    private final LayoutInflater mInflater;
    private final FixtureFragment mFragment;
    private boolean canNotSubmit;
    private boolean mShowImage;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    public FixtureAdapter(Fragment fragment, List<FixtureInfo> fixtures, boolean completed) {
        super(DIFF_CALLBACK);

        mFragment = (FixtureFragment) fragment;
        mInflater = fragment.getLayoutInflater();
        mRepository = LeagueRepository.getInstance(fragment.getActivity().getApplication());
        setHasStableIds(true);
        canNotSubmit = completed;

        //                holder.mBinding.textHome.setMaxLines(len);
        //                holder.mBinding.textAway.setMaxLines(len);
        mPrefListener = (sharedPreferences, key) -> {
            if (key.equals("KEY_IMAGE")) {
                mShowImage = sharedPreferences.getBoolean(key, true);
                notifyItemRangeChanged(0, getCurrentList().size());
            } else if (key.equals("KEY_NAME_LENGTH")) {
                int len = Integer.parseInt(sharedPreferences.getString(key, "3"));
//                holder.mBinding.textHome.setMaxLines(len);
//                holder.mBinding.textAway.setMaxLines(len);
            }
        };
        PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
                .registerOnSharedPreferenceChangeListener(mPrefListener);

        mShowImage = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
                .getBoolean("KEY_IMAGE", true);
        submitList(fixtures);
    }

    private static final DiffUtil.ItemCallback<FixtureInfo> DIFF_CALLBACK = new DiffUtil.ItemCallback<FixtureInfo>() {
        @Override
        public boolean areItemsTheSame(@NonNull FixtureInfo oldItem, @NonNull FixtureInfo newItem) {
            return oldItem.getFixtureNo() == newItem.getFixtureNo();
        }
        @Override
        public boolean areContentsTheSame(@NonNull FixtureInfo oldItem, @NonNull FixtureInfo newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public void onCurrentListChanged(@NonNull List<FixtureInfo> previousList, @NonNull List<FixtureInfo> currentList) {
        mFragment.updateToolbar(currentList.size(), mRepository.totalFixtures());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_fixtures, parent, false);
        ListFixturesBinding bind = DataBindingUtil.bind(v);
        return new ViewHolder(bind);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FixtureInfo fixture = getItem(position);

        holder.mBinding.setFixture(fixture);

        holder.mBinding.submitButt.setEnabled(!canNotSubmit);

        int vis = mShowImage ? View.VISIBLE : View.GONE;
        holder.mBinding.imageHome.setVisibility(vis);
        holder.mBinding.imageAway.setVisibility(vis);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getFixtureNo();
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    public void setSubmittable(boolean submit){
        canNotSubmit = submit;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ListFixturesBinding mBinding;

        ViewHolder(@NonNull ListFixturesBinding binding) {
            super(binding.getRoot());

            binding.setHolder(this);
            mBinding = binding;
        }

        public void onSubmitClicked(View v) {

            new Thread(() -> {
                FixtureInfo fixture = mBinding.getFixture();
                if(fixture.getHomeScore() < 0) return;

                int index = getCurrentList().indexOf(fixture);

                LeagueUtils.updateDb(fixture, mRepository, true);
                List<FixtureInfo> mutate = new ArrayList<>(getCurrentList());
                mutate.remove(index);
                submitList(mutate);
            }).start();
        }
    }
}
