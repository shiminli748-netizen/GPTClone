package com.gptclone.app.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
    tableName = "messages",
    foreignKeys = @ForeignKey(
        entity = Conversation.class,
        parentColumns = "id",
        childColumns = "conversationId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("conversationId")}
)
public class Message {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long conversationId;

    @NonNull
    public String role;

    @NonNull
    public String content;

    public long createdAt;

    public Message(long conversationId, @NonNull String role, @NonNull String content, long createdAt) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }
}
