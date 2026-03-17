package com.anspark.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.models.Profile;
import com.anspark.viewmodel.EditProfileViewModel;
import com.google.android.material.button.MaterialButton;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        TextView profileTitle = findViewById(R.id.textProfileStepTitle);
        EditText bioInput = findViewById(R.id.inputBio);
        MaterialButton backButton = findViewById(R.id.buttonBackStep);
        MaterialButton finishButton = findViewById(R.id.buttonFinishSetup);

        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");

        if (name != null && age != null) {
            profileTitle.setText(name + ", " + age);
        }

        EditProfileViewModel viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        viewModel.getProfile().observe(this, profile -> {
            if (profile != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());

        finishButton.setOnClickListener(v -> {
            Profile profile = new Profile();
            if (name != null) {
                profile.setName(name);
            }
            if (age != null) {
                try {
                    profile.setAge(Integer.parseInt(age));
                } catch (NumberFormatException ignored) {
                }
            }
            profile.setBio(bioInput.getText().toString().trim());
            viewModel.updateProfile(profile);
        });
    }
}
