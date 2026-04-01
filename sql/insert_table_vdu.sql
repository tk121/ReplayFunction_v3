INSERT INTO vdu_operations_per_minute (
    unit_no,
    vdu_no,
    bucket_start,
    operations_count
)
SELECT
    1 AS unit_no,
    v.vdu_no,
    t.bucket_start,
    floor(random() * 10)::int AS operation_count
FROM
    generate_series(
        '2026-01-01 00:00:00'::timestamp,
        '2026-01-01 23:59:00'::timestamp,
        interval '1 minute'
    ) t(bucket_start)
CROSS JOIN
    generate_series(1, 4) v(vdu_no)
ORDER BY
    t.bucket_start, v.vdu_no;