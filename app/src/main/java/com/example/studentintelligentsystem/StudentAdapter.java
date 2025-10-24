package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> studentList;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // milliseconds

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.text1.setText(student.getName());
        holder.text2.setText("Grade: " + student.getGrade());

        holder.itemView.setOnClickListener(v -> {
            long clickTime = SystemClock.elapsedRealtime();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                // Double-click detected
                Context context = v.getContext();
                Intent intent = new Intent(context, AttendanceHistoryActivity.class);
                intent.putExtra(AttendanceHistoryActivity.EXTRA_STUDENT, student);
                context.startActivity(intent);
            }
            lastClickTime = clickTime;
        });
    }

    @Override
    public int getItemCount() {
        return studentList == null ? 0 : studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
