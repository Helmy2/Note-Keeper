package com.example.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notekeeper.databinding.ActivityNoteListBinding;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {
    ActivityNoteListBinding binding;
    NoteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeDisplayContent();
        binding.fabAdd.setOnClickListener(this::onFABAddClicked);
    }

    private void initializeDisplayContent() {
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new NoteListAdapter(notes);
        binding.listNotes.setLayoutManager(layoutManager);
        binding.listNotes.setAdapter(adapter);

        adapter.setOnItemClickedListener(this::onNoteItemClicked);
    }

    private void onNoteItemClicked(int position) {
        Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
        intent.putExtra(NoteActivity.NOTE_POSITION, position);
        startActivity(intent);
    }

    private void onFABAddClicked(View view) {
        Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
    }
}