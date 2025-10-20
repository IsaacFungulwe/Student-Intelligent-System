package com.example.studentintelligentsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateResultsActivity extends AppCompatActivity {

    private EditText editStudentId, editSubject, editScore;
    private Button btnSaveResult;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_results);

        db = new DatabaseHelper(this);

        editStudentId = findViewById(R.id.editStudentId);
        editSubject = findViewById(R.id.editSubject);
        editScore = findViewById(R.id.editScore);
        btnSaveResult = findViewById(R.id.btnSaveResult);

        btnSaveResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = editStudentId.getText().toString().trim();
                String subject = editSubject.getText().toString().trim();
                String scoreStr = editScore.getText().toString().trim();

                if (idStr.isEmpty() || subject.isEmpty() || scoreStr.isEmpty()) {
                    Toast.makeText(UpdateResultsActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int id = Integer.parseInt(idStr);
                    int score = Integer.parseInt(scoreStr);
                    boolean ok = db.addOrUpdateResult(id, subject, score);
                    if (ok) {
                        Toast.makeText(UpdateResultsActivity.this, "Result saved", Toast.LENGTH_SHORT).show();
                        editStudentId.setText(""); editSubject.setText(""); editScore.setText("");
                    } else {
                        Toast.makeText(UpdateResultsActivity.this, "Error saving result", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(UpdateResultsActivity.this, "ID and score must be numbers", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

