package com.example.notekeeper;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notekeeper.databinding.ItemCourseListBinding;

import java.util.List;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.CourseListViewHolder> {
    private OnItemClickListener onItemClickListener;
    private final List<CourseInfo> localDataSet;

    public CourseListAdapter(List<CourseInfo> localDataSet) {
        this.localDataSet = localDataSet;
    }

    public void setOnItemClickedListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CourseListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CourseListViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseListViewHolder holder, int position) {
        CourseInfo item = localDataSet.get(position);
        holder.bind(onItemClickListener, position, item);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    static class CourseListViewHolder extends RecyclerView.ViewHolder {
        public ItemCourseListBinding binding;

        public CourseListViewHolder(@NonNull ItemCourseListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static CourseListViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemCourseListBinding binding = ItemCourseListBinding.inflate(layoutInflater, parent, false);
            return new CourseListViewHolder(binding);
        }

        public void bind(OnItemClickListener onItemClickListener, int position, CourseInfo courseInfo) {
            if (onItemClickListener != null)
                binding.getRoot().setOnClickListener(view -> onItemClickListener.onItemClick(position));
            binding.setCourse(courseInfo);
        }
    }
}




