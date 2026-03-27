package com.anspark.api;

import android.content.Context;

import com.anspark.utils.Constants;
import com.anspark.utils.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final AuthInterceptor authInterceptor;

    private RetrofitClient(Context context) {
        authInterceptor = new AuthInterceptor(context);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public <T> T create(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public AuthInterceptor getAuthInterceptor() {
        return authInterceptor;
    }

    public TokenManager getTokenManager() {
        return authInterceptor.getTokenManager();
    }
}