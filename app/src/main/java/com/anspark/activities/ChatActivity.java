package com.anspark.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.adapters.MessagesAdapter;
import com.anspark.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;

public class ChatActivity extends AppCompatActivity {

    private MessagesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ChatViewModel viewModel;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView chatTitle = findViewById(R.id.chatTitle);
        TextView chatStatus = findViewById(R.id.chatStatus);
        EditText inputMessage = findViewById(R.id.inputMessage);
        MaterialButton sendButton = findViewById(R.id.buttonSend);
        RecyclerView messagesList = findViewById(R.id.messagesList);

        String name = getIntent().getStringExtra("chat_name");
        chatId = getIntent().getStringExtra("chat_id");
        if (chatId == null || chatId.isEmpty()) {
            chatId = "chat_mock";
        }
        if (name == null || name.isEmpty()) {
            name = "Maja, 24";
        }
        chatTitle.setText(name);
        chatStatus.setText("online");

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter();
        messagesList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getMessages().observe(this, messages -> {
            adapter.submitList(messages);
            if (messages != null && !messages.isEmpty()) {
                messagesList.scrollToPosition(messages.size() - 1);
            }
        });
        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadMessages(chatId);

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                return;
            }
            inputMessage.setText("");
            viewModel.sendMessage(chatId, message);
        });
    }
}
