package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewResultsActivity extends AppCompatActivity {

    private Spinner yearSpinner;
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
        yearSpinner = findViewById(R.id.yearSpinner);
        resultsTable = findViewById(R.id.resultsTable);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        parentId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (parentId == -1) {
            Toast.makeText(this, "Error: Could not verify your login. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupYearSpinner();
    }

    private void setupYearSpinner() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < 5; i++) {
            years.add(String.valueOf(currentYear - i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadResults(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Load results for the default year
        if (!years.isEmpty()) {
            loadResults(years.get(0));
        }
    }

    private void loadResults(String year) {
        resultsTable.removeViews(1, resultsTable.getChildCount() - 1); // Clear previous results

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT r." + DatabaseHelper.RESULT_SUBJECT + ", r." + DatabaseHelper.RESULT_TERM + ", r." + DatabaseHelper.RESULT_MARKS + " FROM " + DatabaseHelper.TABLE_RESULTS + " r INNER JOIN " + DatabaseHelper.TABLE_STUDENT + " s ON r." + DatabaseHelper.RESULT_FK_STUDENT_ID + " = s." + DatabaseHelper.STUDENT_ID + " WHERE s." + DatabaseHelper.STUDENT_FK_PARENT_ID + " = ? AND r." + DatabaseHelper.RESULT_TERM + " LIKE ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(parentId), "%" + year + "%"});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                String term = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_TERM));
                int marks = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS));

                TableRow row = new TableRow(this);
                TextView tvSubject = new TextView(this);
                tvSubject.setText(subject);
                tvSubject.setPadding(8, 8, 8, 8);
                tvSubject.setTextColor(Color.BLACK);

                TextView tvTerm = new TextView(this);
                tvTerm.setText(term);
                tvTerm.setPadding(8, 8, 8, 8);
                tvTerm.setTextColor(Color.BLACK);

                TextView tvMarks = new TextView(this);
                tvMarks.setText(String.valueOf(marks));
                tvMarks.setPadding(8, 8, 8, 8);
                tvMarks.setTextColor(Color.BLACK);

                TextView tvComments = new TextView(this);
                tvComments.setText(""); // Placeholder for comments
                tvComments.setPadding(8, 8, 8, 8);
                tvComments.setTextColor(Color.BLACK);

                row.addView(tvSubject);
                row.addView(tvTerm);
                row.addView(tvMarks);
                row.addView(tvComments);

                resultsTable.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TableRow row = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText("No results found for " + year);
            tv.setPadding(8, 8, 8, 8);
            row.addView(tv);
            resultsTable.addView(row);
        }
        db.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
