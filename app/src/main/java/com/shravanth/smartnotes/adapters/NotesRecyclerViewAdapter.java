package com.shravanth.smartnotes.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.entities.Note;
import com.shravanth.smartnotes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesRecyclerViewAdapter extends RecyclerView.Adapter<NotesRecyclerViewAdapter.NoteViewHolder> {

    //class attributes
    private List<Note> notes; //notes list of Note class
    private NotesListener notesListener; //notelistener of notelistener class
    private Timer timer; //timer of timer class
    private List<Note> notesSource; //notesource list of note class

    //constructor to initialize attributes
    public NotesRecyclerViewAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        this.notesSource = notes;

    }

    //oncreate view holder to inflate the layout
    @NonNull
    @Override
    public NotesRecyclerViewAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //returning the inflated layout
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                R.layout.note_display,
                parent,
                false
            )
        );
    }

    //onbind view holder which performs the functions related to recycler view
    @Override
    public void onBindViewHolder(@NonNull NotesRecyclerViewAdapter.NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position)); //to set the notes to the recycler view
        holder.layoutNote.setOnClickListener(new View.OnClickListener() { //creating onclick listener to the layout
            @Override
            public void onClick(View view) {
                //calling on note clicked function and passing notes linked to the position and the position itself
                notesListener.onNoteClicked(notes.get(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition(), view);
            }
        });

        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                notesListener.onNoteLongClicked(notes.get(holder.getAbsoluteAdapterPosition()), holder.getAbsoluteAdapterPosition(), view);
                return true;

            }
        });
    }

    //method to get the item count
    @Override
    public int getItemCount() {
        return notes.size();
    }

    //noteview holder which contains all the views related to the layout
    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        //initialising attributes related to recycler view
        TextView titleTextView, notesTextView;
        LinearLayout layoutNote;

        //assigning the variables to the views
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutNote = itemView.findViewById(R.id.layoutNote);
            titleTextView = itemView.findViewById(R.id.titleTextViewMainScreen);
            notesTextView = itemView.findViewById(R.id.notesTextViewMainScreen);

        }

        //set note function to set the text to the view
        void setNote(Note note){
            String title = note.getTitle();
            String notesText = note.getNoteText();

            titleTextView.setText(title);
            notesTextView.setText(notesText);

        }
    }

    //search notes function to search the notes with the keyword
    public void searchNotes(final String searchKeyword) {
        timer = new Timer(); //creating a new timer

        //scheduling the timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //if keyword is empty assigning notesource to the notes
                if (searchKeyword.trim().isEmpty()) {
                    notes = notesSource;

                } else {
                    //creating a temp list of note type
                    ArrayList<Note> temp = new ArrayList<>();

                    //for every note in notesource we will check if the keyword is in the notes
                    for (Note note : notesSource) {
                        String noteTitle = "";
                        String noteText = "";

                        //checking if the title is null or empty
                        if (note.getTitle() != null & note.getTitle() != "") {
                            noteTitle = note.getTitle().toLowerCase();
                        }

                        //checking if the notetext is null or empty
                        if (note.getNoteText() != null & note.getNoteText() != "") {
                            noteText = note.getNoteText().toLowerCase();
                        }

                        //if title or text contains the search keyword
                        if (noteTitle.contains(searchKeyword.toLowerCase())
                                || noteText.contains(searchKeyword.toLowerCase())) {
                            temp.add(note); //adding that note to temp list
                        }

                        //assigning the temp to notes
                        notes = temp;
                    }
                }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged(); //notifying recycler view about the change in data

                        }
                    });
            }
        }, 500); //using a delay of 500
    }

    //cancelling the timer
    public void cancelTimer() {
        if(timer != null) {
            timer.cancel();

        }
    }
}
