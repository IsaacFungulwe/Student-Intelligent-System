package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminRegisterActivity extends AppCompatActivity {

    private EditText editSchoolName, editDistrict, editSchoolEmail, editAdminPassword;
    private Button btnRegisterAdmin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        dbHelper = new DatabaseHelper(this);

        editSchoolName = findViewById(R.id.editSchoolName);
        editDistrict = findViewById(R.id.editDistrict);
        editSchoolEmail = findViewById(R.id.editSchoolEmail);
        editAdminPassword = findViewById(R.id.editAdminPassword);
        btnRegisterAdmin = findViewById(R.id.btnRegisterAdmin);

        btnRegisterAdmin.setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String schoolName = editSchoolName.getText().toString().trim();
        String district = editDistrict.getText().toString().trim();
        String email = editSchoolEmail.getText().toString().trim();
        String password = editAdminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(schoolName) || TextUtils.isEmpty(district) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        long adminId = addAdmin(schoolName, district, email, password);

        if (adminId != -1) {
            Toast.makeText(this, "School registered successfully!", Toast.LENGTH_LONG).show();
            finish(); // Go back to the previous screen
        } else {
            Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_LONG).show();
        }
    }

    private long addAdmin(String schoolName, String district, String email, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.ADMIN_SCHOOL_NAME, schoolName);
        values.put(DatabaseHelper.ADMIN_DISTRICT, district);
        values.put(DatabaseHelper.ADMIN_EMAIL, email);
        values.put(DatabaseHelper.ADMIN_PASSWORD, DatabaseHelper.hashPassword(password)); // Hash the password

        // Inserting Row - db.insert() returns the new row ID, or -1 if an error occurred.
        long newRowId = db.insert(DatabaseHelper.TABLE_ADMIN, null, values);
        db.close();
        return newRowId;
    }
}
