-- 必要に応じて既存データ削除
-- delete from operation_log where unit_no = 1 and graphic_type = 'VDU';

insert into operation_log
    (unit_no, graphic_type, vdu_no, occurred_at, action_type, page_id, control_id, button_id, value)
values
    -- VDU 1 : page_id = s1000 / s1001 交互
    (1, 'VDU', 1, '2026-01-01 00:00:00', 'OPEN', 's1000', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:00:07', 'OPEN', 's1001', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:00:15', 'OPEN', 's1000', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:00:24', 'OPEN', 's1001', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:00:36', 'OPEN', 's1000', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:00:49', 'OPEN', 's1001', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:01:03', 'OPEN', 's1000', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:01:18', 'OPEN', 's1001', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:01:34', 'OPEN', 's1000', null, null, null),
    (1, 'VDU', 1, '2026-01-01 00:01:51', 'OPEN', 's1001', null, null, null),

    -- VDU 2 : page_id = s1050 / s1051 交互
    (1, 'VDU', 2, '2026-01-01 00:00:03', 'OPEN', 's1050', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:00:11', 'OPEN', 's1051', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:00:20', 'OPEN', 's1050', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:00:31', 'OPEN', 's1051', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:00:43', 'OPEN', 's1050', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:00:57', 'OPEN', 's1051', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:01:12', 'OPEN', 's1050', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:01:28', 'OPEN', 's1051', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:01:45', 'OPEN', 's1050', null, null, null),
    (1, 'VDU', 2, '2026-01-01 00:02:03', 'OPEN', 's1051', null, null, null),

    -- VDU 3 : page_id = s1100 / s1101 交互
    (1, 'VDU', 3, '2026-01-01 00:00:05', 'OPEN', 's1100', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:00:14', 'OPEN', 's1101', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:00:25', 'OPEN', 's1100', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:00:37', 'OPEN', 's1101', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:00:50', 'OPEN', 's1100', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:01:04', 'OPEN', 's1101', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:01:19', 'OPEN', 's1100', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:01:35', 'OPEN', 's1101', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:01:52', 'OPEN', 's1100', null, null, null),
    (1, 'VDU', 3, '2026-01-01 00:02:10', 'OPEN', 's1101', null, null, null),

    -- VDU 4 : page_id = s1150 / s1151 交互
    (1, 'VDU', 4, '2026-01-01 00:00:02', 'OPEN', 's1150', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:00:10', 'OPEN', 's1151', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:00:19', 'OPEN', 's1150', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:00:29', 'OPEN', 's1151', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:00:41', 'OPEN', 's1150', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:00:54', 'OPEN', 's1151', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:01:08', 'OPEN', 's1150', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:01:23', 'OPEN', 's1151', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:01:39', 'OPEN', 's1150', null, null, null),
    (1, 'VDU', 4, '2026-01-01 00:01:56', 'OPEN', 's1151', null, null, null);
