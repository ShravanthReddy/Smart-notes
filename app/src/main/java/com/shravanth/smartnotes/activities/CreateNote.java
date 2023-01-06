package com.shravanth.smartnotes.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;

import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.MenuItem;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.database.NotesDatabase;
import com.shravanth.smartnotes.entities.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateNote extends AppCompatActivity {

    //initialising views
    private EditText editText;
    private FloatingActionButton floatingActionButton;
    private MaterialToolbar topAppBar;

    //initialising int, string, list and boolean variables
    private int endTitle;
    private String text;
    private boolean boldDone = false;
    private int id;
    private boolean textChanged = false;
    private List<Note> arrayList;
    private String title;
    private String note;
    private int titleFontSize;
    private int noteFontSize;
    private int counter = 0;
    private boolean isUndo = false;
    private boolean isRedo = false;
    private String result;

    //initialising typefaces
    private Typeface poppins_bold;
    private Typeface poppins_regular;

    //initialising stacks to do undo and redo operations
    ArrayDeque<String> undoStack;
    ArrayDeque<String> redoStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //setting the content to go behind the status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        undoStack = new ArrayDeque<>(10);
        redoStack = new ArrayDeque<>(10);

        //initializing layout widgets
        editText = findViewById(R.id.editText);
        topAppBar = findViewById(R.id.topAppBar);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        //editText.setMovementMethod(LinkMovementMethod.getInstance());

        //when the navigation button clicked on the topappbar, finishing the activity
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //creating typefaces
        poppins_bold = ResourcesCompat.getFont(this, R.font.poppins_bold);
        poppins_regular = ResourcesCompat.getFont(this, R.font.poppins_regular);

        //importing title and notes font size from dimensions
        titleFontSize = (int) (getResources().getDimension(R.dimen.titleFontSize)/getResources().getDisplayMetrics().density);
        noteFontSize = (int) (getResources().getDimension(R.dimen.noteFontSize)/getResources().getDisplayMetrics().density);

        //calling function to span hint text
        styleTextHint(editText, poppins_bold, poppins_regular, titleFontSize, noteFontSize);

        //getting boolean isView or not from the intent called
        boolean isView = getIntent().getBooleanExtra("isView", true);

        //if not a new note
        if (!isView) {
            //getting the notes from the mainactivity
            Note noteList = (Note) getIntent().getSerializableExtra("notes");
            //getting id called from the mainactivity and assigning it to id
            id = getIntent().getIntExtra("id", 0);

            //if notetext is not null setting notetitle and notetext it to editText view
            if (noteList.getNoteText() != null) {
                editText.setText(noteList.getTitle() + "\n" + noteList.getNoteText());

            } else {
                //setting only title to edittext
                editText.setText(noteList.getTitle());

            }

            undoStack.push(editText.getText().toString());
            //calling style edit text which adds spans to the edittext
            styleEditText();

        }

        //when floating action button is clicked
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //calling getSelectedText function to get the selected text
                String selectedText = getSelectedText();

                //when the selected text is empty
                if (selectedText.isEmpty()) {
                    //showing a toast message
                    Toast.makeText(CreateNote.this, "No equation selected. Please select an equation and try again", Toast.LENGTH_SHORT).show();

                } else {

                    //creating a background thread
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    //executing the background thread
                    executor.execute(() -> {

                        //initialising the calculation class
                        Calculation calculation = new Calculation(selectedText);
                        //storing the returned value to result variable
                        result = calculation.doCalculation();

                        //checking if the result is a floating point
                        try{
                            Float.parseFloat(result);

                            //if not wrong equation has been selected
                        } catch(NumberFormatException e) {
                            result = "Wrong equation";

                        }

                        //after background execution is done
                        handler.post(() -> {
                            //if wrong equation has been selected
                            if (result == "Wrong equation") {
                                //displaying a toast message to the user
                                Toast.makeText(CreateNote.this, result + ", please correct the equation and try again.", Toast.LENGTH_SHORT).show();
                                //getting text using get text
                                String text = getText();
                                //setting the text to edit text
                                editText.setText(text);
                                //styling edit text
                                styleEditText();

                            } else {
                                //displaying the calculated value to the user
                                Toast.makeText(CreateNote.this, "Calculated value: " + result, Toast.LENGTH_SHORT).show();
                                //getting text using get text
                                String text = getText();
                                //setting the text to edit text along with the calculated value
                                editText.setText(text + "\n#Calculated Value: " + result);
                                //styling edit text
                                styleEditText();

                            }
                        });
                    });
                }
            }
        });

        //creating text watcher for edit text field
        editText.addTextChangedListener(new TextWatcher() {

            //function for before text changed
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = charSequence.toString();

                //when counter is 5
                if (counter == 5 & !isUndo & !isRedo) {
                    //making counter 0
                    counter = 0;

                    //if both undo & redo are not selected
                    if (!isUndo & !isRedo) {
                        //if undo stack is already full
                        if (undoStack.size() == 10) {
                            //removing first element in the stack
                            undoStack.removeLast();

                        }

                        //if stack doesn't contain text
                        if (!undoStack.contains(text)) {
                            //pushing the value to the stack
                            undoStack.push(text);

                        }

                    }

                    //else if undo button is pressed
                } else if(isUndo) {
                    isUndo = false;

                    //if redo stack is already full
                    if (redoStack.size() == 10) {
                        //removing the first element from stack
                        redoStack.removeLast();

                    }

                    //if stack doesn't contain the value
                    if (!redoStack.contains(text)) {
                        //pushing the value to redo stack
                        redoStack.push(text);

                    }

                } else {
                    //incrementing the counter
                    isRedo = false;
                    counter++;

                }

            }

            //function for on text changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                textChanged = true;
                //creating start and end index for text field
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();

                //getting line break index
                endTitle = charSequence.toString().indexOf("\n");
                int length = editText.getText().length();

                //checking if a line break has been made
                if (endTitle > -1) {

                    //if bold done or No title was written or start > 0 and linebreak has been done before title has been written and writing title now
                    if (!boldDone || ( endTitle == 0 & start != 0 ) || ( start > 0 & endTitle == start) || ((endTitle > start) & (start > 0)) || (endTitle + 1 == start & start < length)) {


                        //linebreak has been done before title has been written and writing is been done now
                        if ((start > 0 & endTitle == start) || (endTitle + 1 == start & start < length)) {
                            //removing any previously written spans from the field
                            spanRemoval(editText);
                        }

                        //changing the text size and font family
                        changeEditTextProperties(editText, poppins_regular, noteFontSize);

                        //calling change text function
                        styleText(endTitle, start, titleFontSize, editText, poppins_bold);
                        boldDone = true;

                    }

                //if no line break and start > 0
                } else if (start > 0) {

                    //changing text size and font family
                    changeEditTextProperties(editText, poppins_bold, titleFontSize);
                    boldDone = false;

                    //calling spanremoval function
                    spanRemoval(editText);

                } else if (editText.getText().length() == 0) {
                    changeEditTextProperties(editText, poppins_bold, titleFontSize);
                    spanRemoval(editText);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) { }

        });

        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {

                    case R.id.delete:
                        onDeletePressed();
                        break;

                    case R.id.undo:
                        if (undoStack.size() != 0 & textChanged) {
                            isUndo = true;
                            String poppedText = undoStack.pop();
                            editText.setText(poppedText);
                            styleEditText();

                        }
                        break;

                    case R.id.redo:
                        if (redoStack.size() != 0) {
                            isRedo = true;
                            String poppedText = redoStack.pop();
                            editText.setText(poppedText);
                            undoStack.push(poppedText);
                            styleEditText();

                        }
                        break;

                }
                return true;
            }
        });
    }

    //function to get selected text
    public String getSelectedText() {

        //getting start and end index of the selection
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();

        //getting the text using start and end index
        String selectedText = editText.getText().toString().substring(selectionStart, selectionEnd);
        //returning the selected text
        return selectedText;

    }

    //function to get text from the edit text
    public String getText() {
        //getting text from the edit text
        String text;
        text = editText.getText().toString();

        //checking if calculated value exists
        int index = text.toLowerCase().indexOf("\n#calculated value: ");
        //if exists removing it from the text
        if (index != -1) {
            text = text.substring(0, index);

        }
        //returning text
        return text;

    }

    public void styleEditText() {
        //getting the text from edittext view and assigning it to written text string
        String writtenText = editText.getText().toString();
        //getting index of the line break
        endTitle = writtenText.indexOf("\n");

        if (endTitle > 0 || (writtenText.length() > 0 & endTitle != -1)) {
            boldDone = true;

            //calling styletext function to style the text
            styleText(endTitle, writtenText.length(), titleFontSize,  editText, poppins_bold);

        } else if(endTitle == -1 & writtenText.length() > 0){
            //calling styletext function to style the text till the length of the text
            styleText(writtenText.length(), writtenText.length(), titleFontSize, editText, poppins_bold);

        }
        //changing edittext properties
        changeEditTextProperties(editText, poppins_regular, noteFontSize);

    }

    public void onDeletePressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete?");

        // Set Alert Title
        builder.setTitle("Alert");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(true);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            // When the user click yes button then app will close
            editText.setText("");
            finish();

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

    }

    //change text function
    public static void styleText(int endTitle, int end, int titleFontSize, EditText editText, Typeface poppins_bold) {

        if (end != 0) {
            //creates span where title is styled to bold and text size changed
            Spannable span = editText.getText();
            span.setSpan(new CustomTypeFaceSpan(poppins_bold), 0, endTitle, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            span.setSpan(new AbsoluteSizeSpan(titleFontSize, true), 0, endTitle, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            editText.setText(span, TextView.BufferType.SPANNABLE);

            //after span setting cursor to the end of the string
            editText.setSelection(end);

        }

    }

    //span removal function
    public static void spanRemoval(EditText editText) {

        //getting all spans from edit text field
        Object[] toRemoveSpans = editText.getText().getSpans(0, editText.getText().length(), Object.class);

        //for each span
        for (final Object span : toRemoveSpans) {

            //checking if the span has any instance of
            if (span instanceof CustomTypeFaceSpan || span instanceof Typeface || span instanceof AbsoluteSizeSpan) {
                editText.getText().removeSpan(span); //removing span

            }
        }
    }

    //function to change font and size properties of edittext
    public static void changeEditTextProperties(EditText editText, Typeface font, int fontSize) {
        editText.setTextSize(fontSize); //changing the font size
        editText.setTypeface(font); //changing the font

    }

    //when application is paused
    @Override
    protected void onPause() {
        super.onPause();

        //getting string from editText
        text = editText.getText().toString();

        //if text has been changed in the activity
        if (textChanged) {

            textChanged = false;
            //creating an executor and a handler
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            if (endTitle >= 0 & text.length() > endTitle + 1) {
                title = text.substring(0, endTitle); //getting title from the string
                note = text.substring(endTitle + 1); //getting notes from the string

            } else if (editText.getText().length() != 0) {
                title = text; //getting title from the string
                note = null;

            }

            //executing the process of saving the text to db in a background thread
            executor.execute(() -> {
                if (editText.getText().length() != 0) { //if length is != 0
                    saveText(title, note); // calling save text function to save the note to db

                } else if(id != 0) {
                    // if an id is present and whole text is removed, deleting the id from db
                    deleteNote();

                }
                handler.post(() -> {});

            });
        }
    }

    //function to save text to db
    private void saveText(String title, String note) {

        //creating time stamp
        String datetime = new SimpleDateFormat("EEEE, dd-MM-yyyy HH:mm a", Locale.getDefault()).format(new Date());
        Note mainNote = new Note(title, datetime, note); //using above var making note object

        //if no id present, creating a new table in db
        if (id == 0) {
            NotesDatabase.getDatabase(getApplicationContext()).notesDao().insertNote(mainNote);
            arrayList = NotesDatabase.getDatabase(getApplicationContext()).notesDao().getAllNotes();
            id = arrayList.get(0).getId();

            //else updating the db using id
        } else {
            NotesDatabase.getDatabase(getApplicationContext()).notesDao().updateNote(title, datetime, note, id);

        }
    }

    //deleting the table from db using id
    private void deleteNote() {
        NotesDatabase.getDatabase(getApplicationContext()).notesDao().deleteByUserId(id);

    }

    //function to span hint text
    private void styleTextHint(EditText editText, Typeface poppins_bold, Typeface poppins_regular, int titleFontSize, int noteFontSize) {
        Spannable span = new SpannableString(" Note Title\n Note");
        span.setSpan(new CustomTypeFaceSpan(poppins_bold), 0, 11, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new CustomTypeFaceSpan(poppins_regular), 11, 17, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(titleFontSize, true), 0, 11, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(noteFontSize, true), 11, 17, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        editText.setHint(span);

    }

}