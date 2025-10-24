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
    private Button btnRegisterStudent, btnMarkAttendance, btnAddResults, btnManageSubjects, btnPostTeacherAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        tvTeacherWelcome = findViewById(R.id.tvTeacherWelcome);
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnAddResults = findViewById(R.id.btnAddResults);
        btnManageSubjects = findViewById(R.id.btnManageSubjects);
        btnPostTeacherAnnouncement = findViewById(R.id.btnPostTeacherAnnouncement);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherGrade != -1) {
            tvTeacherWelcome.setText("Teacher Dashboard (Grade " + teacherGrade + ")");
        } else {
            tvTeacherWelcome.setText("Teacher Dashboard");
        }

        btnRegisterStudent.setOnClickListener(v -> startActivity(new Intent(this, StudentRegisterActivity.class)));
        btnMarkAttendance.setOnClickListener(v -> startActivity(new Intent(this, MarkAttendanceActivity.class)));
        btnAddResults.setOnClickListener(v -> startActivity(new Intent(this, AddResultsActivity.class)));
        btnManageSubjects.setOnClickListener(v -> startActivity(new Intent(this, ManageSubjectsActivity.class)));
        btnPostTeacherAnnouncement.setOnClickListener(v -> startActivity(new Intent(this, PostAnnouncementActivity.class)));
    }
}
