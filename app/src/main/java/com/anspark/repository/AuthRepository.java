package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.AuthApi;
import com.anspark.models.AuthResponse;
import com.anspark.models.LoginRequest;
import com.anspark.models.RegisterRequest;
import com.anspark.session.SessionManager;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final AuthApi api;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        this.api = ApiService.auth(context);
        this.sessionManager = new SessionManager(context);
    }

    public void login(LoginRequest request, RepositoryCallback<AuthResponse> callback) {
        if (Constants.USE_MOCK_DATA) {
            AuthResponse response = MockData.sampleAuthResponse();
            saveSession(response);
            callback.onSuccess(response);
            return;
        }

        api.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveSession(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response, "Nieudane logowanie"));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void register(RegisterRequest request, RepositoryCallback<AuthResponse> callback) {
        if (Constants.USE_MOCK_DATA) {
            AuthResponse response = MockData.sampleAuthResponse();
            saveSession(response);
            callback.onSuccess(response);
            return;
        }

        api.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveSession(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(extractErrorMessage(response, "Nieudana rejestracja"));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void logout() {
        sessionManager.clear();
    }

    private void saveSession(AuthResponse response) {
        if (response == null) {
            return;
        }

        if (response.getAccessToken() != null) {
            sessionManager.saveAuthTokens(response.getAccessToken(), response.getRefreshToken());
        }

        if (response.getUser() != null) {
            sessionManager.saveUserId(response.getUser().getId());
        }
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
