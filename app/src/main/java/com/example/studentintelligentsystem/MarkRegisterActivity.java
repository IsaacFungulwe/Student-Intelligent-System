package com.example.studentintelligentsystem;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;

public class MarkRegisterActivity extends AppCompatActivity {

    private RecyclerView rvStudentAttendance;
    private EditText editDate;
    private Button btnMark, btnViewAttendance;
    private DatabaseHelper db;
    private StudentAttendanceAdapter adapter;
    private List<Student> studentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_register);

        db = new DatabaseHelper(this);

        rvStudentAttendance = findViewById(R.id.rvStudentAttendance);
        editDate = findViewById(R.id.editDate);
        btnMark = findViewById(R.id.btnMark);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);

        rvStudentAttendance.setLayoutManager(new LinearLayoutManager(this));

        // Default date to today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        editDate.setText(today);

        loadStudents();

        btnMark.setOnClickListener(v -> markAttendance());

        btnViewAttendance.setOnClickListener(v -> {
            String date = editDate.getText().toString().trim();
            if (date.isEmpty()) {
                Toast.makeText(this, "Date cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ViewAttendanceActivity.class);
            intent.putExtra(ViewAttendanceActivity.EXTRA_DATE, date);
            startActivity(intent);
        });
    }

    private void loadStudents() {
        Cursor cursor = db.getAllStudents();
        if (cursor != null) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("stu_name");
            int gradeIndex = cursor.getColumnIndex("stu_grade");

            while (cursor.moveToNext()) {
                if (idIndex != -1 && nameIndex != -1 && gradeIndex != -1) {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    int grade = cursor.getInt(gradeIndex);
                    studentList.add(new Student(id, name, grade));
                }
            }
            cursor.close();
        }

        adapter = new StudentAttendanceAdapter(studentList);
        rvStudentAttendance.setAdapter(adapter);
    }

    private void markAttendance() {
        String date = editDate.getText().toString().trim();
        if (date.isEmpty()) {
            Toast.makeText(this, "Date cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<Long, Boolean> attendanceStatus = adapter.getAttendanceStatus();
        int successCount = 0;

        for (Map.Entry<Long, Boolean> entry : attendanceStatus.entrySet()) {
            long studentId = entry.getKey();
            boolean isPresent = entry.getValue();
            if (db.addAttendance((int) studentId, date, isPresent)) {
                successCount++;
            }
        }

        Toast.makeText(this, "Attendance marked for " + successCount + " students.", Toast.LENGTH_SHORT).show();
    }
}
