package com.gptclone.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    long insert(Message message);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    List<Message> getMessagesByConversationId(long conversationId);

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    void deleteByConversationId(long conversationId);

    @Query("SELECT * FROM messages WHERE id = :id")
    Message getMessageById(long id);

    @Query("UPDATE messages SET content = :content WHERE id = :id")
    void updateContent(long id, String content);
}
