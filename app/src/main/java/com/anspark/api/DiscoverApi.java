package com.anspark.api;

import com.anspark.models.Profile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DiscoverApi {
    @GET("discover")
    Call<List<Profile>> getDiscoverProfiles(
            @Query("page") int page,
            @Query("size") int size
    );
}