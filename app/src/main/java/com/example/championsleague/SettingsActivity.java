package com.example.championsleague;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.championsleague.database.LeagueRepository;
import com.google.android.material.snackbar.Snackbar;

import java.util.stream.IntStream;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tb.getNavigationIcon().setTint(getResources().getColor(R.color.actionBarTint, null));

    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference lengthPref = findPreference("KEY_MAX_TEAM");
            lengthPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            lengthPref.setSummaryProvider(preference -> ((EditTextPreference) preference).getText());


            EditTextPreference nameLength = findPreference("KEY_NAME_LENGTH");
            nameLength.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            nameLength.setSummaryProvider(pref -> ((EditTextPreference) pref).getText());


            String[] stream = IntStream.range(0, 9).mapToObj(String::valueOf).toArray(String[]::new);


            MultiSelectListPreference listPref = findPreference("KEY_TABLE_INFO");
            listPref.setEntryValues(stream);

            Preference pref = findPreference("KEY_RESET_FILTER");

            pref.setOnPreferenceClickListener(preference -> {
                LocalPersistence pers = new LocalPersistence(getContext().getSharedPreferences(LocalPersistence.PREF_NAME, Context.MODE_PRIVATE));
                pers.resetQueryPref();
                Snackbar.make(getView(), "Filter reset successfully", Snackbar.LENGTH_SHORT).show();

                return true;
            });

            findPreference("KEY_RESET_DB").setOnPreferenceClickListener(preference -> {
                LeagueRepository.getInstance(requireActivity().getApplication()).clearDb();
                Snackbar.make(getView(), "Database Reset Successfully", Snackbar.LENGTH_SHORT).show();
                return true;
            });

        }

        public SettingsFragment getInstance(){
            return new SettingsFragment();
        }
    }
}