-- Student Intelligent System - Supabase Database Schema
-- Created: November 26, 2025
-- This file contains the complete database schema for migrating to Supabase

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pgcrypto for additional security features
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- TABLES
-- ============================================

-- Profiles table (extends Supabase auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    student_id TEXT UNIQUE,
    phone_number TEXT,
    avatar_url TEXT,
    role TEXT DEFAULT 'student' CHECK (role IN ('student', 'teacher', 'admin', 'parent')),
    parent_id UUID REFERENCES public.profiles(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Subjects/Courses table
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    subject_code TEXT UNIQUE NOT NULL,
    subject_name TEXT NOT NULL,
    description TEXT,
    teacher_id UUID REFERENCES public.profiles(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enrollments table (students enrolled in subjects)
CREATE TABLE IF NOT EXISTS public.enrollments (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    student_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE CASCADE,
    enrollment_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status TEXT DEFAULT 'active' CHECK (status IN ('active', 'completed', 'dropped')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(student_id, subject_id)
);

-- Results/Grades table
CREATE TABLE IF NOT EXISTS public.results (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    student_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE CASCADE,
    exam_type TEXT NOT NULL, -- 'midterm', 'final', 'quiz', 'assignment'
    marks_obtained DECIMAL(5,2) NOT NULL,
    total_marks DECIMAL(5,2) NOT NULL,
    percentage DECIMAL(5,2) GENERATED ALWAYS AS ((marks_obtained / total_marks) * 100) STORED,
    grade TEXT,
    exam_date DATE NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Attendance table
CREATE TABLE IF NOT EXISTS public.attendance (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    student_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('present', 'absent', 'late', 'excused')),
    remarks TEXT,
    marked_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(student_id, subject_id, attendance_date)
);

-- Announcements table
CREATE TABLE IF NOT EXISTS public.announcements (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    target_role TEXT CHECK (target_role IN ('all', 'student', 'teacher', 'parent', 'admin')),
    is_important BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- AI Analysis History table (for storing Gemini AI analysis results)
CREATE TABLE IF NOT EXISTS public.ai_analysis (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    student_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    analysis_type TEXT NOT NULL, -- 'performance', 'attendance', 'prediction'
    analysis_data JSONB NOT NULL,
    recommendations TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Parent-Student Relationships table
CREATE TABLE IF NOT EXISTS public.parent_student_relationships (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    parent_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    student_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    relationship_type TEXT, -- 'father', 'mother', 'guardian'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(parent_id, student_id)
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_student_id ON public.profiles(student_id);
CREATE INDEX IF NOT EXISTS idx_subjects_teacher ON public.subjects(teacher_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON public.enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_subject ON public.enrollments(subject_id);
CREATE INDEX IF NOT EXISTS idx_results_student ON public.results(student_id);
CREATE INDEX IF NOT EXISTS idx_results_subject ON public.results(subject_id);
CREATE INDEX IF NOT EXISTS idx_results_exam_date ON public.results(exam_date);
CREATE INDEX IF NOT EXISTS idx_attendance_student ON public.attendance(student_id);
CREATE INDEX IF NOT EXISTS idx_attendance_subject ON public.attendance(subject_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON public.attendance(attendance_date);
CREATE INDEX IF NOT EXISTS idx_announcements_created_at ON public.announcements(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_analysis_student ON public.ai_analysis(student_id);

-- ============================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.enrollments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.results ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_analysis ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.parent_student_relationships ENABLE ROW LEVEL SECURITY;

-- Profiles policies
DROP POLICY IF EXISTS "Users can view their own profile" ON public.profiles;
CREATE POLICY "Users can view their own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;
CREATE POLICY "Users can update their own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Teachers can view student profiles" ON public.profiles;
CREATE POLICY "Teachers can view student profiles"
    ON public.profiles FOR SELECT
    USING (
        role = 'student' AND
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'teacher'
        )
    );

DROP POLICY IF EXISTS "Parents can view their children's profiles" ON public.profiles;
CREATE POLICY "Parents can view their children's profiles"
    ON public.profiles FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.parent_student_relationships
            WHERE parent_id = auth.uid() AND student_id = profiles.id
        )
    );

-- Subjects policies
DROP POLICY IF EXISTS "Anyone authenticated can view subjects" ON public.subjects;
CREATE POLICY "Anyone authenticated can view subjects"
    ON public.subjects FOR SELECT
    USING (auth.uid() IS NOT NULL);

DROP POLICY IF EXISTS "Teachers can manage their subjects" ON public.subjects;
CREATE POLICY "Teachers can manage their subjects"
    ON public.subjects FOR ALL
    USING (teacher_id = auth.uid());

DROP POLICY IF EXISTS "Admins can manage all subjects" ON public.subjects;
CREATE POLICY "Admins can manage all subjects"
    ON public.subjects FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- Enrollments policies
DROP POLICY IF EXISTS "Students can view their enrollments" ON public.enrollments;
CREATE POLICY "Students can view their enrollments"
    ON public.enrollments FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Teachers can view enrollments for their subjects" ON public.enrollments;
CREATE POLICY "Teachers can view enrollments for their subjects"
    ON public.enrollments FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.subjects
            WHERE subjects.id = enrollments.subject_id
            AND subjects.teacher_id = auth.uid()
        )
    );

-- Results policies
DROP POLICY IF EXISTS "Students can view their results" ON public.results;
CREATE POLICY "Students can view their results"
    ON public.results FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Teachers can manage results for their subjects" ON public.results;
CREATE POLICY "Teachers can manage results for their subjects"
    ON public.results FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.subjects
            WHERE subjects.id = results.subject_id
            AND subjects.teacher_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Parents can view their children's results" ON public.results;
CREATE POLICY "Parents can view their children's results"
    ON public.results FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.parent_student_relationships
            WHERE parent_id = auth.uid() AND student_id = results.student_id
        )
    );

-- Attendance policies
DROP POLICY IF EXISTS "Students can view their attendance" ON public.attendance;
CREATE POLICY "Students can view their attendance"
    ON public.attendance FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Teachers can manage attendance for their subjects" ON public.attendance;
CREATE POLICY "Teachers can manage attendance for their subjects"
    ON public.attendance FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.subjects
            WHERE subjects.id = attendance.subject_id
            AND subjects.teacher_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS "Parents can view their children's attendance" ON public.attendance;
CREATE POLICY "Parents can view their children's attendance"
    ON public.attendance FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.parent_student_relationships
            WHERE parent_id = auth.uid() AND student_id = attendance.student_id
        )
    );

-- Announcements policies
DROP POLICY IF EXISTS "Everyone can view announcements" ON public.announcements;
CREATE POLICY "Everyone can view announcements"
    ON public.announcements FOR SELECT
    USING (auth.uid() IS NOT NULL);

DROP POLICY IF EXISTS "Teachers and admins can create announcements" ON public.announcements;
CREATE POLICY "Teachers and admins can create announcements"
    ON public.announcements FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role IN ('teacher', 'admin')
        )
    );

