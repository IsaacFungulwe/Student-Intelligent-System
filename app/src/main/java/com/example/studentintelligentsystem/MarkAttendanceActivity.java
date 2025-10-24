package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MarkAttendanceActivity extends AppCompatActivity {

    private Spinner spinnerStudents;
    private EditText editAttendanceDate;
    private RadioGroup rgAttendanceStatus;
    private Button btnSubmitAttendance;
    private DatabaseHelper dbHelper;

    private HashMap<String, Integer> studentMap = new HashMap<>();
    private int selectedStudentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        dbHelper = new DatabaseHelper(this);

        spinnerStudents = findViewById(R.id.spinnerStudents);
        editAttendanceDate = findViewById(R.id.editAttendanceDate);
        rgAttendanceStatus = findViewById(R.id.rgAttendanceStatus);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);

        // Set current date as default
        editAttendanceDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        loadStudentsForTeacher();

        spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = parent.getItemAtPosition(position).toString();
                selectedStudentId = studentMap.get(selectedName);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStudentId = -1;
            }
        });

        btnSubmitAttendance.setOnClickListener(v -> markAttendance());
    }

    private void loadStudentsForTeacher() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherGrade == -1) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_STUDENT,
                new String[]{DatabaseHelper.STUDENT_ID, DatabaseHelper.STUDENT_NAME},
                DatabaseHelper.STUDENT_GRADE + " = ?",
                new String[]{String.valueOf(teacherGrade)},
                null, null, null
        );

        ArrayList<String> studentNames = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                studentMap.put(name, id);
                studentNames.add(name);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(adapter);
    }

    private void markAttendance() {
        String date = editAttendanceDate.getText().toString().trim();
        int selectedStatusId = rgAttendanceStatus.getCheckedRadioButtonId();

        if (selectedStudentId == -1 || TextUtils.isEmpty(date) || selectedStatusId == -1) {
            Toast.makeText(this, "Please select a student, date, and status.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedStatusId);
        String status = selectedRadioButton.getText().toString(); // "Present" or "Absent"

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        dbHelper.addAttendance(selectedStudentId, date, status.equals("Present"));

        Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
