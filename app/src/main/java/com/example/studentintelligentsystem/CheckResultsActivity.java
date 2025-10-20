package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
                String line = (name != null ? name : "Unknown") + " (ID:" + studentId + ") - " + subject + ": " + score;
                items.add(line);
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listViewResults.setAdapter(adapter);
    }
}

