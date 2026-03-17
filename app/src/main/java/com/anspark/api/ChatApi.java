package com.anspark.api;

import com.anspark.models.Chat;
import com.anspark.models.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApi {
    @GET("chats")
    Call<List<Chat>> getChats();

    @GET("matches/{matchId}/messages")
    Call<List<Message>> getMessages(@Path("matchId") long matchId);

    @POST("matches/{matchId}/messages")
    Call<Message> sendMessage(@Path("matchId") long matchId, @Body String content);
}
