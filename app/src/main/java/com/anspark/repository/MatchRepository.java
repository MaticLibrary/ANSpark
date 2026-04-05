package com.anspark.repository;

import android.content.Context;
import android.util.Log;

import com.anspark.api.ApiService;
import com.anspark.api.MatchApi;
import com.anspark.models.MatchResponse;
import com.anspark.models.Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {
    private static final String TAG = "MATCH_REPO";
    private final MatchApi matchApi;

    public MatchRepository(Context context) {
        this.matchApi = ApiService.match(context);
    }

    // Отримання списку Match
    public void getMatches(RepositoryCallback<List<MatchResponse>> callback) {
        matchApi.getMatches().enqueue(new Callback<List<MatchResponse>>() {
            @Override
            public void onResponse(Call<List<MatchResponse>> call, Response<List<MatchResponse>> response) {
                Log.d(TAG, "getMatches response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Matches count: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Nie udalo sie pobrac matchy: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<MatchResponse>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // Відправка лайка (для Discover)
    public void sendLike(Profile profile, RepositoryCallback<Map<String, Object>> callback) {
        Map<String, Object> likeRequest = new HashMap<>();
        likeRequest.put("toProfileId", profile.getId());

        matchApi.like(likeRequest).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d(TAG, "Like response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String error = "Like failed: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}