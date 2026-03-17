package com.anspark.api;

import com.anspark.models.Match;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MatchApi {
    @GET("matches")
    Call<List<Match>> getMatches();

    @POST("likes/")
    Call<Map<String, Object>> like(@Body Map<String, Object> likeRequest);

    @GET("likes/matches")
    Call<List<Match>> getMatches();
}
