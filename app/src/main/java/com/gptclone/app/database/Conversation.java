package com.gptclone.app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "conversations")
public class Conversation {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    public long createdAt;

    public long updatedAt;

    public Conversation(@NonNull String title, long createdAt, long updatedAt) {
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
