package com.example.championsleague.utils;

import androidx.databinding.InverseMethod;

public class Converter {

    public static Integer stringToInt(String value){
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException nfe){
            return -1;
        }
    }

    @InverseMethod("stringToInt")
    public static String intToString(Integer value){
        return value < 0 ? "-" : String.valueOf(value);
    }
}
