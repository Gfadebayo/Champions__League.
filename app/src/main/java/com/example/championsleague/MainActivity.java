package com.example.championsleague;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.championsleague.utils.FileUtils;
import com.example.championsleague.viewmodels.FixtureViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        while(true){
            int perm = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(perm != PermissionChecker.PERMISSION_GRANTED){
                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, perms, 1);
            }else{
                break;
            }
        }

        FileUtils.transferToDisk(getAssets(), getExternalFilesDir(null));

        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        BottomNavigationView bottomView = findViewById(R.id.bottom_nav_view);


        AppBarConfiguration bar = new AppBarConfiguration.Builder(R.id.nav_fixtures, R.id.nav_home, R.id.nav_standing).build();
        NavigationUI.setupWithNavController(toolbar, navController, bar);
        NavigationUI.setupWithNavController(bottomView, navController);

        //Use this to initialize Fixture View Model beforehand so every logo can be loaded in advance
        new ViewModelProvider(this, new SavedStateViewModelFactory(getApplication(), this)).get(FixtureViewModel.class).loadTeamLogos();

//        new ViewModelProvider(this, new SavedStateViewModelFactory(getApplication(), this)).get(SelectionViewModel.class).watchTeamChanges(this);
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

        if(itemId == R.id.action_setting) startActivity(new Intent(this, SettingsActivity.class));

        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }
}