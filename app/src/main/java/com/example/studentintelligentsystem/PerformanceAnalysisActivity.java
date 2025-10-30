package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PerformanceAnalysisActivity extends AppCompatActivity {

    private Spinner spinnerStudents;
    private Button btnAnalyze;
    private TextView tvAnalysisResult;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private GeminiAIService geminiService;

    private HashMap<String, Integer> studentMap = new HashMap<>();
    private int selectedStudentId = -1;
    private String selectedStudentName = "";

    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("AI Performance Analysis");

        dbHelper = new DatabaseHelper(this);
        geminiService = new GeminiAIService();
        executor = Executors.newSingleThreadExecutor();

        spinnerStudents = findViewById(R.id.spinnerStudents);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        tvAnalysisResult = findViewById(R.id.tvAnalysisResult);
        progressBar = findViewById(R.id.progressBar);

        loadStudents();

        spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStudentName = (String) parent.getItemAtPosition(position);
                selectedStudentId = studentMap.get(selectedStudentName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnAnalyze.setOnClickListener(v -> performAnalysis());
    }

    private void loadStudents() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int parentId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (parentId == -1) {
            Toast.makeText(this, "Error: Could not verify your login.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = dbHelper.getStudentsByParentId(parentId);
        List<String> studentNames = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                studentMap.put(name, id);
                studentNames.add(name);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (studentNames.isEmpty()) {
            Toast.makeText(this, "No students found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(adapter);
    }

    private void performAnalysis() {
        if (selectedStudentId == -1) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnAnalyze.setEnabled(false);
        tvAnalysisResult.setText("Analyzing student performance with AI...");

        // Get student data
        Cursor resultsCursor = dbHelper.getResultsForStudent(selectedStudentId);
        Map<String, Integer> grades = GeminiAIService.extractGradesFromCursor(resultsCursor, dbHelper);

        // Reset cursor for comments extraction
        resultsCursor = dbHelper.getResultsForStudent(selectedStudentId);
        String teacherComments = GeminiAIService.extractCommentsFromCursor(resultsCursor, dbHelper);

        if (resultsCursor != null) {
            resultsCursor.close();
        }

        double attendancePercentage = dbHelper.getAttendancePercentage(selectedStudentId);

        // Check if there's data to analyze
        if (grades.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            btnAnalyze.setEnabled(true);
            tvAnalysisResult.setText("No academic data available for analysis. Please ensure results have been recorded for this student.");
            return;
        }

        // Perform AI analysis
        geminiService.analyzeStudentPerformance(
                selectedStudentId,
                selectedStudentName,
                grades,
                attendancePercentage,
                teacherComments,
                executor,
                new GeminiAIService.AnalysisCallback() {
                    @Override
                    public void onSuccess(String analysis) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnAnalyze.setEnabled(true);
                            tvAnalysisResult.setText(analysis);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnAnalyze.setEnabled(true);
                            tvAnalysisResult.setText("Error: " + error + "\n\nPlease check your internet connection and API key.");
                            Toast.makeText(PerformanceAnalysisActivity.this, error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

