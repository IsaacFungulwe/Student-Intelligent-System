package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherWelcome;
    private CardView cardRegisterStudent, cardMarkAttendance, cardAddResults, cardManageSubjects,
            cardPostTeacherAnnouncement, cardManageAnnouncements, cardViewParents, cardViewEditResults;
    private ListView lvTeacherAnnouncements;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        tvTeacherWelcome = findViewById(R.id.tvTeacherWelcome);
        cardRegisterStudent = findViewById(R.id.cardRegisterStudent);
        cardMarkAttendance = findViewById(R.id.cardMarkAttendance);
        cardAddResults = findViewById(R.id.cardAddResults);
        cardManageSubjects = findViewById(R.id.cardManageSubjects);
        cardPostTeacherAnnouncement = findViewById(R.id.cardPostTeacherAnnouncement);
        cardManageAnnouncements = findViewById(R.id.cardManageAnnouncements);
        cardViewParents = findViewById(R.id.cardViewParents);
        cardViewEditResults = findViewById(R.id.cardViewEditResults);
        lvTeacherAnnouncements = findViewById(R.id.lvTeacherAnnouncements);

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
        cardManageAnnouncements.setOnClickListener(v -> startActivity(new Intent(this, ManageAnnouncementsActivity.class)));
        cardViewParents.setOnClickListener(v -> startActivity(new Intent(this, ViewParentsActivity.class)));
        cardViewEditResults.setOnClickListener(v -> startActivity(new Intent(this, ViewEditResultsActivity.class)));

        loadAnnouncements(teacherGrade);
    }

    private void loadAnnouncements(int teacherGrade) {
        ArrayList<String> announcementList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " = ? OR " + DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IS NULL";
        String[] selectionArgs = {String.valueOf(teacherGrade)};

        Cursor cursor = db.query(DatabaseHelper.TABLE_ANNOUNCEMENT, new String[]{DatabaseHelper.ANNOUNCEMENT_TITLE, DatabaseHelper.ANNOUNCEMENT_MESSAGE, DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL, DatabaseHelper.ANNOUNCEMENT_TIMESTAMP},
                selection, selectionArgs, null, null, DatabaseHelper.ANNOUNCEMENT_TIMESTAMP + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_MESSAGE));
                String source = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TIMESTAMP));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                try {
                    Date date = sdf.parse(timestamp);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
                    timestamp = outputFormat.format(date);
                } catch (Exception e) {
                    // Keep raw timestamp if parsing fails
                }

                announcementList.add("[" + source + "] " + title + "\n" + message + "\n" + timestamp);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (announcementList.isEmpty()) {
            announcementList.add("No announcements available right now.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
        lvTeacherAnnouncements.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
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
