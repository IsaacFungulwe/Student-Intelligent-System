# Migration Guide: SQLite to Supabase

This guide helps you migrate your existing Student Intelligent System from SQLite to Supabase.

## Overview

The migration involves:
1. Setting up Supabase (follow SUPABASE_SETUP.md)
2. Migrating existing data (optional)
3. Updating Activities to use Supabase repositories
4. Testing the integration

## Phase 1: Parallel Running (Recommended)

Run both SQLite and Supabase side-by-side initially for testing.

### Step 1: Keep DatabaseHelper.java

Don't delete `DatabaseHelper.java` yet. You may need it for data migration.

### Step 2: Add Supabase Check

Create a utility to determine which database to use:

```java
public class DatabaseConfig {
    private static final boolean USE_SUPABASE = true; // Toggle this
    
    public static boolean useSupabase() {
        return USE_SUPABASE && SupabaseConfig.isConfigured();
    }
}
```

## Phase 2: Update Activities

### Example: LoginActivity Migration

**Before (SQLite):**
```java
public class LoginActivity extends AppCompatActivity {
    private DatabaseHelper db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this);
        
        // Login logic
        boolean isValid = db.validateUser(email, password);
    }
}
```

**After (Supabase):**
```java
public class LoginActivity extends AppCompatActivity {
    private AuthManager authManager;
    private ProfileRepository profileRepo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = new AuthManager();
        profileRepo = new ProfileRepository();
        
        // Login logic
        loginWithSupabase(email, password);
    }
    
    private void loginWithSupabase(String email, String password) {
        new Thread(() -> {
            try {
                kotlin.Result<String> result = authManager.signIn(email, password);
                
                runOnUiThread(() -> {
                    if (result.isSuccess()) {
                        String userId = result.component1();
                        // Login successful
                        loadUserProfile(userId);
                    } else {
                        // Login failed
                        Throwable error = result.exceptionOrNull();
                        Toast.makeText(this, "Login failed: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
```

### Example: ViewResultsActivity Migration

**Before (SQLite):**
```java
Cursor cursor = db.getStudentResults(studentId);
while (cursor.moveToNext()) {
    String subject = cursor.getString(cursor.getColumnIndex("subject"));
    int marks = cursor.getInt(cursor.getColumnIndex("marks"));
    // Process results
}
```

**After (Supabase):**
```java
private void loadResults(String studentId) {
    ResultRepository resultRepo = new ResultRepository();
    
    new Thread(() -> {
        kotlin.Result<List<com.example.studentintelligentsystem.supabase.models.Result>> 
            result = resultRepo.getStudentResults(studentId);
        
        runOnUiThread(() -> {
            if (result.isSuccess()) {
                List<com.example.studentintelligentsystem.supabase.models.Result> results 
                    = result.component1();
                
                // Update RecyclerView adapter
                resultsAdapter.setResults(results);
            } else {
                Toast.makeText(this, "Failed to load results", Toast.LENGTH_SHORT).show();
            }
        });
    }).start();
}
```

## Phase 3: Data Migration Script

If you have existing data in SQLite that needs to be migrated:

### Step 1: Export SQLite Data

```java
public class DataMigrationHelper {
    
    public static void exportStudentsToSupabase(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        ProfileRepository profileRepo = new ProfileRepository();
        
        Cursor cursor = db.getAllStudents();
        
        new Thread(() -> {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
                
                Map<String, Object> profile = new HashMap<>();
                profile.put("full_name", name);
                profile.put("email", email);
                profile.put("student_id", studentId);
                profile.put("role", "student");
                
                try {
                    profileRepo.createProfile(profile);
                    Log.d("Migration", "Migrated student: " + name);
                } catch (Exception e) {
                    Log.e("Migration", "Failed to migrate student: " + name, e);
                }
            }
            cursor.close();
        }).start();
    }
    
    public static void exportResultsToSupabase(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        ResultRepository resultRepo = new ResultRepository();
        
        Cursor cursor = db.getAllResults();
        
        new Thread(() -> {
            while (cursor.moveToNext()) {
                Map<String, Object> result = new HashMap<>();
                result.put("student_id", cursor.getString(cursor.getColumnIndex("student_id")));
                result.put("subject_id", cursor.getString(cursor.getColumnIndex("subject_id")));
                result.put("exam_type", cursor.getString(cursor.getColumnIndex("exam_type")));
                result.put("marks_obtained", cursor.getDouble(cursor.getColumnIndex("marks")));
                result.put("total_marks", cursor.getDouble(cursor.getColumnIndex("total_marks")));
                result.put("exam_date", cursor.getString(cursor.getColumnIndex("exam_date")));
                
                try {
                    resultRepo.addResult(result);
                    Log.d("Migration", "Migrated result");
                } catch (Exception e) {
                    Log.e("Migration", "Failed to migrate result", e);
                }
            }
            cursor.close();
        }).start();
    }
}
```

### Step 2: Run Migration

Add a button in admin dashboard to trigger migration:

```java
migrationButton.setOnClickListener(v -> {
    new AlertDialog.Builder(this)
        .setTitle("Migrate Data")
        .setMessage("This will copy all data from local database to Supabase. Continue?")
        .setPositiveButton("Migrate", (dialog, which) -> {
            DataMigrationHelper.exportStudentsToSupabase(this);
            DataMigrationHelper.exportResultsToSupabase(this);
            Toast.makeText(this, "Migration started. Check logs for progress.", 
                Toast.LENGTH_LONG).show();
        })
        .setNegativeButton("Cancel", null)
        .show();
});
```

