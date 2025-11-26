-- ============================================
-- VERIFICATION SCRIPT
-- Run this AFTER running supabase_schema.sql
-- ============================================

-- Check if all tables exist
SELECT
    table_name,
    CASE
        WHEN table_name IN (
            'profiles', 'subjects', 'enrollments', 'results',
            'attendance', 'announcements', 'ai_analysis',
            'parent_student_relationships'
        ) THEN '✅ EXISTS'
        ELSE '❌ MISSING'
    END as status
FROM information_schema.tables
WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
    AND table_name IN (
        'profiles', 'subjects', 'enrollments', 'results',
        'attendance', 'announcements', 'ai_analysis',
        'parent_student_relationships'
    )
ORDER BY table_name;

-- Count tables
SELECT
    COUNT(*) as total_tables,
    CASE
        WHEN COUNT(*) = 8 THEN '✅ All 8 tables created'
        ELSE '❌ Missing tables: ' || (8 - COUNT(*)) || ' tables'
    END as result
FROM information_schema.tables
WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
    AND table_name IN (
        'profiles', 'subjects', 'enrollments', 'results',
        'attendance', 'announcements', 'ai_analysis',
        'parent_student_relationships'
    );

-- Check columns in key tables
SELECT
    'profiles' as table_name,
    COUNT(*) as column_count,
    string_agg(column_name, ', ' ORDER BY ordinal_position) as columns
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'profiles'

UNION ALL

SELECT
    'subjects' as table_name,
    COUNT(*) as column_count,
    string_agg(column_name, ', ' ORDER BY ordinal_position) as columns
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'subjects'

UNION ALL

SELECT
    'results' as table_name,
    COUNT(*) as column_count,
    string_agg(column_name, ', ' ORDER BY ordinal_position) as columns
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'results'

UNION ALL

SELECT
    'attendance' as table_name,
    COUNT(*) as column_count,
    string_agg(column_name, ', ' ORDER BY ordinal_position) as columns
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'attendance';

-- Check RLS is enabled
SELECT
    schemaname,
    tablename,
    rowsecurity as rls_enabled,
    CASE
        WHEN rowsecurity THEN '✅ RLS Enabled'
        ELSE '❌ RLS Disabled'
    END as status
FROM pg_tables
WHERE schemaname = 'public'
    AND tablename IN (
        'profiles', 'subjects', 'enrollments', 'results',
        'attendance', 'announcements', 'ai_analysis',
        'parent_student_relationships'
    )
ORDER BY tablename;

-- Check indexes
SELECT
    schemaname,
    tablename,
    indexname,
    '✅ Created' as status
FROM pg_indexes
WHERE schemaname = 'public'
    AND tablename IN (
        'profiles', 'subjects', 'enrollments', 'results',
        'attendance', 'announcements', 'ai_analysis',
        'parent_student_relationships'
    )
ORDER BY tablename, indexname;

-- Check policies
SELECT
    schemaname,
    tablename,
    policyname,
    '✅ Created' as status
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, policyname;

-- Final summary
SELECT
    '✅ SETUP COMPLETE!' as message,
    (SELECT COUNT(*) FROM information_schema.tables
     WHERE table_schema = 'public' AND table_type = 'BASE TABLE') as total_tables,
    (SELECT COUNT(*) FROM pg_policies WHERE schemaname = 'public') as total_policies,
    (SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public') as total_indexes;

