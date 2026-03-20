package com.anspark.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.models.Profile;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;
import com.anspark.viewmodel.EditProfileViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {
    public static final String EXTRA_REGISTRATION_FLOW = "registration_flow";
    public static final String EXTRA_DISPLAY_NAME = "display_name";
    public static final String EXTRA_BIRTH_DATE = "birth_date";
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_GENDER = "gender";
    public static final String EXTRA_PREFERENCE = "preference";

    private final ActivityResultLauncher<String> avatarPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleAvatarPicked);

    private EditProfileViewModel viewModel;
    private EditText bioInput;
    private TextView profileTitle;
    private TextView avatarPlaceholder;
    private TextView stepBadge;
    private ImageView avatarPreview;
    private MaterialButton finishButton;
    private MaterialButton backButton;

    private Profile draftProfile;
    private File selectedAvatarFile;
    private boolean registrationFlow;
    private boolean saveInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileTitle = findViewById(R.id.textProfileStepTitle);
        stepBadge = findViewById(R.id.textProfileStepBadge);
        bioInput = findViewById(R.id.inputBio);
        avatarPreview = findViewById(R.id.imageAvatarPreview);
        avatarPlaceholder = findViewById(R.id.textAvatarPlaceholder);
        View avatarPicker = findViewById(R.id.cardAvatarPicker);
        backButton = findViewById(R.id.buttonBackStep);
        finishButton = findViewById(R.id.buttonFinishSetup);

        registrationFlow = getIntent().getBooleanExtra(EXTRA_REGISTRATION_FLOW, false);
        draftProfile = buildDraftProfileFromIntent();

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        viewModel.getProfile().observe(this, profile -> {
            if (profile == null) {
                return;
            }

            if (saveInProgress) {
                saveInProgress = false;
                if (registrationFlow) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Profil zapisany", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            draftProfile = profile;
            bindProfile(profile);
        });

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                saveInProgress = false;
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            finishButton.setEnabled(enabled);
            backButton.setEnabled(enabled);
            avatarPicker.setEnabled(enabled);
        });

        avatarPicker.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));

        backButton.setOnClickListener(v -> finish());

        finishButton.setOnClickListener(v -> submitProfile());

        if (draftProfile != null) {
            bindProfile(draftProfile);
        } else {
            configureForEditMode();
            viewModel.loadProfile();
        }
    }

    private void submitProfile() {
        Profile base = draftProfile != null ? new Profile(draftProfile) : new Profile();
        base.setBio(bioInput.getText().toString().trim());

        if (registrationFlow
                && selectedAvatarFile == null
                && (base.getAvatarUrl() == null || base.getAvatarUrl().trim().isEmpty())) {
            Toast.makeText(this, "Dodaj avatar, aby zakonczyc rejestracje", Toast.LENGTH_SHORT).show();
            return;
        }

        saveInProgress = true;
        viewModel.completeProfile(base, selectedAvatarFile);
    }

    private void configureForEditMode() {
        stepBadge.setText("Edycja profilu");
        finishButton.setText("Zapisz zmiany");
    }

    private Profile buildDraftProfileFromIntent() {
        String displayName = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);
        String birthDate = getIntent().getStringExtra(EXTRA_BIRTH_DATE);
        String city = getIntent().getStringExtra(EXTRA_CITY);
        String gender = getIntent().getStringExtra(EXTRA_GENDER);
        String preference = getIntent().getStringExtra(EXTRA_PREFERENCE);

        if (displayName == null && birthDate == null && city == null && gender == null && preference == null) {
            return null;
        }

        Profile profile = new Profile();
        profile.setDisplayName(displayName);
        profile.setBirthDate(birthDate);
        profile.setCity(city);
        profile.setGender(gender);
        profile.setPreference(preference);
        return profile;
    }

    private void bindProfile(Profile profile) {
        if (!registrationFlow) {
            configureForEditMode();
        }

        profileTitle.setText(buildTitle(profile));
        bioInput.setText(profile.getBio() != null ? profile.getBio() : "");

        String primaryImage = profile.getPrimaryImageUrl();
        if (primaryImage != null && !primaryImage.trim().isEmpty()) {
            ProfileImageLoader.load(
                    avatarPreview,
                    primaryImage,
                    ImageUtils.pickProfilePlaceholder(profile.getId(), profile.getGender())
            );
            avatarPlaceholder.setVisibility(View.GONE);
        } else {
            avatarPreview.setImageResource(ImageUtils.pickProfilePlaceholder(profile.getId(), profile.getGender()));
            avatarPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private String buildTitle(Profile profile) {
        String name = profile.getDisplayName() != null ? profile.getDisplayName() : "Profil";
        int age = profile.getAge();
        if (age > 0) {
            return name + ", " + age;
        }
        return name;
    }

    private void handleAvatarPicked(Uri uri) {
        if (uri == null) {
            return;
        }

        File tempFile = createTempFileFromUri(uri);
        if (tempFile == null) {
            Toast.makeText(this, "Nie udalo sie odczytac zdjecia", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAvatarFile != null && selectedAvatarFile.exists()) {
            selectedAvatarFile.delete();
        }

        selectedAvatarFile = tempFile;
        avatarPreview.setImageURI(uri);
        avatarPlaceholder.setVisibility(View.GONE);
    }

    private File createTempFileFromUri(Uri uri) {
        ContentResolver resolver = getContentResolver();
        File tempFile = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");

        try (InputStream inputStream = resolver.openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return tempFile;
        } catch (IOException ignored) {
            return null;
        }
    }
}
