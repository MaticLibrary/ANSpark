package com.anspark.repository;

import android.content.Context;
import android.util.Log;

import com.anspark.api.ApiService;
import com.anspark.api.ProfileApi;
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;
import com.anspark.utils.TokenManager;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private static final String TAG = "PROFILE_REPO";
    private static Profile cachedProfile;
    private final ProfileApi api;
    private final TokenManager tokenManager;

    public ProfileRepository(Context context) {
        this.api = ApiService.profile(context);
        this.tokenManager = new TokenManager(context);
    }

    public void getMyProfile(RepositoryCallback<Profile> callback) {
        Log.d(TAG, "getMyProfile called");
        String token = tokenManager.getToken();
        Log.d(TAG, "Token exists: " + (token != null ? "YES" : "NO"));
        if (token != null) {
            Log.d(TAG, "Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        }

        if (Constants.USE_MOCK_DATA) {
            Profile profile = MockData.sampleProfile();
            cacheProfile(profile);
            callback.onSuccess(new Profile(profile));
            return;
        }

        api.getMyProfile().enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Log.d(TAG, "getMyProfile response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "getMyProfile success, bio: " + response.body().getBio());
                    Profile merged = mergeLocalState(response.body());
                    cacheProfile(merged);
                    callback.onSuccess(new Profile(merged));
                } else {
                    String errorMsg = extractErrorMessage(response, "Nie udalo sie pobrac profilu");
                    Log.e(TAG, "getMyProfile failed: " + response.code() + " - " + errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.e(TAG, "getMyProfile network error: " + t.getMessage(), t);
                if (cachedProfile != null) {
                    callback.onSuccess(new Profile(cachedProfile));
                    return;
                }
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void updateProfile(Profile profile, RepositoryCallback<Profile> callback) {
        Log.d(TAG, "updateProfile called");

        String token = tokenManager.getToken();
        Log.d(TAG, "Token before update: " + (token != null ? "YES" : "NO"));

        if (token == null) {
            Log.e(TAG, "No token! User not logged in!");
            callback.onError("Brak tokenu. Zaloguj się ponownie.");
            return;
        }

        Log.d(TAG, "Profile bio: " + profile.getBio());
        Log.d(TAG, "Profile city: " + profile.getCity());
        Log.d(TAG, "Profile gender: " + profile.getGender());
        Log.d(TAG, "Profile preference: " + profile.getPreference());

        if (Constants.USE_MOCK_DATA) {
            saveLocalProfile(profile);
            callback.onSuccess(new Profile(profile));
            return;
        }

        api.updateProfile(profile).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Log.d(TAG, "updateProfile response code: " + response.code());
                Log.d(TAG, "updateProfile response message: " + response.message());

                if (response.isSuccessful()) {
                    Profile result = response.body() != null ? response.body() : profile;
                    result.setVerified(profile.isVerified());
                    cacheProfile(result);
                    Log.d(TAG, "updateProfile success!");
                    callback.onSuccess(new Profile(result));
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    String errorMsg = extractErrorMessage(response, "Nie udalo sie zapisac profilu");
                    Log.e(TAG, "updateProfile failed: " + response.code() + " - " + errorMsg);
                    Log.e(TAG, "Error details: " + errorBody);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.e(TAG, "updateProfile network error: " + t.getMessage(), t);
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void uploadPhoto(File photoFile, RepositoryCallback<Photo> callback) {
        Log.d(TAG, "uploadPhoto called: " + photoFile.getName());

        if (Constants.USE_MOCK_DATA) {
            Photo photo = new Photo();
            photo.setId("mock_photo_" + System.currentTimeMillis());
            photo.setUrl(photoFile.toURI().toString());
            callback.onSuccess(photo);
            return;
        }

        RequestBody requestBody = RequestBody.create(photoFile, MediaType.parse("image/*"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", photoFile.getName(), requestBody);

        api.uploadPhoto(filePart).enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                Log.d(TAG, "uploadPhoto response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "uploadPhoto success, url: " + response.body().getUrl());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "uploadPhoto failed: " + response.code());
                    callback.onError(extractErrorMessage(response, "Nie udalo sie dodac zdjecia"));
                }
            }

            @Override
            public void onFailure(Call<Photo> call, Throwable t) {
                Log.e(TAG, "uploadPhoto network error: " + t.getMessage(), t);
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void saveLocalProfile(Profile profile) {
        cacheProfile(profile);
        if (Constants.USE_MOCK_DATA) {
            MockData.updateSampleProfile(profile);
        }
    }

    private void cacheProfile(Profile profile) {
        cachedProfile = profile != null ? new Profile(profile) : null;
    }

    private Profile mergeLocalState(Profile remoteProfile) {
        Profile merged = new Profile(remoteProfile);
        if (cachedProfile != null && cachedProfile.isVerified()) {
            merged.setVerified(true);
        }
        return merged;
    }

    private String extractErrorMessage(Response<?> response, String fallback) {
        if (response == null || response.errorBody() == null) {
            return fallback;
        }

        try {
            String body = response.errorBody().string();
            Log.d(TAG, "Error body: " + body);
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        } catch (IOException ignored) {
        }

        return fallback;
    }
}