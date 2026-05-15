package com.gptclone.app.network;

import com.gptclone.app.model.ChatRequest;
import com.gptclone.app.model.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {

    @POST
    Call<ChatResponse> sendChatRequest(
        @Url String url,
        @Header("Authorization") String auth,
        @Header("Content-Type") String contentType,
        @Body ChatRequest request
    );
}
