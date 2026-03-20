package com.anspark.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.models.Profile;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;
import com.anspark.viewmodel.DiscoverViewModel;
import com.google.android.material.button.MaterialButton;

public class DiscoverFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName;
    private TextView profileDescription;
    private DiscoverViewModel viewModel;

    public DiscoverFragment() {
        super(R.layout.fragment_discover);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = view.findViewById(R.id.imageDiscoverProfile);
        profileName = view.findViewById(R.id.textDiscoverName);
        profileDescription = view.findViewById(R.id.textDiscoverDescription);

        MaterialButton dislikeButton = view.findViewById(R.id.buttonDislike);
        MaterialButton matchButton = view.findViewById(R.id.buttonMatch);

        viewModel = new ViewModelProvider(this).get(DiscoverViewModel.class);

        viewModel.getCurrentProfile().observe(getViewLifecycleOwner(), this::renderProfile);
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        dislikeButton.setOnClickListener(v -> viewModel.sendDecision(false));
        matchButton.setOnClickListener(v -> viewModel.sendDecision(true));

        viewModel.load();
    }

    private void renderProfile(Profile profile) {
        if (profile == null) {
            return;
        }

        String name = profile.getName() != null ? profile.getName() : "Profil";
        if (profile.getAge() > 0) {
            name = name + ", " + profile.getAge();
        }
        profileName.setText(name);

        String bio = profile.getBio() != null ? profile.getBio() : "";
        profileDescription.setText(bio);

        String seed = profile.getId() != null ? profile.getId() : profile.getName();
        ProfileImageLoader.load(
                profileImage,
                profile.getPrimaryImageUrl(),
                ImageUtils.pickProfilePlaceholder(seed, profile.getGender())
        );
    }
}
