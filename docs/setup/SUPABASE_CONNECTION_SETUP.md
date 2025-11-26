# Supabase Connection Setup Guide

## Prerequisites
- Supabase account (https://supabase.com)
- Student Intelligent System Android project
- Android Studio

## Step 1: Create a Supabase Project

1. Go to https://supabase.com
2. Sign in or create an account
3. Click "New Project"
4. Fill in:
   - **Project Name**: `student-intelligent-system` (or your choice)
   - **Database Password**: Create a strong password (save it!)
   - **Region**: Select closest to your location
5. Click "Create new project" and wait for initialization (~2 minutes)

## Step 2: Get Your Credentials

Once your project is created:

1. Go to **Settings** → **API** in the sidebar
2. Find these values:
   - **URL**: Your Supabase project URL (looks like: `https://xyzabc.supabase.co`)
   - **anon (public)**: Your anonymous public key

3. **Example values** (replace with yours):
```
URL: https://abcdefgh.supabase.co
anon key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Step 3: Configure local.properties

1. Open **local.properties** file in your Android project root directory
   ```
   /home/violet-nyirenda/AndroidStudioProjects/Student-Intelligent-System/local.properties
   ```

2. Add these lines:
   ```properties
   SUPABASE_URL=https://your-project-id.supabase.co
   SUPABASE_ANON_KEY=your-anon-public-key-here
   ```

3. **Example**:
   ```properties
   SUPABASE_URL=https://abcdefgh.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiJhbm9u...
   ```

4. **Do NOT commit this file to Git** - it contains sensitive data

## Step 4: Set Up Database Schema

1. In your Supabase dashboard, go to **SQL Editor**
2. Click **"New query"**
3. Copy the entire content from: `supabase_migration.sql`
4. Paste it into the SQL editor
5. Click **"Run"** (or Cmd+Enter)
6. Wait for the schema to be created

**Expected Output**:
```
CREATE EXTENSION
CREATE TABLE
CREATE TABLE
... (multiple tables created)
CREATE INDEX
CREATE POLICY
... (policies created)
```

## Step 5: Verify Database Schema

1. Go to **Table Editor** in sidebar
2. You should see these tables:
   - `profiles`
   - `admins`
   - `teachers`
   - `parents`
   - `students`
   - `attendance`
   - `results`
   - `subjects`
   - `announcements`
   - `sync_logs`

If all tables appear, schema setup is successful! ✓

## Step 6: Build and Run the App

1. In Android Studio, open the project
2. Click **Build** → **Clean Project**
3. Click **Build** → **Rebuild Project**
4. Connect an Android device or emulator
5. Click **Run** → **Run 'app'**

## Step 7: Monitor Supabase Logs

Open Android Logcat and filter by:

```bash
# View all initialization logs
adb logcat | grep "StudentIntelligent"

# Watch for success messages
adb logcat | grep "✓"

# Watch for any errors
adb logcat | grep "Error"
```

**Expected logs on app startup**:
```
[StudentIntelligentApp] Application starting...
[StudentIntelligentApp] Initializing Supabase connection...
[StudentIntelligentApp] ✓ Supabase initialized successfully!
[StudentIntelligentApp] ✓ Supabase connection test SUCCESSFUL
[SupabaseClient] Connection test response code: 200
```

## Step 8: Test Data Synchronization

1. **Register a new student**:
   - Login as Teacher
   - Register a student
   - Check logs for: `✓ Student sync initiated to Supabase`

2. **Mark attendance**:
   - Mark attendance for a student
   - Check logs for: `✓ Attendance sync initiated to Supabase`

3. **Add results**:
   - Add results for a student
   - Check logs for: `✓ Result sync initiated to Supabase`

4. **Verify in Supabase**:
   - Go to **Table Editor**
   - Click on `students` table
   - You should see your new student record!

## Step 9: Configure Row Level Security (RLS)

For production, you should configure proper RLS policies:

1. Go to **Authentication** → **Policies** in Supabase
2. For each table, set up policies:
   - Teachers can only see their own students
   - Parents can only see their own children
   - Students can only see their own records

**Example Policy** (for your reference):
```sql
-- Teachers can only view students in their grade
CREATE POLICY "Teachers view own students" ON students
    FOR SELECT
    USING (teacher_id = (SELECT id FROM teachers WHERE id = auth.uid()));
```

## Troubleshooting

### "Failed to get Supabase credentials"
**Issue**: SUPABASE_URL or SUPABASE_ANON_KEY missing
**Solution**:
- Check `local.properties` file
- Verify both values are present
- Ensure no typos
- Rebuild the project

### "Connection test FAILED"
**Issue**: Cannot connect to Supabase
**Solution**:
1. Check internet connection
2. Verify credentials are correct
3. Check if Supabase project is running
4. Check firewall settings

### "Sync is not working"
**Issue**: Data not appearing in Supabase
**Solution**:
1. Check Logcat for error messages
2. Verify schema is created correctly
3. Check RLS policies aren't blocking inserts
4. Verify API key has correct permissions

### "Premature end of file" or "ParseError" in XML files
**Issue**: XML resource files are malformed
**Solution**:
1. Clean project: `Build` → `Clean Project`
2. Rebuild: `Build` → `Rebuild Project`
3. Check XML files for syntax errors
4. Verify all tags are properly closed

## Security Checklist

- [ ] `local.properties` is NOT committed to Git
- [ ] `.gitignore` includes `local.properties`
- [ ] API key has restricted permissions in Supabase
- [ ] RLS policies are configured
- [ ] Never log sensitive data
- [ ] Use HTTPS only (Supabase handles this)

## What's Working Now

✓ App initializes Supabase on startup
✓ Logs all Supabase operations
✓ Student registration syncs to Supabase
✓ Attendance marking syncs to Supabase
✓ Results addition syncs to Supabase
✓ Announcements sync to Supabase (when implemented)
✓ Background thread sync (no UI blocking)
✓ Error logging for debugging

## Next Steps

1. **Implement User Authentication** using Supabase Auth
2. **Add Offline Support** with local queue for offline operations
3. **Implement Conflict Resolution** for offline changes
4. **Add Progress Notifications** for sync operations
5. **Implement Batch Sync** for better performance
6. **Add Analytics** for usage tracking

## Support Resources

- **Supabase Documentation**: https://supabase.com/docs
- **Android HTTP Requests**: https://developer.android.com/studio/write/java-8-support
- **Logcat Guide**: https://developer.android.com/studio/debug/logcat
- **Git Setup for local.properties**: https://git-scm.com/docs/gitignore

## Quick Reference Commands

```bash
# View all app logs
adb logcat | grep "StudentIntelligent"

# View Supabase client logs
adb logcat | grep "SupabaseClient"

# View sync manager logs
adb logcat | grep "SupabaseSyncManager"

# Clear logcat
adb logcat -c

# Save logs to file
adb logcat > logs.txt
```

