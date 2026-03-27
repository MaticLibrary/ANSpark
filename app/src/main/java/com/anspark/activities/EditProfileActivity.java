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
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;
import com.anspark.utils.TokenManager;
import com.anspark.viewmodel.EditProfileViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private static final int MAX_PROFILE_PHOTOS = 3;
    private static final String TAG = "EDIT_PROFILE";

    public static final String EXTRA_REGISTRATION_FLOW = "registration_flow";
    public static final String EXTRA_DISPLAY_NAME = "display_name";
    public static final String EXTRA_BIRTH_DATE = "birth_date";
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_GENDER = "gender";
    public static final String EXTRA_PREFERENCE = "preference";

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handlePhotoPicked);

    private EditProfileViewModel viewModel;
    private EditText bioInput;
    private TextView profileTitle;
    private TextView profileDescription;
    private TextView stepBadge;
    private MaterialButton finishButton;
    private MaterialButton backButton;
    private View photoSection;
    private final View[] photoSlotCards = new View[MAX_PROFILE_PHOTOS];
    private final ImageView[] photoPreviews = new ImageView[MAX_PROFILE_PHOTOS];
    private final TextView[] photoPlaceholders = new TextView[MAX_PROFILE_PHOTOS];
    private final TextView[] photoBadges = new TextView[MAX_PROFILE_PHOTOS];
    private final File[] selectedPhotoFiles = new File[MAX_PROFILE_PHOTOS];
    private final boolean[] persistedPhotoSlots = new boolean[MAX_PROFILE_PHOTOS];
    private final List<Photo> editablePhotos = new ArrayList<>();

    private Profile draftProfile;
    private boolean registrationFlow;
    private boolean saveInProgress;
    private int pendingSlotIndex = -1;
    private int primaryPhotoIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileTitle = findViewById(R.id.textProfileStepTitle);
        profileDescription = findViewById(R.id.textProfileStepDescription);
        stepBadge = findViewById(R.id.textProfileStepBadge);
        bioInput = findViewById(R.id.inputBio);
        photoSection = findViewById(R.id.layoutPhotoSection);
        backButton = findViewById(R.id.buttonBackStep);
        finishButton = findViewById(R.id.buttonFinishSetup);
        bindPhotoViews();

        registrationFlow = getIntent().getBooleanExtra(EXTRA_REGISTRATION_FLOW, false);
        draftProfile = buildDraftProfileFromIntent();

        // Перевірка токена
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        android.util.Log.d(TAG, "========== EDIT PROFILE DEBUG ==========");
        android.util.Log.d(TAG, "Token exists: " + (token != null ? "YES" : "NO"));
        if (token != null) {
            android.util.Log.d(TAG, "Token: " + token.substring(0, Math.min(30, token.length())) + "...");
        }
        android.util.Log.d(TAG, "Registration flow: " + registrationFlow);
        android.util.Log.d(TAG, "Draft profile exists: " + (draftProfile != null));
        android.util.Log.d(TAG, "======================================");

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        viewModel.getProfile().observe(this, profile -> {
            if (profile == null) {
                return;
            }

            if (saveInProgress) {
                saveInProgress = false;
                android.util.Log.d(TAG, "Profile saved successfully!");
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
                android.util.Log.e(TAG, "Error from ViewModel: " + message);
                saveInProgress = false;
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            finishButton.setEnabled(enabled);
            backButton.setEnabled(enabled);
            for (View photoSlotCard : photoSlotCards) {
                if (photoSlotCard != null) {
                    photoSlotCard.setEnabled(enabled);
                }
            }
        });

        backButton.setOnClickListener(v -> finish());

        finishButton.setOnClickListener(v -> submitProfile());

        if (draftProfile != null) {
            configureForCurrentMode();
            bindProfile(draftProfile);
        } else {
            configureForEditMode();
            viewModel.loadProfile();
        }
    }

    private void submitProfile() {
        Profile base = draftProfile != null ? new Profile(draftProfile) : new Profile();
        String bioText = bioInput.getText().toString().trim();
        base.setBio(bioText);
        base.setPhotos(buildPersistedPhotosForSubmit());

        android.util.Log.d(TAG, "Submitting profile with bio: " + bioText);
        android.util.Log.d(TAG, "Photos count: " + (base.getPhotos() != null ? base.getPhotos().size() : 0));

        saveInProgress = true;
        viewModel.completeProfile(base, Arrays.asList(selectedPhotoFiles.clone()), primaryPhotoIndex);
    }

    private void configureForCurrentMode() {
        if (registrationFlow) {
            configureForRegistrationMode();
        } else {
            configureForEditMode();
        }
    }

    private void configureForRegistrationMode() {
        if (profileDescription != null) {
            profileDescription.setText("Dodaj bio. Zdjecia profilowe uzupelnisz pozniej z poziomu panelu profilu.");
        }
        if (photoSection != null) {
            photoSection.setVisibility(View.GONE);
        }
    }

    private void configureForEditMode() {
        stepBadge.setText("Edycja profilu");
        finishButton.setText("Zapisz zmiany");
        if (profileDescription != null) {
            profileDescription.setText("Dodaj minimum 2 swoje zdjecia i ustaw glowne, aby przygotowac profil do weryfikacji.");
        }
        if (photoSection != null) {
            photoSection.setVisibility(View.VISIBLE);
        }
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
        configureForCurrentMode();

        profileTitle.setText(buildTitle(profile));
        bioInput.setText(profile.getBio() != null ? profile.getBio() : "");
        bindEditablePhotos(profile);
    }

    private String buildTitle(Profile profile) {
        String name = profile.getDisplayName() != null ? profile.getDisplayName() : "Profil";
        int age = profile.getAge();
        if (age > 0) {
            return name + ", " + age;
        }
        return name;
    }

    private void bindPhotoViews() {
        photoSlotCards[0] = findViewById(R.id.cardPhotoSlot1);
        photoSlotCards[1] = findViewById(R.id.cardPhotoSlot2);
        photoSlotCards[2] = findViewById(R.id.cardPhotoSlot3);

        photoPreviews[0] = findViewById(R.id.imagePhotoSlot1);
        photoPreviews[1] = findViewById(R.id.imagePhotoSlot2);
        photoPreviews[2] = findViewById(R.id.imagePhotoSlot3);

        photoPlaceholders[0] = findViewById(R.id.textPhotoSlot1Placeholder);
        photoPlaceholders[1] = findViewById(R.id.textPhotoSlot2Placeholder);
        photoPlaceholders[2] = findViewById(R.id.textPhotoSlot3Placeholder);

        photoBadges[0] = findViewById(R.id.textPhotoSlot1Badge);
        photoBadges[1] = findViewById(R.id.textPhotoSlot2Badge);
        photoBadges[2] = findViewById(R.id.textPhotoSlot3Badge);

        for (int i = 0; i < MAX_PROFILE_PHOTOS; i++) {
            final int slotIndex = i;
            photoSlotCards[i].setOnClickListener(v -> onPhotoSlotClicked(slotIndex));
        }
    }

    private void bindEditablePhotos(Profile profile) {
        editablePhotos.clear();
        Arrays.fill(persistedPhotoSlots, false);

        List<Photo> profilePhotos = profile.getPhotos();
        if (profilePhotos != null) {
            for (int i = 0; i < profilePhotos.size() && i < MAX_PROFILE_PHOTOS; i++) {
                Photo photo = profilePhotos.get(i);
                if (photo != null && photo.getUrl() != null && !photo.getUrl().trim().isEmpty()) {
                    editablePhotos.add(new Photo(photo));
                    persistedPhotoSlots[i] = true;
                }
            }
        }

        if (editablePhotos.isEmpty()) {
            String avatarUrl = profile.getAvatarUrl();
            if (avatarUrl != null
                    && !avatarUrl.trim().isEmpty()
                    && !ImageUtils.isLocalPlaceholder(avatarUrl)) {
                editablePhotos.add(new Photo(null, avatarUrl, true));
                persistedPhotoSlots[0] = true;
            }
        }

        primaryPhotoIndex = resolvePrimaryPhotoIndex(profile);
        renderPhotoSlots();
    }

    private int resolvePrimaryPhotoIndex(Profile profile) {
        String primaryImageUrl = profile.getPrimaryImageUrl();
        for (int i = 0; i < editablePhotos.size(); i++) {
            Photo photo = editablePhotos.get(i);
            if (photo != null && photo.getUrl() != null && photo.getUrl().equals(primaryImageUrl)) {
                return i;
            }
            if (photo != null && photo.isPrimary()) {
                return i;
            }
        }
        return editablePhotos.isEmpty() ? 0 : 0;
    }

    private void renderPhotoSlots() {
        String seed = draftProfile != null ? draftProfile.getId() : null;
        String gender = draftProfile != null ? draftProfile.getGender() : null;
        int fallbackRes = ImageUtils.pickProfilePlaceholder(seed, gender);

        for (int i = 0; i < MAX_PROFILE_PHOTOS; i++) {
            Photo photo = i < editablePhotos.size() ? editablePhotos.get(i) : null;
            boolean hasPhoto = photo != null && photo.getUrl() != null && !photo.getUrl().trim().isEmpty();

            if (photoSlotCards[i] != null) {
                photoSlotCards[i].setSelected(hasPhoto && i == primaryPhotoIndex);
            }
            if (photoPreviews[i] != null) {
                if (hasPhoto) {
                    ProfileImageLoader.load(photoPreviews[i], photo.getUrl(), fallbackRes);
                    photoPreviews[i].setAlpha(1f);
                } else {
                    photoPreviews[i].setImageResource(fallbackRes);
                    photoPreviews[i].setAlpha(0.22f);
                }
            }
            if (photoPlaceholders[i] != null) {
                photoPlaceholders[i].setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
            }
            if (photoBadges[i] != null) {
                photoBadges[i].setVisibility(hasPhoto && i == primaryPhotoIndex ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void onPhotoSlotClicked(int slotIndex) {
        if (registrationFlow) {
            return;
        }
        if (hasPhotoAtSlot(slotIndex)) {
            primaryPhotoIndex = slotIndex;
            renderPhotoSlots();
            return;
        }
        if (slotIndex > editablePhotos.size()) {
            Toast.makeText(this, "Najpierw uzupelnij poprzednie pole", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingSlotIndex = slotIndex;
        photoPickerLauncher.launch("image/*");
    }

    private boolean hasPhotoAtSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= editablePhotos.size()) {
            return false;
        }
        Photo photo = editablePhotos.get(slotIndex);
        return photo != null && photo.getUrl() != null && !photo.getUrl().trim().isEmpty();
    }

    private List<Photo> buildPersistedPhotosForSubmit() {
        List<Photo> persistedPhotos = new ArrayList<>();
        for (int i = 0; i < editablePhotos.size(); i++) {
            if (!persistedPhotoSlots[i] || selectedPhotoFiles[i] != null) {
                continue;
            }

            Photo photo = editablePhotos.get(i);
            if (photo == null || photo.getUrl() == null || photo.getUrl().trim().isEmpty()) {
                continue;
            }

            Photo copy = new Photo(photo);
            copy.setPrimary(i == primaryPhotoIndex);
            persistedPhotos.add(copy);
        }
        return persistedPhotos;
    }

    private void handlePhotoPicked(Uri uri) {
        if (uri == null || pendingSlotIndex < 0 || pendingSlotIndex >= MAX_PROFILE_PHOTOS) {
            return;
        }

        File tempFile = createTempFileFromUri(uri);
        if (tempFile == null) {
            Toast.makeText(this, "Nie udalo sie odczytac zdjecia", Toast.LENGTH_SHORT).show();
            pendingSlotIndex = -1;
            return;
        }

        File previousFile = selectedPhotoFiles[pendingSlotIndex];
        if (previousFile != null && previousFile.exists()) {
            previousFile.delete();
        }

        selectedPhotoFiles[pendingSlotIndex] = tempFile;
        Photo selectedPhoto = new Photo("local_slot_" + pendingSlotIndex, uri.toString(), pendingSlotIndex == primaryPhotoIndex);
        if (pendingSlotIndex < editablePhotos.size()) {
            editablePhotos.set(pendingSlotIndex, selectedPhoto);
        } else {
            editablePhotos.add(selectedPhoto);
        }

        if (editablePhotos.size() == 1) {
            primaryPhotoIndex = 0;
        }

        renderPhotoSlots();
        pendingSlotIndex = -1;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (File selectedPhotoFile : selectedPhotoFiles) {
            if (selectedPhotoFile != null && selectedPhotoFile.exists()) {
                selectedPhotoFile.delete();
            }
        }
    }
}