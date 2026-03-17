package com.anspark.api;

import android.content.Context;

public final class ApiService {
    private ApiService() {
    }

    public static AuthApi auth(Context context) {
        return RetrofitClient.createService(context, AuthApi.class);
    }

    public static ChatApi chat(Context context) {
        return RetrofitClient.createService(context, ChatApi.class);
    }

    public static DiscoverApi discover(Context context) {
        return RetrofitClient.createService(context, DiscoverApi.class);
    }

    public static MatchApi match(Context context) {
        return RetrofitClient.createService(context, MatchApi.class);
    }

    public static ProfileApi profile(Context context) {
        return RetrofitClient.createService(context, ProfileApi.class);
    }
}
