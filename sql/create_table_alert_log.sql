drop table if exists alert_log cascade; 

create table if not exists alert_log( 
    alert_id bigserial primary key
    , unit_no integer not null
    , occurred_at timestamptz not null
    , action_type text not null
    , alert_tag text
    , alert_name_1 text
    , alert_name_2 text
    , alert_severity text
    , column_no integer
    , firsthit boolean default false
    , flick boolean default false
    , yokoku_color text
);

