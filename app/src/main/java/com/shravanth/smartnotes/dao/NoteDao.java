package com.shravanth.smartnotes.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.shravanth.smartnotes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    @Query("UPDATE notes SET title = :title, datetime = :datetime, noteText = :noteText WHERE id = :id")
    void updateNote(String title, String datetime, String noteText, int id);

    @Query("DELETE FROM notes WHERE id = :id")
    void deleteByUserId(int id);

    @Query("SELECT EXISTS (SELECT 1 FROM notes WHERE id = :id)")
    boolean exists(int id);

    @Query("SELECT * FROM notes WHERE id = :id")
    List<Note> getNote(int id);

    @Query("SELECT COUNT(*) FROM notes")
    int dbSize();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void delete(Note note);

}
