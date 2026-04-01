INSERT INTO alert_counts_per_minute (
    unit_no,
    system_no,
    bucket_start,
    alerts_count
)
SELECT
    1 AS unit_no,
    s.system_no,
    t.bucket_start,
    floor(random() * 10)::int AS alert_count
FROM
    generate_series(
        '2026-01-01 00:00:00'::timestamp,
        '2026-01-01 23:59:00'::timestamp,
        interval '1 minute'
    ) t(bucket_start)
CROSS JOIN
    generate_series(1, 3) s(system_no)
ORDER BY
    t.bucket_start, s.system_no;