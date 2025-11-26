package com.example.studentintelligentsystem;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditResultDialog extends Dialog {

    private Result result;
    private OnResultUpdatedListener listener;
    private EditText etMarks;
    private EditText etComment;
    private Button btnSave;
    private Button btnCancel;

    public interface OnResultUpdatedListener {
        void onResultUpdated(Result updatedResult);
    }

    public EditResultDialog(Context context, Result result, OnResultUpdatedListener listener) {
        super(context);
        this.result = result;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_result);

        etMarks = findViewById(R.id.etMarks);
        etComment = findViewById(R.id.etComment);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Pre-fill with current values
        etMarks.setText(String.valueOf(result.getMarks()));
        if (result.getComment() != null) {
            etComment.setText(result.getComment());
        }

        btnSave.setOnClickListener(v -> saveResult());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveResult() {
        String marksStr = etMarks.getText().toString().trim();
        String comment = etComment.getText().toString().trim();

        if (marksStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter marks", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int marks = Integer.parseInt(marksStr);
            if (marks < 0 || marks > 100) {
                Toast.makeText(getContext(), "Marks must be between 0 and 100", Toast.LENGTH_SHORT).show();
                return;
            }

            result.setMarks(marks);
            result.setComment(comment);

            if (listener != null) {
                listener.onResultUpdated(result);
            }
            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid marks format", Toast.LENGTH_SHORT).show();
        }
    }
}

