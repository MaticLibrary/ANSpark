package com.anspark.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private final List<Message> items = new ArrayList<>();

    public void submitList(List<Message> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = items.get(position);
        holder.text.setText(message.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.text.getLayoutParams();
        if (message.isOutgoing()) {
            params.gravity = Gravity.END;
            holder.text.setBackgroundResource(R.drawable.bg_bubble_outgoing);
        } else {
            params.gravity = Gravity.START;
            holder.text.setBackgroundResource(R.drawable.bg_bubble_incoming);
        }
        holder.text.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.itemMessageText);
        }
    }
}
