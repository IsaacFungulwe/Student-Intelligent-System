package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MarkRegisterActivity extends AppCompatActivity {

    private Spinner spinnerStudents;
    private EditText editDate;
    private CheckBox cbPresent;
    private Button btnMark;
    private DatabaseHelper db;

    // Map to hold student name -> ID
    private HashMap<String, Integer> studentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_register);

        db = new DatabaseHelper(this);

        spinnerStudents = findViewById(R.id.spinnerStudents);
        editDate = findViewById(R.id.editDate);
        cbPresent = findViewById(R.id.cbPresent);
        btnMark = findViewById(R.id.btnMark);

        // Default date to today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        editDate.setText(today);

        loadStudentsIntoSpinner();

        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedStudent = spinnerStudents.getSelectedItem().toString();
                Integer studentId = studentMap.get(selectedStudent);
                String date = editDate.getText().toString().trim();
                boolean present = cbPresent.isChecked();

                if (studentId == null) {
                    Toast.makeText(MarkRegisterActivity.this, "No student selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (date.isEmpty()) {
                    Toast.makeText(MarkRegisterActivity.this, "Date cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ok = db.addAttendance(studentId, date, present);
                if (ok) {
                    Toast.makeText(MarkRegisterActivity.this, "Attendance marked for " + selectedStudent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MarkRegisterActivity.this, "Error marking attendance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadStudentsIntoSpinner() {
        Cursor cursor = db.getAllStudents();
        ArrayList<String> studentNames = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("stu_name"));
                studentNames.add(name);
                studentMap.put(name, id);
            }
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(adapter);
    }
}
