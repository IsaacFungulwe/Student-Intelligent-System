package com.example.studentintelligentsystem.supabase.examples;

/*
 * This is an EXAMPLE file showing how to use Supabase repositories.
 * It is NOT used in the actual application build.
 * Refer to the documentation for proper implementation in your Activities.
 *
 * See: SUPABASE_INTEGRATION.md for usage examples
 *
 * To use this file:
 * 1. Uncomment all the code below
 * 2. Adapt to your specific Activity
 * 3. Use Kotlin coroutines or RxJava for async operations
 */

// All code commented out to prevent build errors
/*
public class SupabaseUsageExample {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Example: Check if user is logged in
        checkAuthStatus();

        // Example: Sign up a new user
        // signUpExample();

        // Example: Sign in
        // signInExample();

        // Example: Load user profile
        // loadProfileExample();

        // Example: Load subjects
        // loadSubjectsExample();

        // Example: Load student results
        // loadResultsExample();

        // Example: Mark attendance
        // markAttendanceExample();

        // Example: Load announcements
        // loadAnnouncementsExample();
    }

    // ========== AUTHENTICATION EXAMPLES ==========

    private void checkAuthStatus() {
        if (authManager.isLoggedIn()) {
            String userId = authManager.getCurrentUserId();
            String email = authManager.getCurrentUserEmail();
            Log.d(TAG, "User is logged in: " + email + " (ID: " + userId + ")");
            Toast.makeText(this, "Welcome back, " + email, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "User is not logged in");
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
        }
    }

    private void signUpExample() {
        String email = "student@example.com";
        String password = "securePassword123";
        String fullName = "John Doe";
        String role = "student";

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return authManager.signUp(email, password, fullName, role);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User registered successfully");
                } else {
                    Toast.makeText(this, "Sign up failed: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Sign up error", throwable);
                }
            });
            return null;
        });
    }

    private void signInExample() {
        String email = "student@example.com";
        String password = "securePassword123";

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return authManager.signIn(email, password);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    String userId = authManager.getCurrentUserId();
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User logged in: " + userId);

                    // Load user data after login
                    loadProfileExample();
                } else {
                    Toast.makeText(this, "Login failed: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Login error", throwable);
                }
            });
            return null;
        });
    }

    // ========== PROFILE EXAMPLES ==========

    private void loadProfileExample() {
        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return profileRepo.getProfile(userId);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    // Handle success - update UI with profile data
                    Log.d(TAG, "Profile loaded successfully");
                    // profile.getFullName(), profile.getStudentId(), etc.
                } else {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Profile load error", throwable);
                }
            });
            return null;
        });
    }

    // ========== SUBJECT EXAMPLES ==========

    private void loadSubjectsExample() {
        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return subjectRepo.getAllSubjects();
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    // Handle success - display subjects in RecyclerView
                    Log.d(TAG, "Subjects loaded successfully");
                } else {
                    Toast.makeText(this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Subjects load error", throwable);
                }
            });
            return null;
        });
    }

    // ========== RESULTS EXAMPLES ==========

    private void loadResultsExample() {
        String studentId = authManager.getCurrentUserId();
        if (studentId == null) return;

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return resultRepo.getStudentResults(studentId);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    // Handle success - display results
                    Log.d(TAG, "Results loaded successfully");
                } else {
                    Toast.makeText(this, "Failed to load results", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Results load error", throwable);
                }
            });
            return null;
        });
    }

    private void addResultExample(String studentId, String subjectId) {
        Map<String, Object> result = new HashMap<>();
        result.put("student_id", studentId);
        result.put("subject_id", subjectId);
        result.put("exam_type", "midterm");
        result.put("marks_obtained", 85.5);
        result.put("total_marks", 100.0);
        result.put("grade", "B+");
        result.put("exam_date", "2025-11-26");
        result.put("remarks", "Good performance");

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return resultRepo.addResult(result);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    Toast.makeText(this, "Result added successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Result added");
                } else {
                    Toast.makeText(this, "Failed to add result", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Add result error", throwable);
                }
            });
            return null;
        });
    }

    // ========== ATTENDANCE EXAMPLES ==========

    private void markAttendanceExample() {
        String studentId = authManager.getCurrentUserId();
        String subjectId = "subject-uuid-here";
        String teacherId = authManager.getCurrentUserId();

        Map<String, Object> attendance = new HashMap<>();
        attendance.put("student_id", studentId);
        attendance.put("subject_id", subjectId);
        attendance.put("attendance_date", "2025-11-26");
        attendance.put("status", "present");
        attendance.put("marked_by", teacherId);
        attendance.put("remarks", "On time");

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return attendanceRepo.markAttendance(attendance);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    Toast.makeText(this, "Attendance marked", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Attendance marked successfully");
                } else {
                    Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Attendance error", throwable);
                }
            });
            return null;
        });
    }

    // ========== ANNOUNCEMENT EXAMPLES ==========

    private void loadAnnouncementsExample() {
        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return announcementRepo.getAllAnnouncements();
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    // Display announcements in RecyclerView
                    Log.d(TAG, "Announcements loaded successfully");
                } else {
                    Toast.makeText(this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Announcements error", throwable);
                }
            });
            return null;
        });
    }

    private void createAnnouncementExample() {
        String authorId = authManager.getCurrentUserId();

        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", "Important Notice");
        announcement.put("content", "Classes will resume on Monday");
        announcement.put("author_id", authorId);
        announcement.put("target_role", "all");
        announcement.put("is_important", true);

        scope.async(Dispatchers.getIO(), CoroutineStart.DEFAULT, (continuation) -> {
            return announcementRepo.createAnnouncement(announcement);
        }).invokeOnCompletion(throwable -> {
            runOnUiThread(() -> {
                if (throwable == null) {
                    Toast.makeText(this, "Announcement posted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Announcement created");
                } else {
                    Toast.makeText(this, "Failed to post announcement", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Announcement error", throwable);
                }
            });
            return null;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scope.cancel(null);
    }
}
*/

