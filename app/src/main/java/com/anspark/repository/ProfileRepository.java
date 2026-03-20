package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.ProfileApi;
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
                    callback.onError(extractErrorMessage(response, "Nie udalo sie pobrac profilu"));
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
                    callback.onError(extractErrorMessage(response, "Nie udalo sie zapisac profilu"));
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void uploadPhoto(File photoFile, RepositoryCallback<Photo> callback) {
        if (Constants.USE_MOCK_DATA) {
            Photo photo = new Photo();
            photo.setUrl("mock://photo");
            callback.onSuccess(photo);
            return;
        }

        RequestBody requestBody = RequestBody.create(photoFile, MediaType.parse("image/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", photoFile.getName(), requestBody);

        api.uploadPhoto(filePart).enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response, "Nie udalo sie dodac zdjecia"));
                }
            }

            @Override
            public void onFailure(Call<Photo> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    private String extractErrorMessage(Response<?> response, String fallback) {
        if (response == null || response.errorBody() == null) {
            return fallback;
        }

        try {
            String body = response.errorBody().string();
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        } catch (IOException ignored) {
        }

        return fallback;
    }
}
