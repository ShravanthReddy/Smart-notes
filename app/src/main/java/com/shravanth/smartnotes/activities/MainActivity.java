package com.shravanth.smartnotes.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity implements NotesListener {

    //action mode variable
    private ActionMode actionMode;
    private ActionMode myActMode;

    //creating list variables
    private List<View> views;
    private List<Note> noteList;
    private List<Integer> selectedNotesId;
    private List<Integer> selectedNotesPosition;

    //creating int, boolean variables
    private int selectedNotesCount = 0;
    private boolean isNoteLongClicked = false;
    private int dbSize;
    private int noteClickedPosition = -1;
    private int noteClickedPositionId = -1;

    //creating view variables
    private ImageView noNotesIcon;
    private TextView noNotesText;
    private ImageView accountImageView;
    private EditText searchEditTextView;
    private ImageView addButton;
    private RecyclerView notesRecyclerView;
    private NotesRecyclerViewAdapter notesRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Generate a random AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128); // Use 128 bits for the key
            SecretKey secretKey = keyGenerator.generateKey();

            System.out.println(secretKey);
            // Store the key in the Android KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.setEntry("my_key", new KeyStore.SecretKeyEntry(secretKey),
                    new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());

        } catch (Exception e) {
            System.out.println("Error in getting the Key");

        }

        //initialising all views
        addButton = findViewById(R.id.add_button);
        accountImageView = findViewById(R.id.accountImageView);
        searchEditTextView = findViewById(R.id.searchEditTextView);
        noNotesIcon = findViewById(R.id.noNotesIcon);
        noNotesText = findViewById(R.id.noNotesText);
        notesRecyclerView = findViewById(R.id.notesRecyclerView);

        //adding text changed listener to search edit textview
        searchEditTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //cancelling timer of recyclerview adapter on text changed
                notesRecyclerViewAdapter.cancelTimer();

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //if notelist size is not 0
                if (noteList.size() != 0) {
                    //calling search notes function
                    notesRecyclerViewAdapter.searchNotes(editable.toString());

                }
            }
        });

        //on account image view click
        accountImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //starting account activity
                Intent intent = new Intent(MainActivity.this, Account.class);
                startActivity(intent);

            }
        });

        //creating notesRecyclerView layout manager
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        //creating noteslist and assigning the same to recycler view adapter
        noteList = new ArrayList<>();
        notesRecyclerViewAdapter = new NotesRecyclerViewAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesRecyclerViewAdapter);

        //creating array list
        selectedNotesId = new ArrayList<>();
        selectedNotesPosition = new ArrayList<>();
        views = new ArrayList<>();

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

    //when action mode is called
    private ActionMode.Callback myActModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            //inflating action mode
            mode.getMenuInflater().inflate(R.menu.contextual_action_bar, menu);
            //setting title
            mode.setTitle("1");
            return true;

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            //using switch to get item clicked
            switch (item.getItemId()) {
                //if delete button clicked
                case R.id.option_1:

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Are you sure you want to delete?");

                    // Set Alert Title
                    builder.setTitle("Alert");

                    // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                    builder.setCancelable(true);

                    // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                        // When the user click yes button then app will close
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());

                        executor.execute(() -> {
                            Collections.sort(selectedNotesPosition, Collections.reverseOrder());

                            for (int position: selectedNotesPosition) {
                                noteList.remove(position);
                            }

                            for (int id: selectedNotesId){
                                deleteNote(id);
                            }

                            handler.post(() -> {
                                for (int position: selectedNotesPosition) {
                                    notesRecyclerViewAdapter.notifyItemRemoved(position);
                                }

                                if(noteList.isEmpty()) {
                                    noNotesIcon.setVisibility(View.VISIBLE);
                                    noNotesText.setVisibility(View.VISIBLE);
                                }

                                mode.finish();
                                dialog.cancel();

                            });
                        });

                    });

                    // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                        // If user click no then dialog box is canceled.
                        dialog.cancel();

                    });

                    // Create the Alert dialog
                    AlertDialog alertDialog = builder.create();
                    // Show the Alert Dialog box
                    alertDialog.show();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            myActMode = null;
            isNoteLongClicked = false;

            selectedNotesId.clear();
            selectedNotesPosition.clear();
            selectedNotesCount = 0;

            removeViews();
        }
    };

    public void removeViews() {
        for (View view: views) {
            view.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.note_background));

        }
        views.clear();

    }

    //deleting the table from db using id
    private void deleteNote(int id) {
        NotesDatabase.getDatabase(getApplicationContext()).notesDao().deleteByUserId(id);

    }

    @Override
    public void onNoteClicked(Note note, int position, View view) {
        if (!isNoteLongClicked) {
            noteClickedPosition = position;
            noteClickedPositionId = note.getId();
            Intent intent = new Intent(MainActivity.this, CreateNote.class);
            intent.putExtra("isView", false);
            intent.putExtra("notes", note);
            intent.putExtra("id", noteClickedPositionId);
            startActivity(intent);

        } else {
            int indexId = selectedNotesId.indexOf(note.getId());
            int indexPosition = selectedNotesPosition.indexOf(position);
            int indexView = views.indexOf(view);

            if (indexId == -1) {
                selectedNotesCount++;
                selectedNotesId.add(note.getId());
                selectedNotesPosition.add(position);
                views.add(view);
                view.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.note_background_selected));

            } else {
                selectedNotesCount--;
                view.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.note_background));
                selectedNotesId.remove(indexId);
                selectedNotesPosition.remove(indexPosition);
                views.remove(indexView);

            }

            if (selectedNotesId.size() == 0) {
                actionMode.finish();

            } else {
                actionMode.setTitle("" + selectedNotesCount);

            }

        }

    }

    @Override
    public void onNoteLongClicked(Note note, int position, View view) {
        if (!isNoteLongClicked) {
            myActMode = startSupportActionMode(myActModeCallback);

        }
        isNoteLongClicked = true;

        int indexId = selectedNotesId.indexOf(note.getId());
        int indexPosition = selectedNotesPosition.indexOf(position);
        int indexView = views.indexOf(view);

        if (indexId == -1) {
            selectedNotesCount++;
            selectedNotesId.add(note.getId());
            selectedNotesPosition.add(position);
            views.add(view);
            view.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.note_background_selected));

        } else {
            selectedNotesCount--;
            view.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.note_background));
            selectedNotesId.remove(indexId);
            selectedNotesPosition.remove(indexPosition);
            views.remove(indexView);

        }

        if (selectedNotesId.size() == 0) {
            actionMode.finish();

        } else {
            actionMode.setTitle("" + selectedNotesCount);

        }

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
        searchEditTextView.clearFocus();
        displayNote();

    }

}