package com.anspark.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.models.Profile;
import com.anspark.models.RegisterRequest;
import com.anspark.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private final Calendar defaultBirthDate = Calendar.getInstance();
    private Profile pendingProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        defaultBirthDate.add(Calendar.YEAR, -18);

        EditText nameInput = findViewById(R.id.inputName);
        EditText birthDateInput = findViewById(R.id.inputBirthDate);
        RadioGroup genderGroup = findViewById(R.id.groupGender);
        RadioGroup preferenceGroup = findViewById(R.id.groupPreference);
        EditText emailInput = findViewById(R.id.inputEmail);
        EditText passwordInput = findViewById(R.id.inputPassword);
        EditText cityInput = findViewById(R.id.inputCity);

        MaterialButton nextButton = findViewById(R.id.buttonNextStep);
        MaterialButton goLoginButton = findViewById(R.id.buttonGoLogin);

        birthDateInput.setOnClickListener(v -> showBirthDatePicker(birthDateInput));
        birthDateInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showBirthDatePicker(birthDateInput);
            }
        });

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getAuthResponse().observe(this, response -> {
            if (response != null && pendingProfile != null) {
                Intent intent = new Intent(this, EditProfileActivity.class);
                intent.putExtra(EditProfileActivity.EXTRA_REGISTRATION_FLOW, true);
                intent.putExtra(EditProfileActivity.EXTRA_DISPLAY_NAME, pendingProfile.getDisplayName());
                intent.putExtra(EditProfileActivity.EXTRA_BIRTH_DATE, pendingProfile.getBirthDate());
                intent.putExtra(EditProfileActivity.EXTRA_CITY, pendingProfile.getCity());
                intent.putExtra(EditProfileActivity.EXTRA_GENDER, pendingProfile.getGender());
                intent.putExtra(EditProfileActivity.EXTRA_PREFERENCE, pendingProfile.getPreference());
                startActivity(intent);
                finish();
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            nextButton.setEnabled(enabled);
        });

        nextButton.setOnClickListener(v -> {
            if (isAnyEmpty(nameInput, birthDateInput, emailInput, passwordInput, cityInput)) {
                Toast.makeText(this, "Uzupelnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            String gender = mapGender(genderGroup.getCheckedRadioButtonId());
            if (gender == null) {
                Toast.makeText(this, "Wybierz plec", Toast.LENGTH_SHORT).show();
                return;
            }

            String preference = mapPreference(preferenceGroup.getCheckedRadioButtonId());
            if (preference == null) {
                Toast.makeText(this, "Wybierz kogo szukasz", Toast.LENGTH_SHORT).show();
                return;
            }

            Profile draft = new Profile();
            draft.setDisplayName(nameInput.getText().toString().trim());
            draft.setBirthDate(birthDateInput.getText().toString().trim());
            draft.setGender(gender);
            draft.setPreference(preference);
            draft.setCity(cityInput.getText().toString().trim());

            if (draft.getAge() <= 0) {
                Toast.makeText(this, "Wybierz poprawna date urodzenia", Toast.LENGTH_SHORT).show();
                return;
            }

            pendingProfile = draft;

            String password = passwordInput.getText().toString().trim();
            RegisterRequest request = new RegisterRequest(
                    emailInput.getText().toString().trim(),
                    password,
                    password
            );

            viewModel.register(request);
        });

        goLoginButton.setOnClickListener(v -> finish());
    }

    private void showBirthDatePicker(EditText birthDateInput) {
        Calendar selectedDate = (Calendar) defaultBirthDate.clone();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> birthDateInput.setText(formatDate(year, month, dayOfMonth)),
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private String formatDate(int year, int month, int dayOfMonth) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
    }

    private String mapGender(int checkedId) {
        if (checkedId == R.id.radioGenderFemale) {
            return "FEMALE";
        }
        if (checkedId == R.id.radioGenderMale) {
            return "MALE";
        }
        return null;
    }

    private String mapPreference(int checkedId) {
        if (checkedId == R.id.radioPreferenceWomen) {
            return "WOMEN";
        }
        if (checkedId == R.id.radioPreferenceMen) {
            return "MEN";
        }
        return null;
    }

    private boolean isAnyEmpty(EditText... fields) {
        for (EditText field : fields) {
            if (TextUtils.isEmpty(field.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }
}
