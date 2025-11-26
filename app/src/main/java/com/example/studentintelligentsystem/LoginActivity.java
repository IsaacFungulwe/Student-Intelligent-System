package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentintelligentsystem.supabase.SupabaseAuthManager;
import com.example.studentintelligentsystem.supabase.SupabaseConfig;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Spinner spinnerRole;
    private EditText editLoginEmail, editLoginPassword;
    private Button btnLogin;
    private TextView tvRegisterPrompt;
    private DatabaseHelper dbHelper;
    private SupabaseAuthManager supabaseAuth;

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
        supabaseAuth = new SupabaseAuthManager();

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

        // Show loading message
        Toast.makeText(this, "Authenticating...", Toast.LENGTH_SHORT).show();

        // Try Supabase authentication first (for multi-device login)
        new Thread(() -> {
            boolean loginSuccess = false;
            int userId = -1;
            int userGrade = -1;
            String userName = "";

            // STEP 1: Try Supabase Authentication
            if (SupabaseConfig.isConfigured()) {
                Log.i(TAG, "Attempting Supabase authentication for " + role);
                JSONObject userData = supabaseAuth.login(email, password, role);

                if (userData != null) {
                    try {
                        userId = userData.getInt("id");

                        if (role.equals("Teacher") && userData.has("grade_assigned")) {
                            userGrade = userData.getInt("grade_assigned");
                        }

                        if (userData.has("name")) {
                            userName = userData.getString("name");
                        } else if (userData.has("school_name")) {
                            userName = userData.getString("school_name");
                        }

                        loginSuccess = true;
                        Log.i(TAG, "✓ Supabase authentication successful");

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Supabase response: " + e.getMessage());
                    }
                }
            }

            // STEP 2: Fallback to Local Authentication
            if (!loginSuccess) {
                Log.i(TAG, "Falling back to local authentication");
                Cursor loggedInUser = dbHelper.checkLogin(email, password, role);

                if (loggedInUser != null && loggedInUser.moveToFirst()) {
                    if (role.equals("Admin")) {
                        userId = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.ADMIN_ID));
                    } else if (role.equals("Teacher")) {
                        userId = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.TEACHER_ID));
                        userGrade = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.TEACHER_GRADE_ASSIGNED));
                    } else if (role.equals("Parent")) {
                        userId = loggedInUser.getInt(loggedInUser.getColumnIndexOrThrow(DatabaseHelper.PARENT_ID));
                    }

                    loginSuccess = true;
                    loggedInUser.close();
                    Log.i(TAG, "✓ Local authentication successful");
                }
            }

            // STEP 3: Handle Login Result
            final boolean finalLoginSuccess = loginSuccess;
            final int finalUserId = userId;
            final int finalUserGrade = userGrade;
            final String finalUserName = userName;

            runOnUiThread(() -> {
                if (finalLoginSuccess && finalUserId != -1) {
                    // Store session
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putInt(KEY_USER_ID, finalUserId);
                    editor.putString(KEY_USER_ROLE, role);
                    if (finalUserGrade != -1) {
                        editor.putInt(KEY_USER_GRADE, finalUserGrade);
                    }
                    editor.apply();

                    String welcomeMsg = "Login Successful! Welcome " + role;
                    if (!TextUtils.isEmpty(finalUserName)) {
                        welcomeMsg = "Welcome back, " + finalUserName + "!";
                    }
                    Toast.makeText(this, welcomeMsg, Toast.LENGTH_SHORT).show();

                    // Redirect to respective dashboard
                    Intent intent;
                    if (role.equals("Admin")) {
                        intent = new Intent(this, AdminDashboardActivity.class);
                    } else if (role.equals("Teacher")) {
                        intent = new Intent(this, TeacherDashboardActivity.class);
                    } else {
                        intent = new Intent(this, ParentDashboardActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Login Failed. Invalid credentials or role.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
