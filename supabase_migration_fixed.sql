-- Supabase SQL Schema for Student Intelligent System
-- FIXED VERSION - Tables created in correct dependency order
-- Run this AFTER running supabase_drop_all.sql

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- STEP 1: Create base tables (no dependencies)
-- ============================================

-- Create profiles table (for users)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE,
    user_type TEXT CHECK (user_type IN ('admin', 'teacher', 'parent', 'student')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create admins table (no dependencies)
CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    school_name TEXT NOT NULL,
    district TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- STEP 2: Create tables that depend on admins
-- ============================================

-- Create teachers table (depends on admins)
CREATE TABLE IF NOT EXISTS teachers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    grade_assigned INT NOT NULL,
    admin_id INT NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create parents table (depends on admins)
CREATE TABLE IF NOT EXISTS parents (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    password_hash TEXT NOT NULL,
    admin_id INT NOT NULL REFERENCES admins(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- STEP 3: Create tables that depend on teachers/parents
-- ============================================

-- Create students table (depends on parents and teachers)
CREATE TABLE IF NOT EXISTS students (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    age INT,
    gender TEXT,
    grade INT NOT NULL,
    address TEXT,
    parent_id INT NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    teacher_id INT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- STEP 4: Create tables that depend on students
-- ============================================

-- Create attendance table (depends on students and teachers)
CREATE TABLE IF NOT EXISTS attendance (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('Present', 'Absent')),
    teacher_id INT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, date)
);

-- Create results table (depends on students and teachers)
CREATE TABLE IF NOT EXISTS results (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_name TEXT NOT NULL,
    term TEXT NOT NULL,
    marks INT NOT NULL CHECK (marks >= 0 AND marks <= 100),
    comment TEXT,
    teacher_id INT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create subjects table (depends on teachers)
CREATE TABLE IF NOT EXISTS subjects (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    grade INT NOT NULL,
    teacher_id INT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, grade, teacher_id)
);

-- ============================================
-- STEP 5: Create independent utility tables
-- ============================================

-- Create announcements table (no foreign key dependencies)
CREATE TABLE IF NOT EXISTS announcements (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    created_by_role TEXT NOT NULL CHECK (created_by_role IN ('admin', 'teacher')),
    created_by_id INT NOT NULL,
    grade_target INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create sync log table for tracking data synchronization
CREATE TABLE IF NOT EXISTS sync_logs (
    id SERIAL PRIMARY KEY,
    table_name TEXT NOT NULL,
    record_id INT NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    sync_status TEXT NOT NULL CHECK (sync_status IN ('pending', 'success', 'failed')),
    error_message TEXT,
    synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- STEP 6: Create indexes for better performance
-- ============================================

CREATE INDEX IF NOT EXISTS idx_teachers_admin_id ON teachers(admin_id);
CREATE INDEX IF NOT EXISTS idx_parents_admin_id ON parents(admin_id);
CREATE INDEX IF NOT EXISTS idx_students_parent_id ON students(parent_id);
CREATE INDEX IF NOT EXISTS idx_students_teacher_id ON students(teacher_id);
CREATE INDEX IF NOT EXISTS idx_students_grade ON students(grade);
CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON attendance(student_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);
CREATE INDEX IF NOT EXISTS idx_attendance_teacher_id ON attendance(teacher_id);
CREATE INDEX IF NOT EXISTS idx_results_student_id ON results(student_id);
CREATE INDEX IF NOT EXISTS idx_results_teacher_id ON results(teacher_id);
CREATE INDEX IF NOT EXISTS idx_subjects_grade ON subjects(grade);
CREATE INDEX IF NOT EXISTS idx_subjects_teacher_id ON subjects(teacher_id);
CREATE INDEX IF NOT EXISTS idx_announcements_created_by_id ON announcements(created_by_id);
CREATE INDEX IF NOT EXISTS idx_sync_logs_status ON sync_logs(sync_status);
CREATE INDEX IF NOT EXISTS idx_sync_logs_table ON sync_logs(table_name);

-- ============================================
-- STEP 7: Enable Row Level Security (RLS)
-- ============================================

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;
ALTER TABLE teachers ENABLE ROW LEVEL SECURITY;
ALTER TABLE parents ENABLE ROW LEVEL SECURITY;
ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE results ENABLE ROW LEVEL SECURITY;
ALTER TABLE subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;

-- ============================================
-- STEP 8: Create RLS policies (PERMISSIVE - Allow All Operations)
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

-- Students: Allow all operations (fixes 401 INSERT error)
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
-- STEP 9: Grant permissions
-- ============================================

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO anon;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon;

-- ============================================
-- STEP 10: Add table comments
-- ============================================

COMMENT ON TABLE profiles IS 'User authentication profiles linked to auth.users';
COMMENT ON TABLE admins IS 'School administrators';
COMMENT ON TABLE teachers IS 'Teachers assigned to grades';
COMMENT ON TABLE parents IS 'Parents/Guardians';
COMMENT ON TABLE students IS 'Students enrolled in the system';
COMMENT ON TABLE attendance IS 'Student attendance records';
COMMENT ON TABLE results IS 'Student academic results/grades';
COMMENT ON TABLE subjects IS 'Subjects taught by teachers';
COMMENT ON TABLE announcements IS 'School announcements and notices';
COMMENT ON TABLE sync_logs IS 'Logs for data synchronization tracking';

-- Success message
SELECT 'Migration completed successfully! All tables created.' AS status;

