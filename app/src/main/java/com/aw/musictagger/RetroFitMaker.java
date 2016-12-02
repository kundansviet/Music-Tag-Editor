package com.aw.musictagger;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kundan on 11/11/2016.
 */

public class RetroFitMaker {

    //create new instance of Retrofit for specific url
    public Retrofit instanceMaker(String url) {
/*

        /*long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(new File(GFileApplication.mContext.getCacheDir(), "http"), SIZE_OF_CACHE);
        OkHttpClient client = new OkHttpClient.Builder().cache(cache).addNetworkInterceptor(new CachingControlInterceptor()).build();
*/

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

}
