package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherWelcome;
    private CardView cardRegisterStudent, cardMarkAttendance, cardAddResults, cardManageSubjects, cardPostTeacherAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        tvTeacherWelcome = findViewById(R.id.tvTeacherWelcome);
        cardRegisterStudent = findViewById(R.id.cardRegisterStudent);
        cardMarkAttendance = findViewById(R.id.cardMarkAttendance);
        cardAddResults = findViewById(R.id.cardAddResults);
        cardManageSubjects = findViewById(R.id.cardManageSubjects);
        cardPostTeacherAnnouncement = findViewById(R.id.cardPostTeacherAnnouncement);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherGrade != -1) {
            tvTeacherWelcome.setText("Teacher Dashboard (Grade " + teacherGrade + ")");
        } else {
            tvTeacherWelcome.setText("Teacher Dashboard");
        }

        cardRegisterStudent.setOnClickListener(v -> startActivity(new Intent(this, StudentRegisterActivity.class)));
        cardMarkAttendance.setOnClickListener(v -> startActivity(new Intent(this, MarkAttendanceActivity.class)));
        cardAddResults.setOnClickListener(v -> startActivity(new Intent(this, AddResultsActivity.class)));
        cardManageSubjects.setOnClickListener(v -> startActivity(new Intent(this, ManageSubjectsActivity.class)));
        cardPostTeacherAnnouncement.setOnClickListener(v -> startActivity(new Intent(this, PostAnnouncementActivity.class)));
    }
}
