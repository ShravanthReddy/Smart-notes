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

    // private ActivityMainBinding binding;
    private ImageView addButton;
    private ArrayList<String> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        addButton = findViewById(R.id.add_button);

        // binding = ActivityMainBinding.inflate(getLayoutInflater());
        // setContentView(binding.getRoot());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateNote();
            }
        });

    }

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