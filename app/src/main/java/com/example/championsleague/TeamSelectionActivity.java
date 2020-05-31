package com.example.championsleague;

import android.content.ContentProviderClient;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.example.championsleague.models.League;
import com.example.championsleague.models.LeagueInfo;
import com.example.championsleague.ui.ChampionFragment;
import com.example.championsleague.ui.home.HomeFragment;
import com.example.championsleague.ui.standing.StandingFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class TeamSelectionActivity extends AppCompatActivity {

    private Fragment fragment;
    private SharedPreferences.OnSharedPreferenceChangeListener listen;
    public static final String TeamSelectionName = "com.example.championsLeague.IntentExtra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(drawerLayout).build();

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {

                changeNavDestination(destination);
            }
        });

        SharedPreferences defPref = PreferenceManager.getDefaultSharedPreferences(this);

        defPref.registerOnSharedPreferenceChangeListener(listen = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String sharedString;
                TextView tv;
                switch(key){
                    case ("fav_club"):
                        sharedString = sharedPreferences.getString(key, "N/A");
                        tv = findViewById(R.id.text_fav_team);
                        postNewInfo(sharedString, tv);
                        break;
                    case ("fav_player"):
                        sharedString = sharedPreferences.getString(key, "N/A");
                        tv = findViewById(R.id.text_fav_player);
                        postNewInfo(sharedString, tv);
                        break;
                    case ("fav_pos"):
                        sharedString = sharedPreferences.getString(key, "N/A");
                        tv = findViewById(R.id.text_fav_pos);
                        postNewInfo(sharedString, tv);
                        break;
                    case ("fav_team_logo"):
                        setLogo(Uri.parse(sharedPreferences.getString(key, null)));
                        break;
                }
            }
        });
    }

    private void changeNavDestination(NavDestination destination) {
//        Bundle bundleExtra = getIntent().getBundleExtra(TeamSelectionName);
        int destId = destination.getId();
        fragment = null;

        switch (destId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
//                fragment.setArguments(bundleExtra);
                break;
            case (R.id.nav_standing):
                fragment = new StandingFragment();
                break;
            case (R.id.nav_settings):
//                fragment = new SettingsActivity.SettingsFragment();
                startActivity(new Intent(this, SettingsActivity.class));
                return;
        }

        getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .setReorderingAllowed(true)
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    public void setLogo(Uri imageData){
        try {
            ContentProviderClient client = getContentResolver().acquireContentProviderClient(imageData);
            ParcelFileDescriptor rwt = client.openFile(imageData, "r");
            final Bitmap imgBm = BitmapFactory.decodeFileDescriptor(rwt.getFileDescriptor());


            final ImageView img = findViewById(R.id.image_logo);
            img.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    img.setImageBitmap(imgBm);
                }
            });
        }catch(Exception e){
            e.getMessage();
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        return super.onCreateOptionsMenu(menu);
//        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        int itemId = item.getItemId();

        if (itemId == R.id.action_team_select) {

//            Intent nextIntent = new Intent(this, TeamResultActivity.class);
//            nextIntent.putExtra(TeamResultActivity.SELECT_BACK, 1);
//            startActivity(nextIntent);

            finish();
        }else if(itemId == R.id.action_setting){
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    public void updateFixtures(View v) {
                ((HomeFragment) fragment).updateFixtures(true);
    }

    private void postNewInfo(final String newInfo, final TextView affectedView){
        String prevText = affectedView.getText().toString();
        final StringBuilder build = new StringBuilder(prevText);
        int colIndec = build.indexOf(":") + 1;

        build.replace(colIndec, affectedView.getText().length(), newInfo);
        affectedView.setText(build.toString());
//        boolean post = affectedView.getHandler().post(new Runnable() {
//            @Override
//            public void run() {
////                affectedView.setText(build.toString());
//            }
//        });
    }

}