## Phase 4: Activity Updates Checklist

Update these activities to use Supabase:

### Authentication Activities
- [ ] `LoginActivity.java` - Use `AuthManager.signIn()`
- [ ] `StudentRegisterActivity.java` - Use `AuthManager.signUp()`
- [ ] `TeacherRegisterActivity.java` - Use `AuthManager.signUp()`
- [ ] `ParentRegisterActivity.java` - Use `AuthManager.signUp()`
- [ ] `AdminRegisterActivity.java` - Use `AuthManager.signUp()`

### Dashboard Activities
- [ ] `StudentDashboardActivity.java` - Use `ProfileRepository.getProfile()`
- [ ] `TeacherDashboardActivity.java` - Use `SubjectRepository.getSubjectsByTeacher()`
- [ ] `ParentDashboardActivity.java` - Use profile and relationship queries
- [ ] `AdminDashboardActivity.java` - Use various repositories

### Results Activities
- [ ] `ViewResultsActivity.java` - Use `ResultRepository.getStudentResults()`
- [ ] `AddResultsActivity.java` - Use `ResultRepository.addResult()`
- [ ] `ViewEditResultsActivity.java` - Use `ResultRepository.updateResult()`

### Attendance Activities
- [ ] `MarkAttendanceActivity.java` - Use `AttendanceRepository.markAttendance()`
- [ ] `ViewAttendanceActivity.java` - Use `AttendanceRepository.getStudentAttendance()`
- [ ] `AttendanceHistoryActivity.java` - Use `AttendanceRepository.getAttendanceBySubject()`

### Announcement Activities
- [ ] `ManageAnnouncementsActivity.java` - Use `AnnouncementRepository`
- [ ] `PostAnnouncementActivity.java` - Use `AnnouncementRepository.createAnnouncement()`

### Subject Management
- [ ] `ManageSubjectsActivity.java` - Use `SubjectRepository`

## Phase 5: Testing

### Test Cases

1. **Authentication**
   - [ ] User registration (all roles)
   - [ ] User login
   - [ ] Password reset
   - [ ] Session persistence

2. **Profile Management**
   - [ ] View profile
   - [ ] Update profile
   - [ ] Search users

3. **Results**
   - [ ] View student results
   - [ ] Add new results
   - [ ] Update existing results
   - [ ] Delete results

4. **Attendance**
   - [ ] Mark attendance
   - [ ] View attendance history
   - [ ] Calculate attendance percentage

5. **Announcements**
   - [ ] Create announcements
   - [ ] View announcements
   - [ ] Filter by role

6. **Permissions**
   - [ ] Students can only see their data
   - [ ] Teachers can manage their subjects
   - [ ] Parents can see children's data
   - [ ] Admins have full access

## Phase 6: Cleanup

Once everything is working with Supabase:

1. **Remove SQLite Dependencies**
   ```java
   // Delete or archive DatabaseHelper.java
   // Remove SQLite database file
   context.deleteDatabase("student_system.db");
   ```

2. **Update Build Configuration**
   - Remove any SQLite-specific dependencies if any

3. **Update Documentation**
   - Update README.md
   - Add Supabase setup to onboarding docs

## Common Issues and Solutions

### Issue: "Cannot access database"
**Solution**: Check Row Level Security policies in Supabase. Ensure user is authenticated.

### Issue: "Duplicate key error"
**Solution**: Check for existing records before inserting. Use upsert operations where needed.

### Issue: "Null user ID"
**Solution**: Ensure user is logged in before making database calls:
```java
if (!authManager.isLoggedIn()) {
    // Redirect to login
    return;
}
```

### Issue: "Slow queries"
**Solution**: Check indexes in Supabase. Add additional indexes if needed:
```sql
CREATE INDEX idx_results_student_subject ON results(student_id, subject_id);
```

## Rollback Plan

If issues arise:

1. Set `USE_SUPABASE = false` in `DatabaseConfig`
2. App will revert to SQLite
3. Debug Supabase issues separately
4. Re-enable when fixed

## Best Practices

1. **Error Handling**: Always handle errors from Supabase operations
2. **Loading States**: Show progress indicators during async operations
3. **Offline Support**: Consider implementing local caching for offline access
4. **Real-time Updates**: Use Supabase Realtime for live data updates
5. **Testing**: Test with multiple users and roles
6. **Monitoring**: Monitor Supabase dashboard for errors and performance

## Performance Tips

1. **Batch Operations**: Use bulk inserts for multiple records
2. **Select Specific Columns**: Only fetch required data
3. **Pagination**: Implement pagination for large datasets
4. **Caching**: Cache frequently accessed data locally
5. **Connection Pooling**: Reuse Supabase client instance

## Next Steps After Migration

1. Implement real-time features (live attendance updates, instant notifications)
2. Add file upload for student documents using Supabase Storage
3. Implement advanced analytics using Supabase Functions
4. Set up automated backups
5. Configure production authentication (custom SMTP, OAuth providers)

## Support

For migration assistance:
- Review SUPABASE_SETUP.md for setup details
- Check SupabaseUsageExample.java for code patterns
- Consult Supabase documentation: https://supabase.com/docs

