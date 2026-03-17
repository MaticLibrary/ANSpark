package com.anspark.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anspark.R;
import com.anspark.activities.LoginActivity;
import com.anspark.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton logoutButton = view.findViewById(R.id.buttonLogout);
        View helpRow = view.findViewById(R.id.rowHelp);

        logoutButton.setOnClickListener(v -> {
            new AuthRepository(requireContext()).logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        helpRow.setOnClickListener(v ->
                Toast.makeText(requireContext(), "FAQ wkrotce", Toast.LENGTH_SHORT).show()
        );
    }
}
