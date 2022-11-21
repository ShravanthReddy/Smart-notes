package com.shravanth.smartnotes.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.database.NotesDatabase;
import com.shravanth.smartnotes.entities.Note;

import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ImageView addButton;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting the statusbar to transparetn
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //initialising addbutton
        addButton = findViewById(R.id.add_button);

        //when add button is clicked
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCreateNote func is called which creates CreateNote activity
                openCreateNote();
            }
        });

    }

    //function creates an intent to start CreateNote activity
    protected void openCreateNote() {
        Intent intent = new Intent(MainActivity.this, CreateNote.class);
        startActivity(intent);

    }


    public void displayNote() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Note> arrayList;
            arrayList = NotesDatabase.getDatabase(getApplicationContext()).notesDao().getAllNotes();
            List<Note> finalArrayList = arrayList;
            handler.post(() -> {
                System.out.println(finalArrayList);

            });
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        displayNote();

    }
}