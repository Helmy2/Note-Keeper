package com.example.notekeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.notekeeper.databinding.ActivityNoteListBinding;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    ActivityNoteListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeDisplayContent();
        binding.fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
            startActivity(intent);
        });
    }

    private void initializeDisplayContent() {
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        ArrayAdapter<NoteInfo> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, notes);

        binding.listNotes.setAdapter(adapter);
        binding.listNotes.setOnItemClickListener((adapterView, view, position, l) -> {
            Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
            intent.putExtra(NoteActivity.NOTE_POSITION, position);
            startActivity(intent);
        });
    }


}