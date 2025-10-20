package com.example.studentintelligentsystem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ManageStudentsActivity extends AppCompatActivity {

    private EditText adminEditName, adminEditGrade, adminEditRemoveId;
    private Button adminBtnAdd, adminBtnRemove;
    private DatabaseHelper db;
    private ListView adminListStudents;
    private ArrayAdapter<String> listAdapter;
    private List<Integer> listIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        db = new DatabaseHelper(this);

        adminEditName = findViewById(R.id.adminEditName);
        adminEditGrade = findViewById(R.id.adminEditGrade);
        adminBtnAdd = findViewById(R.id.adminBtnAdd);

        adminEditRemoveId = findViewById(R.id.adminEditRemoveId);
        adminBtnRemove = findViewById(R.id.adminBtnRemove);

        adminListStudents = findViewById(R.id.adminListStudents);
        listIds = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        adminListStudents.setAdapter(listAdapter);

        adminBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = adminEditName.getText().toString().trim();
                String gradeStr = adminEditGrade.getText().toString().trim();

                if (name.isEmpty() || gradeStr.isEmpty()) {
                    Toast.makeText(ManageStudentsActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int grade = Integer.parseInt(gradeStr);
                    boolean success = db.addStudent(name, grade);
                    if (success) {
                        Toast.makeText(ManageStudentsActivity.this, "Student added", Toast.LENGTH_SHORT).show();
                        adminEditName.setText("");
                        adminEditGrade.setText("");
                        loadStudents();
                    } else {
                        Toast.makeText(ManageStudentsActivity.this, "Error adding student", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(ManageStudentsActivity.this, "Grade must be a number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adminBtnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = adminEditRemoveId.getText().toString().trim();
                if (idStr.isEmpty()) {
                    Toast.makeText(ManageStudentsActivity.this, "Enter student ID to remove", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int id = Integer.parseInt(idStr);
                    showDeleteConfirmation(id);
                } catch (NumberFormatException e) {
                    Toast.makeText(ManageStudentsActivity.this, "ID must be a number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Long-press on list item to delete
        adminListStudents.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listIds.size()) {
                    int studentId = listIds.get(position);
                    showDeleteConfirmation(studentId);
                }
                return true;
            }
        });

        loadStudents();
    }

    private void loadStudents() {
        listIds.clear();
        List<String> items = new ArrayList<>();
        Cursor cursor = db.getAllStudentsSorted();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("stu_name"));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow("stu_grade"));
                listIds.add(id);
                items.add("ID:" + id + " - " + name + " (Grade " + grade + ")");
            } while (cursor.moveToNext());
            cursor.close();
        }

        listAdapter.clear();
        listAdapter.addAll(items);
        listAdapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmation(final int studentId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete student")
                .setMessage("Are you sure you want to delete student ID: " + studentId + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleted = db.deleteStudent(studentId);
                        if (deleted) {
                            Toast.makeText(ManageStudentsActivity.this, "Student removed", Toast.LENGTH_SHORT).show();
                            adminEditRemoveId.setText("");
                            loadStudents();
                        } else {
                            Toast.makeText(ManageStudentsActivity.this, "No student found with that ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
