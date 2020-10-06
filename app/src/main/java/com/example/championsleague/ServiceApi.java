package com.example.championsleague;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ServiceApi {


        @GET("{val}/")
        Call<ResponseBody> next(@Path(value = "val", encoded = true) String val);

        @GET("/teams/club-teams/")
        Call<ResponseBody> move();
    }