package com.example.studentintelligentsystem;

import android.database.Cursor;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class GeminiAIService {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private final GenerativeModelFutures model;

    public GeminiAIService() {
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
        this.model = GenerativeModelFutures.from(gm);
    }

    public interface AnalysisCallback {
        void onSuccess(String analysis);
        void onError(String error);
    }

    public void analyzeStudentPerformance(
            int studentId,
            String studentName,
            Map<String, Integer> grades,
            double attendancePercentage,
            String teacherComments,
            Executor executor,
            AnalysisCallback callback) {

        String prompt = buildAnalysisPrompt(studentName, grades, attendancePercentage, teacherComments);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String analysisText = result.getText();
                callback.onSuccess(analysisText);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Error analyzing performance: " + t.getMessage());
            }
        }, executor);
    }

    private String buildAnalysisPrompt(String studentName, Map<String, Integer> grades, double attendance, String comments) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an educational AI assistant. Analyze the following student's academic performance comprehensively:\n\n");

        prompt.append("Student Name: ").append(studentName).append("\n\n");

        prompt.append("Academic Grades: \n");
        if (grades.isEmpty()) {
            prompt.append("No grades available.\n");
        } else {
            for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                String subject = entry.getKey();
                int mark = entry.getValue();
                prompt.append("- ").append(subject).append(": ").append(mark).append("%\n");
            }
        }

        prompt.append("\nAttendance Rate: ").append(String.format("%.1f", attendance)).append("%\n");

        if (comments != null && !comments.trim().isEmpty()) {
            prompt.append("\n Teacher's Comments: ").append(comments).append("\n");
        }

        prompt.append("\nProvide a brief analysis with ONLY these three sections. Use plain text format with clear section headers. Do not use markdown, asterisks, or special formatting.\n\n");

        prompt.append("ENCOURAGEMENT AND FEEDBACK MESSAGES\n");
        prompt.append("Write encouraging and positive feedback messages based on ").append(studentName).append("'s academic performance, attendance, and teacher comments. Acknowledge strengths and provide constructive hope for improvement.\n\n");

        prompt.append("BEHAVIOUR AND LEARNING ISSUES\n");
        prompt.append("List behavioral or learning issues identified from the grades, attendance rate, and teacher comments. Be specific about areas of concern such as weak subjects, attendance problems, or behavioral patterns.\n\n");

        prompt.append("SUGGESTED IMPROVEMENT ACTIONS\n");
        prompt.append("Provide specific, actionable recommendations for improvement. Include strategies for the student, parents, and teachers. Focus on practical steps that can address identified issues and build on strengths.\n\n");

        prompt.append("End with a brief summary statement about the student's potential and path forward.");

        return prompt.toString();
    }

    public static Map<String, Integer> extractGradesFromCursor(Cursor cursor, DatabaseHelper dbHelper) {
        Map<String, Integer> grades = new HashMap<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_SUBJECT));
                int marks = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_MARKS));

                // Store the latest grade for each subject (or average if multiple entries)
                if (grades.containsKey(subject)) {
                    // Average the grades if subject appears multiple times
                    int existingMark = grades.get(subject);
                    grades.put(subject, (existingMark + marks) / 2);
                } else {
                    grades.put(subject, marks);
                }
            } while (cursor.moveToNext());
        }

        return grades;
    }

    public static String extractCommentsFromCursor(Cursor cursor, DatabaseHelper dbHelper) {
        StringBuilder comments = new StringBuilder();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.RESULT_COMMENT));
                if (comment != null && !comment.trim().isEmpty()) {
                    if (comments.length() > 0) {
                        comments.append(" | ");
                    }
                    comments.append(comment);
                }
            } while (cursor.moveToNext());
        }

        return comments.length() > 0 ? comments.toString() : "No teacher comments available.";
    }
}

