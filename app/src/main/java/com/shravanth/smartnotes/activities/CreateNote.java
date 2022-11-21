package com.shravanth.smartnotes.activities;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.shravanth.smartnotes.R;
import com.shravanth.smartnotes.database.NotesDatabase;
import com.shravanth.smartnotes.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateNote extends AppCompatActivity {

    private ImageView backButton;
    private TextView datetimeTV;
    private EditText editText;
    private int endTitle;
    private String text;
    private boolean boldDone = false;
    private Typeface poppins_bold;
    private Typeface poppins_regular;
    private int id;
    private boolean textChanged = false;
    private List<Note> arrayList;
    private String title;
    private String note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //changing status bar to transparent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //initializing layout widgets
        backButton = findViewById(R.id.back_button);
        editText = findViewById(R.id.editText);
        datetimeTV = findViewById(R.id.TVdatetime);

        //creating typefaces
        poppins_bold = ResourcesCompat.getFont(this, R.font.poppins_bold);
        poppins_regular = ResourcesCompat.getFont(this, R.font.poppins_regular);

        //calling function to span hint text
        changeTextHint(editText, poppins_bold, poppins_regular, 78, 58);

        //requesting focus to text field
        editText.requestFocus();
        datetimeTV.setText(new SimpleDateFormat("EEEE, dd MM yyyy HH:mm a", Locale.getDefault()).format(new Date()));

        //on clicking back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //closing the activity

            }
        });

        //creating text watcher for edit text field
        editText.addTextChangedListener(new TextWatcher() {

            //function for before text changed
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

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

                System.out.println("New line: " + endTitle + " Length: " + length);
                //checking if a line break has been made
                if (endTitle > -1) {

                    //if bold done or No title was written or start > 0 and linebreak has been done before title has been written and writing title now
                    if (!boldDone || ( endTitle == 0 & start != 0 ) || ( start > 0 & endTitle == start) || ((endTitle > start) & (start > 0)) || (endTitle + 1 == start & start < length)) {

                        //linebreak has been done before title has been written and writing is been done now
                        if ((start > 0 & endTitle == start) || (endTitle + 1 == start & start < length)) {
                            //removing any previously written spans from the field
                            System.out.println("Span removal 1");
                            spanRemoval(editText);
                        }

                        //changing the text size and font family
                        changeEditTextProperties(editText, poppins_regular, 22);
                        boldDone = true;

                        //calling change text function
                        changeText(endTitle, end, start, editText, poppins_bold);

                    }

                //if no line break and start > 0
                } else if (start > 0) {

                    //changing text size and font family
                    changeEditTextProperties(editText, poppins_bold, 30);
                    boldDone = false;

                    //calling spanremoval function
                    spanRemoval(editText);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

    }

    //change text function
    public static void changeText(int endTitle, int end, int start, EditText editText, Typeface poppins_bold) {
        if ((end == endTitle + 1 & endTitle != 0) || (endTitle == 0 & end == endTitle + 2) || (endTitle == start) || (endTitle > start)) {

            //creates span where title is styled to bold and text size changed
            Spannable span = editText.getText();
            span.setSpan(new CustomTypeFaceSpan(poppins_bold), 0, endTitle, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            span.setSpan(new AbsoluteSizeSpan(78), 0, endTitle, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            if (endTitle > 0 & text.length() > endTitle + 1) {
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
    private void changeTextHint(EditText editText, Typeface poppins_bold, Typeface poppins_regular, int titleFontSize, int noteFontSize) {
        Spannable span = new SpannableString("Note Title\nNote");
        span.setSpan(new CustomTypeFaceSpan(poppins_bold), 0, 10, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new CustomTypeFaceSpan(poppins_regular), 11, 15, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(titleFontSize), 0, 10, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(noteFontSize), 11, 15, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        editText.setHint(span);

    }

}