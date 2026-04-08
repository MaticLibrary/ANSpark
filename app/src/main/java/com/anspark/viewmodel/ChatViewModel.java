package com.anspark.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Chat;
import com.anspark.models.Message;
import com.anspark.repository.ChatRepository;
import com.anspark.repository.RepositoryCallback;
import com.anspark.websocket.WebSocketManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";

    private final ChatRepository repository;
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final WebSocketManager webSocketManager;
    private Long currentMatchId;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChatRepository(application);
        this.webSocketManager = WebSocketManager.getInstance();
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<List<Chat>> getChats() {
        return chats;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadChats() {
        repository.getChats(new RepositoryCallback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> data) {
                chats.postValue(data);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void loadMessages(Long matchId) {
        this.currentMatchId = matchId;
        repository.getMessages(matchId, new RepositoryCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> data) {
                messages.postValue(data);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void sendMessage(Long matchId, String text) {
        repository.sendMessage(matchId, text, new RepositoryCallback<Message>() {
            @Override
            public void onSuccess(Message data) {
                Log.d(TAG, "Message sent via HTTP");
                List<Message> current = messages.getValue();
                if (current == null) {
                    current = new ArrayList<>();
                }
                current = new ArrayList<>(current);
                current.add(data);
                messages.postValue(current);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void connectWebSocket(String token, Long matchId) {
        this.currentMatchId = matchId;
        webSocketManager.connect(token, new WebSocketManager.MessageListener() {
            @Override
            public void onMessageReceived(String messageJson) {
                Log.d(TAG, "WebSocket message: " + messageJson);
                try {
                    JSONObject json = new JSONObject(messageJson);
                    Message newMessage = new Message();
                    newMessage.setId(json.optString("id"));
                    newMessage.setText(resolveJsonString(json, "text", "content"));
                    newMessage.setCreatedAt(resolveJsonString(json, "created_at", "createdAt", "sent_at"));
                    newMessage.setSenderId(resolveJsonString(json, "sender_profile_id", "sender_id", "senderId"));
                    newMessage.setChatId(String.valueOf(matchId));
                    newMessage.setOutgoing(json.optBoolean("outgoing", false));

                    List<Message> current = messages.getValue();
                    if (current == null) {
                        current = new ArrayList<>();
                    }
                    current = new ArrayList<>(current);
                    current.add(newMessage);
                    messages.postValue(current);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message: " + e.getMessage());
                }
            }

            @Override
            public void onConnected() {
                Log.d(TAG, "WebSocket connected");
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "WebSocket disconnected");
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "WebSocket error: " + errorMsg);
            }
        });
    }

    public void disconnectWebSocket() {
        webSocketManager.disconnect();
    }

    public void sendMessageViaWebSocket(Long matchId, String content) {
        webSocketManager.sendMessage(String.valueOf(matchId), content);
    }

    private String resolveJsonString(JSONObject json, String... keys) {
        if (json == null || keys == null) {
            return "";
        }

        for (String key : keys) {
            String value = json.optString(key);
            if (value != null && !value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return "";
    }
}
