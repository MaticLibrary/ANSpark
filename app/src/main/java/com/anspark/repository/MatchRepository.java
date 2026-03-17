package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.MatchApi;
import com.anspark.models.Match;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {
    private final MatchApi api;

    public MatchRepository(Context context) {
        this.api = ApiService.match(context);
    }

    public void getMatches(RepositoryCallback<List<Match>> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(MockData.sampleMatches());
            return;
        }

        api.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Nie udalo sie pobrac dopasowan");
                }
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void like(Profile profile, RepositoryCallback<Map<String, Object>> callback) {
        if (Constants.USE_MOCK_DATA) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profileId", profile.getId());
            callback.onSuccess(response);
            return;
        }

        Map<String, Object> likeRequest = new HashMap<>();
        likeRequest.put("liked_profile_id", profile.getId());

        api.like(likeRequest).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Nie udalo sie wyslac sympatii");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }
}
