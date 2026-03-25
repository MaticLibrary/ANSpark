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
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;
import com.anspark.viewmodel.ProfileViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private PhotosAdapter photosAdapter;
    private ProfileViewModel viewModel;
    private Profile currentProfile;
    private MaterialButton setPrimaryButton;
    private MaterialButton verifyButton;
    private TextView verificationStatusText;
    private TextView photoSummaryText;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton editButton = view.findViewById(R.id.buttonEditProfile);
        MaterialButton addPhotoButton = view.findViewById(R.id.buttonAddPhoto);
        setPrimaryButton = view.findViewById(R.id.buttonSetPrimaryPhoto);
        verifyButton = view.findViewById(R.id.buttonVerifyProfile);

        ImageView headerImage = view.findViewById(R.id.imageProfileHeader);
        TextView nameText = view.findViewById(R.id.textProfileName);
        TextView taglineText = view.findViewById(R.id.textProfileTagline);
        TextView bioText = view.findViewById(R.id.textProfileBio);
        verificationStatusText = view.findViewById(R.id.textProfileVerificationStatus);
        photoSummaryText = view.findViewById(R.id.textProfilePhotoSummary);

        RecyclerView photosList = view.findViewById(R.id.recyclerProfilePhotos);
        photosList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        photosAdapter = new PhotosAdapter();
        photosList.setAdapter(photosAdapter);
        photosAdapter.setOnPhotoClickListener(this::updateActionButtons);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> bindProfile(profile, nameText, taglineText, bioText, headerImage));
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean enabled = loading == null || !loading;
            editButton.setEnabled(enabled);
            addPhotoButton.setEnabled(enabled);
            updateActionButtons(photosAdapter.getSelectedPhoto());
        });

        editButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        addPhotoButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        setPrimaryButton.setOnClickListener(v -> handleSetPrimaryPhoto());
        verifyButton.setOnClickListener(v -> handleVerifyProfile());

    }

    private void bindProfile(Profile profile, TextView nameText, TextView taglineText, TextView bioText, ImageView headerImage) {
        if (profile == null) {
            return;
        }

        currentProfile = profile;

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
            photosAdapter.submitList(buildDisplayPhotos(profile));
        }
        updateVerificationSection(profile);
        updateActionButtons(photosAdapter.getSelectedPhoto());

        ProfileImageLoader.load(
                headerImage,
                profile.getPrimaryImageUrl(),
                ImageUtils.pickProfilePlaceholder(profile.getId(), profile.getGender())
        );
    }

    private void updateVerificationSection(Profile profile) {
        if (profile == null) {
            return;
        }

        int photoCount = profile.getPhotoCount();
        if (photoSummaryText != null) {
            photoSummaryText.setText("Zdjecia profilowe: " + photoCount + " / 2");
        }
        if (verificationStatusText == null) {
            return;
        }

        if (profile.isVerified()) {
            verificationStatusText.setText("Profil zweryfikowany. Konto jest juz aktywne w weryfikacji.");
            return;
        }

        if (profile.hasMinimumPhotosForVerification()) {
            verificationStatusText.setText("Warunek spelniony. Mozesz kliknac \"Weryfikuj konto\".");
        } else {
            verificationStatusText.setText("Dodaj minimum 2 swoje zdjecia, aby odblokowac weryfikacje.");
        }
    }

    private List<Photo> buildDisplayPhotos(Profile profile) {
        List<Photo> displayPhotos = new ArrayList<>();
        if (profile.getPhotos() != null) {
            for (Photo photo : profile.getPhotos()) {
                if (photo != null && photo.getUrl() != null && !photo.getUrl().trim().isEmpty()) {
                    displayPhotos.add(photo);
                }
            }
        }

        if (!displayPhotos.isEmpty()) {
            return displayPhotos;
        }

        String avatarUrl = profile.getAvatarUrl();
        if (avatarUrl != null
                && !avatarUrl.trim().isEmpty()
                && !ImageUtils.isLocalPlaceholder(avatarUrl)) {
            displayPhotos.add(new Photo(null, avatarUrl, true));
        }
        return displayPhotos;
    }

    private void updateActionButtons(Photo selectedPhoto) {
        boolean loading = viewModel != null && Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean hasSelectedPhoto = selectedPhoto != null
                && currentProfile != null
                && currentProfile.getPhotos() != null
                && !currentProfile.getPhotos().isEmpty();
        boolean canVerify = currentProfile != null && !currentProfile.isVerified() && currentProfile.hasMinimumPhotosForVerification();

        if (setPrimaryButton != null) {
            setPrimaryButton.setEnabled(!loading && hasSelectedPhoto);
        }
        if (verifyButton != null) {
            verifyButton.setEnabled(!loading && canVerify);
        }
    }

    private void handleSetPrimaryPhoto() {
        if (currentProfile == null) {
            Toast.makeText(requireContext(), "Profil nie jest jeszcze gotowy", Toast.LENGTH_SHORT).show();
            return;
        }

        Photo selectedPhoto = photosAdapter != null ? photosAdapter.getSelectedPhoto() : null;
        if (selectedPhoto == null) {
            Toast.makeText(requireContext(), "Wybierz zdjecie z listy na dole", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPhoto.isPrimary()) {
            Toast.makeText(requireContext(), "To zdjecie jest juz glowne", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.setPrimaryPhoto(currentProfile, selectedPhoto);
    }

    private void handleVerifyProfile() {
        if (currentProfile == null) {
            Toast.makeText(requireContext(), "Profil nie jest jeszcze gotowy", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentProfile.isVerified()) {
            Toast.makeText(requireContext(), "To konto jest juz zweryfikowane", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentProfile.hasMinimumPhotosForVerification()) {
            Toast.makeText(requireContext(), "Dodaj minimum 2 zdjecia profilowe", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.verifyProfile(currentProfile);
        Toast.makeText(requireContext(), "Weryfikacja zakonczona pomyslnie", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadProfile();
        }
    }
}