DROP POLICY IF EXISTS "Authors can update their announcements" ON public.announcements;
CREATE POLICY "Authors can update their announcements"
    ON public.announcements FOR UPDATE
    USING (author_id = auth.uid());

DROP POLICY IF EXISTS "Authors can delete their announcements" ON public.announcements;
CREATE POLICY "Authors can delete their announcements"
    ON public.announcements FOR DELETE
    USING (author_id = auth.uid());

-- AI Analysis policies
DROP POLICY IF EXISTS "Students can view their AI analysis" ON public.ai_analysis;
CREATE POLICY "Students can view their AI analysis"
    ON public.ai_analysis FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Parents can view their children's AI analysis" ON public.ai_analysis;
CREATE POLICY "Parents can view their children's AI analysis"
    ON public.ai_analysis FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.parent_student_relationships
            WHERE parent_id = auth.uid() AND student_id = ai_analysis.student_id
        )
    );

-- ============================================
-- FUNCTIONS AND TRIGGERS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
DROP TRIGGER IF EXISTS update_profiles_updated_at ON public.profiles;
CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_subjects_updated_at ON public.subjects;
CREATE TRIGGER update_subjects_updated_at
    BEFORE UPDATE ON public.subjects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_results_updated_at ON public.results;
CREATE TRIGGER update_results_updated_at
    BEFORE UPDATE ON public.results
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_announcements_updated_at ON public.announcements;
CREATE TRIGGER update_announcements_updated_at
    BEFORE UPDATE ON public.announcements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to automatically create profile on user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, role)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', 'New User'),
        COALESCE(NEW.raw_user_meta_data->>'role', 'student')
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on signup
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================
-- VIEWS FOR ANALYTICS
-- ============================================

-- View for student performance summary
CREATE OR REPLACE VIEW public.student_performance_summary AS
SELECT
    p.id as student_id,
    p.full_name,
    p.student_id as student_id_number,
    COUNT(DISTINCT r.subject_id) as total_subjects,
    AVG(r.percentage) as average_percentage,
    COUNT(DISTINCT a.id) FILTER (WHERE a.status = 'present') as days_present,
    COUNT(DISTINCT a.id) FILTER (WHERE a.status = 'absent') as days_absent,
    ROUND(
        (COUNT(DISTINCT a.id) FILTER (WHERE a.status = 'present')::decimal /
         NULLIF(COUNT(DISTINCT a.id), 0) * 100), 2
    ) as attendance_percentage
FROM public.profiles p
LEFT JOIN public.results r ON p.id = r.student_id
LEFT JOIN public.attendance a ON p.id = a.student_id
WHERE p.role = 'student'
GROUP BY p.id, p.full_name, p.student_id;

-- View for subject statistics
CREATE OR REPLACE VIEW public.subject_statistics AS
SELECT
    s.id as subject_id,
    s.subject_name,
    s.subject_code,
    COUNT(DISTINCT e.student_id) as enrolled_students,
    AVG(r.percentage) as average_performance,
    COUNT(DISTINCT r.id) as total_exams
FROM public.subjects s
LEFT JOIN public.enrollments e ON s.id = e.subject_id AND e.status = 'active'
LEFT JOIN public.results r ON s.id = r.subject_id
GROUP BY s.id, s.subject_name, s.subject_code;

-- ============================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================

-- Note: Uncomment these if you want sample data for testing
-- Sample admin user (password should be set during signup)
/*
INSERT INTO public.profiles (id, email, full_name, role)
VALUES (gen_random_uuid(), 'admin@school.com', 'System Admin', 'admin')
ON CONFLICT (email) DO NOTHING;
*/

-- ============================================
-- GRANTS (Ensure proper permissions)
-- ============================================

GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL TABLES IN SCHEMA public TO authenticated;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO authenticated;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO authenticated;

-- Grant select on views
GRANT SELECT ON public.student_performance_summary TO authenticated;
GRANT SELECT ON public.subject_statistics TO authenticated;

