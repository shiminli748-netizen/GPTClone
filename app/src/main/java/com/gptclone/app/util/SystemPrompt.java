package com.gptclone.app.util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SystemPrompt {

    private static String cachedPrompt = null;

    public static String getSystemPrompt(Context context) {
        if (cachedPrompt != null) {
            return cachedPrompt;
        }
        try {
            InputStream is = context.getResources().openRawResource(com.gptclone.app.R.raw.system_prompt);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            cachedPrompt = sb.toString().trim();
        } catch (Exception e) {
            cachedPrompt = "You are a helpful AI assistant.";
        }
        return cachedPrompt;
    }
}
