package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherRegisterActivity extends AppCompatActivity {

    private EditText editTeacherName, editTeacherEmail, editTeacherPassword, editGradeAssigned;
    private Button btnRegisterTeacher;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_register);

        dbHelper = new DatabaseHelper(this);

        editTeacherName = findViewById(R.id.editTeacherName);
        editTeacherEmail = findViewById(R.id.editTeacherEmail);
        editTeacherPassword = findViewById(R.id.editTeacherPassword);
        editGradeAssigned = findViewById(R.id.editGradeAssigned);
        btnRegisterTeacher = findViewById(R.id.btnRegisterTeacher);

        btnRegisterTeacher.setOnClickListener(v -> registerTeacher());
    }

    private void registerTeacher() {
        String name = editTeacherName.getText().toString().trim();
        String email = editTeacherEmail.getText().toString().trim();
        String password = editTeacherPassword.getText().toString().trim();
        String gradeStr = editGradeAssigned.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(gradeStr)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the logged-in Admin's ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int adminId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (adminId == -1) {
            Toast.makeText(this, "Error: Could not verify Admin. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TEACHER_NAME, name);
        values.put(DatabaseHelper.TEACHER_EMAIL, email);
        values.put(DatabaseHelper.TEACHER_PASSWORD, DatabaseHelper.hashPassword(password));
        values.put(DatabaseHelper.TEACHER_GRADE_ASSIGNED, Integer.parseInt(gradeStr));
        values.put(DatabaseHelper.TEACHER_FK_ADMIN_ID, adminId);

        long newRowId = db.insert(DatabaseHelper.TABLE_TEACHER, null, values);
        db.close();

        if (newRowId != -1) {
            Toast.makeText(this, "Teacher registered successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity and return to the admin dashboard
        } else {
            Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_LONG).show();
        }
    }
}
