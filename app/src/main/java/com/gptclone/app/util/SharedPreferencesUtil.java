package com.gptclone.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.gptclone.app.model.ApiConfig;

public class SharedPreferencesUtil {

    private static final String PREFS_NAME = "gptclone_prefs";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_MODEL = "model";
    private static final String KEY_MAX_TOKENS = "max_tokens";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static ApiConfig getApiConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ApiConfig config = new ApiConfig();
        config.baseUrl = prefs.getString(KEY_BASE_URL, config.baseUrl);
        config.apiKey = prefs.getString(KEY_API_KEY, config.apiKey);
        config.model = prefs.getString(KEY_MODEL, config.model);
        config.maxTokens = prefs.getInt(KEY_MAX_TOKENS, config.maxTokens);
        config.temperature = (double) prefs.getFloat(KEY_TEMPERATURE, (float) config.temperature);
        return config;
    }

    public static void saveApiConfig(Context context, ApiConfig config) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_BASE_URL, config.baseUrl)
            .putString(KEY_API_KEY, config.apiKey)
            .putString(KEY_MODEL, config.model)
            .putInt(KEY_MAX_TOKENS, config.maxTokens)
            .putFloat(KEY_TEMPERATURE, (float) config.temperature)
            .apply();
    }

    public static int getThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, 0);
    }

    public static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
}
