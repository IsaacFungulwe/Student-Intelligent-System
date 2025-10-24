package com.example.studentintelligentsystem;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardRegisterTeacher, cardRegisterParent, cardPostAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        cardRegisterTeacher = findViewById(R.id.cardRegisterTeacher);
        cardRegisterParent = findViewById(R.id.cardRegisterParent);
        cardPostAnnouncement = findViewById(R.id.cardPostAnnouncement);

        cardRegisterTeacher.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherRegisterActivity.class);
            startActivity(intent);
        });

        cardRegisterParent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentRegisterActivity.class);
            startActivity(intent);
        });

        cardPostAnnouncement.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostAnnouncementActivity.class);
            startActivity(intent);
        });
    }
}
