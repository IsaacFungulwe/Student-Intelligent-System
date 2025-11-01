package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private ListView lvAttendanceHistory;
    private TextView tvStudentName;
    private DatabaseHelper dbHelper;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);
        lvAttendanceHistory = findViewById(R.id.lvAttendanceHistory);
        tvStudentName = findViewById(R.id.tvStudentName);

        studentId = getIntent().getIntExtra("studentId", -1);

        if (studentId != -1) {
            loadStudentName();
            loadAttendanceHistory();
        } else {
            // Handle error - studentId not passed
        }
    }

    private void loadStudentName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT,
                new String[]{DatabaseHelper.STUDENT_NAME},
                DatabaseHelper.STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String studentName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
            tvStudentName.setText(studentName + "'s Attendance");
            getSupportActionBar().setTitle(studentName + "'s Attendance");
            cursor.close();
        }
        db.close();
    }

    private void loadAttendanceHistory() {
        ArrayList<String> attendanceList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ATTENDANCE,
                new String[]{DatabaseHelper.ATTENDANCE_DATE, DatabaseHelper.ATTENDANCE_STATUS},
                DatabaseHelper.ATTENDANCE_FK_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)},
                null, null, DatabaseHelper.ATTENDANCE_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ATTENDANCE_STATUS));
                attendanceList.add(date + " - " + status);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        if (attendanceList.isEmpty()) {
            attendanceList.add("No attendance records found.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        lvAttendanceHistory.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
