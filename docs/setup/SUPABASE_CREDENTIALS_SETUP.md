# Supabase Credentials Setup - Step by Step

## ⚡ Quick Setup (5 Minutes)

Follow these exact steps to get your Supabase credentials and configure the app.

---

## Step 1: Create Supabase Account

1. Open your browser and go to: **https://supabase.com**
2. Click **"Start your project"** or **"Sign In"**
3. Sign up using:
   - GitHub (recommended - fastest)
   - Or email/password

---

## Step 2: Create New Project

1. After logging in, click **"New Project"**
2. Fill in the form:
   ```
   Name: Student-Intelligent-System
   Database Password: [Create a strong password - SAVE THIS!]
   Region: [Choose closest to you - e.g., "South Africa (Cape Town)"]
   Pricing Plan: Free
   ```
3. Click **"Create new project"**
4. ⏳ Wait 2-3 minutes for project to initialize

---

## Step 3: Get Your Credentials

### A. Get Project URL

1. In your project dashboard, look at the top-left
2. You'll see your project name and a URL like:
   ```
   https://abcdefghijklmnop.supabase.co
   ```
3. **Copy this entire URL** (you'll need it in Step 5)

### B. Get API Key

1. Click the **⚙️ Settings** icon in the left sidebar (gear icon at bottom)
2. Click **"API"** in the Settings menu
3. Find the section **"Project API keys"**
4. Look for **"anon" "public"** key
5. Click the **👁️ eye icon** to reveal the key
6. Click **📋 copy icon** to copy it
7. It will look something like:
   ```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiY2RlZmdoaWprbG1ub3AiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYzMjc0ODE2NCwiZXhwIjoxOTQ4MzI0MTY0fQ.abcdefghijklmnopqrstuvwxyz1234567890
   ```

---

## Step 4: Setup Database Schema

1. In your Supabase project, click **"SQL Editor"** in the left sidebar
2. Click **"New query"** button
3. Open the file `supabase_schema.sql` in your project root
4. **Copy ALL the content** from that file (Ctrl+A, Ctrl+C)
5. **Paste it** into the SQL Editor in Supabase
6. Click **"Run"** button (or press Ctrl+Enter)
7. ✅ You should see: **"Success. No rows returned"**
8. Verify tables were created:
   - Click **"Table Editor"** in left sidebar
   - You should see: profiles, subjects, enrollments, results, attendance, announcements

---

## Step 5: Configure Your Android App

### A. Update local.properties

1. Open your project in Android Studio
2. In the **Project** view (left sidebar), find **`local.properties`**
3. If it doesn't exist:
   - Right-click on the project root folder
   - Select **New → File**
   - Name it: `local.properties`
4. Open `local.properties` and add these lines:

```properties
# Android SDK (should already be there)
sdk.dir=/path/to/your/Android/Sdk

# Gemini API Key (should already be there)
GEMINI_API_KEY=your-existing-gemini-key

# ADD THESE LINES:
# Replace with your actual values from Step 3
SUPABASE_URL=https://abcdefghijklmnop.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiY2RlZmdoaWprbG1ub3AiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYzMjc0ODE2NCwiZXhwIjoxOTQ4MzI0MTY0fQ.abcdefghijklmnopqrstuvwxyz1234567890
```

5. **Replace the example values** with your actual credentials from Step 3
6. Save the file (Ctrl+S)

---

## Step 6: Enable Email Authentication

1. In Supabase dashboard, click **"Authentication"** in left sidebar
2. Click **"Providers"** tab
3. Find **"Email"** in the list
4. Make sure it's **Enabled** (toggle should be green/on)
5. Click **"Save"** if you made changes

### Optional: Email Templates

For production use, configure email templates:
1. Go to **Authentication → Email Templates**
2. Customize the templates for:
   - Confirm signup
   - Magic link
   - Reset password

---

## Step 7: Sync and Build Project

1. In Android Studio, click: **File → Sync Project with Gradle Files**
2. Wait for sync to complete (watch bottom-right corner)
3. If you see any errors:
   - Check that `local.properties` has correct format
   - Make sure you saved the file
   - Try: **File → Invalidate Caches and Restart**
4. Once sync succeeds, click: **Build → Make Project**
5. ✅ Build should succeed!

---

## Step 8: Test Connection (Optional but Recommended)

Add this code to test your setup:

```java
// In any Activity's onCreate() method
if (SupabaseConfig.isConfigured()) {
    Log.d("Supabase", "✅ Supabase is configured correctly!");
    Toast.makeText(this, "Supabase connected!", Toast.LENGTH_SHORT).show();
} else {
    Log.e("Supabase", "❌ Supabase configuration missing!");
    Toast.makeText(this, "Please configure Supabase", Toast.LENGTH_SHORT).show();
}
```

---

## ✅ Verification Checklist

- [ ] Supabase account created
- [ ] Project created and initialized
- [ ] Project URL copied
- [ ] API anon key copied
- [ ] Database schema executed successfully
- [ ] Tables visible in Table Editor
- [ ] Email authentication enabled
- [ ] `local.properties` updated with credentials
- [ ] Gradle sync completed successfully
- [ ] Project builds without errors

---

## 🎯 What You've Just Set Up

### Backend (Supabase)
- ✅ PostgreSQL database with 8 tables
- ✅ Row Level Security policies
- ✅ Email authentication system
- ✅ Real-time capabilities
- ✅ File storage ready

### Android App
- ✅ Supabase client configured
- ✅ Authentication manager ready
- ✅ 5 repository classes for data operations
- ✅ Complete data models
- ✅ Secure credential handling

---

## 🚀 Next Steps

Now you can:

1. **Test Authentication**
   - Use `AuthManager` to sign up/sign in users
   - See example in `SupabaseUsageExample.java`

2. **Migrate Existing Code**
   - Follow `MIGRATION_GUIDE.md`
   - Update your Activities to use Supabase repositories

3. **Add Test Data**
   - Create test users via Supabase dashboard
   - Or use the app's registration screens

---

## 🆘 Troubleshooting

### "Cannot resolve symbol 'Supabase'"
**Fix**: Sync Gradle again: File → Sync Project with Gradle Files

### "URL or Key is empty"
**Fix**: Check `local.properties` file - ensure no typos, save file, sync Gradle

### "Table does not exist"
**Fix**: Re-run the `supabase_schema.sql` in SQL Editor

### "Authentication error"
**Fix**: Enable Email provider in Authentication → Providers

### "Build failed"
**Fix**: Check that Kotlin plugin version matches (1.9.21) in build.gradle.kts

---

## 🔐 Security Reminders

- ✅ **DO**: Keep `local.properties` in `.gitignore`
- ✅ **DO**: Use the anon key (safe for client apps)
- ❌ **DON'T**: Commit `local.properties` to Git
- ❌ **DON'T**: Share your service_role key publicly
- ❌ **DON'T**: Use service_role key in mobile apps

---

## 📱 Example: Your First Supabase Operation

Try this in any Activity:

```java
// 1. Create manager
AuthManager authManager = new AuthManager();

// 2. Register a user
new Thread(() -> {
    kotlin.Result<String> result = authManager.signUp(
        "test@example.com",
        "password123",
        "Test User",
        "student"
    );
    
    runOnUiThread(() -> {
        if (result.isSuccess()) {
            Log.d("Success", "User registered!");
            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("Error", result.exceptionOrNull().getMessage());
        }
    });
}).start();
```

---

## 📚 Documentation

- **Quick Reference**: `SUPABASE_INTEGRATION.md`
- **Detailed Setup**: `SUPABASE_SETUP.md`
- **Migration Guide**: `MIGRATION_GUIDE.md`
- **Usage Examples**: `app/.../examples/SupabaseUsageExample.java`

---

## 💡 Pro Tips

1. **Use Supabase Studio**: Manage data visually in Table Editor
2. **Check Logs**: Supabase Dashboard → Logs for debugging
3. **Test RLS**: Try accessing data from different user accounts
4. **Backup**: Export your database schema regularly
5. **Monitor**: Check API usage in project settings

---

**Setup Time**: ~5 minutes  
**Difficulty**: Easy  
**Status**: Ready to use! 🎉

Need help? Check the other documentation files or Supabase Discord community.

