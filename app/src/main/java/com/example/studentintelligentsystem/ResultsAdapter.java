package com.example.studentintelligentsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter for displaying and managing result items
public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private List<Result> results;
    private OnResultActionListener listener;

    public interface OnResultActionListener {
        void onEditResult(Result result, int position);
        void onDeleteResult(Result result, int position);
    }

    public ResultsAdapter(List<Result> results, OnResultActionListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Result result = results.get(position);
        holder.bind(result, position);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        TextView tvSubject;
        TextView tvTerm;
        TextView tvMarks;
        TextView tvComment;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvMarks = itemView.findViewById(R.id.tvMarks);
            tvComment = itemView.findViewById(R.id.tvComment);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Result result, int position) {
            tvStudentName.setText(result.getStudentName());
            tvSubject.setText(result.getSubject());
            tvTerm.setText("Term: " + result.getTerm());
            tvMarks.setText("Marks: " + result.getMarks() + "%");
            tvComment.setText(result.getComment() != null && !result.getComment().isEmpty()
                ? result.getComment() : "No comment");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditResult(result, position);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteResult(result, position);
                }
            });
        }
    }
}

