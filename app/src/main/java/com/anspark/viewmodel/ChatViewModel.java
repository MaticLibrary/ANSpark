package com.anspark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Chat;
import com.anspark.models.Message;
import com.anspark.repository.ChatRepository;
import com.anspark.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChatRepository(application);
    }

    public LiveData<List<Chat>> getChats() {
        return chats;
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
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

    public void loadMessages(String chatId) {
        repository.getMessages(chatId, new RepositoryCallback<List<Message>>() {
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

    public void sendMessage(String chatId, String text) {
        repository.sendMessage(chatId, text, new RepositoryCallback<Message>() {
            @Override
            public void onSuccess(Message data) {
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
}
