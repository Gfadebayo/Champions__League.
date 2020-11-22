package com.example.championsleague;

import android.content.SharedPreferences;

public class LocalPersistence {
    public static final String PREF_NAME = "local_pref";

    private final String DEFAULT_QUERY = "SELECT * FROM Fixtures";

    private final SharedPreferences mPreferences;
    private final SharedPreferences.Editor mEditor;

    private final String KEY_QUERY = "query_string";
    private final String KEY_COMPLETED = "completed_fixtures";

    public LocalPersistence(SharedPreferences myPref){
        mPreferences = myPref;
        mEditor = myPref.edit();
    }

    public void setQuery(String query){
        mEditor.putString(KEY_QUERY, query).commit();
    }

    public String getQuery(){
        return mPreferences.getString(KEY_QUERY, DEFAULT_QUERY);
    }

    public void resetQueryPref(){
        mEditor.putString(KEY_QUERY, DEFAULT_QUERY).commit();
    }

    public void setComplete(boolean forComplete){
        mEditor.putBoolean(KEY_COMPLETED, forComplete).commit();
    }

    public boolean getComplete(){
        return mPreferences.getBoolean(KEY_COMPLETED, false);
    }
}
