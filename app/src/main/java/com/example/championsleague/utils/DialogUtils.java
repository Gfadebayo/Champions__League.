package com.example.championsleague.utils;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.championsleague.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class DialogUtils {

    public static void saveTeamsDialog(final Activity context, final List<String> names) {

        new AlertDialog.Builder(context)
                .setMessage("Do you want to save these Teams to use next time?")
                .setPositiveButton("Yes", (dialog, which) -> FileUtils.saveTeams(context
                        .getExternalFilesDir(FileUtils.EXISTING_TEAMS_DIR), names))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> Navigation.findNavController(context, R.id.nav_host_fragment)
                        .navigate(R.id.action_nav_home_to_nav_fixtures)).create().show();
    }

    public static AlertDialog pleaseWaitDialog(final Fragment fragment){
        return new MaterialAlertDialogBuilder(fragment.requireContext())
                .setCancelable(false)
                .create();
    }
}
