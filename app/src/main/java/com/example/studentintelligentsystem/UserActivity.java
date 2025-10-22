package com.example.studentintelligentsystem;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private DatabaseHelper db;
    private List<Student> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);
        studentList = new ArrayList<>();

        loadStudents();
    }

    private void loadStudents() {
        studentList.clear();
        Cursor cursor = db.getAllStudents(); // fixed method
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("stu_name"));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow("stu_grade"));
                studentList.add(new Student(id, name, grade));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(adapter);
    }
}
