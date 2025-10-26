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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ParentDashboardActivity extends AppCompatActivity {

    private TextView tvParentWelcome;
    private ListView lvMyChildren, lvAnnouncements;
    private DatabaseHelper dbHelper;
    private int parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        tvParentWelcome = findViewById(R.id.tvParentWelcome);
        lvMyChildren = findViewById(R.id.lvMyChildren);
        lvAnnouncements = findViewById(R.id.lvAnnouncements);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        parentId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (parentId == -1) {
            Toast.makeText(this, "Error: Could not verify your login. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadChildrenData();
        loadAnnouncements();
    }

    private void loadChildrenData() {
        Set<Integer> childrenGrades = new HashSet<>();
        ArrayList<String> childrenList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT, new String[]{DatabaseHelper.STUDENT_NAME, DatabaseHelper.STUDENT_GRADE}, DatabaseHelper.STUDENT_FK_PARENT_ID + " = ?", new String[]{String.valueOf(parentId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE));
                childrenList.add(name + " - Grade: " + grade);
                childrenGrades.add(grade);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (childrenList.isEmpty()) {
            childrenList.add("No children linked to this account yet.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, childrenList);
        lvMyChildren.setAdapter(adapter);

        SharedPreferences.Editor editor = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE).edit();
        Set<String> gradesStrSet = new HashSet<>();
        for (Integer grade : childrenGrades) {
            gradesStrSet.add(String.valueOf(grade));
        }
        editor.putStringSet("userChildrenGrades", gradesStrSet);
        editor.apply();
    }

    private void loadAnnouncements() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> childrenGrades = prefs.getStringSet("userChildrenGrades", new HashSet<>());

        ArrayList<String> announcementList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder(DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IS NULL");
        if (!childrenGrades.isEmpty()) {
            selection.append(" OR " + DatabaseHelper.ANNOUNCEMENT_GRADE_TARGET + " IN (");
            for (int i = 0; i < childrenGrades.size(); i++) {
                selection.append("?");
                if (i < childrenGrades.size() - 1) {
                    selection.append(",");
                }
            }
            selection.append(")");
        }

        String[] selectionArgs = childrenGrades.toArray(new String[0]);

        Cursor cursor = db.query(DatabaseHelper.TABLE_ANNOUNCEMENT, new String[]{DatabaseHelper.ANNOUNCEMENT_TITLE, DatabaseHelper.ANNOUNCEMENT_MESSAGE, DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL}, selection.toString(), selectionArgs, null, null, DatabaseHelper.ANNOUNCEMENT_TIMESTAMP + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_MESSAGE));
                String source = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ANNOUNCEMENT_SOURCE_LABEL));
                announcementList.add("[" + source + "] " + title + "\n" + message);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (announcementList.isEmpty()) {
            announcementList.add("No announcements available right now.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
        lvAnnouncements.setAdapter(adapter);
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
