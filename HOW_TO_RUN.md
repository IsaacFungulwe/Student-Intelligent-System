# 🚀 How to Run the Student Intelligent System

## ✅ Pre-Run Checklist

- [x] API key added to `local.properties`
- [x] Internet permission added to AndroidManifest
- [x] Gemini AI dependencies configured
- [x] BuildConfig setup complete

## 📱 Running the Project in Android Studio

### Step 1: Sync Gradle Dependencies

1. **Open Android Studio**
2. **Open the project** if not already open:
   - File > Open
   - Navigate to: `/home/violet-nyirenda/AndroidStudioProjects/Student-Intelligent-System`
   
3. **Sync Gradle** (IMPORTANT - First Time):
   - Look for a banner at the top saying "Gradle files have changed"
   - Click **"Sync Now"**
   - OR: File > Sync Project with Gradle Files
   - Wait for sync to complete (may take 1-2 minutes)
   
   This will:
   - Download Gemini AI SDK (~50MB)
   - Download Guava and Reactive Streams libraries
   - Generate BuildConfig with your API key

### Step 2: Clean & Build

1. **Clean the project**:
   - Menu: Build > Clean Project
   - Wait for completion

2. **Rebuild the project**:
   - Menu: Build > Rebuild Project
   - This ensures all new code is compiled
   - Wait for "BUILD SUCCESSFUL"

### Step 3: Set Up Emulator or Device

**Option A: Use Android Emulator**
1. Tools > Device Manager
2. Create a device if none exists:
   - Click "Create Device"
   - Choose: Pixel 6 or similar
   - System Image: API 34 (Android 14) or higher
   - Click "Finish"
3. Start the emulator by clicking ▶️ play button

**Option B: Use Physical Device**
1. Enable Developer Options on your phone
2. Enable USB Debugging
3. Connect via USB
4. Allow debugging when prompted

### Step 4: Run the App

1. **Select run configuration**:
   - Top toolbar: Select "app" from dropdown
   - Select your device/emulator

2. **Click Run** (Green ▶️ button):
   - Or: Run > Run 'app'
   - Or: Shift + F10

3. **Wait for build and installation**:
   - Gradle will build the APK
   - App will install on device/emulator
   - App will launch automatically

### Step 5: Test the App

**Initial Login:**
- The app will open to LoginActivity
- You'll need to register users first

**Register Users (in order):**

1. **Register Admin:**
   - Click "Register as Admin"
   - Fill in school details
   - Set email and password

2. **Register Teacher:**
   - Login as Admin
   - Register a teacher
   - Logout

3. **Register Parent:**
   - Login as Admin  
   - Register a parent
   - Logout

4. **Register Student:**
   - Login as Teacher
   - Register a student (link to parent)
   - Add subjects
   - Add results with comments
   - Mark attendance

**Test AI Analysis:**

1. **Login as Parent**
2. **Navigate to Dashboard**
3. **Click "AI Performance Analysis" card**
4. **Select a student**
5. **Click "Analyze with AI"**
6. **Wait 5-10 seconds** for AI to generate report
7. **View comprehensive analysis**

## 🐛 Troubleshooting

### Build Errors

**"Cannot resolve symbol 'BuildConfig'"**
```
Fix: Sync Gradle (File > Sync Project with Gradle Files)
```

**"Cannot resolve symbol 'ai'"**
```
Fix: Gradle dependencies not downloaded yet
Solution: Wait for Gradle sync to complete
Check: Bottom status bar should say "Gradle sync finished"
```

**"SDK location not found"**
```
Fix: Check local.properties has correct SDK path
Should be: sdk.dir=/home/violet-nyirenda/Android/Sdk
```

### Runtime Errors

**"API key is empty"**
```
Fix: Check local.properties has GEMINI_API_KEY line
Rebuild project after editing local.properties
```

**"No internet connection"**
```
Fix: Ensure emulator/device has internet
Emulator: Should have internet by default
Device: Check WiFi/Mobile data enabled
```

**"No academic data available"**
```
Fix: Add student results first (login as Teacher)
Need at least one result with marks
Attendance is optional but recommended
```

**Database errors**
```
Fix: Clear app data
Settings > Apps > Student Intelligent System > Storage > Clear Data
Or: Uninstall and reinstall the app
```

### Gradle Sync Issues

**"Download failed"**
```
Fix: Check internet connection
Try: File > Invalidate Caches / Restart
```

**"Build configuration error"**
```
Fix: Check build.gradle.kts syntax is correct
Try: File > Sync Project with Gradle Files
```

## 📊 Expected Behavior

### First Launch:
- Login screen appears
- Database is created (version 6)
- No users exist yet

### After Registration:
- Admin can register teachers and parents
- Teachers can register students
- Teachers can add results with comments
- Parents can view results and AI analysis

### AI Analysis:
- Takes 5-10 seconds to generate
- Requires internet connection
- Student must have at least one result
- Shows 6 sections of analysis

## 🔍 Viewing Logs

**Android Studio Logcat:**
1. View > Tool Windows > Logcat
2. Select your device
3. Filter by: `package:com.example.studentintelligentsystem`
4. Look for errors or warnings

**Search for specific logs:**
- Gemini API: Filter by "Gemini" or "GenerativeModel"
- Database: Filter by "DatabaseHelper"
- Activities: Filter by activity name (e.g., "PerformanceAnalysis")

## 📱 App Features to Test

### Teacher Features:
- [x] Register students
- [x] Manage subjects
- [x] Add results with comments
- [x] Mark attendance
- [x] View announcements

### Parent Features:
- [x] View children list
- [x] View results
- [x] View attendance
- [x] **NEW: AI Performance Analysis**
- [x] View announcements

### Admin Features:
- [x] Register teachers
- [x] Register parents
- [x] Post announcements
- [x] View dashboard

## 🎯 Testing the AI Integration

### Test Scenario 1: Good Performance
```
Student: John Doe
Math: 85%
English: 90%
Science: 88%
Attendance: 95%
Comment: "Excellent student, participates actively"

Expected AI Analysis:
- Strong overall performance
- High attendance correlation
- Positive behavioral feedback
- Encouragement to maintain standards
```

### Test Scenario 2: Needs Improvement
```
Student: Jane Smith
Math: 45%
English: 50%
Science: 38%
Attendance: 60%
Comment: "Often distracted, needs more focus"

Expected AI Analysis:
- Identify weak subjects (Math, Science)
- Attendance-performance correlation
- Behavioral issues identified
- Specific improvement recommendations
```

## ⚡ Quick Run Commands

**From Terminal (if Android Studio UI has issues):**
```bash
cd /home/violet-nyirenda/AndroidStudioProjects/Student-Intelligent-System

# Sync and build
./gradlew clean build

# Install on connected device
./gradlew installDebug

# Run on device
adb shell am start -n com.example.studentintelligentsystem/.LoginActivity
```

## 📞 Getting Help

If issues persist:
1. Check Logcat for error messages
2. Review GEMINI_SETUP.md for detailed setup
3. Verify all files from API_KEY_MIGRATION_SUMMARY.md
4. Try: File > Invalidate Caches / Restart

## ✅ Success Indicators

You'll know it's working when:
- ✅ App launches without crashes
- ✅ Can register and login users
- ✅ Can add results with comments
- ✅ AI Analysis button appears on Parent Dashboard
- ✅ AI generates analysis report (5-10 seconds)
- ✅ Report shows all 6 sections with detailed insights

---

**Good luck! The app is ready to run.** 🚀

Next step: Click the green ▶️ Run button in Android Studio!

