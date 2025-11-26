# Supabase Setup Guide

This guide will help you set up Supabase for the Student Intelligent System.

## Prerequisites

- A Supabase account (sign up at https://supabase.com)
- Android Studio
- Basic understanding of SQL and Android development

## Step 1: Create a Supabase Project

1. Go to [Supabase Dashboard](https://app.supabase.com)
2. Click "New Project"
3. Fill in the following details:
   - **Name**: Student Intelligent System
   - **Database Password**: Create a strong password (save it securely)
   - **Region**: Choose the closest region to your users
4. Click "Create new project" and wait for the project to be ready (2-3 minutes)

## Step 2: Set Up the Database Schema

1. In your Supabase project dashboard, go to the **SQL Editor** (left sidebar)
2. Click "New Query"
3. Copy the entire content from `supabase_schema.sql` file
4. Paste it into the SQL editor
5. Click "Run" to execute the schema
6. You should see a success message confirming all tables, indexes, and policies were created

## Step 3: Get Your Supabase Credentials

1. Go to **Project Settings** (gear icon in the left sidebar)
2. Navigate to **API** section
3. You'll find two important values:
   - **Project URL** (looks like: `https://xxxxxxxxxxxxx.supabase.co`)
   - **anon public** key (a long string starting with `eyJ...`)
4. Copy both values

## Step 4: Configure Your Android App

1. Open the project in Android Studio
2. Locate the `local.properties` file in the project root
   - If it doesn't exist, copy `local.properties.example` to `local.properties`
3. Add your Supabase credentials:

```properties
# Supabase Configuration
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key-here
```

4. Replace the placeholder values with your actual credentials

## Step 5: Sync Gradle and Build

1. In Android Studio, click **File → Sync Project with Gradle Files**
2. Wait for the sync to complete
3. If there are any errors, resolve them (usually related to internet connectivity or Gradle cache)
4. Build the project: **Build → Make Project**

## Step 6: Enable Email Authentication (Optional)

1. Go to **Authentication** in your Supabase dashboard
2. Click on **Providers**
3. Enable **Email** provider
4. Configure email templates if needed
5. For production, set up a custom SMTP server (Settings → Auth → SMTP Settings)

## Step 7: Test the Connection

Create a simple test in your app to verify the connection:

```java
// Test connection
if (SupabaseConfig.isConfigured()) {
    Log.d("Supabase", "Configuration is valid");
    
    // Test authentication
    AuthManager authManager = new AuthManager();
    boolean isLoggedIn = authManager.isLoggedIn();
    Log.d("Supabase", "User logged in: " + isLoggedIn);
} else {
    Log.e("Supabase", "Supabase is not configured properly");
}
```

## Step 8: Verify Database Tables

1. In Supabase dashboard, go to **Table Editor**
2. You should see all tables:
   - profiles
   - subjects
   - enrollments
   - results
   - attendance
   - announcements
   - ai_analysis
   - parent_student_relationships

## Database Structure Overview

### Tables Created

1. **profiles** - User profiles (students, teachers, parents, admins)
2. **subjects** - Course/subject information
3. **enrollments** - Student enrollments in subjects
4. **results** - Student exam results and grades
5. **attendance** - Attendance records
6. **announcements** - System announcements
7. **ai_analysis** - AI-generated analysis results
8. **parent_student_relationships** - Links parents to students

### Security Features

- **Row Level Security (RLS)** enabled on all tables
- Students can only view their own data
- Teachers can view/modify data for their subjects
- Parents can view their children's data
- Admins have full access

## Using the Repositories

### Profile Operations

```kotlin
val profileRepo = ProfileRepository()

// Get current user's profile
lifecycleScope.launch {
    val result = profileRepo.getProfile(userId)
    result.onSuccess { profile ->
        // Use profile data
        Log.d("Profile", "Name: ${profile.fullName}")
    }
}
```

### Subject Operations

```kotlin
val subjectRepo = SubjectRepository()

// Get all subjects
lifecycleScope.launch {
    val result = subjectRepo.getAllSubjects()
    result.onSuccess { subjects ->
        // Display subjects
    }
}
```

### Results Operations

```kotlin
val resultRepo = ResultRepository()

// Get student results
lifecycleScope.launch {
    val result = resultRepo.getStudentResults(studentId)
    result.onSuccess { results ->
        // Display results
    }
}
```

### Attendance Operations

```kotlin
val attendanceRepo = AttendanceRepository()

// Mark attendance
lifecycleScope.launch {
    val attendance = mapOf(
        "student_id" to studentId,
        "subject_id" to subjectId,
        "attendance_date" to "2025-11-26",
        "status" to "present"
    )
    attendanceRepo.markAttendance(attendance)
}
```

### Authentication

```kotlin
val authManager = AuthManager()

// Sign up
lifecycleScope.launch {
    val result = authManager.signUp(
        email = "student@example.com",
        password = "securePassword123",
        fullName = "John Doe",
        role = "student"
    )
    result.onSuccess {
        // Registration successful
    }
}

// Sign in
lifecycleScope.launch {
    val result = authManager.signIn(
        email = "student@example.com",
        password = "securePassword123"
    )
    result.onSuccess { userId ->
        // Login successful
    }
}
```

## Troubleshooting

### Issue: "Unable to resolve dependency"
**Solution**: Check your internet connection and sync Gradle again

### Issue: "Supabase URL or Key is empty"
**Solution**: Make sure you've added the credentials to `local.properties` and synced Gradle

### Issue: "Row Level Security Error"
**Solution**: Ensure RLS policies are created correctly. Re-run the schema SQL.

### Issue: "Authentication error"
**Solution**: Verify email authentication is enabled in Supabase dashboard

## Security Best Practices

1. **Never commit** `local.properties` to version control
2. Use **environment-specific** credentials (dev vs production)
3. Implement **proper error handling** for all database operations
4. Use **parameterized queries** (already handled by Supabase SDK)
5. Validate all user inputs before sending to database
6. Monitor your Supabase dashboard for suspicious activity

## Next Steps

1. Migrate existing data from SQLite to Supabase (if applicable)
2. Update existing Activities to use Supabase repositories
3. Implement real-time features using Supabase Realtime
4. Set up file uploads using Supabase Storage
5. Configure production authentication settings

## Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Android Examples](https://github.com/supabase-community/supabase-kt)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)

## Support

If you encounter any issues:
1. Check the [Supabase Status Page](https://status.supabase.com)
2. Review logs in Android Studio Logcat
3. Check Supabase Dashboard → Logs for server-side errors
4. Refer to the Supabase Discord community for help

