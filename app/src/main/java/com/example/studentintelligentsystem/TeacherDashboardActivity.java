package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherWelcome;
    private Button btnRegisterStudent, btnMarkAttendance, btnAddResults, btnPostTeacherAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        tvTeacherWelcome = findViewById(R.id.tvTeacherWelcome);
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnAddResults = findViewById(R.id.btnAddResults);
        btnPostTeacherAnnouncement = findViewById(R.id.btnPostTeacherAnnouncement);

        // Get the logged-in teacher's grade from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherGrade != -1) {
            tvTeacherWelcome.setText("Teacher Dashboard (Grade " + teacherGrade + ")");
        } else {
            tvTeacherWelcome.setText("Teacher Dashboard");
        }

        btnRegisterStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentRegisterActivity.class);
            startActivity(intent);
        });

        btnMarkAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(this, MarkAttendanceActivity.class);
            startActivity(intent);
        });

        btnAddResults.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddResultsActivity.class);
            startActivity(intent);
        });

        btnPostTeacherAnnouncement.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostAnnouncementActivity.class);
            startActivity(intent);
        });
    }
}
