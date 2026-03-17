package com.anspark.utils;

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
        int[] images = {R.drawable.female_profile_1, R.drawable.female_profile_2};
        if (seed == null || seed.isEmpty()) {
            return images[0];
        }
        int index = Math.abs(seed.hashCode()) % images.length;
        return images[index];
    }
}
