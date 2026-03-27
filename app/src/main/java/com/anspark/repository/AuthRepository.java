package com.anspark.repository;

import android.content.Context;
import android.util.Log;

import com.anspark.api.AuthApi;
import com.anspark.api.RetrofitClient;
import com.anspark.models.AuthResponse;
import com.anspark.models.LoginRequest;
import com.anspark.models.RegisterRequest;
import com.anspark.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AUTH_REPO";
    private final AuthApi authApi;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        RetrofitClient client = RetrofitClient.getInstance(context);
        this.authApi = client.create(AuthApi.class);
        this.tokenManager = client.getTokenManager();
        Log.d(TAG, "AuthRepository created");
    }

    public void logout() {
        tokenManager.clear();
        Log.d(TAG, "User logged out, token cleared");
    }

    public void register(RegisterRequest request, RepositoryCallback<AuthResponse> callback) {
        Log.d(TAG, "========== REGISTER START ==========");
        Log.d(TAG, "Register request email: " + request.getEmail());
        Log.d(TAG, "Register request displayName: " + request.getDisplayName());
        Log.d(TAG, "Register request gender: " + request.getGender());
        Log.d(TAG, "Register request preference: " + request.getPreference());
        Log.d(TAG, "Register request city: " + request.getCity());

        authApi.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                Log.d(TAG, "Register response code: " + response.code());
                Log.d(TAG, "Register response message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Log.d(TAG, "✓ Register SUCCESS");
                    Log.d(TAG, "Token received: " + (authResponse.getToken() != null ? "YES" : "NO"));
                    Log.d(TAG, "UserId received: " + (authResponse.getUserId() != null ? "YES" : "NO"));
                    Log.d(TAG, "Email received: " + (authResponse.getEmail() != null ? "YES" : "NO"));

                    if (authResponse.getToken() != null) {
                        String tokenPreview = authResponse.getToken().substring(0, Math.min(30, authResponse.getToken().length()));
                        Log.d(TAG, "Token preview: " + tokenPreview + "...");
                    }

                    // Зберігаємо токен
                    tokenManager.saveToken(
                            authResponse.getToken(),
                            authResponse.getUserId(),
                            authResponse.getEmail()
                    );
                    Log.d(TAG, "Token saved to SharedPreferences");

                    // Перевіряємо що токен зберігся
                    String savedToken = tokenManager.getToken();
                    Log.d(TAG, "Token after save: " + (savedToken != null ? "YES" : "NO"));
                    if (savedToken != null) {
                        Log.d(TAG, "Saved token preview: " + savedToken.substring(0, Math.min(30, savedToken.length())) + "...");
                    }

                    Log.d(TAG, "========== REGISTER END ==========");
                    callback.onSuccess(authResponse);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "✗ Register FAILED: " + response.code() + " - " + response.message());
                    Log.e(TAG, "Error details: " + errorBody);
                    Log.d(TAG, "========== REGISTER END ==========");
                    callback.onError("Registration failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "✗ Register NETWORK ERROR: " + t.getMessage(), t);
                Log.d(TAG, "========== REGISTER END ==========");
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void login(LoginRequest request, RepositoryCallback<AuthResponse> callback) {
        Log.d(TAG, "Login request: " + request.getEmail());

        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                Log.d(TAG, "Login response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Log.d(TAG, "Token received: " + (authResponse.getToken() != null ? "YES" : "NO"));

                    tokenManager.saveToken(
                            authResponse.getToken(),
                            authResponse.getUserId(),
                            authResponse.getEmail()
                    );
                    Log.d(TAG, "Token saved successfully");
                    callback.onSuccess(authResponse);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Login failed: " + response.code() + " - " + response.message());
                    callback.onError("Login failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Login network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}