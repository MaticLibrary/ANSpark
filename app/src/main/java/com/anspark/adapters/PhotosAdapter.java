package com.anspark.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {
    private final List<Photo> items = new ArrayList<>();

    public void submitList(List<Photo> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = items.get(position);
        holder.image.setImageResource(resolvePhoto(photo));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int resolvePhoto(Photo photo) {
        if (photo == null || photo.getUrl() == null) {
            return R.drawable.male_profile;
        }
        String url = photo.getUrl();
        if (url.contains("female_profile_1")) {
            return R.drawable.female_profile_1;
        }
        if (url.contains("female_profile_2")) {
            return R.drawable.female_profile_2;
        }
        return R.drawable.male_profile;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemPhotoImage);
        }
    }
}
