package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PostAnnouncementActivity extends AppCompatActivity {

    private TextView tvPostAnnouncementTitle;
    private EditText editAnnouncementTitle, editAnnouncementMessage;
    private Button btnSubmitAnnouncement;
    private DatabaseHelper dbHelper;

    private String userRole;
    private int userId;
    private int userGrade; // Only relevant for teachers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_announcement);

        dbHelper = new DatabaseHelper(this);

        tvPostAnnouncementTitle = findViewById(R.id.tvPostAnnouncementTitle);
        editAnnouncementTitle = findViewById(R.id.editAnnouncementTitle);
        editAnnouncementMessage = findViewById(R.id.editAnnouncementMessage);
        btnSubmitAnnouncement = findViewById(R.id.btnSubmitAnnouncement);

        // Get user info from session
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        userGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if ("Teacher".equals(userRole) && userGrade != -1) {
            tvPostAnnouncementTitle.setText("Post to Grade " + userGrade + " Parents");
        } else {
            tvPostAnnouncementTitle.setText("Post School-Wide Announcement");
        }

        btnSubmitAnnouncement.setOnClickListener(v -> postAnnouncement());
    }

    private void postAnnouncement() {
        String title = editAnnouncementTitle.getText().toString().trim();
        String message = editAnnouncementMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Title and message cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == -1 || TextUtils.isEmpty(userRole)) {
            Toast.makeText(this, "Error: Could not verify login. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ANNOUNCEMENT_TITLE, title);
        values.put(DatabaseHelper.ANNOUNCEMENT_MESSAGE, message);
        values.put(DatabaseHelper.ANNOUNCEMENT_ROLE, userRole);
        values.put(DatabaseHelper.ANNOUNCEMENT_FK_CREATOR_ID, userId);

        if ("Teacher".equals(userRole)) {
            values.put(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET, userGrade);
            values.put(DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL, "Class Teacher"); // Set source for Teacher
        } else {
            values.putNull(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET);
            values.put(DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL, "School"); // Set source for Admin
        }

        long newRowId = db.insert(DatabaseHelper.TABLE_ANNOUNCEMENT, null, values);
        db.close();

        if (newRowId != -1) {
            Toast.makeText(this, "Announcement posted successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error posting announcement.", Toast.LENGTH_LONG).show();
        }
    }
}
