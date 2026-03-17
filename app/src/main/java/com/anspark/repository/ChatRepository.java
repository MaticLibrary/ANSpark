package com.anspark.repository;

import android.content.Context;

import com.anspark.api.ApiService;
import com.anspark.api.ChatApi;
import com.anspark.models.Chat;
import com.anspark.models.Message;
import com.anspark.utils.Constants;
import com.anspark.utils.MockData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApi api;

    public ChatRepository(Context context) {
        this.api = ApiService.chat(context);
    }

    public void getChats(RepositoryCallback<List<Chat>> callback) {
        if (Constants.USE_MOCK_DATA) {
            callback.onSuccess(MockData.sampleChats());
            return;
        }

        api.getChats().enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
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
            callback.onSuccess(MockData.sampleMessages("" + matchId));
            return;
        }

        api.getMessages(matchId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
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
            Message message = new Message(UUID.randomUUID().toString(), "" + matchId, "user_1", content, "now", true);
            callback.onSuccess(message);
            return;
        }

        api.sendMessage(matchId, content).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
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
}
