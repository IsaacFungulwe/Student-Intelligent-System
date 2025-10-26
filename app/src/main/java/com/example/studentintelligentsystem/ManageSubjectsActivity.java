package com.example.studentintelligentsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ManageSubjectsActivity extends AppCompatActivity {

    private TextView tvManageSubjectsTitle;
    private EditText editSubjectName;
    private Button btnAddSubject;
    private ListView lvSubjects;

    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private List<String> subjectList;

    private int teacherId;
    private int teacherGrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_subjects);

        dbHelper = new DatabaseHelper(this);

        tvManageSubjectsTitle = findViewById(R.id.tvManageSubjectsTitle);
        editSubjectName = findViewById(R.id.editSubjectName);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        lvSubjects = findViewById(R.id.lvSubjects);

        // Get teacher info from session
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        teacherId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        teacherGrade = prefs.getInt(LoginActivity.KEY_USER_GRADE, -1);

        if (teacherId == -1 || teacherGrade == -1) {
            Toast.makeText(this, "Error: Could not verify teacher. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvManageSubjectsTitle.setText("Manage Subjects for Grade " + teacherGrade);

        btnAddSubject.setOnClickListener(v -> addSubject());

        loadSubjects();
        // Register the ListView for the context menu
        registerForContextMenu(lvSubjects);
    }

    private void addSubject() {
        String subjectName = editSubjectName.getText().toString().trim();
        if (TextUtils.isEmpty(subjectName)) {
            Toast.makeText(this, "Subject name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.addSubject(subjectName, teacherGrade, teacherId);
        if (result != -1) {
            Toast.makeText(this, "Subject added successfully.", Toast.LENGTH_SHORT).show();
            editSubjectName.setText(""); // Clear the input
            loadSubjects(); // Refresh the list
        } else {
            Toast.makeText(this, "Error adding subject.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSubjects() {
        subjectList = dbHelper.getSubjectsByGrade(teacherGrade);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjectList);
        lvSubjects.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvSubjects) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_subject_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.delete_subject) {
            String subjectToDelete = subjectList.get(info.position);
            deleteSubject(subjectToDelete);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteSubject(String subjectName) {
        dbHelper.deleteSubject(subjectName, teacherGrade);
        Toast.makeText(this, "Subject '" + subjectName + "' deleted.", Toast.LENGTH_SHORT).show();
        loadSubjects(); // Refresh the list to show the change
    }
}
