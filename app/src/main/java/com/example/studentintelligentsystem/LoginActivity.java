package com.example.studentintelligentsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Spinner spinnerRole;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonSignup;

    private DatabaseHelper dbHelper;
    private static final int MAX_ATTEMPTS = 3;
    private int loginAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        spinnerRole = findViewById(R.id.spinnerRole);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignup = findViewById(R.id.buttonSignup);

        dbHelper = new DatabaseHelper(this);

        buttonLogin.setOnClickListener(v -> handleLogin());
        buttonSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String role = spinnerRole.getSelectedItem().toString();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (role.equals("Select Role")) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (role.equals("Admin")) {
            // Admin has fixed credentials
            if (email.equals("admin@gmail.com") && password.equals("admin123")) {
                saveSession("admin");
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                showError();
            }
        } else if (role.equals("Parent")) {
            // Check parent in DB
            if (dbHelper.validateParent(email, password)) {
                saveSession("parent");
                startActivity(new Intent(this, UserActivity.class));
                finish();
            } else {
                showError();
            }
        }
    }

    private void showError() {
        loginAttempts++;
        if (loginAttempts >= MAX_ATTEMPTS) {
            buttonLogin.setEnabled(false);
            Toast.makeText(this, "Too many failed attempts!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Invalid credentials. Attempts left: " + (MAX_ATTEMPTS - loginAttempts), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSession(String role) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userRole", role)
                .apply();
    }
}
