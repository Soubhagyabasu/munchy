SET search_path TO account, public;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'account'
ORDER BY table_name;

SELECT id, name, description
FROM roles
ORDER BY id;

SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS referenced_table,
    ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.constraint_schema = kcu.constraint_schema
JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.constraint_schema = tc.constraint_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'account'
ORDER BY tc.table_name, kcu.column_name;
