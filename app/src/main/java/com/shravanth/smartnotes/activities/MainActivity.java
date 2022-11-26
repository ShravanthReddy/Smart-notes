package com.shravanth.smartnotes.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.adapters.NotesRecyclerViewAdapter;
import com.shravanth.smartnotes.database.NotesDatabase;
import com.shravanth.smartnotes.entities.Note;
import com.shravanth.smartnotes.listeners.NotesListener;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private ImageView addButton;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesRecyclerViewAdapter notesRecyclerViewAdapter;

    private ImageView noNotesIcon;
    private TextView noNotesText;

    private int noteClickedPosition = -1;
    private int noteClickedPositionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //setting the statusbar to transparent
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //initialising addbutton
        addButton = findViewById(R.id.add_button);

        noNotesIcon = findViewById(R.id.noNotesIcon);
        noNotesText = findViewById(R.id.noNotesText);

        notesRecyclerView = findViewById(R.id.notesRecyclerView);

        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesRecyclerViewAdapter = new NotesRecyclerViewAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesRecyclerViewAdapter);

        //displayNote();

        //when add button is clicked
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creates CreateNote activity
                Intent intent = new Intent(MainActivity.this, CreateNote.class);
                intent.putExtra("isViewOrAvailable", false);

                startActivity(intent);
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {

        noteClickedPosition = position;
        noteClickedPositionId = noteList.get(position).getId();
        Intent intent = new Intent(MainActivity.this, CreateNote.class);
        System.out.println("In note click: " + noteClickedPositionId);
        intent.putExtra("isView", false);
        intent.putExtra("notes", noteList.get(position));
        intent.putExtra("id", noteClickedPositionId);
        startActivity(intent);

    }

    public void displayNote() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        System.out.println("In display note: " + noteClickedPositionId);

        executor.execute(() -> {
            List<Note> arrayList;
            arrayList = NotesDatabase.getDatabase(getApplicationContext()).notesDao().getAllNotes();

            handler.post(() -> {
                if (noteList.size() == 0) {
                    noNotesIcon.setVisibility(View.INVISIBLE);
                    noNotesText.setVisibility(View.INVISIBLE);
                    //noNotesIcon.setVisibility(View.VISIBLE);
                    //noNotesText.setVisibility(View.VISIBLE);
                    noteList.addAll(arrayList);
                    notesRecyclerViewAdapter.notifyDataSetChanged();

                } else {
                    noNotesIcon.setVisibility(View.INVISIBLE);
                    noNotesText.setVisibility(View.INVISIBLE);
                    //notesRecyclerView.invalidate();
                    //notesRecyclerView.setAdapter(new NotesRecyclerViewAdapter(noteList, this));

                }

            });

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayNote();

    }

}