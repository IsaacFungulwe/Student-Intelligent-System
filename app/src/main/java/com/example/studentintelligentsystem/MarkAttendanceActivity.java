package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarkAttendanceActivity extends AppCompatActivity implements StudentAttendanceAdapter.OnAttendanceMarkedListener {

    private RecyclerView rvStudents;
    private EditText editAttendanceDate;
    private DatabaseHelper dbHelper;
    private StudentAttendanceAdapter adapter;
    private List<Student> studentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        dbHelper = new DatabaseHelper(this);

        editAttendanceDate = findViewById(R.id.editAttendanceDate);
        rvStudents = findViewById(R.id.rvStudents);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        editAttendanceDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        loadStudentsForTeacher();
    }

    private void loadStudentsForTeacher() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherGrade == -1) {
            Toast.makeText(this, "Unable to load students for your grade", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_STUDENT,
                new String[]{DatabaseHelper.STUDENT_ID, DatabaseHelper.STUDENT_NAME, DatabaseHelper.STUDENT_GRADE},
                DatabaseHelper.STUDENT_GRADE + " = ?",
                new String[]{String.valueOf(teacherGrade)},
                null, null, DatabaseHelper.STUDENT_NAME + " ASC");

        studentList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_GRADE));

                Student student = new Student(id, name, grade);
                studentList.add(student);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        adapter = new StudentAttendanceAdapter(studentList, this);
        rvStudents.setAdapter(adapter);

        if (studentList.isEmpty()) {
            Toast.makeText(this, "No students found for your grade", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttendanceMarked(Student student, boolean isPresent) {
        String date = editAttendanceDate.getText().toString().trim();

        // Save attendance to database
        dbHelper.addAttendance((int) student.getId(), date, isPresent);

        String status = isPresent ? "Present" : "Absent";
        Toast.makeText(this, student.getName() + " marked as " + status, Toast.LENGTH_SHORT).show();

        // Check if all students have been marked
        if (studentList.isEmpty()) {
            Toast.makeText(this, "All students have been marked!", Toast.LENGTH_LONG).show();
        }
    }
}
