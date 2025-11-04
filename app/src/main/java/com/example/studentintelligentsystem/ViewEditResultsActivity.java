package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewEditResultsActivity extends AppCompatActivity implements ResultsAdapter.OnResultActionListener {

    private RecyclerView rvResults;
    private ResultsAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Result> resultsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_results);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View & Edit Results");

        dbHelper = new DatabaseHelper(this);
        rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        loadResults();
    }

    private void loadResults() {
        resultsList.clear();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);
        int teacherId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (teacherGrade == -1 || teacherId == -1) {
            Toast.makeText(this, "Unable to load results for your grade", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get all results for students in the teacher's grade
        String query = "SELECT r." + DatabaseHelper.RESULT_ID + ", " +
                "r." + DatabaseHelper.RESULT_SUBJECT + ", " +
                "r." + DatabaseHelper.RESULT_TERM + ", " +
                "r." + DatabaseHelper.RESULT_MARKS + ", " +
                "r." + DatabaseHelper.RESULT_COMMENT + ", " +
                "s." + DatabaseHelper.STUDENT_NAME + ", " +
                "s." + DatabaseHelper.STUDENT_ID +
                " FROM " + DatabaseHelper.TABLE_RESULTS + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON r." +
                DatabaseHelper.RESULT_FK_STUDENT_ID + " = s." + DatabaseHelper.STUDENT_ID +
                " WHERE s." + DatabaseHelper.STUDENT_GRADE + " = ? " +
                " ORDER BY s." + DatabaseHelper.STUDENT_NAME + " ASC, r." +
                DatabaseHelper.RESULT_SUBJECT + " ASC, r." + DatabaseHelper.RESULT_TERM + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherGrade)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int resultId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_ID));
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                String term = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_TERM));
                int marks = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT));
                String studentName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_NAME));
                int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.STUDENT_ID));

                Result result = new Result(resultId, studentId, studentName, subject, term, marks, comment);
                resultsList.add(result);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        adapter = new ResultsAdapter(resultsList, this);
        rvResults.setAdapter(adapter);

        if (resultsList.isEmpty()) {
            Toast.makeText(this, "No results found for your grade", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditResult(Result result, int position) {
        // Open edit dialog
        EditResultDialog dialog = new EditResultDialog(this, result, updatedResult -> {
            // Update in database with individual parameters
            if (dbHelper.updateResult(
                    updatedResult.getResultId(),
                    updatedResult.getSubject(),
                    updatedResult.getTerm(),
                    updatedResult.getMarks(),
                    updatedResult.getComment())) {
                // Update in list
                resultsList.set(position, updatedResult);
                adapter.notifyItemChanged(position);
                Toast.makeText(this, "Result updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update result", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onDeleteResult(Result result, int position) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Result")
                .setMessage("Are you sure you want to delete this result for " + result.getStudentName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteResult(result.getResultId())) {
                        resultsList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Result deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete result", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

