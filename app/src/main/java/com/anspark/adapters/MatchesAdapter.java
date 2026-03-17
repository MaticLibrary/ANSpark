package com.anspark.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Match;
import com.anspark.models.Profile;
import com.anspark.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {
    private final List<Match> items = new ArrayList<>();

    public void submitList(List<Match> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
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
        Match match = items.get(position);
        Profile profile = match.getProfile();
        String name = profile != null ? profile.getName() : "Match";
        if (profile != null && profile.getAge() > 0) {
            name = name + ", " + profile.getAge();
        }
        holder.name.setText(name);
        holder.image.setImageResource(ImageUtils.pickDiscoverPlaceholder(match.getId()));
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
