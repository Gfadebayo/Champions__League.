package com.example.championsleague.internet

import com.example.championsleague.models.League
import com.example.championsleague.models.TeamEmpty
import com.example.championsleague.models.TeamInfo
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Converter


class LeagueConverter: Converter<ResponseBody, ArrayList<League>> {


    override fun convert(value: ResponseBody): ArrayList<League> {
        val list = ArrayList<League>()

        val body = value.string()
        value.close()

        val json = JSONObject(body)
        with(json.getJSONArray("competitions")){
            val length = length()
            for (i in 0 until length){
                val obj = getJSONObject(i)

                if(obj.optString("plan").equals("tier_one", true)) list.add(readArrayObject(obj))
            }
        }

        return list
    }

    private fun readArrayObject(obj: JSONObject): League{

        val id = obj.optString("id")
        val leagueName = obj.optString("name")
        val countryName = obj.optJSONObject("area").optString("name") ?: ""
        val plan = obj.optString("plan")

        return League(id.toInt(), leagueName, countryName)
    }
}

class TeamConverter: Converter<ResponseBody, ArrayList<TeamEmpty>>{


    override fun convert(value: ResponseBody): ArrayList<TeamEmpty>? {


        val list = ArrayList<TeamEmpty>()

        val body = value.string()
        value.close()

        val json = JSONObject(body)

        with(json.optJSONArray("teams")){
            for(i in 0 until length()){
                list.add(readArrayObject(getJSONObject(i)))
            }
        }

        return list
    }

    private fun readArrayObject(obj: JSONObject): TeamEmpty {
        val id = obj.optString("id")
        val name = obj.optString("name")
        val shortName = obj.optString("shortName")
        val crestUrl = obj.optString("crestUrl").apply {
            this.filter { it != '\\' }
        }

        return TeamEmpty(id.toInt(), name, shortName, crestUrl)
    }
}