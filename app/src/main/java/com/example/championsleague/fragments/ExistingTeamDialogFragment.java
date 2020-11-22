package com.example.championsleague.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.R;
import com.example.championsleague.internet.FootballApi;
import com.example.championsleague.adapters.LeagueAdapter;
import com.example.championsleague.internet.Repository;
import com.example.championsleague.models.League;
import com.example.championsleague.models.TeamEmpty;
import com.example.championsleague.models.TeamInfo;
import com.example.championsleague.utils.FileUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExistingTeamDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ExistingTeams";
    private LeagueAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private FootballApi mServiceApi;
    private List<String> mAllowedCountries;
    private Repository mRepo;

    public static ExistingTeamDialogFragment getInstance() {
        return new ExistingTeamDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mRepo = Repository.Companion.getInstance(requireActivity());
        Toast.makeText(requireContext(), "Fetching Teams, Please Wait", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_frag_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.button_teams_submit).setOnClickListener(v -> {

            List<TeamEmpty> teams = ((ConcatAdapter) mRecyclerView.getAdapter()).getAdapters().stream()
                    .flatMap((Function<RecyclerView.Adapter<? extends RecyclerView.ViewHolder>, Stream<TeamEmpty>>)
                            adapter -> ((LeagueAdapter) adapter).getSelectedTeams().stream()).collect(Collectors.toList());

            ((TeamSelectionFragment) getParentFragment()).addToList(teams.stream().map(TeamEmpty::getName).collect(Collectors.toList()));
            FileUtils.saveImages(requireContext().getExternalFilesDir(FileUtils.TEAMS_LOGO_DIR), teams);
            dismiss();
        });

        mRecyclerView = view.findViewById(R.id.recycler_test);

        ConcatAdapter adapter = new ConcatAdapter(createLocalLeagueAdapter());
        mRecyclerView.setAdapter(adapter);


        mRepo.getLeagues().observeOn(AndroidSchedulers.mainThread()).doOnNext(leagues -> {
            leagues.forEach(league -> {
                        LeagueAdapter ad = new LeagueAdapter(ExistingTeamDialogFragment.this, league);
                        ad.setOnHeaderClickedAction(integer -> fetchTeams(integer, ad));

                        ((ConcatAdapter) mRecyclerView.getAdapter()).addAdapter(ad);
                    });
        }).subscribe();

//        addLocalTeams();
        //To avoid fetching every time the dialog is opened
    }

    private LeagueAdapter createLocalLeagueAdapter(){
        Map<String, List<TeamEmpty>> localTeams = FileUtils.getLocalTeams(requireContext());
        String league = new ArrayList<>(localTeams.keySet()).get(0);

        LeagueAdapter adapter = new LeagueAdapter(this, new League(-1, league, league));
        adapter.setTeams(localTeams.get(league));

        return adapter;
    }

    private void fetchTeams(int competitionId, LeagueAdapter adapter){
//        mRepo.getTeams(competitionId);
        if(competitionId == -1) return;
        mRepo.getTeams(competitionId).observeOn(AndroidSchedulers.mainThread()).doOnNext(teamInfos -> {
            adapter.setTeams(teamInfos);
        }).subscribe();
    }

//    private void addLocalTeams() {
//        File dir = requireContext().getExternalFilesDir(FileUtils.EXISTING_TEAMS_DIR);
//        Map<String, List<String>> teamMap = FileUtils.readExistingTeamsFromFile(dir);
//        String country = new ArrayList<>(teamMap.keySet()).get(0);
//        mAdapter.addCountry(country);
//
//        //Create a little space so VH can be created before we call it
//        new Handler().postDelayed(() -> {
//            int index = mAdapter.pathPosition(country);
//            Log.i(TAG, String.valueOf(index));
//            index = (int) mAdapter.getItemId(index);
//            LeagueAdapter.BodyViewHolder ada = (LeagueAdapter.BodyViewHolder) mRecyclerView.findViewHolderForItemId(index);
//            teamMap.get(country).forEach(t -> ada.addTeam(t, null));
//        }, 1000);
//    }
}
