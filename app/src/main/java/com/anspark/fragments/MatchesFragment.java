package com.anspark.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.adapters.MatchesAdapter;
import com.anspark.viewmodel.MatchViewModel;

public class MatchesFragment extends Fragment {
    public MatchesFragment() {
        super(R.layout.fragment_matches);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView emptyState = view.findViewById(R.id.textMatchesEmpty);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerMatches);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        MatchesAdapter adapter = new MatchesAdapter();
        recyclerView.setAdapter(adapter);

        MatchViewModel viewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        viewModel.getMatches().observe(getViewLifecycleOwner(), matches -> {
            adapter.submitList(matches);
            boolean showEmpty = matches == null || matches.isEmpty();
            emptyState.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadMatches();
    }
}
