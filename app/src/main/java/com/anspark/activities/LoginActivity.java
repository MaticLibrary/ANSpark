package com.anspark.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailInput = findViewById(R.id.inputEmailLogin);
        EditText passwordInput = findViewById(R.id.inputPasswordLogin);
        MaterialButton loginButton = findViewById(R.id.buttonLogin);
        MaterialButton registerButton = findViewById(R.id.buttonGoRegister);
        MaterialButton openFeedButton = findViewById(R.id.buttonOpenFeed);

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getAuthResponse().observe(this, response -> {
            if (response != null) {
                openMain();
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            loginButton.setEnabled(enabled);
        });

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Wpisz e-mail i haslo", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(email, password);
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        openFeedButton.setOnClickListener(v -> openMain());
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
