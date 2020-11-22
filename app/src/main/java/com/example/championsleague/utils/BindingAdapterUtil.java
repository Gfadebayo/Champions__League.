package com.example.championsleague.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.example.championsleague.R;
import com.example.championsleague.databinding.ListFixturesBinding;
import com.example.championsleague.models.FixtureInfo;

public class BindingAdapterUtil {

    @BindingAdapter("setImageBitmap")
    public static void getTeamLogo(final ImageView view, final FixtureInfo fileName) {

        new Handler(Looper.getMainLooper()).post(() -> {
            Bitmap map = view.getId() == R.id.image_home ? fileName.getHomeLogo() : fileName.getAwayLogo();
            if(map == null) view.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_flag_black_24dp, null));
            else {
                Request request = Glide.with(view)
                        .load(map)
                        .centerInside()
                        .into(view)
                        .getRequest();

                if (request.isRunning()) return;
                request.begin();
            }
        });
        }

    @BindingAdapter("android:onTextChanged")
    public static void textChangeListener(final TextView view, final FixtureInfo fix){

        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                ListFixturesBinding bind = DataBindingUtil.findBinding(view);
                if(fix.getHomeScore() != -1 && fix.getAwayScore() != -1) bind.submitButt.setVisibility(View.VISIBLE);
                else bind.submitButt.setVisibility(View.GONE);
            }
        });
    }

    @BindingAdapter("setPrefValues")
    public static void defaultPrefValues(View v, String anythingElse){
        Context context = v.getContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        switch(v.getId()){
            case R.id.image_home:
            case R.id.image_away:
                boolean ok = pref.getBoolean("KEY_IMAGE", true);
                int vis = ok ? View.VISIBLE : View.GONE;
                v.setVisibility(vis);
                break;

            case R.id.text_away:
            case R.id.text_home:
                break;
        }
    }
}
