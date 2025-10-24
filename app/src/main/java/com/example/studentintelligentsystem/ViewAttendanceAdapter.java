package com.example.studentintelligentsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder> {

    private final List<AttendanceRecord> attendanceRecords;

    public ViewAttendanceAdapter(List<AttendanceRecord> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = attendanceRecords.get(position);
        holder.tvDate.setText(record.getStudentName()); // We'll reuse the date TextView for the student's name

        if (record.isPresent()) {
            holder.ivStatus.setImageResource(R.drawable.dot_green);
        } else {
            holder.ivStatus.setImageResource(R.drawable.dot_red);
        }
    }

    @Override
    public int getItemCount() {
        return attendanceRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView ivStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }
    }
}
