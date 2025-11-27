package com.example.studentintelligentsystem.supabase;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Supabase Client Singleton
 * Handles all communication with Supabase backend
 */
public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private static SupabaseClient instance;
    private Context context;
    private boolean isInitialized = false;

    private SupabaseClient(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Initialize the Supabase client
     */
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context);
            instance.isInitialized = true;
            Log.d(TAG, "SupabaseClient initialized");
        }
    }

    /**
     * Get the singleton instance
     */
    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SupabaseClient not initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Test connection to Supabase
     */
    public boolean testConnection() {
        if (!SupabaseConfig.isConfigured()) {
            Log.e(TAG, "Cannot test connection: Supabase not configured");
            return false;
        }

        try {
            // Try to query the profiles table (should exist)
            String urlString = SupabaseConfig.SUPABASE_URL + "/rest/v1/profiles?limit=1";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            Log.d(TAG, "Connection test response code: " + responseCode);
            return responseCode == 200 || responseCode == 201;

        } catch (Exception e) {
            Log.e(TAG, "Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Intelligent UPSERT: Insert if not exists, update if exists
     * Prevents duplicate key errors (409)
     */
    public boolean upsertData(String tableName, JSONObject data) {
        if (!isInitialized || !SupabaseConfig.isConfigured()) {
            Log.e(TAG, "Cannot upsert: Supabase not properly initialized");
            return false;
        }

        try {
            // Use Supabase's UPSERT feature with Prefer header
            String urlString = SupabaseConfig.SUPABASE_URL + "/rest/v1/" + tableName;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation");
            conn.setDoOutput(true);

            // Write JSON data
            OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 201) {
                Log.d(TAG, "Data upserted successfully to " + tableName);
                return true;
            } else if (responseCode == 409) {
                // Conflict - try update instead
                Log.d(TAG, "Record exists, attempting update for " + tableName);
                if (data.has("id")) {
                    return updateData(tableName, "id", String.valueOf(data.get("id")), data);
                } else {
                    Log.w(TAG, "Cannot update: no 'id' field in data");
                    return false;
                }
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                Log.e(TAG, "Upsert failed: " + responseCode + " - " + response.toString());
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error upserting data to " + tableName + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Insert data into Supabase table
     * @deprecated Use upsertData() instead to avoid duplicate key errors
     */
    public boolean insertData(String tableName, JSONObject data) {
        // Redirect to upsert for intelligent handling
        return upsertData(tableName, data);
    }

    /**
     * Update data in Supabase table
     */
    public boolean updateData(String tableName, String idColumn, String idValue, JSONObject data) {
        if (!isInitialized || !SupabaseConfig.isConfigured()) {
            Log.e(TAG, "Cannot update: Supabase not properly initialized");
            return false;
        }

        try {
            String urlString = SupabaseConfig.SUPABASE_URL + "/rest/v1/" + tableName + "?" + idColumn + "=eq." + idValue;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 204) {
                Log.d(TAG, "Data updated successfully in " + tableName);
                return true;
            } else {
                Log.e(TAG, "Update failed: " + responseCode);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating data in " + tableName + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Query data from Supabase table
     */
    public String queryData(String tableName, String filter) {
        if (!isInitialized || !SupabaseConfig.isConfigured()) {
            Log.e(TAG, "Cannot query: Supabase not properly initialized");
            return null;
        }

        try {
            String urlString = SupabaseConfig.SUPABASE_URL + "/rest/v1/" + tableName;
            if (filter != null && !filter.isEmpty()) {
                urlString += "?" + filter;
            }

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                return response.toString();
            } else {
                Log.e(TAG, "Query failed: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error querying data from " + tableName + ": " + e.getMessage(), e);
            return null;
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Query parent by email from Supabase
     * Returns the parent JSON object if found, null otherwise
     */
    public JSONObject getParentByEmail(String email) {
        if (!isInitialized || !SupabaseConfig.isConfigured()) {
            Log.e(TAG, "Cannot query parent: Supabase not properly initialized");
            return null;
        }

        try {
            String urlString = SupabaseConfig.SUPABASE_URL + "/rest/v1/parents?email=eq." + email;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                // Parse response as JSON array
                String jsonResponse = response.toString();
                org.json.JSONArray jsonArray = new org.json.JSONArray(jsonResponse);
                
                if (jsonArray.length() > 0) {
                    Log.d(TAG, "✓ Parent found in Supabase with email: " + email);
                    return jsonArray.getJSONObject(0);
                } else {
                    Log.d(TAG, "No parent found in Supabase with email: " + email);
                    return null;
                }
            } else {
                Log.e(TAG, "Query parent by email failed: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error querying parent by email: " + e.getMessage(), e);
            return null;
        }
    }
}

