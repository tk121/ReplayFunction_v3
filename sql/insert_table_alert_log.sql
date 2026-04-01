INSERT INTO alert_log (
    unit_no,
    occurred_at,
    action_type,
    alert_tag,
    alert_name_1,
    alert_name_2,
    alert_severity,
    column_no,
    firsthit,
    flick,
    yokoku_color
) VALUES
-- 1
(1, '2026-01-01 00:00:00', 'OCCUR'  , 'alert-0001', 'ポンプ異常'      , '圧力低下'        , 'alert'  , 1, true , true , 'red'),
-- 2
(1, '2026-01-01 00:00:00', 'OCCUR'  , 'alert-0002', '温度異常'        , '高温警報'        , 'warn'   , 2, true , true , 'red'),
-- 3
(1, '2026-01-01 00:00:10', 'OCCUR'  , 'alert-0003', '流量異常'        , '流量低下'        , 'info'   , 3, true , true , 'red'),
-- 4
(1, '2026-01-01 00:00:20', 'CHANGE' , 'alert-0001', 'ポンプ異常'      , '圧力低下'        , 'alert'  , 1, true , false, 'red'),
-- 5
(1, '2026-01-01 00:00:30', 'CHANGE' , 'alert-0002', '温度異常'        , '高温警報'        , 'info'   , 2, true , false, 'red'),
-- 6
(1, '2026-01-01 00:00:40', 'CHANGE' , 'alert-0003', '流量異常'        , '流量低下'        , 'warn'   , 3, true , false, 'red'),
-- 7
(1, '2026-01-01 00:01:00', 'OCCUR'  , 'alert-0004', '電源異常'        , '電圧低下'        , 'alert'  , 1, false, true , 'red'),
-- 8
(1, '2026-01-01 00:01:10', 'CHANGE' , 'alert-0004', '電源異常'        , '電圧低下'        , 'warn'   , 1, false, false, 'yellow'),
-- 9
(1, '2026-01-01 00:01:20', 'RESOLVE', 'alert-0002', '温度異常'        , '高温警報'        , 'resolve', 2, false, false, 'green'),
-- 10
(1, '2026-01-01 00:01:30', 'RESOLVE', 'alert-0003', '流量異常'        , '流量低下'        , 'resolve', 3, false, false, 'green'),

-- 11
(1, '2026-01-01 00:02:00', 'CHANGE' , 'alert-0001', 'ポンプ異常'      , '圧力低下'        , 'warn'   , 1, true , false, 'yellow'),
-- 12
(1, '2026-01-01 00:02:10', 'OCCUR'  , 'alert-0005', '冷却系異常'      , '冷却水温上昇'    , 'warn'   , 2, true , true , 'red'),
-- 13
(1, '2026-01-01 00:02:10', 'OCCUR'  , 'alert-0006', '通信異常'        , '応答遅延'        , 'info'   , 3, true , true , 'red'),
-- 14
(1, '2026-01-01 00:02:20', 'CHANGE' , 'alert-0005', '冷却系異常'      , '冷却水温上昇'    , 'alert'  , 2, true , false, 'red'),
-- 15
(1, '2026-01-01 00:02:30', 'CHANGE' , 'alert-0006', '通信異常'        , '応答遅延'        , 'warn'   , 3, true , false, 'red'),
-- 16
(1, '2026-01-01 00:02:40', 'RESOLVE', 'alert-0004', '電源異常'        , '電圧低下'        , 'resolve', 1, false, false, 'green'),
-- 17
(1, '2026-01-01 00:03:00', 'RESOLVE', 'alert-0001', 'ポンプ異常'      , '圧力低下'        , 'resolve', 1, false, false, 'green'),
-- 18
(1, '2026-01-01 00:03:10', 'OCCUR'  , 'alert-0007', '弁異常'          , '開度異常'        , 'info'   , 1, true , true , 'red'),
-- 19
(1, '2026-01-01 00:03:20', 'CHANGE' , 'alert-0007', '弁異常'          , '開度異常'        , 'warn'   , 1, true , false, 'red'),
-- 20
(1, '2026-01-01 00:03:30', 'RESOLVE', 'alert-0006', '通信異常'        , '応答遅延'        , 'resolve', 3, false, false, 'green'),

