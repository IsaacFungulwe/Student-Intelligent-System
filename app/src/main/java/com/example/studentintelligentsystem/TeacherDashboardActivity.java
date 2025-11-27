package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherWelcome;
    private CardView cardRegisterStudent, cardMarkAttendance, cardAddResults, cardManageSubjects, cardPostTeacherAnnouncement, cardManageAnnouncements, cardViewParents;
    private ListView lvTeacherAnnouncements;
    private DatabaseHelper dbHelper;
    private AnnouncementLoader announcementLoader;
    private int teacherGrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        announcementLoader = new AnnouncementLoader(this);

        tvTeacherWelcome = findViewById(R.id.tvTeacherWelcome);
        cardRegisterStudent = findViewById(R.id.cardRegisterStudent);
        cardMarkAttendance = findViewById(R.id.cardMarkAttendance);
        cardAddResults = findViewById(R.id.cardAddResults);
        cardManageSubjects = findViewById(R.id.cardManageSubjects);
        cardPostTeacherAnnouncement = findViewById(R.id.cardPostTeacherAnnouncement);
        cardManageAnnouncements = findViewById(R.id.cardManageAnnouncements);
        cardViewParents = findViewById(R.id.cardViewParents);
        lvTeacherAnnouncements = findViewById(R.id.lvTeacherAnnouncements);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

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
        cardManageAnnouncements.setOnClickListener(v -> startActivity(new Intent(this, ManageAnnouncementsActivity.class)));
        cardViewParents.setOnClickListener(v -> startActivity(new Intent(this, ViewParentsActivity.class)));

        loadAnnouncements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        if (teacherGrade == -1) {
            ArrayList<String> errorList = new ArrayList<>();
            errorList.add("Error: Could not load grade information");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, errorList);
            lvTeacherAnnouncements.setAdapter(adapter);
            return;
        }

        ArrayList<String> announcementList = (ArrayList<String>) announcementLoader.loadTeacherAnnouncements(teacherGrade);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
        lvTeacherAnnouncements.setAdapter(adapter);
    }

    private void syncAnnouncements() {
        Toast.makeText(this, "Syncing announcements...", Toast.LENGTH_SHORT).show();

        announcementLoader.syncAnnouncementsFromSupabase((success, message) -> {
            runOnUiThread(() -> {
                if (success) {
                    loadAnnouncements();
                    Toast.makeText(this, "Announcements synced", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Sync error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_teacher_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            syncAnnouncements();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Overriding to do nothing and stay on the dashboard
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
