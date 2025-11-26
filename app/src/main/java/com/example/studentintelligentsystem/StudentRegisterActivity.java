package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentintelligentsystem.supabase.SupabaseSyncManager;
import com.example.studentintelligentsystem.supabase.SupabaseConfig;

public class StudentRegisterActivity extends AppCompatActivity {
    private static final String TAG = "StudentRegisterActivity";

    private EditText editStudentName, editStudentAge, editStudentGender, editStudentAddress, editParentEmailToLink;
    private Button btnRegisterStudent;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        dbHelper = new DatabaseHelper(this);

        editStudentName = findViewById(R.id.editStudentName);
        editStudentAge = findViewById(R.id.editStudentAge);
        editStudentGender = findViewById(R.id.editStudentGender);
        editStudentAddress = findViewById(R.id.editStudentAddress);
        editParentEmailToLink = findViewById(R.id.editParentEmailToLink);
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent);

        btnRegisterStudent.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String studentName = editStudentName.getText().toString().trim();
        String ageStr = editStudentAge.getText().toString().trim();
        String gender = editStudentGender.getText().toString().trim();
        String address = editStudentAddress.getText().toString().trim();
        String parentEmail = editParentEmailToLink.getText().toString().trim();

        if (TextUtils.isEmpty(studentName) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(parentEmail)) {
            Toast.makeText(this, "Student Name, Age, and Parent Email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Get Teacher and Parent IDs ---
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);
        long parentId = getParentIdByEmail(parentEmail);

        if (teacherId == -1 || teacherGrade == -1) {
            Toast.makeText(this, "Error: Could not verify logged-in teacher. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (parentId == -1) {
            Toast.makeText(this, "Error: No parent found with that email address. Please register the parent first.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- Insert Student into Database ---
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.STUDENT_NAME, studentName);
        values.put(DatabaseHelper.STUDENT_AGE, Integer.parseInt(ageStr));
        values.put(DatabaseHelper.STUDENT_GENDER, gender);
        values.put(DatabaseHelper.STUDENT_ADDRESS, address);
        values.put(DatabaseHelper.STUDENT_GRADE, teacherGrade); // Automatically assign teacher's grade
        values.put(DatabaseHelper.STUDENT_FK_TEACHER_ID, teacherId); // Automatically link to this teacher
        values.put(DatabaseHelper.STUDENT_FK_PARENT_ID, parentId); // Link to the found parent

        long newRowId = db.insert(DatabaseHelper.TABLE_STUDENT, null, values);
        db.close();

        if (newRowId != -1) {
            Log.i(TAG, "✓ Student registered with ID: " + newRowId);
            Toast.makeText(this, "Student registered and linked successfully!", Toast.LENGTH_SHORT).show();

            // Sync to Supabase if configured
            if (SupabaseConfig.isConfigured()) {
                try {
                    SupabaseSyncManager syncManager = SupabaseSyncManager.getInstance(this);
                    Log.d(TAG, "Syncing student " + newRowId + " to Supabase...");
                    syncManager.syncStudent((int) newRowId);
                    Log.i(TAG, "✓ Student sync initiated to Supabase");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to sync student to Supabase: " + e.getMessage());
                }
            }

            finish(); // Go back to the teacher dashboard
        } else {
            Toast.makeText(this, "Error registering student.", Toast.LENGTH_LONG).show();
        }
    }

    private long getParentIdByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PARENT,
                new String[]{DatabaseHelper.PARENT_ID},
                DatabaseHelper.PARENT_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            long parentId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_ID));
            cursor.close();
            db.close();
            return parentId;
        } else {
            if (cursor != null) cursor.close();
            db.close();
            return -1; // Parent not found
        }
    }
}
