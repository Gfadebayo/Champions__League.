package com.example.championsleague.internet;

import android.util.Log;

import com.example.championsleague.BuildConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

    abstract class Factories {

        public static class LeagueJsonFactory extends Converter.Factory {

            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (BuildConfig.DEBUG) Log.i("Converter", "Type found is: " + type.toString());

                String name = type.toString();
                if (!(name.contains("League")))
                    return super.responseBodyConverter(type, annotations, retrofit);

                return new LeagueConverter();
            }

            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            }

            @Override
            public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                return super.stringConverter(type, annotations, retrofit);
            }
        }

        public static class TeamJsonFactory extends Converter.Factory{

            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (BuildConfig.DEBUG) Log.i("Converter", "Type found is: " + type.toString());

                String name = type.toString();
                if (!(name.contains("TeamEmpty")))
                    return super.responseBodyConverter(type, annotations, retrofit);

                return new TeamConverter();
            }

            @Override
            public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            }

            @Override
            public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                return super.stringConverter(type, annotations, retrofit);
            }
        }
    }
