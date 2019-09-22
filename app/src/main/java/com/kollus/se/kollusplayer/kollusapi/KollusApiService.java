package com.kollus.se.kollusplayer.kollusapi;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KollusApiService {
    @GET("channel/media_content")
    Call<JsonObject> getContentList(@Query("access_token") String access_token, @Query("channel_key") String channel_key);
}
