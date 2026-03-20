package com.anspark.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.activities.EditProfileActivity;
import com.anspark.adapters.PhotosAdapter;
import com.anspark.models.Profile;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;
import com.anspark.viewmodel.ProfileViewModel;

import java.util.List;

public class ProfileFragment extends Fragment {

    private PhotosAdapter photosAdapter;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View editButton = view.findViewById(R.id.buttonEditProfile);
        View addPhotoButton = view.findViewById(R.id.buttonAddPhoto);
        View setPrimaryButton = view.findViewById(R.id.buttonSetPrimaryPhoto);
        View verifyButton = view.findViewById(R.id.buttonVerifyProfile);

        ImageView headerImage = view.findViewById(R.id.imageProfileHeader);
        TextView nameText = view.findViewById(R.id.textProfileName);
        TextView taglineText = view.findViewById(R.id.textProfileTagline);
        TextView bioText = view.findViewById(R.id.textProfileBio);

        RecyclerView photosList = view.findViewById(R.id.recyclerProfilePhotos);
        photosList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        photosAdapter = new PhotosAdapter();
        photosList.setAdapter(photosAdapter);

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> bindProfile(profile, nameText, taglineText, bioText, headerImage));
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        editButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        addPhotoButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Dodawanie zdjec wkrotce", Toast.LENGTH_SHORT).show());
        setPrimaryButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Ustawiono zdjecie glowne", Toast.LENGTH_SHORT).show());
        verifyButton.setOnClickListener(v -> Toast.makeText(requireContext(), "Weryfikacja wkrotce", Toast.LENGTH_SHORT).show());

        viewModel.loadProfile();
    }

    private void bindProfile(Profile profile, TextView nameText, TextView taglineText, TextView bioText, ImageView headerImage) {
        if (profile == null) {
            return;
        }

        String name = profile.getName() != null ? profile.getName() : "Profil";
        if (profile.getAge() > 0) {
            name += ", " + profile.getAge();
        }
        nameText.setText(name);

        List<String> tags = profile.getTags();
        if (tags != null && !tags.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                builder.append(tags.get(i));
                if (i < tags.size() - 1) {
                    builder.append(", ");
                }
            }
            taglineText.setText(builder.toString());
        } else {
            taglineText.setText(profile.getCity() != null ? profile.getCity() : "");
        }

        bioText.setText(profile.getBio() != null ? profile.getBio() : "");

        if (photosAdapter != null) {
            photosAdapter.submitList(profile.getPhotos());
        }

        ProfileImageLoader.load(
                headerImage,
                profile.getPrimaryImageUrl(),
                ImageUtils.pickProfilePlaceholder(profile.getId(), profile.getGender())
        );
    }
}
