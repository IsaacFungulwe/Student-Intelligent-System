package com.example.studentintelligentsystem;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editStudentName, editStudentGrade;
    private Button btnAddStudent;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        editStudentName = findViewById(R.id.editStudentName);
        editStudentGrade = findViewById(R.id.editStudentGrade);
        btnAddStudent = findViewById(R.id.btnAddStudent);

        btnAddStudent.setOnClickListener(v -> {
            String name = editStudentName.getText().toString().trim();
            String gradeStr = editStudentGrade.getText().toString().trim();

            if (name.isEmpty() || gradeStr.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int grade = Integer.parseInt(gradeStr);
                boolean success = db.addStudent(name, grade);
                if (success) {
                    Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
                    editStudentName.setText("");
                    editStudentGrade.setText("");
                } else {
                    Toast.makeText(this, "Error adding student", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Grade must be a number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
