package com.example.studentintelligentsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {

    private final List<Student> students;
    private final Map<Long, Boolean> attendanceStatus = new HashMap<>();

    public StudentAttendanceAdapter(List<Student> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.tvStudentName.setText(student.getName());

        holder.rgAttendance.setOnCheckedChangeListener(null);

        Boolean isPresent = attendanceStatus.get(student.getId());
        if (isPresent != null) {
            if (isPresent) {
                holder.rbPresent.setChecked(true);
            } else {
                holder.rbAbsent.setChecked(true);
            }
        } else {
            holder.rgAttendance.clearCheck();
        }

        holder.rgAttendance.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPresent) {
                attendanceStatus.put(student.getId(), true);
            } else if (checkedId == R.id.rbAbsent) {
                attendanceStatus.put(student.getId(), false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public Map<Long, Boolean> getAttendanceStatus() {
        return attendanceStatus;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        RadioGroup rgAttendance;
        RadioButton rbPresent, rbAbsent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            rgAttendance = itemView.findViewById(R.id.rgAttendance);
            rbPresent = itemView.findViewById(R.id.rbPresent);
            rbAbsent = itemView.findViewById(R.id.rbAbsent);
        }
    }
}