-- 21
(1, '2026-01-01 00:04:00', 'OCCUR'  , 'alert-0008', '回転数異常'      , '回転低下'        , 'alert'  , 1, false, true , 'red'),
-- 22
(1, '2026-01-01 00:04:00', 'OCCUR'  , 'alert-0009', '圧力異常'        , '過圧警報'        , 'warn'   , 2, false, true , 'yellow'),
-- 23
(1, '2026-01-01 00:04:10', 'RESOLVE', 'alert-0005', '冷却系異常'      , '冷却水温上昇'    , 'resolve', 2, false, false, 'green'),
-- 24
(1, '2026-01-01 00:04:20', 'CHANGE' , 'alert-0008', '回転数異常'      , '回転低下'        , 'warn'   , 1, false, false, 'yellow'),
-- 25
(1, '2026-01-01 00:04:30', 'CHANGE' , 'alert-0009', '圧力異常'        , '過圧警報'        , 'alert'  , 2, false, false, 'red'),
-- 26
(1, '2026-01-01 00:04:40', 'RESOLVE', 'alert-0007', '弁異常'          , '開度異常'        , 'resolve', 1, false, false, 'green'),
-- 27
(1, '2026-01-01 00:05:00', 'OCCUR'  , 'alert-0010', '水位異常'        , '水位低下'        , 'info'   , 3, true , true , 'red'),
-- 28
(1, '2026-01-01 00:05:10', 'CHANGE' , 'alert-0010', '水位異常'        , '水位低下'        , 'warn'   , 3, true , false, 'red'),
-- 29
(1, '2026-01-01 00:05:20', 'RESOLVE', 'alert-0008', '回転数異常'      , '回転低下'        , 'resolve', 1, false, false, 'green'),
-- 30
(1, '2026-01-01 00:05:30', 'OCCUR'  , 'alert-0011', '電流異常'        , '過電流警報'      , 'alert'  , 1, true , true , 'red'),

-- 31
(1, '2026-01-01 00:06:00', 'RESOLVE', 'alert-0009', '圧力異常'        , '過圧警報'        , 'resolve', 2, false, false, 'green'),
-- 32
(1, '2026-01-01 00:06:10', 'CHANGE' , 'alert-0011', '電流異常'        , '過電流警報'      , 'warn'   , 1, true , false, 'red'),
-- 33
(1, '2026-01-01 00:06:20', 'RESOLVE', 'alert-0010', '水位異常'        , '水位低下'        , 'resolve', 3, false, false, 'green'),
-- 34
(1, '2026-01-01 00:06:30', 'OCCUR'  , 'alert-0012', '軸受異常'        , '温度上昇'        , 'warn'   , 2, true , true , 'red'),
-- 35
(1, '2026-01-01 00:06:40', 'CHANGE' , 'alert-0012', '軸受異常'        , '温度上昇'        , 'alert'  , 2, true , false, 'red'),
-- 36
(1, '2026-01-01 00:06:50', 'RESOLVE', 'alert-0011', '電流異常'        , '過電流警報'      , 'resolve', 1, false, false, 'green'),
-- 37
(1, '2026-01-01 00:07:00', 'OCCUR'  , 'alert-0013', '漏洩異常'        , 'シール漏れ'      , 'info'   , 1, true , true , 'red'),
-- 38
(1, '2026-01-01 00:07:10', 'CHANGE' , 'alert-0013', '漏洩異常'        , 'シール漏れ'      , 'warn'   , 1, true , false, 'red'),
-- 39
(1, '2026-01-01 00:07:20', 'RESOLVE', 'alert-0012', '軸受異常'        , '温度上昇'        , 'resolve', 2, false, false, 'green'),
-- 40
(1, '2026-01-01 00:07:30', 'RESOLVE', 'alert-0013', '漏洩異常'        , 'シール漏れ'      , 'resolve', 1, false, false, 'green');
