#!/bin/bash

# Documentation Organization Script
# This script moves all documentation files into organized folders

echo "📚 Organizing Student Intelligent System Documentation..."

# Create folder structure
mkdir -p docs/setup
mkdir -p docs/guides
mkdir -p docs/features
mkdir -p docs/supabase
mkdir -p docs/sync
mkdir -p docs/database
mkdir -p docs/fixes
mkdir -p docs/summaries
mkdir -p docs/reference

echo "✅ Created folder structure"

# Move setup files
mv START_HERE_SUPABASE.md docs/setup/ 2>/dev/null
mv SETUP_CHECKLIST.md docs/setup/ 2>/dev/null
mv API_KEY_SETUP.md docs/setup/ 2>/dev/null
mv SUPABASE_CREDENTIALS_SETUP.md docs/setup/ 2>/dev/null
mv SUPABASE_CONNECTION_SETUP.md docs/setup/ 2>/dev/null

echo "✅ Moved setup files"

# Move guide files
mv QUICKSTART.md docs/guides/ 2>/dev/null
mv HOW_TO_RUN.md docs/guides/ 2>/dev/null
mv RUN_NOW.md docs/guides/ 2>/dev/null

echo "✅ Moved guide files"

# Move feature files
mv ATTENDANCE_ANIMATION_FEATURE.md docs/features/ 2>/dev/null
mv VIEW_EDIT_RESULTS_FEATURE.md docs/features/ 2>/dev/null
mv SETUP_ATTENDANCE_ANIMATION.md docs/features/ 2>/dev/null
mv SETUP_VIEW_EDIT_RESULTS.md docs/features/ 2>/dev/null
mv MULTI_DEVICE_LOGIN_IMPLEMENTATION.md docs/features/ 2>/dev/null

echo "✅ Moved feature files"

# Move Supabase files
mv SUPABASE_DOCUMENTATION_INDEX.md docs/supabase/ 2>/dev/null
mv SUPABASE_INTEGRATION.md docs/supabase/ 2>/dev/null
mv SUPABASE_IMPLEMENTATION_COMPLETE.md docs/supabase/ 2>/dev/null
mv SUPABASE_IMPLEMENTATION_DETAILS.md docs/supabase/ 2>/dev/null
mv SUPABASE_IMPLEMENTATION_SUMMARY.md docs/supabase/ 2>/dev/null
mv SUPABASE_SETUP.md docs/supabase/ 2>/dev/null
mv SUPABASE_QUICK_START.md docs/supabase/ 2>/dev/null
mv SUPABASE_INTEGRATION_SUMMARY.md docs/supabase/ 2>/dev/null
mv SUPABASE_LOGGING_GUIDE.md docs/supabase/ 2>/dev/null
mv SUPABASE_FIX_GUIDE.md docs/supabase/ 2>/dev/null

echo "✅ Moved Supabase files"

# Move sync files
mv AUTO_SYNC_ON_LAUNCH_COMPLETE.md docs/sync/ 2>/dev/null
mv INTELLIGENT_UPSERT_IMPLEMENTATION.md docs/sync/ 2>/dev/null
mv COMPLETE_SYNC_VERIFICATION_GUIDE.md docs/sync/ 2>/dev/null
mv FIX_SUBJECT_SYNC_AND_FOREIGN_KEY.md docs/sync/ 2>/dev/null

echo "✅ Moved sync files"

# Move database files
mv RUN_SQL_GUIDE.md docs/database/ 2>/dev/null
mv SQL_SCHEMA_FIXED.md docs/database/ 2>/dev/null
mv SQL_2_STEP_SOLUTION.md docs/database/ 2>/dev/null

echo "✅ Moved database files"

# Move fix files
mv FIX_DATABASE_HELPER.md docs/fixes/ 2>/dev/null
mv FIX_RESULT_CLASS_ERROR.md docs/fixes/ 2>/dev/null
mv FIX_ATTENDANCE_NOT_NULL_ERROR.md docs/fixes/ 2>/dev/null
mv FIX_SUPABASE_MIGRATION.md docs/fixes/ 2>/dev/null

echo "✅ Moved fix files"

# Move summary files
mv COMPLETE_SOLUTION_FINAL.md docs/summaries/ 2>/dev/null
mv IMPLEMENTATION_SUMMARY.md docs/summaries/ 2>/dev/null
mv API_KEY_MIGRATION_SUMMARY.md docs/summaries/ 2>/dev/null

echo "✅ Moved summary files"

# Move reference files
mv QUICK_REFERENCE.md docs/reference/ 2>/dev/null
mv MIGRATION_GUIDE.md docs/reference/ 2>/dev/null
mv NEXT_STEPS.md docs/reference/ 2>/dev/null
mv GEMINI_SETUP.md docs/reference/ 2>/dev/null
mv FILE_LISTING_COMPLETE.md docs/reference/ 2>/dev/null
mv SUPABASE_COMPLETE_SUMMARY.txt docs/reference/ 2>/dev/null
mv SQL_READY_TO_RUN.txt docs/reference/ 2>/dev/null
mv FINAL_SOLUTION.txt docs/reference/ 2>/dev/null

echo "✅ Moved reference files"

echo ""
echo "🎉 Documentation organization complete!"
echo ""
echo "📂 Documentation structure:"
echo "   docs/"
echo "   ├── README.md (documentation index)"
echo "   ├── setup/     (8 files)"
echo "   ├── guides/    (3 files)"
echo "   ├── features/  (5 files)"
echo "   ├── supabase/  (10 files)"
echo "   ├── sync/      (4 files)"
echo "   ├── database/  (3 files)"
echo "   ├── fixes/     (4 files)"
echo "   ├── summaries/ (3 files)"
echo "   └── reference/ (8 files)"
echo ""
echo "📖 Start with: docs/README.md"

