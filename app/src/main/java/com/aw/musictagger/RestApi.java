package com.aw.musictagger;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by kundan on 11/11/2016.
 */

public interface RestApi {
    @GET("/search?")
    Call<Model> searchItunes(
            @Header("Content-Type")String content_type,
            @Query("term") String term);

}
