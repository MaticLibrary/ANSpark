package com.anspark.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.adapters.MessagesAdapter;
import com.anspark.utils.ImageUtils;
import com.anspark.utils.ProfileImageLoader;
import com.anspark.utils.TokenManager;
import com.anspark.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;

public class ChatActivity extends AppCompatActivity {

    private MessagesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ChatViewModel viewModel;
    private Long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageView chatAvatar = findViewById(R.id.chatAvatar);
        TextView chatTitle = findViewById(R.id.chatTitle);
        TextView chatStatus = findViewById(R.id.chatStatus);
        EditText inputMessage = findViewById(R.id.inputMessage);
        MaterialButton sendButton = findViewById(R.id.buttonSend);
        RecyclerView messagesList = findViewById(R.id.messagesList);

        String name = getIntent().getStringExtra("chat_name");
        String chatIdStr = getIntent().getStringExtra("chat_id");
        String participantId = getIntent().getStringExtra("participant_id");
        String participantImageUrl = getIntent().getStringExtra("participant_image_url");
        String participantGender = getIntent().getStringExtra("participant_gender");
        String participantName = extractParticipantName(name);

        if (chatIdStr == null || chatIdStr.isEmpty()) {
            chatId = 0L;
        } else {
            try {
                chatId = Long.parseLong(chatIdStr);
            } catch (NumberFormatException e) {
                chatId = 0L;
            }
        }

        if (name == null || name.isEmpty()) {
            name = "Chat";
        }

        ProfileImageLoader.load(
                chatAvatar,
                participantImageUrl,
                ImageUtils.pickProfilePlaceholder(participantId, participantGender)
        );
        chatTitle.setText(name);
        chatStatus.setText("Online teraz");

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(participantName, participantId, participantImageUrl, participantGender);
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

        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        if (token != null && chatId > 0) {
            viewModel.connectWebSocket(token, chatId);
        }

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                return;
            }
            inputMessage.setText("");
            viewModel.sendMessageViaWebSocket(chatId, message);
            viewModel.sendMessage(chatId, message);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.disconnectWebSocket();
    }

    private String extractParticipantName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "Match";
        }

        String[] parts = fullName.split(",");
        String shortName = parts.length > 0 ? parts[0].trim() : fullName.trim();
        return shortName.isEmpty() ? "Match" : shortName;
    }
}
