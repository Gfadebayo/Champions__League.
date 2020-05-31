package com.example.championsleague;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProviders;

import com.example.championsleague.models.FixtureInfo;
import com.example.championsleague.models.LeagueInfo;
import com.example.championsleague.models.TeamInfo;
import com.example.championsleague.ui.TeamDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeamResultActivity extends AppCompatActivity implements TeamDialogFragment.TeamDialogListener {
    private LeagueInfo leagueInfo = LeagueInfo.getInstance();
    private ViewGroup rootLayout;
    private int butId;
    private boolean isChangingTeam = false;
    private Spinner spinTeam;
//    private List<String> prevTeams;
    private MainActivityViewModel viewModel;
    private boolean existingViewModel;
    private ExecutorService privateService = Executors.newFixedThreadPool(6);
    private ContentLoadingProgressBar mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SavedStateViewModelFactory factory = new SavedStateViewModelFactory(getApplication(), this);
//        viewModel = new ViewModelProvider(this, factory).get(MainActivityViewModel.class);
        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);
//        getLifecycle().addObserver(viewModel);
        viewModel.initialHandleValues();
        rootLayout = findViewById(R.id.layout_select_team);
        spinTeam = findViewById(R.id.spinner_teams);
        becomeExisting();


        final List<Integer> noOfTeams = new ArrayList<>();
        noOfTeams.add(0, 0);
        privateService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 4; i < 21; i++) {
                    noOfTeams.add(i);
                }
            }
        });
        mProgress = findViewById(R.id.progress_loading);
        mProgress.hide();

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_value, noOfTeams);
        spinTeam.setAdapter(arrayAdapter);

        spinTeam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int isInitialized = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialized != 0) {
                    int teamNo = (Integer) spinTeam.getItemAtPosition(position);

                    if (isInitialized != 1) {
                        temporaryTeamNames();
//                        removeSelectedViews();
                    }
                    initializeTeamViews(teamNo);

                    isInitialized = 2;

                } else {
                    isInitialized++;
                    spinTeam.setSelection(viewModel.getSpinnerValue().getValue());
                    presetValues();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.button_existing_teams).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.show();
                existingTeamsDialog();
            }
        });


    }

    public void presetValues() {
        if (viewModel.getTeamInfo().getValue() != null && viewModel.getSpinnerValue().getValue() > 0) {
            existingViewModel = true;
        }
    }

    public void temporaryTeamNames() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (viewModel.getTeamInfo().getValue() != null && spinTeam.getSelectedItemPosition() > 0) {

                    final List<Integer> ids = viewModel.getTextViewIds().getValue();
//                    List<String> currentNames = new ArrayList<>();

//                    for (int i = 0; i < ids.size(); i++) {
//
//                        TextInputLayout text = findViewById(ids.get(i));
//
//                        currentNames.add(i, text.getEditText().getText().toString());
//                    }
                    viewModel.getTeamInfo().observe(TeamResultActivity.this, new Observer<Map<String, String>>() {
                        @Override
                        public void onChanged(Map<String, String> stringStringMap) {
                            final List<String> values = (List<String>) stringStringMap.values();
                            stringStringMap.clear();
                            for (int i = 0; i < ids.size(); i++) {
                                TextInputLayout text = findViewById(ids.get(i)).findViewById(R.id.team_text_layout);
                                stringStringMap.put(text.getEditText().getText().toString(), values.get(i));
                            }
                        }
                    });
                    existingViewModel = true;
                }
            }
        });
    }

    public List<String> organizeNames() {
        final List<Integer> teamIds = viewModel.getTextViewIds().getValue();
        List<String> createdTeams = null;
        try {
            createdTeams = privateService.submit(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    final List<String> allTeam = new ArrayList<>();

                    for (int i = 0; i < teamIds.size(); i++) {
                        TextInputLayout teamText = findViewById(teamIds.get(i)).findViewById(R.id.team_text_layout);

                        allTeam.add(i, teamText.getEditText().getText().toString());
                    }
                    return allTeam;
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return createdTeams;
    }

    public void initializeTeamViews(final int noOfViewsNeeded) {

        float px8 = getResources().getDimension(R.dimen.pixel_size_8);
        float px0 = getResources().getDimension(R.dimen.pixel_size_0);

        if (noOfViewsNeeded == 0) return;

//        viewModel.clearSavedValue(0);

        if(rootLayout.getChildCount() > 3) removeSelectedViews();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//        teamIds = new ArrayList<>(noOfViewsNeeded);
                for (int i = 0; i < noOfViewsNeeded; i++) {
                    String hint = "Team " + (i + 1);

                    final FrameLayout txt = inflateTeamTexts(i, hint);

//            teamIds.add(txt.getId());
                    viewModel.getTextViewIds().observe(TeamResultActivity.this, new Observer<List<Integer>>() {
                        @Override
                        public void onChanged(List<Integer> integers) {
                            integers.add(txt.getId());
                            //  viewModel.setNewTeamIds(integers);
                        }
                    });
                    rootLayout.addView(txt);
                }

                Button standButton = createButton();
                rootLayout.addView(standButton);

                butId = standButton.getId();
                List<Integer> teamIds = viewModel.getTextViewIds().getValue();

                setConstraints(teamIds, butId);

            }
        });

    }

    private void setConstraints(final List<Integer> teamsId, final int buttonId) {

        try {
            privateService.execute(new Runnable() {
                @Override
                public void run() {
                    int noOfViews = teamsId.size();
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone((ConstraintLayout) rootLayout);

                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        constraintSet.connect(teamsId.get(0), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                        constraintSet.connect(teamsId.get(0), ConstraintSet.START, R.id.constraint_guide, ConstraintSet.END);
                        constraintSet.connect(teamsId.get(0), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                        constraintSet.connect(buttonId, ConstraintSet.TOP, R.id.linear_select_team, ConstraintSet.BOTTOM);
                    }else{
                        constraintSet.connect(teamsId.get(0), ConstraintSet.START, R.id.linear_select_team, ConstraintSet.START);
                        constraintSet.connect(teamsId.get(0), ConstraintSet.END, R.id.linear_select_team, ConstraintSet.END);
                        constraintSet.connect(teamsId.get(0), ConstraintSet.TOP, R.id.linear_select_team, ConstraintSet.BOTTOM);

                        constraintSet.connect(buttonId, ConstraintSet.TOP, teamsId.get(teamsId.size() - 1), ConstraintSet.BOTTOM);
                    }

                      constraintSet.connect(teamsId.get(0), ConstraintSet.BOTTOM, teamsId.get(1), ConstraintSet.TOP);

                    for (int i = 1; i < noOfViews; i++) {

                        if (i != noOfViews - 1) {
                            // constraintSet.connect(teamIds.get(i), ConstraintSet.BOTTOM, standButton.getId(), ConstraintSet.TOP);
                            constraintSet.connect(teamsId.get(i), ConstraintSet.BOTTOM, teamsId.get(i + 1), ConstraintSet.TOP);
                        }

                        constraintSet.connect(teamsId.get(i), ConstraintSet.TOP, teamsId.get(i - 1), ConstraintSet.BOTTOM);
                        constraintSet.connect(teamsId.get(i), ConstraintSet.START, teamsId.get(0), ConstraintSet.START);
                        constraintSet.connect(teamsId.get(i), ConstraintSet.END, teamsId.get(0), ConstraintSet.END);
                    }

//        constraintSet.connect(buttonId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

                    constraintSet.connect(buttonId, ConstraintSet.START, R.id.linear_select_team, ConstraintSet.START);
                    constraintSet.connect(buttonId, ConstraintSet.END, R.id.linear_select_team, ConstraintSet.END);

                    constraintSet.connect(R.id.button_existing_teams, ConstraintSet.START, buttonId, ConstraintSet.START);
                    constraintSet.connect(R.id.button_existing_teams, ConstraintSet.END, buttonId, ConstraintSet.END);
                    constraintSet.connect(R.id.button_existing_teams, ConstraintSet.TOP, buttonId, ConstraintSet.BOTTOM);

                    constraintSet.applyTo((ConstraintLayout) rootLayout);


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.button_existing_teams).setVisibility(View.VISIBLE);
    }

    private void removeSelectedViews() {

        final List<Integer> teamIds = viewModel.getTextViewIds().getValue();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int length = teamIds.size();

                for (int i = 0; i < teamIds.size(); i++) {

                    rootLayout.removeView(findViewById(teamIds.get(i)));
                }

                rootLayout.removeView(findViewById(butId));
                teamIds.clear();
            }
        });

    }

//    public TextView createTextViews(ConstraintLayout.LayoutParams params, String hint){
//
//        EditText textView = new EditText(this);
//        textView.setLayoutParams(params);
//
//        textView.setId(textView.generateViewId());
//
//        textView.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
//        textView.setGravity(Gravity.START);
//        textView.setEms(5);
//        textView.setHint(hint);
////        textView.setText(lastTeamName);
//
//        return textView;
//    }
//
////    public TextView createTextViews(ConstraintLayout.LayoutParams params, String hint, String lastTeamName){
//
//        TextView txtView = createTextViews(params, hint);
//        txtView.setText(lastTeamName);
//        return txtView;
//    }

    private Button createButton() {

        final float px8 = getResources().getDimension(R.dimen.pixel_size_8);
        final float px0 = getResources().getDimension(R.dimen.pixel_size_0);
        final MaterialButton standButton = new MaterialButton(this);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins((int) px0, (int) px8, (int) px0, (int) px0);

                standButton.setLayoutParams(params);
                standButton.setId(View.generateViewId());

                if (isChangingTeam) {
                    standButton.setText(getString(R.string.change_teams));
                } else standButton.setText(getString(R.string.string_standing));
                standButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProgress.show();
                        final Map<String, String> chosenTeams = viewModel.getTeamInfo().getValue();

                        changeDatabase(chosenTeams);

                        observeChanges(chosenTeams);

                        saveTeamsDialog();
                        mProgress.hide();
                    }
                });
            }
        });

        return standButton;
    }

    private void observeChanges(final Map<String, String> chosenTeam) {
        final List<String> chosenTeams = new ArrayList<>(chosenTeam.keySet());
        viewModel.setNewSpinnerValue(spinTeam.getSelectedItemPosition());

        viewModel.getTeamNames().observe(TeamResultActivity.this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {

                strings.clear();
                strings.addAll(chosenTeams);
            }
        });
    }

    private void changeDatabase(final Map<String, String> chosenTeams) {
        AssetManager ass = getAssets();
//        neoLogo.put("III", null);
        for(final String ki : chosenTeams.keySet()){
            try{
                if(viewModel.getExistingTeams().getValue().contains(ki)){
                    String name = chosenTeams.get(ki) + ".png";

                    File filesDir = getFilesDir();
                    File openFile = new File(filesDir, name);

                    final String path = openFile.getPath();
                    viewModel.getTeamInfo().observe(this, new Observer<Map<String, String>>() {
                        @Override
                        public void onChanged(Map<String, String> stringStringMap) {
                            stringStringMap.put(ki, path);
                        }
                    });

                }
            }catch(Exception e){e.printStackTrace();}
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<TeamInfo> dbTeams;
                List<FixtureInfo> dbFixture;
                Map<String, String> value = viewModel.getTeamInfo().getValue();
                if(isChangingTeam){
                    dbTeams = viewModel.getPreviousSavedTeams();
                    dbFixture = viewModel.getPreviousSavedFixtures();
                }else{
                    dbTeams = null;
                    dbFixture = null;
                }
                List<TeamInfo> newTeams = leagueInfo.createTeams(value, dbTeams);
                viewModel.clearAllNames();
                viewModel.addTeams(newTeams);

                List<FixtureInfo> newFixtures = leagueInfo.generateFixtures(newTeams, dbFixture);
                viewModel.removeAllFixtures();
                viewModel.addFixtures(newFixtures);

            }
        });
    }

    @Override
    protected void onDestroy() {
        privateService.shutdownNow();
        super.onDestroy();
    }

    private FrameLayout inflateTeamTexts(final int i, final String hint) {
//        LiveData<List<String>> teamNames = viewModel.getTeamNames();
        List<String> teamNames = new ArrayList<>(viewModel.getTeamInfo().getValue().keySet());
        final List<String> existingTeam = viewModel.getExistingTeams().getValue();

        FrameLayout parentView = (FrameLayout) getLayoutInflater().inflate(R.layout.team_text_view, (ViewGroup) findViewById(R.id.team_text_parent), false);
        TextInputLayout inputLayout = parentView.findViewById(R.id.team_text_layout);
//        ((ViewGroup) inputLayout.getParent()).removeView(inputLayout);
        TextInputEditText editText = (TextInputEditText) inputLayout.getEditText();


        if (existingViewModel && i < teamNames.size()) {
            editText.setText(teamNames.get(i));
        }

        inputLayout.setHint(hint);

        final int id = View.generateViewId();
        parentView.setId(id);

        editText.addTextChangedListener(new TextWatcher() {
            private String newName;
            String previousName;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousName = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newName = s.toString();
                viewModel.getTeamInfo().observe(TeamResultActivity.this, new Observer<Map<String, String>>() {
                    @Override
                    public void onChanged(Map<String, String> stringStringMap) {
                        //each time the text in this textView changes, the new name should be observed immediately
                        String logoData = stringStringMap.get(previousName);
                        stringStringMap.keySet().remove(previousName);
                        stringStringMap.put(newName, logoData);
                    }
                });
            }
        });

        parentView.findViewById(R.id.image_team_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                fileIntent.setType("image/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(fileIntent, id);
            }
        });

        return parentView;
    }

    @Override
    protected void onResume() {
        spinTeam.setSelection(viewModel.getSpinnerValue().getValue());
        presetValues();

        if (spinTeam.getSelectedItemPosition() > 0) isChangingTeam = true;

        super.onResume();
    }

    private void existingTeamsDialog() {

        Map<String, List<String>> readed = readExistingFromFile();
        for(List<String> lis : readed.values()){
            viewModel.getExistingTeams().getValue().addAll(lis);
        }
        if(readed.containsKey("Others")) viewModel.getExistingTeams().getValue().add("Others#");
        AppCompatDialogFragment frag = TeamDialogFragment.dialogInstance(readed);
        frag.show(getSupportFragmentManager(), null);

        mProgress.hide();
    }

    private Map<String, List<String>> readExistingFromFile() {
        Map<String, List<String>> teams = new HashMap<>();

        try {
            teams =
                    privateService.submit(new Callable<Map<String, List<String>>>() {
                        @Override
                        public Map<String, List<String>> call() throws Exception {
                            InputStream input;
                            Map<String, List<String>> existingTeams = new HashMap<>();
                            File newFile;

                            if ((newFile = TeamResultActivity.this.getFilesDir()) == null)
                                return null;
                            try {
                                input = new FileInputStream(newFile.listFiles()[0]);
                                if (input != null) {
                                    InputStreamReader reader = new InputStreamReader(input);
                                    BufferedReader buff = new BufferedReader(reader);
                                    String nextLine = "";
                                    String currentLeague = null;

                                    while ((nextLine = buff.readLine()) != null) {
                                        StringBuilder nextTeam = new StringBuilder(nextLine);

                                        //The league carries # before the end of the line
                                        //so use a condition to check if its the current line string
                                        //meaning a key(league) has been found
                                        if (nextTeam.charAt(nextTeam.length() - 1) == '#') {
                                            nextTeam.deleteCharAt(nextTeam.length() - 1);
                                            currentLeague = nextTeam.toString();
                                            //a new key is born
                                            existingTeams.put(currentLeague, new ArrayList<String>());
                                            continue;
                                        }

                                        existingTeams.get(currentLeague).add(nextLine);
                                    }

                                    input.close();
                                    reader.close();
                                }
                            } catch (FileNotFoundException file) {
                                file.printStackTrace();
                            } catch (IOException io) {
                            }

                            return existingTeams;
                        }
                    }).get();
        } catch (Exception e) {
        }

        return teams;
    }

    private void saveTeamsDialog() {
        final Intent nextIntent = new Intent(TeamResultActivity.this, TeamSelectionActivity.class);
        //each key(team name) will help get its respective value(logo)
//        Map<String, String> urGroup = viewModel.getTeamInfo().getValue();
//        Bundle bund = new Bundle();
//        for(String key : urGroup.keySet()){
//            bund.putParcelable(key, urGroup.get(key));
//        }
//        nextIntent.putExtra(TeamSelectionActivity.TeamSelectionName, bund);

        new AlertDialog.Builder(this)
                .setMessage("Do you want to save these Teams to use next time?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveTeams();
                        startActivity(nextIntent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(nextIntent);
            }
        }).create().show();
    }

    private void becomeExisting() {

        privateService.execute(new Runnable() {
            @Override
            public void run() {
                if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
                    return;
                InputStream rawTeams = getResources().openRawResource(R.raw.existing_teams);
                try {
                    File dir = TeamResultActivity.this.getFilesDir();
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    dir.createNewFile();

//                    Path path = Paths.get(dir.toURI());
                    File newFile = new File(dir, "existing_teams.txt");
                    OutputStream os = new FileOutputStream(newFile);
                    byte[] teamBytes = new byte[rawTeams.available()];
                    rawTeams.read(teamBytes);
                    os.write(teamBytes);

                    os.close();
                    rawTeams.close();
//                    Files.createFile(path, new FileAttribute<String>() {
//                        @Override
//                        public String name() {
//                            return "existing_teams";
//                        }
//
//                        @Override
//                        public String value() {
//                            return null;
//                        }
//                    });
//                    Files.copy(rawTeams, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    //copy data from assets to the  folder
                    AssetManager ma = getAssets();
                    String[] logoList = ma.list("images");
                    for(String logo : logoList){
                        InputStream open = ma.open(new File("image/" + logo).getPath());
                        dir.createNewFile();
                        if(Arrays.asList(dir.list()).contains(logo)) continue;
                        OutputStream op = new FileOutputStream(new File(dir, logo));
                        byte[] by = new byte[open.available()];
                        open.read(by);
                        op.write(by);

                        op.close();
                        open.close();


                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void saveTeams(){
        privateService.execute(new Runnable() {
            @Override
            public void run() {
                String leagueName = "Others#";
//                List<String> teams = viewModel.getTeamNames().getValue();
                // TODO: Check lines 160, 170, 181, 410, createButton method, inflateTeamText, saveTeams methods and observe viewModel.TeamNames
                //TODO: Iterate through every files in Assets and copy its data onto the device
                List<String> teams = new ArrayList<>(viewModel.getTeamInfo().getValue().keySet());
                StringBuilder teamBuild = null;

                if(!viewModel.getExistingTeams().getValue().contains(leagueName)) {
                    teamBuild = new StringBuilder(leagueName + "\n");
                }

                for(String team : teams){
                    if(viewModel.getExistingTeams().getValue().contains(team)) continue;
                    teamBuild.append(team + "\n");
                }

                byte[] bite = teamBuild.toString().getBytes();
                File existDir = TeamResultActivity.this.getFilesDir();
                try {
                    if(existDir.exists()) {
//                        Files.write(existDir, teams, StandardOpenOption.APPEND);
                        OutputStream os = new FileOutputStream(existDir.listFiles()[0], true);
                        os.write(bite);

                        os.close();
                    }

                }catch(Exception e){e.printStackTrace();}
            }
        });
    }

    @Override
    public void positiveDialogButtonClicked(View dialogView) {
        List<String> chosenTeams = new ArrayList<>();
        LinearLayout linearDialogView = dialogView.findViewById(R.id.layout_dialog_teams);

        int childCount = linearDialogView.getChildCount();

        for(int i = 0; i < childCount; i++){

            View v = linearDialogView.getChildAt(i);
            if(v.getId() == R.id.text_league_header) continue;

            if(!((CheckBox)v).isChecked()) continue;
            chosenTeams.add(((TextView) v).getText().toString());

        }

        addToTextViews(chosenTeams);
    }

    private void addToTextViews(final List<String> teamList) {

        existingViewModel = true;
        ArrayAdapter<Integer> ok = (ArrayAdapter<Integer>) spinTeam.getAdapter();
        int positionSize = ok.getPosition(teamList.size());
        spinTeam.setSelection(positionSize, true);
        viewModel.getTeamInfo().observe(this, new Observer<Map<String, String>>() {
            @Override
            public void onChanged(Map<String, String> stringStringMap) {
                StringBuilder built = new StringBuilder("logo_**");
                for(String exist : teamList){
                    built.replace(5, built.length(), exist);
                    stringStringMap.put(exist, built.toString());
                }
            }
        });
        initializeTeamViews(teamList.size());
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        if(data.getData() == null) return;

        super.onActivityResult(requestCode, resultCode, data);
        final List<Integer> id = viewModel.getTextViewIds().getValue();
        Uri uri = data.getData();
        URI uRI;
        final String ur = "";
        try {
            uRI = new URI(uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
            ur.replaceAll("", new File(uRI).getPath());
        }catch(Exception e){e.printStackTrace();}

        if(resultCode <= requestCode){
            viewModel.getTeamInfo().observe(this, new Observer<Map<String, String>>() {
                @Override
                public void onChanged(Map<String, String> stringStringMap) {
                    for(int i : id){
                        if(requestCode == i){
                            //means the image gotten is for the team with this id
                            String namae = ((TextInputLayout) findViewById(i).findViewById(R.id.team_text_layout)).getEditText().getText().toString();
                            stringStringMap.put(namae, ur);
                            break;
                        }
                    }
                }
            });
        }
    }
}