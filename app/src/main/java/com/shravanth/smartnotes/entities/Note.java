package com.shravanth.smartnotes.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.shravanth.smartnotes.encryption.EncryptedConverter;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "datetime")
    private String dateTime;

    @ColumnInfo(name = "noteText")
    private String noteText;

    public Note(String title, String dateTime, String noteText) {
        this.title = title;
        this.dateTime = dateTime;
        this.noteText = noteText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ",note='" + noteText + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
