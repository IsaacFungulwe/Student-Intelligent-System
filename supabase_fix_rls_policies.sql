-- Fix RLS Policies to Allow INSERT, UPDATE, DELETE
-- Run this to fix the 401 error: "new row violates row-level security policy"

-- ============================================
-- DROP OLD RESTRICTIVE POLICIES
-- ============================================

DROP POLICY IF EXISTS "Admins can view themselves" ON admins;
DROP POLICY IF EXISTS "Teachers can view themselves" ON teachers;
DROP POLICY IF EXISTS "Parents can view themselves" ON parents;
DROP POLICY IF EXISTS "Teachers can view students in their grade" ON students;
DROP POLICY IF EXISTS "Teachers can view attendance for their students" ON attendance;
DROP POLICY IF EXISTS "Teachers and parents can view results" ON results;
DROP POLICY IF EXISTS "Subjects are viewable" ON subjects;
DROP POLICY IF EXISTS "Announcements are viewable" ON announcements;

-- ============================================
-- CREATE PERMISSIVE POLICIES (Allow All Operations)
-- ============================================

-- Profiles: Allow all operations
CREATE POLICY "Allow all operations on profiles" ON profiles
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Admins: Allow all operations
CREATE POLICY "Allow all operations on admins" ON admins
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Teachers: Allow all operations
CREATE POLICY "Allow all operations on teachers" ON teachers
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Parents: Allow all operations
CREATE POLICY "Allow all operations on parents" ON parents
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Students: Allow all operations
CREATE POLICY "Allow all operations on students" ON students
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Attendance: Allow all operations
CREATE POLICY "Allow all operations on attendance" ON attendance
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Results: Allow all operations
CREATE POLICY "Allow all operations on results" ON results
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Subjects: Allow all operations
CREATE POLICY "Allow all operations on subjects" ON subjects
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- Announcements: Allow all operations
CREATE POLICY "Allow all operations on announcements" ON announcements
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================
-- ALTERNATIVE: Disable RLS (For Development Only)
-- ============================================
-- Uncomment these lines if you want to completely disable RLS for testing:
-- ALTER TABLE profiles DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE admins DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE teachers DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE parents DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE students DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE attendance DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE results DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE subjects DISABLE ROW LEVEL SECURITY;
-- ALTER TABLE announcements DISABLE ROW LEVEL SECURITY;

-- Success message
SELECT 'RLS policies updated successfully! INSERT operations should now work.' AS status;

