package com.gptclone.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("object")
    public String object;

    @SerializedName("choices")
    public List<Choice> choices;

    public static class Choice {

        @SerializedName("index")
        public int index;

        @SerializedName("message")
        public ChatRequest.ChatMessage message;

        @SerializedName("finish_reason")
        public String finishReason;
    }
}
