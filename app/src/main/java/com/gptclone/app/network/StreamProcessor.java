package com.gptclone.app.network;

import okhttp3.*;
import okio.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StreamProcessor {

    public interface StreamListener {
        void onToken(String token);
        void onComplete();
        void onError(String error);
    }

    public static void processStream(Response response, StreamListener listener) {
        try {
            ResponseBody body = response.body();
            if (body == null) {
                listener.onError("Empty response body");
                return;
            }

            BufferedSource source = body.source();
            StringBuilder buffer = new StringBuilder();

            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) continue;

                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    if (data.equals("[DONE]")) {
                        listener.onComplete();
                        return;
                    }
                    if (data.isEmpty()) continue;

                    try {
                        org.json.JSONObject json = new org.json.JSONObject(data);
                        org.json.JSONArray choices = json.optJSONArray("choices");
                        if (choices != null && choices.length() > 0) {
                            org.json.JSONObject choice = choices.getJSONObject(0);
                            org.json.JSONObject delta = choice.optJSONObject("delta");
                            if (delta != null) {
                                String content = delta.optString("content", "");
                                if (!content.isEmpty()) {
                                    listener.onToken(content);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // skip malformed JSON
                    }
                }
            }
            listener.onComplete();
        } catch (IOException e) {
            listener.onError(e.getMessage());
        }
    }
}
