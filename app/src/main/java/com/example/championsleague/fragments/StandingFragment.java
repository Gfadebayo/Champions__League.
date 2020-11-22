package com.example.championsleague.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.championsleague.R;
import com.example.championsleague.databinding.TableRowValuesBinding;
import com.example.championsleague.viewmodels.StandingViewModel;
import com.example.championsleague.models.TeamInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StandingFragment extends Fragment {

    private StandingViewModel mViewModel;
    private TableLayout mTableLayout;
    private List<TableRowValuesBinding> mRowList;
    private SharedPreferences mPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.
                getInstance(getActivity().getApplication())).get(StandingViewModel.class);
        setRetainInstance(true);

        mPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = (prefs, key) -> prefChange();
        mPref.registerOnSharedPreferenceChangeListener(mPrefListener);

        
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_standing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTableLayout = view.findViewById(R.id.table_team_info);

        List<TeamInfo> teams = mViewModel.getTeams();

        getStanding(teams);

        mViewModel.getLatestTeamInfo(requireActivity()).observe(requireActivity(), teamInfos -> {
            for(int i = 0; i < teamInfos.size(); i++){

                if(i >= mRowList.size()-1) break;
                TableRowValuesBinding bind = mRowList.get(i+1);
                bind.setTeam(teamInfos.get(i));
            }

            int diff = mRowList.size() - (teamInfos.size()+1);
            if(diff > 0) IntStream.range(1, Math.abs(diff)).forEach(value -> mRowList.remove(mRowList.size()-value));
        });

        prefChange();
    }

    private void prefChange(){
        List<Integer> col = mPref.getStringSet("KEY_TABLE_INFO", new HashSet<>()).stream()
                .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        IntStream.range(0, mTableLayout.getChildCount()+1).forEach(val -> mTableLayout.setColumnCollapsed(val, col.contains(val)));
    }

    private void getStanding(List<TeamInfo> allTeams){
        mRowList = new ArrayList<>(allTeams.size());


        for(int i = 0; i <= allTeams.size(); i++){
            TableRowValuesBinding inflate = TableRowValuesBinding.inflate(getLayoutInflater()
                    , mTableLayout, true);
            if(i != 0) inflate.setTeam(allTeams.get(i-1));
            mRowList.add(inflate);
        }
    }
}
