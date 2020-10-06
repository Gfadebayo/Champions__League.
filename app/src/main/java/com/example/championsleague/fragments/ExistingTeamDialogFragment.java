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
import androidx.recyclerview.widget.RecyclerView;

import com.example.championsleague.R;
import com.example.championsleague.ServiceApi;
import com.example.championsleague.adapters.WebRecyclerAdapter;
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
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ExistingTeamDialogFragment extends BottomSheetDialogFragment implements Callback<ResponseBody> {

    public static final String TAG = "ExistingTeams";
    private WebRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ServiceApi mServiceApi;
    private List<String> mAllowedCountries;

    public static ExistingTeamDialogFragment getInstance() {
        return new ExistingTeamDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Toast.makeText(requireContext(), "Fetching Teams, Please Wait", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_frag_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pref();

        view.findViewById(R.id.button_teams_submit).setOnClickListener(v -> {
            Map<String, String> teams = mAdapter.getChosenTeams();
            ((TeamSelectionFragment) getParentFragment()).addToList(teams.keySet());
            FileUtils.saveImages(requireContext().getExternalFilesDir(FileUtils.TEAMS_LOGO_DIR), teams);
            dismiss();
        });

        mRecyclerView = view.findViewById(R.id.recycler_test);

        mAdapter = new WebRecyclerAdapter(this);

        mRecyclerView.setAdapter(mAdapter);

        addLocalTeams();
        //To avoid fetching every time the dialog is opened
        if (mAdapter.getItemCount() <= 1) {

            Retrofit fit = new Retrofit.Builder().baseUrl("https://soccerway.com/")
                    .build();

            mServiceApi = fit.create(ServiceApi.class);

            mServiceApi.move().enqueue(ExistingTeamDialogFragment.this);
        }
    }

    private void addLocalTeams() {
        File dir = requireContext().getExternalFilesDir(FileUtils.EXISTING_TEAMS_DIR);
        Map<String, List<String>> teamMap = FileUtils.readExistingTeamsFromFile(dir);
        String country = new ArrayList<>(teamMap.keySet()).get(0);
        mAdapter.addCountry(country);

        //Create a little space so VH can be created before we call it
        new Handler().postDelayed(() -> {
            int index = mAdapter.pathPosition(country);
            Log.i(TAG, String.valueOf(index));
            index = (int) mAdapter.getItemId(index);
            WebRecyclerAdapter.ViewHolder ada = (WebRecyclerAdapter.ViewHolder) mRecyclerView.findViewHolderForItemId(index);
            teamMap.get(country).forEach(t -> ada.addTeam(t, null));
        }, 1000);
    }

    private void pref() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mAllowedCountries = Arrays.asList(settings.getString("KEY_LEAGUE", "").split(","));
        mAllowedCountries.forEach(String::toLowerCase);

        mAllowedCountries = mAllowedCountries.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        String path = call.request().url().encodedPath();

        if (response.isSuccessful()) {

            try {
                Document parse = Jsoup.parse(response.body().string());

                if (path.contains("club-teams")) getCountries(parse);
                else if (path.contains("national/") && !path.contains("table"))
                    resolveCountries(parse);
                else if (path.contains("tables")) getTeams(parse);
                else resolveTeams(parse);

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    private void getCountries(Document parse) {

        parse.body().getElementsByAttributeValue("class", "expandable").forEach(c -> {
            String attr = c.getElementsByTag("a").attr("href");
            if (mAllowedCountries.contains(attr.split("/")[2])) {
                mServiceApi.next(attr).enqueue(this);

            }
        });
    }

    private void resolveCountries(Document parse) {
        final StringBuilder league = new StringBuilder();

        Elements strea = parse.body().getElementsByAttributeValue("id", "subheading");

        strea.stream().limit(1).forEach(c -> league.append(c.getElementsByTag("h1").text()));

        //country name
        Elements aClass = parse.body().getElementsByAttributeValue("class", "header-label");
        String text = "(" + aClass.text().split("\\s+")[0] + ")";
        league.append(text);

        parse.body().getElementsByAttributeValue("class", "current").forEach(c -> {

            String link = c.getElementsByTag("a").attr("href").concat("tables/");
            mAdapter.addCountry(league.toString());
            addLocalTeams();
            mServiceApi.next(link).enqueue(this);
        });

    }

    private void getTeams(Document parse) {

        parse.body().getElementsByAttributeValue("class", "leaguetable sortable table detailed-table")
                .forEach(c -> c.getElementsByAttributeValue("class", "text team large-link").forEach(el -> {
                    String attr = el.getElementsByTag("a").attr("href");
                    mServiceApi.next(attr).enqueue(this);
                }));
    }

    private void resolveTeams(Document parse) {

        String path = parse.body().getElementsContainingOwnText("Country").next().text();

        parse.body().getElementsByAttributeValue("class", "logo").forEach(el -> el.getElementsByTag("img").forEach(c -> {
            String logo = c.attr("src");
            String alt = c.attr("alt");

            int pos = mAdapter.pathPosition(path);
            WebRecyclerAdapter.ViewHolder holder = (WebRecyclerAdapter.ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);

            holder.addTeam(alt, logo);
        }));
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        t.printStackTrace();
        if(isVisible())
            Toast.makeText(requireActivity(), t.getMessage() + ": retrying", Toast.LENGTH_SHORT).show();

    }
}
