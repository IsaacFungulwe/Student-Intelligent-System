# Supabase Integration - Quick Reference

## 📁 Files Created

### Configuration
- `supabase_schema.sql` - Complete database schema
- `local.properties.example` - Updated with Supabase credentials template
- `app/build.gradle.kts` - Updated with Supabase dependencies

### Core Files
```
app/src/main/java/com/example/studentintelligentsystem/supabase/
├── SupabaseConfig.java          # Configuration class
├── SupabaseClient.kt            # Singleton client instance
├── auth/
│   └── AuthManager.kt           # Authentication manager
├── models/
│   └── Models.kt                # Data models (Profile, Subject, Result, etc.)
├── repository/
│   ├── ProfileRepository.kt     # User profile operations
│   ├── SubjectRepository.kt     # Subject/course operations
│   ├── ResultRepository.kt      # Results/grades operations
│   ├── AttendanceRepository.kt  # Attendance operations
│   └── AnnouncementRepository.kt # Announcement operations
└── examples/
    └── SupabaseUsageExample.java # Usage examples
```

### Documentation
- `SUPABASE_SETUP.md` - Complete setup guide
- `MIGRATION_GUIDE.md` - SQLite to Supabase migration guide

## 🚀 Quick Start

### 1. Get Supabase Credentials

