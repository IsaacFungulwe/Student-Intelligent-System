package com.example.studentintelligentsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterStudentActivity extends AppCompatActivity {

    // UI Components
    private EditText editStudentName, editStudentAge, editStudentGrade, editStudentGender, editStudentAddress;
    private EditText editParentName, editParentEmail, editParentPhone;
    private Button btnRegister;
    private LinearLayout layoutGeneratedId;
    private TextView tvGeneratedId;

    // Database Helper
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        // Link UI components
        initializeViews();

        // Set click listener for the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerStudent();
            }
        });
    }

    private void initializeViews() {
        // Student fields
        editStudentName = findViewById(R.id.editStudentName);
        editStudentAge = findViewById(R.id.editStudentAge);
        editStudentGrade = findViewById(R.id.editStudentGrade);
        editStudentGender = findViewById(R.id.editStudentGender);
        editStudentAddress = findViewById(R.id.editStudentAddress);

        // Parent fields
        editParentName = findViewById(R.id.editParentName);
        editParentEmail = findViewById(R.id.editParentEmail);
        editParentPhone = findViewById(R.id.editParentPhone);

        // Action button and result display
        btnRegister = findViewById(R.id.btnRegister);
        layoutGeneratedId = findViewById(R.id.layoutGeneratedId);
        tvGeneratedId = findViewById(R.id.tvGeneratedId);
    }

    private void registerStudent() {
        // --- 1. Get Input from EditText fields ---
        String name = editStudentName.getText().toString().trim();
        String ageStr = editStudentAge.getText().toString().trim();
        String gradeStr = editStudentGrade.getText().toString().trim();
        String gender = editStudentGender.getText().toString().trim();
        String address = editStudentAddress.getText().toString().trim();
        String parentName = editParentName.getText().toString().trim();
        String parentEmail = editParentEmail.getText().toString().trim();
        String parentPhone = editParentPhone.getText().toString().trim();

        // --- 2. Validate Input ---
        if (name.isEmpty() || ageStr.isEmpty() || gradeStr.isEmpty() || parentName.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields (Name, Age, Grade, Parent Name).", Toast.LENGTH_LONG).show();
            return;
        }

        int age;
        int grade;
        try {
            age = Integer.parseInt(ageStr);
            grade = Integer.parseInt(gradeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for Age and Grade.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 3. Create Student Object ---
        // Note: The Student constructor doesn't need an ID, as it will be auto-generated.
        Student newStudent = new Student(name, grade, age, gender, parentName, parentEmail, parentPhone, address);

        // --- 4. Save to Database and Get the New ID ---
        // Use the new method that returns the generated ID
        long newStudentId = dbHelper.addStudentAndGetId(newStudent);

        // --- 5. Update UI with the result ---
        if (newStudentId != -1) {
            // Success! Display the new ID.
            Toast.makeText(this, "Student registered successfully!", Toast.LENGTH_SHORT).show();

            // Update the TextView and make the layout visible
            tvGeneratedId.setText("The new Student ID is: " + newStudentId);
            layoutGeneratedId.setVisibility(View.VISIBLE);

            // Optional: Clear all input fields for the next entry
            clearForm();

        } else {
            // Failure
            Toast.makeText(this, "Error: Could not register student.", Toast.LENGTH_LONG).show();
            // Ensure the generated ID layout is hidden on failure
            layoutGeneratedId.setVisibility(View.GONE);
        }
    }

    private void clearForm() {
        editStudentName.setText("");
        editStudentAge.setText("");
        editStudentGrade.setText("");
        editStudentGender.setText("");
        editStudentAddress.setText("");
        editParentName.setText("");
        editParentEmail.setText("");
        editParentPhone.setText("");
        editStudentName.requestFocus(); // Set focus back to the first field
    }
}
