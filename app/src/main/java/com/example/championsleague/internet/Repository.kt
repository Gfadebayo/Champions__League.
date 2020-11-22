package com.example.championsleague.internet

import android.content.Context
import android.util.Log
import com.example.championsleague.BuildConfig.*
import com.example.championsleague.models.League
import com.example.championsleague.models.TeamEmpty
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit

class Repository private constructor(private val context: Context){

    private val mUrl = "https://api.football-data.org/"
    private val mClient: Retrofit by lazy { createClient() }
    private val mFootballApi: FootballApi

    init {
        mFootballApi = mClient.create(FootballApi::class.java)
    }

    private fun createClient(): Retrofit{

        val http = OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES).addInterceptor(ApiInterceptor()).cache(createCacheDir()).build()

        return Retrofit.Builder().baseUrl(mUrl).addConverterFactory(Factories.TeamJsonFactory())
                .addConverterFactory(Factories.LeagueJsonFactory())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()).client(http).build()
    }

    private fun createCacheDir(): Cache{
        val dir = if(DEBUG) context.getExternalFilesDir("retrofit_cache")
        else context.getDir("cache_", 0)

        return Cache(dir, 3000000)
    }

    fun getLeagues() = mFootballApi.leagues.subscribeOn(Schedulers.io())

    fun getTeams(id: Int) = mFootballApi.getTeams(id).subscribeOn(Schedulers.io())

    companion object{
        val AUTH_TOKEN = "2762b8b4dc2a4a348f6f44716bc15f5f"
        val AUTH_HEADER = "X-Auth-Token"

        @Volatile private var INSTANCE: Repository? = null

        fun getInstance(context: Context): Repository =
            INSTANCE ?: synchronized(this){
                INSTANCE = Repository(context)
                return INSTANCE!!
        }
    }
}