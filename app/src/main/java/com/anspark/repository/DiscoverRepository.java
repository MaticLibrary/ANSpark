package com.anspark.repository;

import android.content.Context;
import android.util.Log;

import com.anspark.api.ApiService;
import com.anspark.api.DiscoverApi;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverRepository {
    private static final String TAG = "DISCOVER_REPO";
    private final DiscoverApi api;

    public DiscoverRepository(Context context) {
        this.api = ApiService.discover(context);
    }

    public void getDiscoverProfiles(int page, RepositoryCallback<List<Profile>> callback) {
        Log.d(TAG, "getDiscoverProfiles called, page: " + page);

        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(MockData.sampleDiscoverProfiles());
            return;
        }

        api.getDiscoverProfiles(page, Constants.PAGE_SIZE).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                Log.d(TAG, "response code: " + response.code());

                // ---- Додаємо лог для сирого JSON ----
                try {
                    // Спробуємо отримати тіло як рядок через Gson
                    String rawJson = response.body() != null ? new com.google.gson.Gson().toJson(response.body()) : "null";
                    Log.d(TAG, "Raw JSON (body): " + rawJson);

                    // Якщо тіло пусте, можливо помилка в errorBody
                    if (response.errorBody() != null) {
                        String errorJson = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorJson);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading response: " + e.getMessage());
                }
                // ------------------------------------

                if (response.isSuccessful() && response.body() != null) {
                    List<Profile> profiles = response.body();
                    Log.d(TAG, "profiles count: " + profiles.size());
                    callback.onSuccess(profiles);
                } else {
                    Log.e(TAG, "error: " + response.code());
                    callback.onError("Nie udalo sie pobrac profili");
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                Log.e(TAG, "failure: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}