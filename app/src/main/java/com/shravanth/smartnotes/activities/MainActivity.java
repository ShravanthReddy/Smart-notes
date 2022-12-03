package com.shravanth.smartnotes.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.adapters.NotesRecyclerViewAdapter;
import com.shravanth.smartnotes.database.NotesDatabase;
import com.shravanth.smartnotes.entities.Note;
import com.shravanth.smartnotes.listeners.NotesListener;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private List<Note> noteList;

    private int dbSize;
    private int noteClickedPosition = -1;
    private int noteClickedPositionId = -1;

    private ImageView noNotesIcon;
    private TextView noNotesText;
    private ImageView accountImageView;
    private EditText searchEditTextView;
    private ImageView addButton;

    private RecyclerView notesRecyclerView;
    private NotesRecyclerViewAdapter notesRecyclerViewAdapter;

    private ActionMode myActMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.add_button);
        accountImageView = findViewById(R.id.accountImageView);
        searchEditTextView = findViewById(R.id.searchEditTextView);
        noNotesIcon = findViewById(R.id.noNotesIcon);
        noNotesText = findViewById(R.id.noNotesText);
        notesRecyclerView = findViewById(R.id.notesRecyclerView);

        searchEditTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notesRecyclerViewAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0) {
                    notesRecyclerViewAdapter.searchNotes(editable.toString());
                }
            }
        });

        accountImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Account.class);
                startActivity(intent);

            }
        });

        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesRecyclerViewAdapter = new NotesRecyclerViewAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesRecyclerViewAdapter);

        addButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (myActMode != null) {
                    return false;
                }
                myActMode = startSupportActionMode(myActModeCallback);
                return true;
            }
        });

        //when add button is clicked
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creates CreateNote activity
                Intent intent = new Intent(MainActivity.this, CreateNote.class);
                intent.putExtra("isView", true);
                startActivity(intent);
            }
        });
    }

    private ActionMode.Callback myActModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_action_bar, menu);
            mode.setTitle("Select option here");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.option_1:
                    Toast.makeText(MainActivity.this, "Selected Option 1", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                case R.id.option_2:
                    Toast.makeText(MainActivity.this, "Selected Option 2", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            myActMode = null;
        }
    };


    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        noteClickedPositionId = note.getId();
        Intent intent = new Intent(MainActivity.this, CreateNote.class);
        intent.putExtra("isView", false);
        intent.putExtra("notes", note);
        intent.putExtra("id", noteClickedPositionId);
        startActivity(intent);

    }

    @Override
    public void onNoteLongClicked(Note note, int position) {

    }

    //display note function to get data from the db and updating recycler view with the data
    public void displayNote() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        //creating a new thread to do data retrieval from Room db
        executor.execute(() -> {

            //getting database size using a query
            int databasePresentSize = NotesDatabase.getDatabase(getApplicationContext()).notesDao().dbSize();
            //initialising a update code to -1
            int UPDATE_CODE = -1;
            boolean dbEmpty = false;

            //if database size is empty
            if (databasePresentSize == 0) {
                dbEmpty = true;
            }

            //if notelist is empty and db not empty
            if (noteList.size() == 0 & !dbEmpty) {

                //creating a temp array list
                List<Note> arrayList;
                //getting all the data from Room db
                arrayList = NotesDatabase.getDatabase(getApplicationContext()).notesDao().getAllNotes();
                UPDATE_CODE = 0; //Update code = 0 when getting whole notes from db
                noteList.addAll(arrayList); //adding the data to notelist which is the dataset for recycler view

                dbSize = databasePresentSize; //changing dbsize variable to present size
                //hiding no notes available views
                noNotesIcon.setVisibility(View.INVISIBLE);
                noNotesText.setVisibility(View.INVISIBLE);

                //if noteclickedposition is not default
            } else if (noteClickedPositionId != -1){
                //checking if the note which is clicked on still exists in the db
                boolean noteExists = NotesDatabase.getDatabase(getApplicationContext()).notesDao().exists(noteClickedPositionId);
                //if no exists
                if (noteExists) {
                    //update code = 1 for note update
                    UPDATE_CODE = 1;
                    //updating the dataset at the position
                    noteList.set(noteClickedPosition, NotesDatabase.getDatabase(getApplicationContext()).notesDao().getNote(noteClickedPositionId).get(0));

                } else {
                    //update code = 2 for note delete
                    UPDATE_CODE = 2;
                    noteList.remove(noteClickedPosition); //removing the data from the position
                    dbSize = databasePresentSize; //updating the db size

                }

                noteClickedPositionId = -1; //updating noteclicked position to default

                //if noteclicked position is default and db is not empty
            } else if (noteClickedPositionId == -1 & !dbEmpty) {

                //checking if the dbsize saved to the program is same as the present db size
                if(dbSize != databasePresentSize) {
                    dbSize = databasePresentSize; //updating the db size
                    UPDATE_CODE = 3; //update code = 3 added new note to db
                    //adding new data to the dataset
                    noteList.add(0, NotesDatabase.getDatabase(getApplicationContext()).notesDao().getAllNotes().get(0));
                }
            }

            int finalUPDATE_CODE = UPDATE_CODE;
            boolean finalDbEmpty = dbEmpty;

            //once the background thread execution is done
            handler.post(() -> {

                //if db empty
                if (finalDbEmpty){
                    noNotesIcon.setVisibility(View.VISIBLE);
                    noNotesText.setVisibility(View.VISIBLE);
                }

                //notifying dataset changed to the recycler view as per the update code
                if (finalUPDATE_CODE == 0) {
                    notesRecyclerViewAdapter.notifyDataSetChanged();

                } else if(finalUPDATE_CODE == 1) {
                    notesRecyclerViewAdapter.notifyItemChanged(noteClickedPosition);

                } else if(finalUPDATE_CODE == 2) {
                    notesRecyclerViewAdapter.notifyItemRemoved(noteClickedPosition);

                } else if(finalUPDATE_CODE == 3) {
                    notesRecyclerViewAdapter.notifyItemInserted(0);

                }

            });

        });

    }

    //overiding onresume function to display note
    @Override
    protected void onResume() {
        super.onResume();
        displayNote();

    }

}