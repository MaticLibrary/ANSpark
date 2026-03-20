package com.anspark.utils;

import android.net.Uri;

import com.anspark.R;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static int pickDiscoverPlaceholder(String seed) {
        int[] images = {R.drawable.female_profile_1, R.drawable.female_profile_2};
        if (seed == null || seed.isEmpty()) {
            return images[0];
        }
        int index = Math.abs(seed.hashCode()) % images.length;
        return images[index];
    }

    public static int pickChatPlaceholder(String seed) {
        return pickDiscoverPlaceholder(seed);
    }

    public static int pickProfilePlaceholder(String seed, String gender) {
        if (gender != null && gender.equalsIgnoreCase("MALE")) {
            return R.drawable.male_profile;
        }
        return pickDiscoverPlaceholder(seed);
    }

    public static boolean isLocalPlaceholder(String url) {
        return url != null && url.startsWith("local://");
    }

    public static int resolvePlaceholder(String url, int fallbackRes) {
        if (url == null) {
            return fallbackRes;
        }
        if (url.contains("male_profile")) {
            return R.drawable.male_profile;
        }
        if (url.contains("female_profile_2")) {
            return R.drawable.female_profile_2;
        }
        if (url.contains("female_profile_1")) {
            return R.drawable.female_profile_1;
        }
        return fallbackRes;
    }

    public static String resolveMediaUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }

        if (rawUrl.startsWith("http://")
                || rawUrl.startsWith("https://")
                || rawUrl.startsWith("content://")
                || rawUrl.startsWith("file://")) {
            return rawUrl;
        }

        Uri baseUri = Uri.parse(Constants.BASE_URL);
        StringBuilder origin = new StringBuilder()
                .append(baseUri.getScheme())
                .append("://")
                .append(baseUri.getHost());

        if (baseUri.getPort() != -1) {
            origin.append(":").append(baseUri.getPort());
        }

        if (rawUrl.startsWith("/")) {
            return origin + rawUrl;
        }

        return origin + "/" + rawUrl;
    }
}
