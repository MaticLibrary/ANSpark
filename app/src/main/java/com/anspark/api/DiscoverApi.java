package com.anspark.api;

import com.anspark.models.PaginationResponse;
import com.anspark.models.Profile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DiscoverApi {
    @GET("discover")
    Call<PaginationResponse<Profile>> getDiscoverProfiles(
            @Query("page") int page,
            @Query("limit") int limit
    );
}
