package com.anspark.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.MatchResponse;
import com.anspark.models.Profile;
import com.anspark.utils.ProfileImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {
    private final List<MatchResponse> items = new ArrayList<>();

    public void submitList(List<MatchResponse> matches) {
        items.clear();
        if (matches != null) {
            items.addAll(matches);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchResponse match = items.get(position);
        Profile profile = match.getProfile();

        if (profile != null) {
            String name = profile.getDisplayName() != null ? profile.getDisplayName() : "Profil";
            if (profile.getAge() > 0) {
                name = name + ", " + profile.getAge();
            }
            holder.name.setText(name);

            ProfileImageLoader.load(holder.image, profile.getAvatarUrl(), R.drawable.female_profile_1);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemMatchImage);
            name = itemView.findViewById(R.id.itemMatchName);
        }
    }
}