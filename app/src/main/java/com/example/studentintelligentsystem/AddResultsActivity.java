package com.example.studentintelligentsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddResultsActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerStudentsResults, spinnerSubjects;
    private EditText editTerm, editMarks, editComment;
    private Button btnSubmitResults;
    private DatabaseHelper dbHelper;

    private HashMap<String, Integer> studentMap = new HashMap<>();
    private int selectedStudentId = -1;
    private String selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_results);

        dbHelper = new DatabaseHelper(this);

        spinnerStudentsResults = findViewById(R.id.spinnerStudentsResults);
        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        editTerm = findViewById(R.id.editTerm);
        editMarks = findViewById(R.id.editMarks);
        editComment = findViewById(R.id.editComment);
        btnSubmitResults = findViewById(R.id.btnSubmitResults);

        loadStudentsForTeacher();
        loadSubjectsForTeacher();

        spinnerStudentsResults.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedStudentId = studentMap.get(selectedName);
        });

        spinnerSubjects.setOnItemClickListener((parent, view, position, id) -> {
            selectedSubject = (String) parent.getItemAtPosition(position);
        });

        btnSubmitResults.setOnClickListener(v -> addResults());
    }

    private void loadStudentsForTeacher() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);
        if (teacherGrade == -1) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT, new String[]{DatabaseHelper.STUDENT_ID, DatabaseHelper.STUDENT_NAME}, DatabaseHelper.STUDENT_GRADE + " = ?", new String[]{String.valueOf(teacherGrade)}, null, null, null);
        ArrayList<String> studentNames = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                studentMap.put(name, id);
                studentNames.add(name);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, studentNames);
        spinnerStudentsResults.setAdapter(adapter);
    }

    private void loadSubjectsForTeacher() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);
        if (teacherGrade == -1) return;

        List<String> subjects = dbHelper.getSubjectsByGrade(teacherGrade);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        spinnerSubjects.setAdapter(adapter);
    }

    private void addResults() {
        String term = editTerm.getText().toString().trim();
        String marksStr = editMarks.getText().toString().trim();
        String comment = editComment.getText().toString().trim();

        if (selectedStudentId == -1 || TextUtils.isEmpty(selectedSubject) || TextUtils.isEmpty(term) || TextUtils.isEmpty(marksStr)) {
            Toast.makeText(this, "Please select a student, subject, and fill all result fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int marks = Integer.parseInt(marksStr);
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.RESULT_FK_STUDENT_ID, selectedStudentId);
        values.put(DatabaseHelper.RESULT_SUBJECT, selectedSubject);
        values.put(DatabaseHelper.RESULT_TERM, term);
        values.put(DatabaseHelper.RESULT_MARKS, marks);
        values.put(DatabaseHelper.RESULT_COMMENT, comment);
        values.put(DatabaseHelper.RESULT_FK_TEACHER_ID, teacherId);
        long newRowId = db.insert(DatabaseHelper.TABLE_RESULTS, null, values);
        db.close();

        if (newRowId != -1) {
            Toast.makeText(this, "Results added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error adding results.", Toast.LENGTH_LONG).show();
        }
    }
}
