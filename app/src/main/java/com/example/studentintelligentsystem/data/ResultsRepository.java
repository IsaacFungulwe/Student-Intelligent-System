package com.example.studentintelligentsystem.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.studentintelligentsystem.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing results with Supabase backend
 */
public class ResultsRepository {
    private static final String TAG = "ResultsRepository";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface UpdateCallback {
        void onSuccess(JSONObject updatedResult);
        void onError(String error);
    }

    /**
     * Update a result in Supabase
     */
    public void updateResult(int resultId, String subjectName, String term, int marks, String comment, UpdateCallback callback) {
        executor.execute(() -> {
            try {
                String url = BuildConfig.SUPABASE_URL.replaceAll("/$", "") + "/rest/v1/results?id=eq." + resultId;

                JSONObject json = new JSONObject();
                json.put("subject_name", subjectName);
                json.put("term", term);
                json.put("marks", marks);
                json.put("comment", comment);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=representation");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == 200 || responseCode == 204) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    String responseBody = response.toString();
                    if (responseBody.isEmpty() || responseBody.equals("[]")) {
                        // Success but no representation returned
                        mainHandler.post(() -> callback.onSuccess(json));
                    } else {
                        JSONArray arr = new JSONArray(responseBody);
                        if (arr.length() > 0) {
                            JSONObject updated = arr.getJSONObject(0);
                            mainHandler.post(() -> callback.onSuccess(updated));
                        } else {
                            mainHandler.post(() -> callback.onSuccess(json));
                        }
                    }
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    String errorMsg = "Update failed: " + responseCode + " - " + errorResponse.toString();
                    Log.e(TAG, errorMsg);
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception updating result", e);
                mainHandler.post(() -> callback.onError("Exception: " + e.getMessage()));
            }
        });
    }
}

