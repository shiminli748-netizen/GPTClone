package com.gptclone.app.model;

public class ApiConfig {

    public String baseUrl;
    public String apiKey;
    public String model;
    public int maxTokens;
    public double temperature;

    public ApiConfig() {
        this.baseUrl = "https://api.openai.com/v1/";
        this.apiKey = "";
        this.model = "gpt-4o";
        this.maxTokens = 4096;
        this.temperature = 0.7;
    }

    public String getChatUrl() {
        String url = baseUrl.trim();
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + "chat/completions";
    }
}
