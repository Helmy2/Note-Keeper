package com.example.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.databinding.ActivityNoteBinding;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
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
    SimpleCursorAdapter adapterCourses;
    private boolean coursesQueryFinished;
    private boolean notesQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        dbOpenHelper = new NoteKeeperOpenHelper(this);


        adapterCourses = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0
        );
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCourses.setAdapter(adapterCourses);

        getLoaderManager().initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        if (!isNewNote)
            getLoaderManager().initLoader(LOADER_NOTES, null, this);

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
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(noteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteCourseId);
        noteInfo.setCourse(course);
        noteInfo.setTitle(originalNoteTitle);
        noteInfo.setText(originalNoteText);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, originalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = binding.textNoteTitle.getText().toString();
        String noteText = binding.textNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = binding.spinnerCourses.getSelectedItemPosition();
        Cursor cursor = adapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        return cursor.getString(courseIdPos);
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(noteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void displayNote() {
        String courseId = noteCursor.getString(courseIdPos);
        String noteTitle = noteCursor.getString(noteTitlePos);
        String noteText = noteCursor.getString(noteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);

        binding.spinnerCourses.setSelection(courseIndex);
        binding.textNoteTitle.setText(noteTitle);
        binding.textNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = adapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
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
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        noteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderCourses() {
        coursesQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes() {
        notesQueryFinished = false;
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(noteId)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                        selection, selectionArgs, null, null, null);

            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if (loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(data);
            coursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        noteCursor = data;
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        noteCursor.moveToFirst();

        notesQueryFinished = true;
        displayNoteWhenQueriesFinished();

    }

    private void displayNoteWhenQueriesFinished() {
        if (notesQueryFinished && coursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            if (noteCursor != null)
                noteCursor.close();
        } else if (loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(null);
        }
    }
}