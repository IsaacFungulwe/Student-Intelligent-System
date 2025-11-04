package com.example.studentintelligentsystem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {

    private List<Student> students;
    private OnAttendanceMarkedListener listener;

    public interface OnAttendanceMarkedListener {
        void onAttendanceMarked(Student student, boolean isPresent);
    }

    public StudentAttendanceAdapter(List<Student> students, OnAttendanceMarkedListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void removeStudent(int position) {
        if (position >= 0 && position < students.size()) {
            students.remove(position);
            notifyItemRemoved(position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        Button btnPresent;
        Button btnAbsent;

        ViewHolder(View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            btnPresent = itemView.findViewById(R.id.btnPresent);
            btnAbsent = itemView.findViewById(R.id.btnAbsent);
        }

        void bind(Student student) {
            tvStudentName.setText(student.getName());

            btnPresent.setOnClickListener(v -> {
                animateAndRemove(true);
            });

            btnAbsent.setOnClickListener(v -> {
                animateAndRemove(false);
            });
        }

        private void animateAndRemove(boolean isPresent) {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            Student student = students.get(position);

            // Disable buttons to prevent multiple clicks
            btnPresent.setEnabled(false);
            btnAbsent.setEnabled(false);

            // Animate the item sliding out
            itemView.animate()
                    .translationX(itemView.getWidth())
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Notify listener after animation
                            if (listener != null) {
                                listener.onAttendanceMarked(student, isPresent);
                            }
                            // Remove item from list
                            removeStudent(position);

                            // Reset the view for recycling
                            itemView.setTranslationX(0);
                            itemView.setAlpha(1.0f);
                        }
                    });
        }
    }
}

