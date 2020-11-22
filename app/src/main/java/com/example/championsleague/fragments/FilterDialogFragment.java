package com.example.championsleague.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.example.championsleague.R;
import com.example.championsleague.viewmodels.FixtureViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class FilterDialogFragment extends BottomSheetDialogFragment {

    private OnDialogItemClicked mOnClick;
    private final String TAG = getClass().getSimpleName();


    //This is used as the names of teams is required and this provides it easily
    private FixtureViewModel mViewModel;
    private PowerSpinnerView mLegSpinner;
    private PowerSpinnerView mLocSpinner;
    private GridLayout mGridTeams;

    public static FilterDialogFragment dialogInstance(){
        return new FilterDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mOnClick = (FixtureFragment) getParentFragment();
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory
                (requireActivity().getApplication(), requireActivity())).get(FixtureViewModel.class);
        changeQuery();

        initLegSpinner(view);

        initLocSpinner(view);

        populateGrid(view);

        view.findViewById(R.id.image_filter_clear).setOnClickListener(v -> {
//            mViewModel.setQuery("SELECT * FROM Fixtures");
            mLegSpinner.selectItemByIndex(2);
            mLocSpinner.selectItemByIndex(2);
            int all = mGridTeams.getChildCount();
            IntStream.range(0, all).forEach(i -> ((MaterialCheckBox)mGridTeams.getChildAt(i)).setChecked(true));
        });
    }

    private void initLegSpinner(View parent){
        String query = mViewModel.getQuery();

        mLegSpinner = parent.findViewById(R.id.spinner_leg);
        mLegSpinner.setItems(Arrays.asList("Leg 1", "Leg 2", "Both"));

        if(query.contains("leg=1")) mLegSpinner.selectItemByIndex(0);
        else if(query.contains("leg=2")) mLegSpinner.selectItemByIndex(1);
        else mLegSpinner.selectItemByIndex(2);

        mLegSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
            mOnClick.onItemClicked(s, mLegSpinner.getId(), null);
        });
    }

    private void initLocSpinner(View parent){
        String query = mViewModel.getQuery();

        mLocSpinner = parent.findViewById(R.id.spinner_location);
        mLocSpinner.setItems(Arrays.asList("Home Only", "Away Only", "Both"));

        if(query.contains("away") && query.contains("home")) mLocSpinner.selectItemByIndex(2);
        else if(query.contains("home")) mLocSpinner.selectItemByIndex(0);
        else mLocSpinner.selectItemByIndex(1);

        mLocSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
            mOnClick.onItemClicked(s, mLocSpinner.getId(), null);
        });
    }

    private void populateGrid(View parent){

        List<String> teamNames = mViewModel.getTeamNames();
        mGridTeams = parent.findViewById(R.id.grid_team_list);
        mGridTeams.setRowCount((int) Math.ceil(teamNames.size() / 3F));

        int rowIndex = 0;
        int columnIndex = 0;

        for (String s : teamNames) {
            MaterialCheckBox box = new MaterialCheckBox(requireContext());

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(rowIndex), GridLayout.spec(columnIndex, 0F));
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            if (columnIndex < 2) columnIndex++;
            else{
                columnIndex = 0;
                rowIndex++;
            }

            box.setLayoutParams(params);
            box.setText(s);
            if (!mViewModel.getQuery().contains(s)) box.setChecked(true);
            box.setOnCheckedChangeListener((buttonView, isChecked) -> mOnClick.onItemClicked(s, mGridTeams.getId(), isChecked));
            mGridTeams.addView(box);
        }
    }

    private void changeQuery(){
        String query = mViewModel.getQuery();
        if(query.equalsIgnoreCase("select * from fixtures")){
            String newQuery = "SELECT * FROM Fixtures WHERE home_team NOT IN () AND away_team NOT IN ()";
            mViewModel.setQuery(newQuery);
        }
    }

    public interface OnDialogItemClicked{
        void onItemClicked(String s, int id, Boolean checked);
   }
}
