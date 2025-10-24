package com.example.studentintelligentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnRegisterTeacher, btnRegisterParent, btnPostAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnRegisterTeacher = findViewById(R.id.btnRegisterTeacher);
        btnRegisterParent = findViewById(R.id.btnRegisterParent);
        btnPostAnnouncement = findViewById(R.id.btnPostAnnouncement);

        btnRegisterTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherRegisterActivity.class);
            startActivity(intent);
        });

        btnRegisterParent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentRegisterActivity.class);
            startActivity(intent);
        });

        btnPostAnnouncement.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostAnnouncementActivity.class);
            startActivity(intent);
        });
    }
}
