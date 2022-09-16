package com.example.notekeeper;

import static com.example.notekeeper.NoteKeeperDatabaseContract.*;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notekeeper.databinding.ItemNoteListBinding;

import java.util.List;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteListViewHolder> {
    private OnNoteItemClickListener onItemClickListener;
    private Cursor cursor;
    private int coursePos;
    private int noteTitlePos;
    private int idPos;

    public NoteListAdapter(Cursor cursor) {
        this.cursor = cursor;
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (cursor == null)
            return;

        coursePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        idPos = cursor.getColumnIndex(NoteInfoEntry._ID);
    }

    public void changeCursor(Cursor cursor) {
        if (this.cursor != null)
            this.cursor.close();
        this.cursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    public void setOnItemClickedListener(OnNoteItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public NoteListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return NoteListViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteListViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String course = cursor.getString(coursePos);
        String noteTitle = cursor.getString(noteTitlePos);
        int id = cursor.getInt(idPos);
        holder.bind(onItemClickListener, course, noteTitle, id);
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    static class NoteListViewHolder extends RecyclerView.ViewHolder {
        public ItemNoteListBinding binding;

        public NoteListViewHolder(@NonNull ItemNoteListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static NoteListViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemNoteListBinding binding = ItemNoteListBinding.inflate(layoutInflater, parent, false);
            return new NoteListViewHolder(binding);
        }

        public void bind(OnNoteItemClickListener onItemClickListener, String course, String noteTitle, int id) {
            if (onItemClickListener != null)
                binding.getRoot().setOnClickListener(view -> onItemClickListener.onItemClick(id));
            binding.textTitle.setText(course);
            binding.textBody.setText(noteTitle);
        }
    }
}




