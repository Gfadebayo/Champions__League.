package com.example.championsleague.viewmodels;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.championsleague.LocalPersistence;
import com.example.championsleague.R;
import com.example.championsleague.adapters.FixtureAdapter;
import com.example.championsleague.utils.LeagueUtils;
import com.example.championsleague.utils.FileUtils;
import com.example.championsleague.database.LeagueRepository;
import com.example.championsleague.models.FixtureInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixtureViewModel extends AndroidViewModel{

    private final String KEY_QUERY = "query";
    private final String KEY_SCORES = "predicted_scores";
    private final String KEY_COMPLETE = "completed";

    private final Context mContext;
    private final SavedStateHandle mHandle;
    private final LeagueRepository mRepo;
    private final LocalPersistence mPersistence;
    private final Map<String, Bitmap> mTeamLogos = new HashMap<>();

    public FixtureViewModel(@NonNull Application application, SavedStateHandle handle) {
        super(application);
        mContext = application.getApplicationContext();
        mHandle = handle;
        mRepo = LeagueRepository.getInstance(application);
        mPersistence = new LocalPersistence(mContext.getSharedPreferences(LocalPersistence.PREF_NAME, Context.MODE_PRIVATE));
    }

    public void initHandle(){
        if(mHandle.get(KEY_QUERY) == null) mHandle.set(KEY_QUERY, mPersistence.getQuery());
        if(mHandle.get(KEY_SCORES) == null) mHandle.set(KEY_SCORES, new SparseArray<int[]>());
        if(mHandle.get(KEY_COMPLETE) == null) mHandle.set(KEY_COMPLETE, mPersistence.getComplete());
    }

    /**
     * Get the team names from the database and load their respective logos if available.
     * This ensures Bitmaps would not have to be created each time the object is recreated,
     * instead they are simply fetched from the Hash map
     */
    public void loadTeamLogos() {
        new Thread(() -> {

            File parent = mContext.getExternalFilesDir(FileUtils.TEAMS_LOGO_DIR);
            for (String team : getTeamNames()) {

                if(mTeamLogos.get(team) != null) continue;
                File logoDir = new File(parent, team + ".png");
                if(!logoDir.exists()) continue;

                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(logoDir.getPath(), op);
                op.inSampleSize = determineSampleSize(op);
                op.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeFile(logoDir.getPath(), op);

                mTeamLogos.put(team, bitmap);
            }
        }).start();
    }

    private int determineSampleSize(BitmapFactory.Options op) {
        int width = op.outWidth;
        int height = op.outHeight;
        float reqWidth =  mContext.getResources().getDimension(R.dimen.max_image_width);
        float reqHeight =  mContext.getResources().getDimension(R.dimen.max_image_height);
        int sampleSize = 1;

        if(width > reqWidth || height > reqHeight){
            int widthRatio = Math.round(width / reqWidth);
            int heightRatio = Math.round(height / reqHeight);

            sampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return sampleSize;
    }

    public List<FixtureInfo> getFixtures(){

        List<FixtureInfo> fixtures = mRepo.getFixtures(mHandle.get(KEY_QUERY), mHandle.get(KEY_COMPLETE));
        loadScoresIntoFixtures(fixtures);
        loadBitmapsIntoFixtures(fixtures);

        return fixtures;
    }

    private void loadScoresIntoFixtures(List<FixtureInfo> fixtures){
        SparseArray<int[]> predictions = mHandle.get(KEY_SCORES);

        for (FixtureInfo f : fixtures) {
            f.addOnPropertyChangedCallback(callback(f));

            if (f.getHomeScore() > -1) continue;

            int[] scores = predictions.get(f.getFixtureNo());

            if(scores == null) continue;
            f.setHomeScore(scores[0]);
            f.setAwayScore(scores[1]);
        }
    }

    private void loadBitmapsIntoFixtures(List<FixtureInfo> fixtures){
        for(FixtureInfo f : fixtures){
            if(f.getHomeLogo() == null) f.setHomeLogo(mTeamLogos.get(f.getHomeTeam()));

            if(f.getAwayLogo() == null) f.setAwayLogo(mTeamLogos.get(f.getAwayTeam()));
        }
    }

    private Observable.OnPropertyChangedCallback callback(final FixtureInfo f){
        return new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                SparseArray<int[]> sparseArray = mHandle.get(KEY_SCORES);
                int[] scores = sparseArray.get(f.getFixtureNo(), new int[2]);

                if(propertyId == BR.awayScore) scores[1] = f.getAwayScore();
                else if(propertyId == BR.homeScore) scores[0] = f.getHomeScore();
            }
        };
    }

    public boolean hasCompletedFixtures(){
        return mRepo.getCompletedFixturesCount() > 0;
    }

    public List<String> getTeamNames(){
        return mRepo.teamNames();
    }

    public void setQuery(String query){
        mHandle.set(KEY_QUERY, query);
    }

    public String getQuery(){
        return mHandle.get(KEY_QUERY);
    }

    public void setPredictions(SparseArray<int[]> predictions){
        mHandle.set(KEY_SCORES, predictions);
    }

    public void setComplete(boolean forComplete){
        mHandle.set(KEY_COMPLETE, forComplete);
    }

    public boolean getComplete(){
        return mHandle.get(KEY_COMPLETE);
    }

    public void undo(FixtureAdapter adapter){
        FixtureInfo submittedFixture = mRepo.getLastCompletedFixture();

        LeagueUtils.updateDb(submittedFixture, mRepo, false);

        ArrayList<FixtureInfo> newList = new ArrayList<>(adapter.getCurrentList());
        newList.add(submittedFixture);
        Collections.sort(newList, (o1, o2) -> Integer.compare(o1.getFixtureNo(), o2.getFixtureNo()));
        adapter.submitList(newList);
    }

    public void performSubmission(final List<FixtureInfo> fixture){
        if(fixture.isEmpty()) return;
        LeagueUtils.batchUpdateDb(fixture, mRepo, true);
    }

    public void registerPrefObserver(LifecycleOwner o){
        MutableLiveData<String> queryData = mHandle.getLiveData(KEY_QUERY);
        queryData.observe(o, s -> mPersistence.setQuery(s));

        MutableLiveData<Boolean> completeData = mHandle.getLiveData(KEY_COMPLETE);
        completeData.observe(o, b -> mPersistence.setComplete(b));
    }

    public void resolveQuery(String s, int id, Boolean checked){
        String oldQuery = mHandle.get(KEY_QUERY);
        StringBuilder build = new StringBuilder(oldQuery);
        int firstAndIndex = oldQuery.indexOf("AND ", oldQuery.indexOf("NOT IN ("))+4;

        //The Leg is always appended to the end of the query
        if(id == R.id.spinner_leg){
            int startPosition1 = oldQuery.indexOf("AND", firstAndIndex);

            if(startPosition1 != -1) build.delete(startPosition1, oldQuery.length()).trimToSize();

            if(s.equals("Leg 1")) build.append(" AND leg=1");
            else if(s.equals("Leg 2")) build.append(" AND leg=2");

        }else if(id == R.id.spinner_location){
            int which = -1;
            if(s.equals("Home Only")) which = 0;
            else if(s.equals("Away Only")) which = 1;

            replaceLocation(oldQuery, build, which);

        }else if(id == R.id.grid_team_list){
            s = "\'" + s + "\'";

            if(!checked) {
                int index = oldQuery.indexOf("NOT IN (")+8;
                int lastIndex = oldQuery.indexOf("NOT IN (", firstAndIndex)+8;

                if(oldQuery.charAt(index) != ')') s = s + ",";
                build.insert(lastIndex, s);
                build.insert(index, s);

            }else {
                int index = oldQuery.indexOf(s);
                int lastIndex = oldQuery.indexOf(s, firstAndIndex);
                if(oldQuery.charAt(index-1) == ',') {
                    s = "," + s;
                    index = oldQuery.indexOf(s);
                    lastIndex = oldQuery.indexOf(s, firstAndIndex);
                }

                build.delete(lastIndex, lastIndex+s.length());
                build.delete(index, index+s.length());
            }
            int comma = build.indexOf("NOT IN (")+8;
            int lastComma = build.indexOf("NOT IN (", firstAndIndex)+8;
            if(build.charAt(comma) == ',') {
                build.deleteCharAt(lastComma);
                build.deleteCharAt(comma);
            }
        }

        Log.i("View model", build.toString());
        mHandle.set(KEY_QUERY, build.toString());
    }

    private void replaceLocation(String query, StringBuilder b, int which){

            String indexToFind = "";
            String insert = "";
            if (which == 0) {
                indexToFind = "away_team";
                insert = "home";
            } else if (which == 1) {
                indexToFind = "home_team";
                insert = "away";
            }

            if (which == 0 || which == 1) {
                int startIndex = 0;
                while (true) {
                    int index = query.indexOf(indexToFind, startIndex);
                    if (index == -1) break;
                    b.replace(index, index + 4, insert);
                    startIndex = index+5;
                }
            } else {
                int index = query.indexOf("home");
                String other = "away";
                if (index == -1) {
                    index = query.indexOf("away");
                    other = "home";
                }
                b.replace(index, index + 4, other);
            }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        for(String team : mTeamLogos.keySet()) mTeamLogos.get(team).recycle();
        mTeamLogos.clear();
    }

}