1. Go to [supabase.com](https://supabase.com) and create an account
2. Create a new project
3. Go to Project Settings → API
4. Copy:
   - Project URL
   - anon/public key

### 2. Configure Local Properties

Edit `local.properties`:

```properties
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key-here
```

### 3. Set Up Database

1. Open Supabase Dashboard → SQL Editor
2. Copy entire content from `supabase_schema.sql`
3. Paste and run the SQL
4. Verify tables are created in Table Editor

### 4. Sync and Build

```bash
# In Android Studio
File → Sync Project with Gradle Files
Build → Make Project
```

## 💡 Usage Examples

### Authentication

```java
AuthManager authManager = new AuthManager();

// Sign Up
new Thread(() -> {
    kotlin.Result<String> result = authManager.signUp(
        "student@example.com",
        "password123",
        "John Doe",
        "student"
    );
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        }
    });
}).start();

// Sign In
new Thread(() -> {
    kotlin.Result<String> result = authManager.signIn(
        "student@example.com",
        "password123"
    );
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            String userId = result.component1();
            // Proceed to dashboard
        }
    });
}).start();

// Check Login Status
boolean isLoggedIn = authManager.isLoggedIn();
String userId = authManager.getCurrentUserId();
```

### Load Student Profile

```java
ProfileRepository profileRepo = new ProfileRepository();
String userId = authManager.getCurrentUserId();

new Thread(() -> {
    kotlin.Result<Profile> result = profileRepo.getProfile(userId);
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            Profile profile = result.component1();
            // Use profile.getFullName(), profile.getStudentId(), etc.
            nameTextView.setText(profile.getFullName());
            emailTextView.setText(profile.getEmail());
        }
    });
}).start();
```

### Load and Display Subjects

```java
SubjectRepository subjectRepo = new SubjectRepository();

new Thread(() -> {
    kotlin.Result<List<Subject>> result = subjectRepo.getAllSubjects();
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            List<Subject> subjects = result.component1();
            // Update RecyclerView
            subjectAdapter.setSubjects(subjects);
        }
    });
}).start();
```

### Add Student Result

```java
ResultRepository resultRepo = new ResultRepository();

Map<String, Object> result = new HashMap<>();
result.put("student_id", studentId);
result.put("subject_id", subjectId);
result.put("exam_type", "midterm");
result.put("marks_obtained", 85.5);
result.put("total_marks", 100.0);
result.put("grade", "B+");
result.put("exam_date", "2025-11-26");

new Thread(() -> {
    kotlin.Result<Unit> addResult = resultRepo.addResult(result);
    
    runOnUiThread(() -> {
        if (addResult.isSuccess()) {
            Toast.makeText(this, "Result added!", Toast.LENGTH_SHORT).show();
        }
    });
}).start();
```

### Mark Attendance

```java
AttendanceRepository attendanceRepo = new AttendanceRepository();

Map<String, Object> attendance = new HashMap<>();
attendance.put("student_id", studentId);
attendance.put("subject_id", subjectId);
attendance.put("attendance_date", "2025-11-26");
attendance.put("status", "present"); // present, absent, late, excused
attendance.put("marked_by", teacherId);

new Thread(() -> {
    kotlin.Result<Unit> result = attendanceRepo.markAttendance(attendance);
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show();
        }
    });
}).start();
```

### Load Student Results

```java
ResultRepository resultRepo = new ResultRepository();

new Thread(() -> {
    kotlin.Result<List<Result>> result = resultRepo.getStudentResults(studentId);
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            List<Result> results = result.component1();
            
            for (Result r : results) {
                Log.d("Results", "Subject: " + r.getSubjectId());
                Log.d("Results", "Marks: " + r.getMarksObtained() + "/" + r.getTotalMarks());
                Log.d("Results", "Percentage: " + r.getPercentage() + "%");
                Log.d("Results", "Grade: " + r.getGrade());
            }
            
            // Update UI
            resultsAdapter.setResults(results);
        }
    });
}).start();
```

### Post Announcement

```java
AnnouncementRepository announcementRepo = new AnnouncementRepository();

Map<String, Object> announcement = new HashMap<>();
announcement.put("title", "Holiday Notice");
announcement.put("content", "School will be closed next Monday");
announcement.put("author_id", authManager.getCurrentUserId());
announcement.put("target_role", "all"); // all, student, teacher, parent
announcement.put("is_important", true);

new Thread(() -> {
    kotlin.Result<Unit> result = announcementRepo.createAnnouncement(announcement);
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            Toast.makeText(this, "Announcement posted!", Toast.LENGTH_SHORT).show();
        }
    });
}).start();
```

## 🗄️ Database Tables

| Table | Description |
|-------|-------------|
| `profiles` | User profiles (students, teachers, parents, admins) |
| `subjects` | Courses/subjects information |
| `enrollments` | Student enrollments in subjects |
| `results` | Exam results and grades |
| `attendance` | Attendance records |
| `announcements` | System announcements |
| `ai_analysis` | AI-generated analysis results |
| `parent_student_relationships` | Links parents to students |

## 🔐 Security Features

- **Row Level Security (RLS)** enabled on all tables
- Students can only access their own data
- Teachers can manage their subjects and students
- Parents can view their children's data
- Admins have full access
- Email authentication with password reset
- Secure API key handling via BuildConfig

## 📊 Available Repositories

### AuthManager
- `signUp()` - Register new user
- `signIn()` - Login user
- `signOut()` - Logout
- `resetPassword()` - Send password reset email
- `isLoggedIn()` - Check login status
- `getCurrentUserId()` - Get logged-in user ID

### ProfileRepository
- `getProfile()` - Get user profile by ID
- `getProfileByStudentId()` - Get profile by student ID
- `getProfilesByRole()` - Get all users of a role
- `updateProfile()` - Update profile information
- `searchProfiles()` - Search users by name

### SubjectRepository
- `getAllSubjects()` - Get all subjects
- `getSubject()` - Get subject by ID
- `getSubjectsByTeacher()` - Get teacher's subjects
- `createSubject()` - Add new subject
- `updateSubject()` - Update subject
- `deleteSubject()` - Remove subject

### ResultRepository
- `getStudentResults()` - Get all results for a student
- `getResultsBySubject()` - Get results for specific subject
- `getSubjectResults()` - Get all results in a subject (teacher view)
- `addResult()` - Add new result
- `updateResult()` - Update result
- `deleteResult()` - Remove result

### AttendanceRepository
- `getStudentAttendance()` - Get student's attendance
- `getAttendanceBySubject()` - Get attendance for a subject
- `getAttendanceByDate()` - Get attendance for specific date
- `markAttendance()` - Mark single attendance
- `markBulkAttendance()` - Mark multiple attendances
- `updateAttendance()` - Update attendance record
- `getAttendanceStats()` - Get attendance statistics

### AnnouncementRepository
- `getAllAnnouncements()` - Get all announcements
- `getAnnouncementsByRole()` - Get role-specific announcements
- `getImportantAnnouncements()` - Get important notices
- `createAnnouncement()` - Post new announcement
- `updateAnnouncement()` - Update announcement
- `deleteAnnouncement()` - Remove announcement

## 🔧 Troubleshooting

### Gradle Sync Issues
```bash
# Clear Gradle cache
./gradlew clean

# In Android Studio:
File → Invalidate Caches and Restart
```

### Connection Issues
- Check internet connection
- Verify Supabase URL and key in `local.properties`
- Ensure Supabase project is active
- Check Supabase status: https://status.supabase.com

### Authentication Issues
- Enable Email auth in Supabase Dashboard → Authentication → Providers
- Check email is verified (for production)
- Verify RLS policies are created

### Database Issues
- Check RLS policies in Supabase Dashboard → Authentication → Policies
- Verify user is logged in before database operations
- Check Supabase logs for errors

## 📚 Documentation

- **Setup Guide**: See `SUPABASE_SETUP.md` for detailed setup instructions
- **Migration Guide**: See `MIGRATION_GUIDE.md` for migrating from SQLite
- **Usage Examples**: See `SupabaseUsageExample.java` for complete examples
- **Supabase Docs**: https://supabase.com/docs

## 🎯 Next Steps

1. ✅ Set up Supabase project
2. ✅ Configure credentials
3. ✅ Run database schema
4. ✅ Test authentication
5. [ ] Migrate existing activities
6. [ ] Test all features
7. [ ] Deploy to production

## 📞 Support

- **Supabase Documentation**: https://supabase.com/docs
- **Supabase Discord**: https://discord.supabase.com
- **GitHub Issues**: Report issues in your repository

## ⚠️ Important Notes

- Never commit `local.properties` to version control
- Keep your Supabase anon key secure
- Use service role key only on server-side (never in mobile app)
- Enable RLS policies before going to production
- Test thoroughly with different user roles
- Set up proper email SMTP for production authentication

## 🎓 Learning Resources

- [Supabase Android Tutorial](https://supabase.com/docs/guides/getting-started/tutorials/with-kotlin)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

---

**Created**: November 26, 2025  
**Version**: 1.0  
**Status**: Ready for Integration

