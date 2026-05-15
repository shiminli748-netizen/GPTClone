package com.gptclone.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StreamChunk {

    @SerializedName("id")
    public String id;

    @SerializedName("object")
    public String object;

    @SerializedName("choices")
    public List<StreamChoice> choices;

    public static class StreamChoice {

        @SerializedName("index")
        public int index;

        @SerializedName("delta")
        public Delta delta;

        @SerializedName("finish_reason")
        public String finishReason;
    }

    public static class Delta {

        @SerializedName("role")
        public String role;

        @SerializedName("content")
        public String content;
    }
}
