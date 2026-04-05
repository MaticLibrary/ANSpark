package com.anspark.api;

import com.anspark.models.MatchResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface MatchApi {
    // Відправка лайка (для Discover)
    @POST("likes/")
    Call<Map<String, Object>> like(@Body Map<String, Object> likeRequest);

    // Отримання списку Match для вкладки "Dopasowania"
    @GET("likes/matches")
    Call<List<MatchResponse>> getMatches();  // ← ЗМІНИТИ НА MatchResponse
}