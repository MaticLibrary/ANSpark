package com.anspark.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.models.Profile;
import com.anspark.models.RegisterRequest;
import com.anspark.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private static final String CITY_TARNOW = "Tarnów";
    private static final String CITY_KRAKOW = "Kraków";
    private static final String[] ALLOWED_CITIES = {CITY_TARNOW, CITY_KRAKOW};

    private final Calendar defaultBirthDate = Calendar.getInstance();
    private Profile pendingProfile;

    private TextView registerErrorSummary;
    private TextView genderErrorText;
    private TextView preferenceErrorText;

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
        AutoCompleteTextView cityInput = findViewById(R.id.inputCity);

        registerErrorSummary = findViewById(R.id.textRegisterErrorSummary);
        genderErrorText = findViewById(R.id.textGenderError);
        preferenceErrorText = findViewById(R.id.textPreferenceError);

        MaterialButton nextButton = findViewById(R.id.buttonNextStep);
        MaterialButton goLoginButton = findViewById(R.id.buttonGoLogin);

        setupCitySelector(cityInput);
        setupValidationReset(nameInput, birthDateInput, emailInput, passwordInput, cityInput);

        nameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                normalizeNameInput(nameInput);
            }
            clearFieldError(nameInput);
            hideSummaryError();
        });

        birthDateInput.setOnClickListener(v -> showBirthDatePicker(birthDateInput));
        birthDateInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showBirthDatePicker(birthDateInput);
            }
            clearFieldError(birthDateInput);
            hideSummaryError();
        });

        genderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            hideFieldError(genderErrorText);
            hideSummaryError();
        });

        preferenceGroup.setOnCheckedChangeListener((group, checkedId) -> {
            hideFieldError(preferenceErrorText);
            hideSummaryError();
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
            Profile draft = validateStepOne(nameInput, birthDateInput, genderGroup, preferenceGroup, emailInput, passwordInput, cityInput);
            if (draft == null) {
                Toast.makeText(this, "Popraw oznaczone pola, aby przejść do kroku 2", Toast.LENGTH_SHORT).show();
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

    private void setupCitySelector(AutoCompleteTextView cityInput) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ALLOWED_CITIES);
        cityInput.setAdapter(adapter);
        cityInput.setKeyListener(null);
        cityInput.setCursorVisible(false);
        cityInput.setOnClickListener(v -> cityInput.showDropDown());
        cityInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                cityInput.showDropDown();
            }
            clearFieldError(cityInput);
            hideSummaryError();
        });
    }

    private void setupValidationReset(EditText... fields) {
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                hideSummaryError();
                for (EditText field : fields) {
                    if (field.getText() == editable) {
                        clearFieldError(field);
                        break;
                    }
                }
            }
        };

        for (EditText field : fields) {
            field.addTextChangedListener(watcher);
        }
    }

    private Profile validateStepOne(
            EditText nameInput,
            EditText birthDateInput,
            RadioGroup genderGroup,
            RadioGroup preferenceGroup,
            EditText emailInput,
            EditText passwordInput,
            AutoCompleteTextView cityInput
    ) {
        clearFieldError(nameInput);
        clearFieldError(birthDateInput);
        clearFieldError(emailInput);
        clearFieldError(passwordInput);
        clearFieldError(cityInput);
        hideFieldError(genderErrorText);
        hideFieldError(preferenceErrorText);
        hideSummaryError();

        List<String> summaryErrors = new ArrayList<>();

        String normalizedName = normalizeDisplayName(nameInput.getText().toString());
        if (!TextUtils.equals(nameInput.getText(), normalizedName)) {
            nameInput.setText(normalizedName);
            nameInput.setSelection(normalizedName.length());
        }

        String nameError = validateName(normalizedName);
        if (nameError != null) {
            nameInput.setError(nameError);
            summaryErrors.add(nameError);
        }

        String birthDate = birthDateInput.getText().toString().trim();
        String birthDateError = validateBirthDate(birthDate);
        if (birthDateError != null) {
            birthDateInput.setError(birthDateError);
            summaryErrors.add(birthDateError);
        }

        String gender = mapGender(genderGroup.getCheckedRadioButtonId());
        if (gender == null) {
            String message = "Wybierz płeć, aby przejść dalej.";
            showFieldError(genderErrorText, message);
            summaryErrors.add(message);
        }

        String preference = mapPreference(preferenceGroup.getCheckedRadioButtonId());
        if (preference == null) {
            String message = "Wybierz, kogo szukasz.";
            showFieldError(preferenceErrorText, message);
            summaryErrors.add(message);
        }

        String city = cityInput.getText().toString().trim();
        String cityError = validateCity(city);
        if (cityError != null) {
            cityInput.setError(cityError);
            summaryErrors.add(cityError);
        }

        String email = emailInput.getText().toString().trim();
        String emailError = validateEmail(email);
        if (emailError != null) {
            emailInput.setError(emailError);
            summaryErrors.add(emailError);
        }

        String password = passwordInput.getText().toString().trim();
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            passwordInput.setError(passwordError);
            summaryErrors.add(passwordError);
        }

        if (!summaryErrors.isEmpty()) {
            showSummaryError(summaryErrors);
            return null;
        }

        Profile draft = new Profile();
        draft.setDisplayName(normalizedName);
        draft.setBirthDate(birthDate);
        draft.setGender(gender);
        draft.setPreference(preference);
        draft.setCity(city);
        return draft;
    }

    private String validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Imię jest wymagane.";
        }
        if (name.length() < 2) {
            return "Imię musi mieć co najmniej 2 litery.";
        }
        if (!Character.isUpperCase(name.codePointAt(0))) {
            return "Imię musi zaczynać się wielką literą.";
        }
        if (!name.matches("^[\\p{Lu}][\\p{L}]*(?:[ -][\\p{Lu}][\\p{L}]*)*$")) {
            return "Imię może zawierać tylko litery, spacje i myślnik.";
        }
        return null;
    }

    private String validateBirthDate(String birthDate) {
        if (TextUtils.isEmpty(birthDate)) {
            return "Data urodzenia jest wymagana.";
        }

        try {
            LocalDate parsedDate = LocalDate.parse(birthDate);
            if (parsedDate.isAfter(LocalDate.now())) {
                return "Data urodzenia nie może być z przyszłości.";
            }
        } catch (DateTimeParseException exception) {
            return "Podaj poprawną datę urodzenia.";
        }

        return null;
    }

    private String validateCity(String city) {
        if (TextUtils.isEmpty(city)) {
            return "Wybierz miasto z listy.";
        }
        if (!CITY_TARNOW.equals(city) && !CITY_KRAKOW.equals(city)) {
            return "Dostępne miasta to tylko Tarnów i Kraków.";
        }
        return null;
    }

    private String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "E-mail jest wymagany.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Podaj poprawny adres e-mail.";
        }
        return null;
    }

    private String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Hasło jest wymagane.";
        }

        List<String> missingRules = new ArrayList<>();
        if (password.length() < 8) {
            missingRules.add("minimum 8 znaków");
        }
        if (!password.matches(".*[a-ząćęłńóśźż].*")) {
            missingRules.add("małą literę");
        }
        if (!password.matches(".*[A-ZĄĆĘŁŃÓŚŹŻ].*")) {
            missingRules.add("dużą literę");
        }
        if (!password.matches(".*\\d.*")) {
            missingRules.add("cyfrę");
        }
        if (!password.matches(".*[^\\p{L}\\d].*")) {
            missingRules.add("znak specjalny");
        }

        if (missingRules.isEmpty()) {
            return null;
        }

        return "Hasło musi zawierać: " + TextUtils.join(", ", missingRules) + ".";
    }

    private void showBirthDatePicker(EditText birthDateInput) {
        Calendar selectedDate = (Calendar) defaultBirthDate.clone();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    birthDateInput.setText(formatDate(year, month, dayOfMonth));
                    clearFieldError(birthDateInput);
                    hideSummaryError();
                },
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

    private String normalizeDisplayName(String rawName) {
        String trimmed = rawName != null ? rawName.trim().replaceAll("\\s+", " ") : "";
        if (trimmed.isEmpty()) {
            return "";
        }

        StringBuilder normalized = new StringBuilder(trimmed.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < trimmed.length(); i++) {
            char character = trimmed.charAt(i);
            if (character == ' ' || character == '-') {
                normalized.append(character);
                capitalizeNext = true;
                continue;
            }

            normalized.append(capitalizeNext
                    ? Character.toUpperCase(character)
                    : Character.toLowerCase(character));
            capitalizeNext = false;
        }
        return normalized.toString();
    }

    private void normalizeNameInput(EditText nameInput) {
        String normalized = normalizeDisplayName(nameInput.getText().toString());
        if (!TextUtils.equals(nameInput.getText(), normalized)) {
            nameInput.setText(normalized);
            nameInput.setSelection(normalized.length());
        }
    }

    private void clearFieldError(EditText field) {
        field.setError(null);
    }

    private void showFieldError(TextView view, String message) {
        view.setText(message);
        view.setVisibility(View.VISIBLE);
    }

    private void hideFieldError(TextView view) {
        view.setText(null);
        view.setVisibility(View.GONE);
    }

    private void showSummaryError(List<String> errors) {
        StringBuilder builder = new StringBuilder("Aby przejść do kroku 2 popraw:\n");
        for (String error : errors) {
            builder.append("• ").append(error).append('\n');
        }
        registerErrorSummary.setText(builder.toString().trim());
        registerErrorSummary.setVisibility(View.VISIBLE);
    }

    private void hideSummaryError() {
        registerErrorSummary.setText(null);
        registerErrorSummary.setVisibility(View.GONE);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
