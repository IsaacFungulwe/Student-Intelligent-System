package com.example.studentintelligentsystem;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.studentintelligentsystem.supabase.SupabaseClient;
import com.example.studentintelligentsystem.supabase.SupabaseConfig;
import com.example.studentintelligentsystem.supabase.SupabaseSyncManager;

/**
 * Application class for Student Intelligent System
 * Initializes Supabase on app startup and syncs existing data
 */
public class StudentIntelligentSystemApp extends Application {
    private static final String TAG = "StudentIntelligentApp";
    private static final String PREFS_NAME = "SupabaseSyncPrefs";
    private static final String KEY_INITIAL_SYNC_DONE = "initial_sync_completed";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Application starting...");

        // Initialize Supabase
        initializeSupabase();
    }

    private void initializeSupabase() {
        try {
            Log.i(TAG, "Initializing Supabase connection...");

            // Check if Supabase is configured
            if (!SupabaseConfig.isConfigured()) {
                Log.e(TAG, "Supabase is NOT configured. Please check local.properties file.");
                Log.e(TAG, "Expected: SUPABASE_URL and SUPABASE_ANON_KEY");
                return;
            }

            Log.d(TAG, "Supabase URL: " + SupabaseConfig.SUPABASE_URL);
            Log.d(TAG, "Supabase API Key configured: " + (SupabaseConfig.SUPABASE_ANON_KEY != null && !SupabaseConfig.SUPABASE_ANON_KEY.isEmpty()));

            // Initialize Supabase client
            SupabaseClient.initialize(this);

            Log.i(TAG, "✓ Supabase initialized successfully!");
            Log.i(TAG, "✓ Supabase is ready for data synchronization");

            // Test connection and perform initial sync
            testConnectionAndSync();

        } catch (Exception e) {
            Log.e(TAG, "✗ Failed to initialize Supabase: " + e.getMessage(), e);
        }
    }

    private void testConnectionAndSync() {
        new Thread(() -> {
            try {
                Log.d(TAG, "Testing Supabase connection...");
                boolean isConnected = SupabaseClient.getInstance().testConnection();

                if (isConnected) {
                    Log.i(TAG, "✓ Supabase connection test SUCCESSFUL");
                    Log.i(TAG, "✓ Data sync is enabled and operational");

                    // Check if initial sync has been done
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    boolean syncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false);

                    if (!syncDone) {
                        Log.i(TAG, "🔄 First launch detected - starting initial data migration...");
                        performInitialDataSync(prefs);
                    } else {
                        Log.d(TAG, "✓ Initial sync already completed on previous launch");
                    }
                } else {
                    Log.w(TAG, "✗ Supabase connection test FAILED");
                    Log.w(TAG, "✗ Data sync may not work properly");
                }
            } catch (Exception e) {
                Log.e(TAG, "✗ Supabase connection test error: " + e.getMessage(), e);
            }
        }).start();
    }

    private void performInitialDataSync(SharedPreferences prefs) {
        try {
            // Get sync manager and trigger full sync
            SupabaseSyncManager syncManager = SupabaseSyncManager.getInstance(this);

            Log.i(TAG, "════════════════════════════════════════");
            Log.i(TAG, "🚀 INITIAL DATA MIGRATION STARTED");
            Log.i(TAG, "This will sync all existing local data to Supabase");
            Log.i(TAG, "════════════════════════════════════════");

            // Perform the sync (runs in background)
            syncManager.syncAllLocalData();

            // Mark sync as done (we mark it now, but actual sync happens async)
            // If sync fails, user can reset by clearing app data
            prefs.edit().putBoolean(KEY_INITIAL_SYNC_DONE, true).apply();

            Log.i(TAG, "✓ Initial sync triggered successfully");
            Log.i(TAG, "📝 Check logs for detailed sync progress");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to trigger initial sync: " + e.getMessage(), e);
        }
    }
}

