package com.shravanth.smartnotes.listeners;

import android.view.View;

import com.shravanth.smartnotes.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position, View view);
    void onNoteLongClicked(Note note, int position, View view);

}
