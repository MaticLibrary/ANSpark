package com.anspark.utils;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

public final class ProfileImageLoader {
    private ProfileImageLoader() {
    }

    public static void load(ImageView imageView, String imageUrl, @DrawableRes int fallbackRes) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setImageResource(fallbackRes);
            return;
        }

        if (ImageUtils.isLocalPlaceholder(imageUrl)) {
            imageView.setImageResource(ImageUtils.resolvePlaceholder(imageUrl, fallbackRes));
            return;
        }

        Glide.with(imageView)
                .load(ImageUtils.resolveMediaUrl(imageUrl))
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .centerCrop()
                .into(imageView);
    }
}
