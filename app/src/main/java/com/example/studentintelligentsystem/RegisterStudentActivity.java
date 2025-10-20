package com.example.studentintelligentsystem;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterStudentActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentGrade, etStudentAge, etParentName, etParentEmail, etParentPhone, etAddress;
    private Spinner spinnerGender;
    private Button btnRegisterStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentGrade = findViewById(R.id.etStudentGrade);
        etStudentAge = findViewById(R.id.etStudentAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        etParentName = findViewById(R.id.etParentName);
        etParentEmail = findViewById(R.id.etParentEmail);
        etParentPhone = findViewById(R.id.etParentPhone);
        etAddress = findViewById(R.id.etAddress);
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerGender.setAdapter(adapter);

        btnRegisterStudent.setOnClickListener(v -> {
            // Handle student registration logic here
        });
    }
}
