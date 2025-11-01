package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ViewResultsActivity extends AppCompatActivity {

    private TableLayout resultsTable;
    private DatabaseHelper dbHelper;
    private int parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View Results");

        dbHelper = new DatabaseHelper(this);
        resultsTable = findViewById(R.id.resultsTable);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        parentId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (parentId == -1) {
            Toast.makeText(this, "Error: Could not verify your login. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Check if data came from AddResultsActivity via Intent
        String subject = getIntent().getStringExtra("subject");
        String term = getIntent().getStringExtra("term");
        String marks = getIntent().getStringExtra("marks");
        String comments = getIntent().getStringExtra("comments");

        if (subject != null && term != null && marks != null) {
            // Show only the passed-in result (from AddResultsActivity)
            displaySingleResult(subject, term, marks, comments);
        } else {
            // Otherwise, load all results for the parent's child
            loadAllResultsForParent();
        }
    }

    /** Displays one result passed via Intent */
    private void displaySingleResult(String subject, String term, String marks, String comments) {
        TableRow newRow = new TableRow(this);

        TextView subjectView = new TextView(this);
        subjectView.setText(subject);
        subjectView.setPadding(8, 8, 8, 8);
        subjectView.setTextColor(Color.BLACK);

        TextView termView = new TextView(this);
        termView.setText(term);
        termView.setPadding(8, 8, 8, 8);
        termView.setTextColor(Color.BLACK);

        TextView marksView = new TextView(this);
        marksView.setText(marks);
        marksView.setPadding(8, 8, 8, 8);
        marksView.setTextColor(Color.BLACK);

        TextView commentsView = new TextView(this);
        commentsView.setText(comments);
        commentsView.setPadding(8, 8, 8, 8);
        commentsView.setTextColor(Color.BLACK);

        newRow.addView(subjectView);
        newRow.addView(termView);
        newRow.addView(marksView);
        newRow.addView(commentsView);

        resultsTable.addView(newRow);
    }
    
    /** Loads all results from the database for the parent's child */
    private void loadAllResultsForParent() {
        resultsTable.removeViews(1, resultsTable.getChildCount() - 1); // Clear previous rows

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT r." + DatabaseHelper.RESULT_SUBJECT + ", r." + DatabaseHelper.RESULT_TERM + ", r." + DatabaseHelper.RESULT_MARKS + ", r." + DatabaseHelper.RESULT_COMMENT +
                " FROM " + DatabaseHelper.TABLE_RESULTS + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON r." + DatabaseHelper.RESULT_FK_STUDENT_ID + " = s." + DatabaseHelper.STUDENT_ID +
                " WHERE s." + DatabaseHelper.STUDENT_FK_PARENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(parentId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                String term = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_TERM));
                int marks = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS));
                String comments = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT));

                TableRow row = new TableRow(this);

                TextView tvSubject = createTextView(subject);
                TextView tvTerm = createTextView(term);
                TextView tvMarks = createTextView(String.valueOf(marks));
                TextView tvComments = createTextView(comments != null ? comments : "");

                row.addView(tvSubject);
                row.addView(tvTerm);
                row.addView(tvMarks);
                row.addView(tvComments);

                resultsTable.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TableRow row = new TableRow(this);
            TextView tv = createTextView("No results found.");
            row.addView(tv);
            resultsTable.addView(row);
        }
        db.close();
    }

    /** Helper method to create TextView cells */
    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        tv.setTextColor(Color.BLACK);
        return tv;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
