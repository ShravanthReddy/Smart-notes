package com.shravanth.smartnotes.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.shravanth.smartnotes.dao.NoteDao;
import com.shravanth.smartnotes.encryption.EncryptedConverter;
import com.shravanth.smartnotes.entities.Note;

@Database(entities = Note.class, version = 2, exportSchema = false)
@TypeConverters({EncryptedConverter.class})
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;

    public static synchronized NotesDatabase getDatabase(Context context) {
        if (notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "smartNotesDb"

            ).build();
        }
        return notesDatabase;
    }

    public abstract NoteDao notesDao();

}
