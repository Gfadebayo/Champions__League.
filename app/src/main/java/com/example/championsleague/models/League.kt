package com.example.championsleague.models

import java.lang.reflect.Type

data class League(val id: Int, val leagueName: String, val countryName: String): Type

data class TeamEmpty(val id: Int, val name: String, val shortName: String, val crestUrl: String): Type