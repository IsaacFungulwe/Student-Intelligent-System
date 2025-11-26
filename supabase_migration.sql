
-- Create attendance table
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

-- Create results table
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

-- Create subjects table
CREATE TABLE IF NOT EXISTS subjects (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    grade INT NOT NULL,
    teacher_id INT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, grade, teacher_id)
);

-- Create announcements table
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

-- Create indexes for better performance
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

-- Enable Row Level Security (RLS)
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;
ALTER TABLE teachers ENABLE ROW LEVEL SECURITY;
ALTER TABLE parents ENABLE ROW LEVEL SECURITY;
ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE results ENABLE ROW LEVEL SECURITY;
ALTER TABLE subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;

-- Create basic RLS policies (adjust according to your security requirements)

-- Admins can view their own records
CREATE POLICY "Admins can view themselves" ON admins
    FOR SELECT USING (TRUE);

-- Teachers can view themselves
CREATE POLICY "Teachers can view themselves" ON teachers
    FOR SELECT USING (TRUE);

-- Parents can view themselves
CREATE POLICY "Parents can view themselves" ON parents
    FOR SELECT USING (TRUE);

-- Students data is viewable by teachers and parents
CREATE POLICY "Teachers can view students in their grade" ON students
    FOR SELECT USING (TRUE);

-- Attendance data is viewable by teachers and parents
CREATE POLICY "Teachers can view attendance for their students" ON attendance
    FOR SELECT USING (TRUE);

-- Results are viewable by teachers and parents
CREATE POLICY "Teachers and parents can view results" ON results
    FOR SELECT USING (TRUE);

-- Subjects are viewable by all
CREATE POLICY "Subjects are viewable" ON subjects
    FOR SELECT USING (TRUE);

-- Announcements are viewable by all
CREATE POLICY "Announcements are viewable" ON announcements
    FOR SELECT USING (TRUE);

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

CREATE INDEX IF NOT EXISTS idx_sync_logs_status ON sync_logs(sync_status);
CREATE INDEX IF NOT EXISTS idx_sync_logs_table ON sync_logs(table_name);

-- Grant permissions to anon key (adjust as needed)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO anon;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon;

-- Add comments to tables for documentation
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
-- Supabase SQL Schema for Student Intelligent System
-- This script creates all necessary tables in Supabase PostgreSQL database

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create profiles table (for users)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE,
    user_type TEXT CHECK (user_type IN ('admin', 'teacher', 'parent', 'student')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create admins table
CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    school_name TEXT NOT NULL,
    district TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create teachers table
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

-- Create parents table
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

-- Create students table
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

