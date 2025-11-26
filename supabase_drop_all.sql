-- Drop All Tables Script
-- Run this FIRST to clean up before running the migration

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS sync_logs CASCADE;
DROP TABLE IF EXISTS announcements CASCADE;
DROP TABLE IF EXISTS subjects CASCADE;
DROP TABLE IF EXISTS results CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS parents CASCADE;
DROP TABLE IF EXISTS teachers CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;

-- Drop any existing policies
DROP POLICY IF EXISTS "Admins can view themselves" ON admins;
DROP POLICY IF EXISTS "Teachers can view themselves" ON teachers;
DROP POLICY IF EXISTS "Parents can view themselves" ON parents;
DROP POLICY IF EXISTS "Teachers can view students in their grade" ON students;
DROP POLICY IF EXISTS "Teachers can view attendance for their students" ON attendance;
DROP POLICY IF EXISTS "Teachers and parents can view results" ON results;
DROP POLICY IF EXISTS "Subjects are viewable" ON subjects;
DROP POLICY IF EXISTS "Announcements are viewable" ON announcements;

-- Success message
SELECT 'All tables dropped successfully! Now run supabase_migration_fixed.sql' AS status;

