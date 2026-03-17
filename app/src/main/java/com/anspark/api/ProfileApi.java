package com.anspark.api;

import com.anspark.models.Photo;
import com.anspark.models.Profile;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileApi {
    @GET("profile/me")
    Call<Profile> getMyProfile();

    @PUT("profile/me")
    Call<Profile> updateProfile(@Body Profile profile);

    @Multipart
    @POST("profile/me/photo")
    Call<Photo> uploadPhoto(@Part MultipartBody.Part file);
}
