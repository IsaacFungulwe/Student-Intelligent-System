package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Spinner spinnerRole;
    private EditText editLoginEmail, editLoginPassword;
    private Button btnLogin;
    private TextView tvRegisterPrompt;
    private DatabaseHelper dbHelper;

    // SharedPreferences constants
    public static final String PREFS_NAME = "StudentIntelligentSystemPrefs";
    public static final String KEY_USER_ID = "loggedInUserId";
    public static final String KEY_USER_ROLE = "loggedInUserRole";
    public static final String KEY_USER_GRADE = "loggedInUserGrade"; // For Teachers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        spinnerRole = findViewById(R.id.spinnerRole);
        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginPassword = findViewById(R.id.editLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterPrompt = findViewById(R.id.tvRegisterPrompt);

        // Populate the role spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Set listeners
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegisterPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminRegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String role = spinnerRole.getSelectedItem().toString();
        String email = editLoginEmail.getText().toString().trim();
        String password = editLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor loggedInUser = dbHelper.checkLogin(email, password, role);

        if (loggedInUser != null && loggedInUser.moveToFirst()) {
            int userId = -1;
            int userGrade = -1; // Specific to teachers

            // --- Store Session --- 
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            
            if (role.equals("Admin")) {
                userId = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.ADMIN_ID));
            } else if (role.equals("Teacher")) {
                userId = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.TEACHER_ID));
                userGrade = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.TEACHER_GRADE_ASSIGNED));
            } else if (role.equals("Parent")) {
                userId = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.PARENT_ID));
            }
            
            editor.putInt(KEY_USER_ID, userId);
            editor.putString(KEY_USER_ROLE, role);
            if (userGrade != -1) {
                editor.putInt(KEY_USER_GRADE, userGrade);
            }
            editor.apply();

            loggedInUser.close();

            Toast.makeText(this, "Login Successful! Welcome " + role, Toast.LENGTH_SHORT).show();

            // --- Redirect to respective dashboard ---
            Intent intent;
            if (role.equals("Admin")) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else if (role.equals("Teacher")) {
                 intent = new Intent(this, TeacherDashboardActivity.class);
            } else {
                 intent = new Intent(this, ParentDashboardActivity.class);
            }
            startActivity(intent);
            finish(); // Prevents user from going back to login screen

        } else {
            Toast.makeText(this, "Login Failed. Invalid credentials or role.", Toast.LENGTH_LONG).show();
        }
    }
}
