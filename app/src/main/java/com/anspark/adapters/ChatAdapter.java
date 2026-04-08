package com.anspark.adapters;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Chat;
import com.anspark.models.Message;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    private final List<Chat> items = new ArrayList<>();
    private final OnChatClickListener listener;

    public ChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Chat> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = items.get(position);
        Message lastMessage = chat.getLastMessage();
        boolean unread = isUnread(chat);

        String name = chat.getParticipant() != null ? chat.getParticipant().getDisplayName() : "Chat";
        if (chat.getParticipant() != null && chat.getParticipant().getAge() > 0) {
            name = name + ", " + chat.getParticipant().getAge();
        }

        holder.name.setText(name);
        holder.message.setText(buildPreview(chat));
        holder.time.setText(formatTimestamp(chat.getLastMessageAt()));
        holder.unreadDot.setVisibility(unread ? View.VISIBLE : View.GONE);

        holder.name.setTypeface(holder.name.getTypeface(), unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.message.setTypeface(holder.message.getTypeface(), unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.message.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                unread ? R.color.text_primary : R.color.text_secondary
        ));
        holder.time.setTypeface(holder.time.getTypeface(), unread ? Typeface.BOLD : Typeface.NORMAL);
        holder.time.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                unread ? R.color.chat_gold_soft : R.color.text_hint
        ));

        ProfileImageLoader.load(
                holder.avatar,
                chat.getParticipant() != null ? chat.getParticipant().getPrimaryImageUrl() : null,
                ImageUtils.pickProfilePlaceholder(
                        chat.getId(),
                        chat.getParticipant() != null ? chat.getParticipant().getGender() : null
                )
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private boolean isUnread(Chat chat) {
        if (chat == null || chat.getLastMessage() == null) {
            return false;
        }

        String participantId = chat.getParticipant() != null ? chat.getParticipant().getId() : null;
        String senderId = chat.getLastMessage().getSenderId();

        if (!TextUtils.isEmpty(participantId) && !TextUtils.isEmpty(senderId)) {
            return participantId.trim().equalsIgnoreCase(senderId.trim());
        }

        return !chat.getLastMessage().isOutgoing();
    }

    private String buildPreview(Chat chat) {
        if (chat == null || chat.getLastMessage() == null || TextUtils.isEmpty(chat.getLastMessage().getText())) {
            return "Nowa para. Napisz pierwsza wiadomosc.";
        }

        Message lastMessage = chat.getLastMessage();
        String text = lastMessage.getText().trim();
        if (text.isEmpty()) {
            return "Nowa para. Napisz pierwsza wiadomosc.";
        }

        String participantId = chat.getParticipant() != null ? chat.getParticipant().getId() : null;
        if (!TextUtils.isEmpty(participantId) && !TextUtils.isEmpty(lastMessage.getSenderId())) {
            return participantId.trim().equalsIgnoreCase(lastMessage.getSenderId().trim())
                    ? text
                    : "Ty: " + text;
        }

        return lastMessage.isOutgoing() ? "Ty: " + text : text;
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

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name;
        final TextView message;
        final TextView time;
        final View unreadDot;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.itemChatAvatar);
            name = itemView.findViewById(R.id.itemChatName);
            message = itemView.findViewById(R.id.itemChatMessage);
            time = itemView.findViewById(R.id.itemChatTime);
            unreadDot = itemView.findViewById(R.id.itemChatUnreadDot);
        }
    }
}
