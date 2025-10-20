package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CheckResultsActivity extends AppCompatActivity {

    private ListView listViewResults;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_results);

        listViewResults = findViewById(R.id.listResults);
        db = new DatabaseHelper(this);

        loadResults();
    }

    private void loadResults() {
        Cursor cursor = db.getAllResults();
        List<String> items = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int studentId = cursor.getInt(cursor.getColumnIndexOrThrow("student_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("stu_name"));
                String subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
                String line1 = (name != null ? name : getString(R.string.unknown_student)) + " (" + getString(R.string.student_id_label) + studentId + ")";
                String line2 = getString(R.string.subject_and_score_label, subject, score);
                items.add(line1 + " - " + line2);
            }
            cursor.close();
        }

        ResultAdapter adapter = new ResultAdapter(this, items);
        listViewResults.setAdapter(adapter);
    }
}
