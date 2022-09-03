package com.example.notekeeper;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notekeeper.databinding.ItemNoteListBinding;

import java.util.List;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteListViewHolder> {
    private OnItemClickListener onItemClickListener;
    private final List<NoteInfo> localDataSet;

    public NoteListAdapter(List<NoteInfo> localDataSet) {
        this.localDataSet = localDataSet;
    }

    public void setOnItemClickedListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public NoteListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return NoteListViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteListViewHolder holder, int position) {
        NoteInfo item = localDataSet.get(position);
        holder.bind(onItemClickListener, position, item);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
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

        public void bind(OnItemClickListener onItemClickListener, int position, NoteInfo noteInfo) {
            if (onItemClickListener != null)
                binding.getRoot().setOnClickListener(view -> onItemClickListener.onItemClick(position));
            binding.textCourse.setText(noteInfo.getCourse().toString());
            binding.textTitle.setText(noteInfo.getTitle());
        }
    }
}




