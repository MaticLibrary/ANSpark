package com.anspark.repository;

import android.content.Context;
import android.text.TextUtils;

import com.anspark.api.ApiService;
import com.anspark.api.ChatApi;
import com.anspark.models.Chat;
import com.anspark.models.Message;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;
import com.anspark.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApi api;
    private final TokenManager tokenManager;

    public ChatRepository(Context context) {
        this.api = ApiService.chat(context);
        this.tokenManager = new TokenManager(context);
    }

    public void getChats(RepositoryCallback<List<Chat>> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(normalizeChats(MockData.sampleChats()));
            return;
        }

        api.getChats().enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(normalizeChats(response.body()));
                } else {
                    callback.onError("Nie udalo sie pobrac listy chatow");
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void getMessages(long matchId, RepositoryCallback<List<Message>> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(normalizeMessages(MockData.sampleMessages(String.valueOf(matchId))));
            return;
        }

        api.getMessages(matchId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(normalizeMessages(response.body()));
                } else {
                    callback.onError("Nie udalo sie pobrac wiadomosci");
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    public void sendMessage(long matchId, String content, RepositoryCallback<Message> callback) {
        if (Constants.USE_MOCK_DATA) {
            Message message = new Message(
                    UUID.randomUUID().toString(),
                    String.valueOf(matchId),
                    tokenManager.getUserId(),
                    content,
                    "now",
                    true
            );
            callback.onSuccess(normalizeMessage(message, true));
            return;
        }

        api.sendMessage(matchId, content).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(normalizeMessage(response.body(), true));
                } else {
                    callback.onError("Nie udalo sie wyslac wiadomosci");
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Blad sieci");
            }
        });
    }

    private List<Chat> normalizeChats(List<Chat> source) {
        List<Chat> normalized = new ArrayList<>();
        if (source == null) {
            return normalized;
        }

        for (Chat chat : source) {
            if (chat == null) {
                continue;
            }

            Message lastMessage = normalizeMessage(chat.getLastMessage(), false);
            chat.setLastMessage(lastMessage);
            if (TextUtils.isEmpty(chat.getLastMessageAt()) && lastMessage != null) {
                chat.setLastMessageAt(lastMessage.getCreatedAt());
            }
            normalized.add(chat);
        }
        return normalized;
    }

    private List<Message> normalizeMessages(List<Message> source) {
        List<Message> normalized = new ArrayList<>();
        if (source == null) {
            return normalized;
        }

        for (Message message : source) {
            normalized.add(normalizeMessage(message, false));
        }
        return normalized;
    }

    private Message normalizeMessage(Message message, boolean forceOutgoing) {
        if (message == null) {
            return null;
        }

        String currentUserId = tokenManager.getUserId();
        String senderId = message.getSenderId();

        if (!TextUtils.isEmpty(currentUserId) && !TextUtils.isEmpty(senderId)) {
            if (currentUserId.trim().equalsIgnoreCase(senderId.trim())) {
                message.setOutgoing(true);
            }
        } else if (forceOutgoing) {
            message.setOutgoing(true);
            if (TextUtils.isEmpty(senderId) && !TextUtils.isEmpty(currentUserId)) {
                message.setSenderId(currentUserId);
            }
        }

        return message;
    }
}
