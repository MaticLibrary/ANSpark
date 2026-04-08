package com.anspark.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Message;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private static final int TYPE_INCOMING = 0;
    private static final int TYPE_OUTGOING = 1;

    private final List<Message> items = new ArrayList<>();
    private final String participantName;
    private final String participantId;
    private final String participantImageUrl;
    private final String participantGender;

    public MessagesAdapter(String participantName, String participantId, String participantImageUrl, String participantGender) {
        this.participantName = TextUtils.isEmpty(participantName) ? "Match" : participantName.trim();
        this.participantId = participantId;
        this.participantImageUrl = participantImageUrl;
        this.participantGender = participantGender;
    }

    public void submitList(List<Message> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isOutgoing(items.get(position)) ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == TYPE_OUTGOING
                ? R.layout.item_message_outgoing
                : R.layout.item_message;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = items.get(position);
        boolean outgoing = isOutgoing(message);

        holder.sender.setText(outgoing ? "Ty" : participantName);
        holder.text.setText(message.getText() != null ? message.getText() : "");

        String timestamp = formatTimestamp(message.getCreatedAt());
        if (TextUtils.isEmpty(timestamp)) {
            holder.meta.setVisibility(View.GONE);
        } else {
            holder.meta.setVisibility(View.VISIBLE);
            holder.meta.setText(timestamp);
        }

        if (outgoing) {
            holder.avatar.setImageResource(R.drawable.man_profile);
        } else {
            ProfileImageLoader.load(
                    holder.avatar,
                    participantImageUrl,
                    ImageUtils.pickProfilePlaceholder(participantId, participantGender)
            );
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private boolean isOutgoing(Message message) {
        if (message == null) {
            return false;
        }

        if (!TextUtils.isEmpty(participantId) && !TextUtils.isEmpty(message.getSenderId())) {
            return !participantId.trim().equalsIgnoreCase(message.getSenderId().trim());
        }

        return message.isOutgoing();
    }

    private String formatTimestamp(String rawTimestamp) {
        if (TextUtils.isEmpty(rawTimestamp)) {
            return "";
        }

        String value = rawTimestamp.trim();
        if (value.isEmpty()) {
            return "";
        }

        if ("now".equalsIgnoreCase(value)) {
            return "Teraz";
        }

        int separatorIndex = value.indexOf('T');
        if (separatorIndex >= 0 && value.length() >= separatorIndex + 6) {
            return value.substring(separatorIndex + 1, separatorIndex + 6);
        }

        return value;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView sender;
        final TextView text;
        final TextView meta;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.itemMessageAvatar);
            sender = itemView.findViewById(R.id.itemMessageSender);
            text = itemView.findViewById(R.id.itemMessageText);
            meta = itemView.findViewById(R.id.itemMessageMeta);
        }
    }
}
