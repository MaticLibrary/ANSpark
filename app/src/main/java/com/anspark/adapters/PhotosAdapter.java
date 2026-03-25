package com.anspark.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Photo;
import com.anspark.utils.ProfileImageLoader;

import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {
    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    private final List<Photo> items = new ArrayList<>();
    private OnPhotoClickListener onPhotoClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public void submitList(List<Photo> data) {
        String selectedUrl = getSelectedPhotoUrl();
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        selectedPosition = resolveSelectedPosition(selectedUrl);
        notifyDataSetChanged();
    }

    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }

    public Photo getSelectedPhoto() {
        if (selectedPosition < 0 || selectedPosition >= items.size()) {
            return null;
        }
        return items.get(selectedPosition);
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
        ProfileImageLoader.load(holder.image, photo != null ? photo.getUrl() : null, resolvePhoto(photo));
        holder.container.setSelected(position == selectedPosition);
        holder.primaryBadge.setVisibility(photo != null && photo.isPrimary() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> selectPhoto(holder.getBindingAdapterPosition()));
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

    private void selectPhoto(int position) {
        if (position == RecyclerView.NO_POSITION || position >= items.size()) {
            return;
        }
        int previousSelection = selectedPosition;
        selectedPosition = position;
        if (previousSelection != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelection);
        }
        notifyItemChanged(selectedPosition);
        if (onPhotoClickListener != null) {
            onPhotoClickListener.onPhotoClick(items.get(selectedPosition));
        }
    }

    private String getSelectedPhotoUrl() {
        Photo selectedPhoto = getSelectedPhoto();
        return selectedPhoto != null ? selectedPhoto.getUrl() : null;
    }

    private int resolveSelectedPosition(String selectedUrl) {
        if (items.isEmpty()) {
            return RecyclerView.NO_POSITION;
        }

        if (selectedUrl != null) {
            for (int i = 0; i < items.size(); i++) {
                Photo photo = items.get(i);
                if (photo != null && selectedUrl.equals(photo.getUrl())) {
                    return i;
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            Photo photo = items.get(i);
            if (photo != null && photo.isPrimary()) {
                return i;
            }
        }

        return 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        final View container;
        final ImageView image;
        final TextView primaryBadge;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.itemPhotoContainer);
            image = itemView.findViewById(R.id.itemPhotoImage);
            primaryBadge = itemView.findViewById(R.id.itemPhotoPrimaryBadge);
        }
    }
}
