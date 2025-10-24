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

public class ParentRegisterActivity extends AppCompatActivity {

    private EditText editParentName, editParentEmail, editParentPhone, editParentPassword;
    private Button btnRegisterParent;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_register);

        dbHelper = new DatabaseHelper(this);

        editParentName = findViewById(R.id.editParentName);
        editParentEmail = findViewById(R.id.editParentEmail);
        editParentPhone = findViewById(R.id.editParentPhone);
        editParentPassword = findViewById(R.id.editParentPassword);
        btnRegisterParent = findViewById(R.id.btnRegisterParent);

        btnRegisterParent.setOnClickListener(v -> registerParent());
    }

    private void registerParent() {
        String name = editParentName.getText().toString().trim();
        String email = editParentEmail.getText().toString().trim();
        String phone = editParentPhone.getText().toString().trim();
        String password = editParentPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Name, email, and password are required.", Toast.LENGTH_SHORT).show();
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
        values.put(DatabaseHelper.PARENT_NAME, name);
        values.put(DatabaseHelper.PARENT_EMAIL, email);
        values.put(DatabaseHelper.PARENT_PHONE, phone);
        values.put(DatabaseHelper.PARENT_PASSWORD, DatabaseHelper.hashPassword(password));
        values.put(DatabaseHelper.PARENT_FK_ADMIN_ID, adminId);

        long newRowId = db.insert(DatabaseHelper.TABLE_PARENT, null, values);
        db.close();

        if (newRowId != -1) {
            Toast.makeText(this, "Parent registered successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to the admin dashboard
        } else {
            Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_LONG).show();
        }
    }
}
