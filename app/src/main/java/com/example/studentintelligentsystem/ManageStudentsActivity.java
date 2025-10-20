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

    private EditText editStudentId, editStudentName, editStudentGrade;
    private EditText editResultStudentId, editResultSubject, editResultScore;
    private Button btnAddEditStudent, btnUpdateResult;
    private DatabaseHelper db;
    private ListView adminListStudents;
    private ArrayAdapter<String> listAdapter;
    private List<Integer> listIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        db = new DatabaseHelper(this);

        // Initialize views for Register/Edit Student
        editStudentId = findViewById(R.id.editStudentId);
        editStudentName = findViewById(R.id.editStudentName);
        editStudentGrade = findViewById(R.id.editStudentGrade);
        btnAddEditStudent = findViewById(R.id.btnAddEditStudent);

        // Initialize views for Update Results
        editResultStudentId = findViewById(R.id.editResultStudentId);
        editResultSubject = findViewById(R.id.editResultSubject);
        editResultScore = findViewById(R.id.editResultScore);
        btnUpdateResult = findViewById(R.id.btnUpdateResult);

        // Initialising the ListView
        adminListStudents = findViewById(R.id.adminListStudents);
        listIds = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        adminListStudents.setAdapter(listAdapter);

        // Set button listeners
        btnAddEditStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrEditStudent();
            }
        });

        btnUpdateResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResults();
            }
        });

        // Set ListView item click listener to populate fields for editing
        adminListStudents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listIds.size()) {
                    int studentId = listIds.get(position);
                    editStudentId.setText(String.valueOf(studentId));
                    editResultStudentId.setText(String.valueOf(studentId));
                }
            }
        });

        // Set ListView long-click listener for deletion
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

    private void addOrEditStudent() {
        String studentIdStr = editStudentId.getText().toString().trim();
        String name = editStudentName.getText().toString().trim();
        String gradeStr = editStudentGrade.getText().toString().trim();

        if (name.isEmpty() || gradeStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.student_name_and_grade_required), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int grade = Integer.parseInt(gradeStr);

            if (studentIdStr.isEmpty()) { // Add new student
                boolean studentAdded = db.addStudent(name, grade);
                if (studentAdded) {
                    Toast.makeText(this, getString(R.string.student_added_successfully), Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    loadStudents();
                } else {
                    Toast.makeText(this, getString(R.string.error_adding_student), Toast.LENGTH_SHORT).show();
                }
            } else { // Update existing student
                int studentId = Integer.parseInt(studentIdStr);
                boolean updated = db.updateStudent(studentId, name, grade);
                if (updated) {
                    Toast.makeText(this, getString(R.string.student_updated_successfully), Toast.LENGTH_SHORT).show();
                    clearInputFields();
                    loadStudents();
                } else {
                    Toast.makeText(this, getString(R.string.error_updating_student), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.grade_and_id_must_be_numbers), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateResults() {
        String studentIdStr = editResultStudentId.getText().toString().trim();
        String subject = editResultSubject.getText().toString().trim();
        String scoreStr = editResultScore.getText().toString().trim();

        if (studentIdStr.isEmpty() || subject.isEmpty() || scoreStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.all_fields_required_to_update_results), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int studentId = Integer.parseInt(studentIdStr);
            int score = Integer.parseInt(scoreStr);

            boolean resultUpdated = db.addOrUpdateResult(studentId, subject, score);
            if (resultUpdated) {
                Toast.makeText(this, getString(R.string.result_updated_successfully), Toast.LENGTH_SHORT).show();
                clearInputFields();
            } else {
                Toast.makeText(this, getString(R.string.error_updating_result), Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.student_id_and_score_must_be_numbers), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputFields() {
        editStudentId.setText("");
        editStudentName.setText("");
        editStudentGrade.setText("");
        editResultStudentId.setText("");
        editResultSubject.setText("");
        editResultScore.setText("");
    }

    private void loadStudents() {
        listIds.clear();
        List<String> items = new ArrayList<>();
        Cursor cursor = db.getAllStudentsSorted();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("stu_name"));
                    int grade = cursor.getInt(cursor.getColumnIndexOrThrow("stu_grade"));
                    listIds.add(id);
                    items.add("ID:" + id + " - " + name + " (Grade " + grade + ")");
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        listAdapter.clear();
        listAdapter.addAll(items);
        listAdapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmation(final int studentId) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_student_dialog_title))
                .setMessage(getString(R.string.delete_student_dialog_message, studentId))
                .setPositiveButton(getString(R.string.delete_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleted = db.deleteStudent(studentId);
                        if (deleted) {
                            Toast.makeText(ManageStudentsActivity.this, getString(R.string.student_removed), Toast.LENGTH_SHORT).show();
                            clearInputFields();
                            loadStudents();
                        } else {
                            Toast.makeText(ManageStudentsActivity.this, getString(R.string.no_student_found_with_that_id), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel_button), null)
                .show();
    }
}
