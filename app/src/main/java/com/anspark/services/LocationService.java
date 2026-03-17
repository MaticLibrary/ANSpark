package com.anspark.services;

import android.content.Context;

public class LocationService {
    public interface LocationCallback {
        void onLocation(double lat, double lon);
        void onError(String message);
    }

    public void fetchSingle(Context context, LocationCallback callback) {
        if (callback != null) {
            callback.onError("Location service not configured yet");
        }
    }
}
