package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.DiscoverApi;
import com.anspark.models.PaginationResponse;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverRepository {
    private final DiscoverApi api;

    public DiscoverRepository(Context context) {
        this.api = ApiService.discover(context);
    }

    public void getDiscoverProfiles(int page, RepositoryCallback<List<Profile>> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(MockData.sampleDiscoverProfiles());
            return;
        }

        api.getDiscoverProfiles(page, Constants.PAGE_SIZE).enqueue(new Callback<PaginationResponse<Profile>>() {
            @Override
            public void onResponse(Call<PaginationResponse<Profile>> call, Response<PaginationResponse<Profile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Nie udalo sie pobrac profili");
                }
            }

            @Override
            public void onFailure(Call<PaginationResponse<Profile>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }
}
