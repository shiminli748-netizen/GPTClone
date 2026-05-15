package com.gptclone.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatRequest {

    @SerializedName("model")
    public String model;

    @SerializedName("messages")
    public List<ChatMessage> messages;

    @SerializedName("stream")
    public boolean stream;

    @SerializedName("max_tokens")
    public int maxTokens;

    @SerializedName("temperature")
    public double temperature;

    public ChatRequest(String model, List<ChatMessage> messages, boolean stream, int maxTokens, double temperature) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    public static class ChatMessage {

        @SerializedName("role")
        public String role;

        @SerializedName("content")
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
