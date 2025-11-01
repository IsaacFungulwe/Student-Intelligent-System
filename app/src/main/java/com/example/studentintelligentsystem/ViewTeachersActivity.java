package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class ViewTeachersActivity extends AppCompatActivity {

    private ListView lvTeachers;
    private DatabaseHelper dbHelper;
    private int adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_teachers);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Registered Teachers");

        dbHelper = new DatabaseHelper(this);
        lvTeachers = findViewById(R.id.lvTeachers);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        adminId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (adminId == -1) {
            Toast.makeText(this, "Error: Could not verify your login.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadTeachers();
    }

    private void loadTeachers() {
        List<String> teacherList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_TEACHER, new String[]{DatabaseHelper.TEACHER_NAME, DatabaseHelper.TEACHER_EMAIL, DatabaseHelper.TEACHER_GRADE_ASSIGNED},
                DatabaseHelper.TEACHER_FK_ADMIN_ID + " = ?", new String[]{String.valueOf(adminId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_EMAIL));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEACHER_GRADE_ASSIGNED));
                teacherList.add(name + "\n" + email + "\nGrade: " + grade);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (teacherList.isEmpty()) {
            teacherList.add("No teachers registered yet.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teacherList);
        lvTeachers.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
