package com.gptclone.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ConversationDao {

    @Insert
    long insert(Conversation conversation);

    @Update
    void update(Conversation conversation);

    @Delete
    void delete(Conversation conversation);

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    List<Conversation> getAllConversations();

    @Query("SELECT * FROM conversations WHERE id = :id")
    Conversation getConversationById(long id);

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    void updateTitle(long id, String title, long updatedAt);

    @Query("DELETE FROM conversations WHERE id = :id")
    void deleteById(long id);
}
