package com.anspark.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.anspark.R;
import com.anspark.fragments.ChatListFragment;
import com.anspark.fragments.DiscoverFragment;
import com.anspark.fragments.ProfileFragment;
import com.anspark.fragments.SettingsFragment;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton navMatch;
    private MaterialButton navChat;
    private MaterialButton navProfile;
    private MaterialButton navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navMatch = findViewById(R.id.navMatch);
        navChat = findViewById(R.id.navChat);
        navProfile = findViewById(R.id.navProfile);
        navSettings = findViewById(R.id.navSettings);

        navMatch.setOnClickListener(v -> showFragment(new DiscoverFragment(), navMatch));
        navChat.setOnClickListener(v -> showFragment(new ChatListFragment(), navChat));
        navProfile.setOnClickListener(v -> showFragment(new ProfileFragment(), navProfile));
        navSettings.setOnClickListener(v -> showFragment(new SettingsFragment(), navSettings));

        if (savedInstanceState == null) {
            showFragment(new DiscoverFragment(), navMatch);
        } else {
            styleNavButton(navMatch, true);
            styleNavButton(navChat, false);
            styleNavButton(navProfile, false);
            styleNavButton(navSettings, false);
        }
    }

    private void showFragment(Fragment fragment, MaterialButton selectedButton) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        styleNavButton(navMatch, selectedButton == navMatch);
        styleNavButton(navChat, selectedButton == navChat);
        styleNavButton(navProfile, selectedButton == navProfile);
        styleNavButton(navSettings, selectedButton == navSettings);
    }

    private void styleNavButton(MaterialButton button, boolean selected) {
        if (selected) {
            button.setBackgroundResource(R.drawable.bg_tab_selected);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            button.setBackgroundResource(R.drawable.bg_tab_unselected);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }
}
