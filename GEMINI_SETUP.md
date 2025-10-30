# Google Gemini AI Integration Guide

## Overview
This Student Intelligent System now includes AI-powered performance analysis using Google's Gemini API. The system analyzes student grades, attendance, and teacher comments to provide comprehensive insights.

## Features Implemented

### 1. **AI Performance Analysis**
- Analyzes student grades across all subjects
- Detects weak subjects and inconsistent performance
- Correlates attendance with academic performance
- Analyzes teacher comments for behavioral/learning issues
- Generates comprehensive reports with:
  - Performance Overview
  - Attendance Correlation Analysis
  - Behavioral and Learning Issues
  - Possible Causes of Underperformance
  - Suggested Improvement Actions
  - Encouragement and Feedback Message

### 2. **Teacher Comment System**
- Teachers can now add comments when submitting student results
- Comments are stored in the database and analyzed by AI
- Multi-line text input for detailed observations

### 3. **Parent Dashboard Integration**
- New "AI Performance Analysis" card on Parent Dashboard
- Easy access to AI-powered insights for their children
- User-friendly interface with loading indicators

## Setup Instructions

### Step 1: Get Your Gemini API Key

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

### Step 2: Add API Key to the Project

**IMPORTANT:** You need to add your Gemini API key to make the AI analysis work.

There are two service files where you need to add your API key:

#### Option 1: GeminiAnalysisService.java (Recommended - Used by StudentPerformanceAnalysisActivity)
Location: `app/src/main/java/com/example/studentintelligentsystem/GeminiAnalysisService.java`

Find this line:
```java
private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
```

Replace with:
```java
private static final String API_KEY = "YOUR_ACTUAL_API_KEY_HERE";
```

#### Option 2: GeminiAIService.java (Alternative implementation)
Location: `app/src/main/java/com/example/studentintelligentsystem/GeminiAIService.java`

Find this line:
```java
private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
```

Replace with:
```java
private static final String API_KEY = "YOUR_ACTUAL_API_KEY_HERE";
```

### Step 3: Sync Gradle Dependencies

The following dependencies have been added to your project:

```gradle
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
implementation("com.google.guava:guava:31.1-android")
implementation("org.reactivestreams:reactive-streams:1.0.4")
```

**Action Required:**
1. Open your project in Android Studio
2. Click "Sync Now" when prompted, or go to `File > Sync Project with Gradle Files`
3. Wait for the sync to complete

### Step 4: Internet Permission

Internet permission has been added to AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 5: Database Migration

The database has been updated to version 6 with a new `comment` column in the Results table.

**Note:** When you run the app, the database will be automatically recreated with the new schema. This means existing data will be lost. If you need to preserve data, export it before running the updated app.

## How to Use

### For Teachers:
1. Login as Teacher
2. Navigate to "Add Results"
3. Select a student and subject
4. Enter marks and term
5. **NEW:** Add a comment about the student's performance or behavior (optional)
6. Submit the results

### For Parents:
1. Login as Parent
2. On the Dashboard, find the new "AI Performance Analysis" card
3. Click on it to open the analysis screen
4. Select a student from the dropdown
5. Click "Analyze with AI"
6. Wait for the AI to generate a comprehensive performance report

## Files Created/Modified

### New Files:
- `GeminiAnalysisService.java` - Main AI service with detailed analysis prompts
- `GeminiAIService.java` - Alternative AI service implementation
- `StudentPerformanceAnalysisActivity.java` - Activity created but not used (deprecated)
- `PerformanceAnalysisActivity.java` - Active AI analysis activity for parents
- `activity_performance_analysis.xml` - Layout for analysis activity
- `activity_student_performance_analysis.xml` - Deprecated layout
- `ic_ai.xml` - AI icon drawable
- `GEMINI_SETUP.md` - This setup guide

### Modified Files:
- `DatabaseHelper.java` - Added RESULT_COMMENT column, new helper methods
- `AddResultsActivity.java` - Added comment field support
- `activity_add_results.xml` - Added comment input field
- `ParentDashboardActivity.java` - Added AI Analysis card integration
- `activity_parent_dashboard.xml` - Added AI Analysis card UI
- `AndroidManifest.xml` - Added Internet permission and new activity
- `build.gradle.kts` - Added Gemini dependencies
- `libs.versions.toml` - Added dependency versions

## Database Schema Changes

### Results Table:
```sql
CREATE TABLE Results (
    resultId INTEGER PRIMARY KEY AUTOINCREMENT,
    studentId INTEGER NOT NULL,
    subject TEXT NOT NULL,
    term TEXT NOT NULL,
    marks INTEGER NOT NULL,
    comment TEXT,  -- NEW FIELD
    recordedByTeacherId INTEGER NOT NULL,
    FOREIGN KEY(studentId) REFERENCES Student(studentId),
    FOREIGN KEY(recordedByTeacherId) REFERENCES Teacher(teacherId)
)
```

## Troubleshooting

### Error: "Invalid API Key"
- Ensure you've replaced `YOUR_GEMINI_API_KEY_HERE` with your actual API key
- Check that there are no extra spaces or quotes around the key
- Verify the API key is active in Google AI Studio

### Error: "No internet connection"
- Ensure the device/emulator has internet access
- Check that INTERNET permission is in AndroidManifest.xml

### Error: "No academic data available"
- The student must have at least one result recorded
- Teachers need to add results through "Add Results" screen
- Attendance data is optional but recommended for better analysis

### Analysis Takes Too Long
- First analysis may take 5-10 seconds
- Ensure stable internet connection
- Check Firebase/API quotas in Google AI Studio

### Database Not Updating
- Uninstall and reinstall the app to force database recreation
- Or manually increment DATABASE_VERSION in DatabaseHelper.java

## API Usage & Costs

- Google Gemini API offers a free tier with generous quotas
- Current implementation uses "gemini-pro" model
- Each analysis counts as one API call
- Monitor usage at [Google AI Studio](https://makersuite.google.com/)

## Security Best Practices

⚠️ **IMPORTANT:** Never commit your API key to version control!

Better approaches for production:
1. Use BuildConfig to store API key
2. Store in local.properties (gitignored by default)
3. Use environment variables
4. Use a backend server to proxy API calls

## Future Enhancements

Potential improvements:
- Add analysis history/cache
- Support for multiple languages
- Comparison with class averages
- Progress tracking over time
- PDF export of analysis reports
- Teacher-specific analysis views

## Support

For issues or questions:
1. Check error logs in Android Studio Logcat
2. Verify all setup steps completed
3. Test with sample data first
4. Check Google AI Studio for API status

## Credits

- **AI Model:** Google Gemini Pro
- **UI Components:** Material Design 3
- **Charts:** MPAndroidChart library

---

**Last Updated:** October 30, 2025
**Version:** 1.0.0

