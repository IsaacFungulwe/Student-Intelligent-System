package com.example.studentintelligentsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MarkRegisterActivity extends AppCompatActivity {

    private EditText editStudentIdAtt, editDate;
    private CheckBox cbPresent;
    private Button btnMark;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_register);

        db = new DatabaseHelper(this);

        editStudentIdAtt = findViewById(R.id.editStudentIdAtt);
        editDate = findViewById(R.id.editDate);
        cbPresent = findViewById(R.id.cbPresent);
        btnMark = findViewById(R.id.btnMark);

        // default date to today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        editDate.setText(today);

        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = editStudentIdAtt.getText().toString().trim();
                String date = editDate.getText().toString().trim();
                boolean present = cbPresent.isChecked();
                if (idStr.isEmpty() || date.isEmpty()) {
                    Toast.makeText(MarkRegisterActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int id = Integer.parseInt(idStr);
                    boolean ok = db.addAttendance(id, date, present);
                    if (ok) {
                        Toast.makeText(MarkRegisterActivity.this, "Attendance marked", Toast.LENGTH_SHORT).show();
                        editStudentIdAtt.setText("");
                    } else {
                        Toast.makeText(MarkRegisterActivity.this, "Error marking attendance", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MarkRegisterActivity.this, "ID must be a number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

