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
        TextView tvStudentId;
        TextView tvSubject;
        TextView tvExamType;
        TextView tvMarks;
        TextView tvGrade;
        TextView tvExamDate;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvExamType = itemView.findViewById(R.id.tvExamType);
            tvMarks = itemView.findViewById(R.id.tvMarks);
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvExamDate = itemView.findViewById(R.id.tvExamDate);
            btnEdit = itemView.findViewById(R.id.btnEditResult);
            btnDelete = itemView.findViewById(R.id.btnDeleteResult);
        }

        void bind(Result result, int position) {
            tvStudentName.setText(result.getStudentName());
            tvStudentId.setText("Student ID: " + result.getStudentId());
            tvSubject.setText(result.getSubject());
            tvExamType.setText(result.getTerm());
            tvMarks.setText(result.getMarks() + "/100");
            
            // Calculate and set grade
            int marks = result.getMarks();
            String grade;
            if (marks >= 90) grade = "A+";
            else if (marks >= 80) grade = "A";
            else if (marks >= 70) grade = "B";
            else if (marks >= 60) grade = "C";
            else if (marks >= 50) grade = "D";
            else grade = "F";
            tvGrade.setText(grade);
            
            // Set exam date if available
            if (result.getComment() != null && !result.getComment().isEmpty()) {
                tvExamDate.setText(result.getComment());
            } else {
                tvExamDate.setText("N/A");
            }

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

