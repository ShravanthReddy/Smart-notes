package com.shravanth.smartnotes.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.entities.Note;
import com.shravanth.smartnotes.listeners.NotesListener;

import java.util.List;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListener notesListener;

    public NotesRecyclerViewAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;

    }

    @NonNull
    @Override
    public NotesRecyclerViewAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                R.layout.note_display,
                parent,
                false
            )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NotesRecyclerViewAdapter.NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(notes.get(position), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, notesTextView;
        LinearLayout layoutNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutNote = itemView.findViewById(R.id.layoutNote);
            titleTextView = itemView.findViewById(R.id.titleTextViewMainScreen);
            notesTextView = itemView.findViewById(R.id.notesTextViewMainScreen);

        }

        void setNote(Note note){
            System.out.println("Title: " + note.getTitle() + "\nNote: " +note.getNoteText());

            String title = note.getTitle();
            String notesText = note.getNoteText();

            titleTextView.setText(title);
            notesTextView.setText(notesText);

        }
    }
}
