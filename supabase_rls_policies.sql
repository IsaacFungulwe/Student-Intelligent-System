-- ============================================
-- RLS POLICIES ONLY
-- Run this AFTER supabase_minimal.sql succeeds
-- ============================================

-- Enable RLS on all tables
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
        EXISTS (SELECT 1 FROM public.profiles p2 WHERE p2.id = auth.uid() AND p2.role = 'teacher')
    );

DROP POLICY IF EXISTS "Parents can view their children's profiles" ON public.profiles;
CREATE POLICY "Parents can view their children's profiles"
    ON public.profiles FOR SELECT
    USING (
        EXISTS (SELECT 1 FROM public.parent_student_relationships psr WHERE psr.parent_id = auth.uid() AND psr.student_id = profiles.id)
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
    USING (EXISTS (SELECT 1 FROM public.profiles p WHERE p.id = auth.uid() AND p.role = 'admin'));

-- Enrollments policies
DROP POLICY IF EXISTS "Students can view their enrollments" ON public.enrollments;
CREATE POLICY "Students can view their enrollments"
    ON public.enrollments FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Teachers can view enrollments for their subjects" ON public.enrollments;
CREATE POLICY "Teachers can view enrollments for their subjects"
    ON public.enrollments FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.subjects s WHERE s.id = enrollments.subject_id AND s.teacher_id = auth.uid()));

-- Results policies
DROP POLICY IF EXISTS "Students can view their results" ON public.results;
CREATE POLICY "Students can view their results"
    ON public.results FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Teachers can manage results for their subjects" ON public.results;
CREATE POLICY "Teachers can manage results for their subjects"
    ON public.results FOR ALL
    USING (EXISTS (SELECT 1 FROM public.subjects s WHERE s.id = results.subject_id AND s.teacher_id = auth.uid()));

DROP POLICY IF EXISTS "Parents can view their children's results" ON public.results;
CREATE POLICY "Parents can view their children's results"
    ON public.results FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.parent_student_relationships psr WHERE psr.parent_id = auth.uid() AND psr.student_id = results.student_id));

-- Attendance policies
DROP POLICY IF EXISTS "Students can view their attendance" ON public.attendance;
CREATE POLICY "Students can view their attendance"
    ON public.attendance FOR SELECT
    USING (student_id = auth.uid());

DROP POLICY IF EXISTS "Teachers can manage attendance for their subjects" ON public.attendance;
CREATE POLICY "Teachers can manage attendance for their subjects"
    ON public.attendance FOR ALL
    USING (EXISTS (SELECT 1 FROM public.subjects s WHERE s.id = attendance.subject_id AND s.teacher_id = auth.uid()));

DROP POLICY IF EXISTS "Parents can view their children's attendance" ON public.attendance;
CREATE POLICY "Parents can view their children's attendance"
    ON public.attendance FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.parent_student_relationships psr WHERE psr.parent_id = auth.uid() AND psr.student_id = attendance.student_id));

-- Announcements policies
DROP POLICY IF EXISTS "Everyone can view announcements" ON public.announcements;
CREATE POLICY "Everyone can view announcements"
    ON public.announcements FOR SELECT
    USING (auth.uid() IS NOT NULL);

DROP POLICY IF EXISTS "Teachers and admins can create announcements" ON public.announcements;
CREATE POLICY "Teachers and admins can create announcements"
    ON public.announcements FOR INSERT
    WITH CHECK (EXISTS (SELECT 1 FROM public.profiles p WHERE p.id = auth.uid() AND p.role IN ('teacher', 'admin')));

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
    USING (EXISTS (SELECT 1 FROM public.parent_student_relationships psr WHERE psr.parent_id = auth.uid() AND psr.student_id = ai_analysis.student_id));

