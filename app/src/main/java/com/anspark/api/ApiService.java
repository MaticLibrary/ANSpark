package com.anspark.api;

import android.content.Context;

public final class ApiService {
    private ApiService() {
    }

    public static AuthApi auth(Context context) {
        return RetrofitClient.getInstance(context).create(AuthApi.class);
    }

    public static ChatApi chat(Context context) {
        return RetrofitClient.getInstance(context).create(ChatApi.class);
    }

    public static DiscoverApi discover(Context context) {
        return RetrofitClient.getInstance(context).create(DiscoverApi.class);
    }

    public static MatchApi match(Context context) {
        return RetrofitClient.getInstance(context).create(MatchApi.class);
    }

    public static ProfileApi profile(Context context) {
        return RetrofitClient.getInstance(context).create(ProfileApi.class);
    }
}