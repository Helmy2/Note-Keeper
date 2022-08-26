package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.example.notekeeper.databinding.ActivityNoteBinding;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.jwhh.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    ActivityNoteBinding binding;
    private NoteInfo note;
    private boolean isNewNote;
    private List<CourseInfo> courses;
    private boolean isCancelling;
    private int notePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courses);
        binding.spinnerCourses.setAdapter(adapter);

        readDisplayStateValues();
        if (!isNewNote)
            displayNote();
    }

    private void displayNote() {
        int courseIndex = courses.indexOf(note.getCourse());
        binding.spinnerCourses.setSelection(courseIndex);
        binding.textNoteText.setText(note.getText());
        binding.textNoteTitle.setText(note.getTitle());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        isNewNote = position == POSITION_NOT_SET;
        if (isNewNote)
            createNode();
        else
            note = DataManager.getInstance().getNotes().get(position);
    }

    private void createNode() {
        DataManager dataManager = DataManager.getInstance();
        notePosition = dataManager.createNewNote();
        note = dataManager.getNotes().get(notePosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isCancelling)
            if (isNewNote)
                DataManager.getInstance().removeNote(notePosition);
            else
                saveNote();
    }

    private void saveNote() {
        note.setCourse((CourseInfo) binding.spinnerCourses.getSelectedItem());
        note.setText(binding.textNoteText.getText().toString());
        note.setTitle(binding.textNoteTitle.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            isCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) binding.spinnerCourses.getSelectedItem();
        String subject = binding.textNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + binding.textNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}