package com.example.studentintelligentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dash_board);

        // Wire Register Student card to RegisterStudentActivity
        CardView cardRegisterStudent = findViewById(R.id.cardRegisterStudent);
        if (cardRegisterStudent != null) {
            cardRegisterStudent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, RegisterStudentActivity.class));
                }
            });
        }

        // Wire Check Results
        CardView cardCheck = findViewById(R.id.cardCheckResults);
        if (cardCheck != null) {
            cardCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, CheckResultsActivity.class));
                }
            });
        }

        // Wire Update Results to ManageStudentsActivity
        CardView cardUpdate = findViewById(R.id.cardUpdateResults);
        if (cardUpdate != null) {
            cardUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, ManageStudentsActivity.class));
                }
            });
        }

        // Wire Mark Register
        CardView cardMark = findViewById(R.id.cardMarkRegister);
        if (cardMark != null) {
            cardMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AdminDashBoard.this, MarkRegisterActivity.class));
                }
            });
        }
    }
}
