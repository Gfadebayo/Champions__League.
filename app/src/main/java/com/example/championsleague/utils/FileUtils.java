package com.example.championsleague.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.example.championsleague.models.TeamEmpty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class FileUtils {

    public static final String TEAMS_LOGO_DIR = "team logos";
    private final static ExecutorService service = Executors.newCachedThreadPool();
    public static final String EXISTING_TEAMS_DIR = "existing_teams.txt";
    public static final String LOCAL_TEAM_FILE = "teams.json";

    public static void transferToDisk(final AssetManager manager, final File saveDir) {

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return;

        service.execute(new Runnable() {
            @Override
            public void run() {

                InputStream inputStream;
                OutputStream outputStream;

                try {
                    File newFile = new File(saveDir, EXISTING_TEAMS_DIR);

                    //only write if the file doesnt exist skip if it does
                    if(!newFile.exists()) {
                        newFile.createNewFile();
                        inputStream = manager.open(EXISTING_TEAMS_DIR);
                        outputStream = new FileOutputStream(newFile);
                        byte[] teamBytes = new byte[inputStream.available()];

                        inputStream.read(teamBytes);
                        outputStream.write(teamBytes);

                        inputStream.close();
                        outputStream.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void saveTeams(final File saveFile, final List<String> teams){
        service.execute(() -> {

            ArrayList<String> currentTeams = new ArrayList<>();
            StringBuilder teamBuild = new StringBuilder();
            OutputStream os = null;

            try{

                //This is to ignore teams already present in the file
                BufferedReader teamChecker = new BufferedReader(new FileReader(saveFile));
                String line;

                //The very first line should be the header ie league name
                teamBuild.append(teamChecker.readLine());

                while((line = teamChecker.readLine()) != null) currentTeams.add(line);

                //Reset then read the very first line so we can get the league name(others)
                teamChecker.close();

                for(String team : teams){

                if(currentTeams.contains(team)) continue;

                teamBuild.append("\n").append(team);

                }

            byte[] bite = teamBuild.toString().getBytes();

            os = new FileOutputStream(saveFile, true);

            os.write(bite);

            }catch(IOException e){e.printStackTrace();
            }finally {
                try {
                    if(os != null) os.close();
                }catch(IOException ioe){ioe.printStackTrace();}
            }
        });
    }

    public static Map<String, List<String>> readExistingTeamsFromFile(final File dir) {
        Map<String, List<String>> teams = new HashMap<>();

        try {
            teams =
                    service.submit(() -> {
                        Map<String, List<String>> existingTeams = new HashMap<>();

                        try {
                            BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(dir)));
                            String nextLine = "";
                            String currentLeague = null;

                            while ((nextLine = buff.readLine()) != null) {
                                StringBuilder nextTeam = new StringBuilder(nextLine);

                                if(nextLine.isEmpty()) continue;
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

//                                    input.close();
//                                    reader.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return existingTeams;
                    }).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return teams;
    }

    public static boolean saveImage(InputStream stream, String name, File saveDir){

        File to = new File(saveDir, name + ".png");

        if(to.exists() && to.length() > 0) return true;

        OutputStream toStream = null;

        try {
            toStream = new FileOutputStream(to);
            byte[] bytes = new byte[stream.available()];

            stream.read(bytes);
            toStream.write(bytes);

            return true;
        }catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }
        finally {
            try{
                stream.close();
                toStream.close();
            }catch (IOException ioe){ioe.printStackTrace(); }
        }
    }

    public static boolean deleteImage(String name, File savedDir){
        File image = new File(savedDir, name + ".png");
        if(!image.exists()) return false;
        else return image.delete();
    }

    public static void saveImages(final File saveDir, final List<TeamEmpty> teamLogos){
        service.submit(() -> {
            OkHttpClient client = new OkHttpClient();



            for(TeamEmpty team : teamLogos){

                try {
                    String urlStr = team.getCrestUrl();
                    if(urlStr.isEmpty()) continue;

                    String extension = urlStr.substring(urlStr.lastIndexOf('.'));

                    File saveFile = new File(saveDir, team.getName() + extension);
                    if(saveFile.exists() && saveFile.length() > 1) return;
                    saveFile.createNewFile();


                    Request url = new Request.Builder().url(HttpUrl.parse(urlStr)).build();

                    ResponseBody execute = client.newCall(url).execute().body();
                    FileOutputStream out = new FileOutputStream(saveFile);
                    out.write(execute.bytes());
                    out.close();
                }catch(IOException ioe){ioe.printStackTrace();}
            }
        });
    }

    public static Map<String, List<TeamEmpty>> getLocalTeams(Context dir){
        File local = dir.getExternalFilesDir(LOCAL_TEAM_FILE);
        Map<String, List<TeamEmpty>> result = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(local));
            String nextLine = "";
            StringBuilder jsonBuilder = new StringBuilder();
            while((nextLine = reader.readLine()) != null){
                jsonBuilder.append(nextLine);
            }

            JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
            JSONArray teamArray = jsonObject.getJSONArray("Local");

            result.put("Local", createLocalTeams(teamArray));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static List<TeamEmpty> createLocalTeams(JSONArray array){
        List<TeamEmpty> empty = new ArrayList<>();

        try {
        for(int i = 0; i < array.length(); i++){

                String teamName = array.getString(i);
                empty.add(new TeamEmpty(0, teamName, teamName, ""));
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return empty;
    }

    public static void transformTxTtoJson(Context saveDir){

        File dir = saveDir.getExternalFilesDir(EXISTING_TEAMS_DIR);
        List<String> teams = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String nextLine = "";
            while((nextLine = reader.readLine()) != null){
                if(!nextLine.contains("Others")) teams.add(nextLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
        JSONArray array = new JSONArray(teams);
        JSONObject obj = new JSONObject();

        obj.put("Local", array);

            String s = obj.toString(4);

            new FileOutputStream(new File(dir.getParent(), LOCAL_TEAM_FILE)).write(s.getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
