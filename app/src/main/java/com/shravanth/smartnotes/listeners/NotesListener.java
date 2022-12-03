package com.shravanth.smartnotes.listeners;

import com.shravanth.smartnotes.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
    void onNoteLongClicked(Note note, int position);

}
