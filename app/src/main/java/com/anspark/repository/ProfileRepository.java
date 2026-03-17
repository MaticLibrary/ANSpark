package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.ProfileApi;
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private final ProfileApi api;

    public ProfileRepository(Context context) {
        this.api = ApiService.profile(context);
    }

    public void getMyProfile(RepositoryCallback<Profile> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(MockData.sampleProfile());
            return;
        }

        api.getMyProfile().enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Nie udalo sie pobrac profilu");
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void updateProfile(Profile profile, RepositoryCallback<Profile> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(profile);
            return;
        }

        api.updateProfile(profile).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Nie udalo sie zapisac profilu");
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void uploadPhoto(Photo photo, RepositoryCallback<Photo> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(photo);
            return;
        }

        api.uploadPhoto(photo).enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Nie udalo sie dodac zdjecia");
                }
            }

            @Override
            public void onFailure(Call<Photo> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }
}
