package com.example.studentintelligentsystem;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editStudentName, editStudentGrade, editStudentAge, editStudentGender,
            editParentName, editParentEmail, editParentPhone, editStudentAddress;
    private Button btnAddStudent;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        // Student info
        editStudentName = findViewById(R.id.editStudentName);
        editStudentGrade = findViewById(R.id.editStudentGrade);
        editStudentAge = findViewById(R.id.editStudentAge);
        editStudentGender = findViewById(R.id.editStudentGender);
        editParentName = findViewById(R.id.editParentName);
        editParentEmail = findViewById(R.id.editParentEmail);
        editParentPhone = findViewById(R.id.editParentPhone);
        editStudentAddress = findViewById(R.id.editStudentAddress);

        btnAddStudent = findViewById(R.id.btnAddStudent);

        btnAddStudent.setOnClickListener(v -> {
            String name = editStudentName.getText().toString().trim();
            String gradeStr = editStudentGrade.getText().toString().trim();
            String ageStr = editStudentAge.getText().toString().trim();
            String gender = editStudentGender.getText().toString().trim();
            String parentName = editParentName.getText().toString().trim();
            String parentEmail = editParentEmail.getText().toString().trim();
            String parentPhone = editParentPhone.getText().toString().trim();
            String address = editStudentAddress.getText().toString().trim();

            if (name.isEmpty() || gradeStr.isEmpty() || ageStr.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int grade = Integer.parseInt(gradeStr);
                int age = Integer.parseInt(ageStr);

                // Create Student object
                Student student = new Student(name, grade, age, gender, parentName, parentEmail, parentPhone, address);

                // Add student to database and check success
                long newId = db.addStudentAndGetId(student);
                if (newId != -1) {
                    Toast.makeText(this, "Student added successfully! ID: " + newId, Toast.LENGTH_SHORT).show();

                    // Clear all fields
                    editStudentName.setText("");
                    editStudentGrade.setText("");
                    editStudentAge.setText("");
                    editStudentGender.setText("");
                    editParentName.setText("");
                    editParentEmail.setText("");
                    editParentPhone.setText("");
                    editStudentAddress.setText("");
                } else {
                    Toast.makeText(this, "Error adding student", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Grade and Age must be numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
