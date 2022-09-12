package com.example.notekeeper;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.databinding.ActivityNoteBinding;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private ActivityNoteBinding binding;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.jwhh.jim.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    private NoteInfo noteInfo = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean isNewNote;
    private int noteId;
    private boolean isCancelling;
    private String originalNoteCourseId;
    private String originalNoteTitle;
    private String originalNoteText;
    private NoteKeeperOpenHelper dbOpenHelper;
    private Cursor noteCursor;
    private int courseIdPos;
    private int noteTitlePos;
    private int noteTextPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        dbOpenHelper = new NoteKeeperOpenHelper(this);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        if (!isNewNote)
            loadNoteData();

        setupUpButton();
    }

    private void setupUpButton() {
        ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        dbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        isCancelling = true;
        finish();
        return super.onSupportNavigateUp();
    }

    private void loadNoteData() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        String selection = NoteInfoEntry._ID + " = ?";

        String[] selectionArgs = {Integer.toString(noteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        noteCursor.moveToNext();
        displayNote();
    }


    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        originalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        originalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        originalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if (isNewNote)
            return;
        originalNoteCourseId = noteInfo.getCourse().getCourseId();
        originalNoteTitle = noteInfo.getTitle();
        originalNoteText = noteInfo.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position: " + noteId);
            if (isNewNote) {
                DataManager.getInstance().removeNote(noteId);
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteCourseId);
        noteInfo.setCourse(course);
        noteInfo.setTitle(originalNoteTitle);
        noteInfo.setText(originalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, originalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText);
    }

    private void saveNote() {
        noteInfo.setCourse((CourseInfo) binding.spinnerCourses.getSelectedItem());
        noteInfo.setTitle(binding.textNoteTitle.getText().toString());
        noteInfo.setText(binding.textNoteText.getText().toString());
    }

    private void displayNote() {
        String courseId = noteCursor.getString(courseIdPos);
        String noteTitle = noteCursor.getString(noteTitlePos);
        String noteText = noteCursor.getString(noteTextPos);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex = courses.indexOf(course);
        binding.spinnerCourses.setSelection(courseIndex);
        binding.textNoteTitle.setText(noteTitle);
        binding.textNoteText.setText(noteText);
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        isNewNote = noteId == ID_NOT_SET;
        if (isNewNote) {
            createNewNote();
        }

        Log.i(TAG, "mNoteId: " + noteId);
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);

    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        noteId = dm.createNewNote();
//        mNote = dm.getNotes().get(mNoteId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_next) {
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(noteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++noteId;
        noteInfo = DataManager.getInstance().getNotes().get(noteId);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) binding.spinnerCourses.getSelectedItem();
        String subject = binding.textNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + binding.textNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}