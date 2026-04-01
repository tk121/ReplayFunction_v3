drop table if exists alert_counts_per_minute cascade; 

create table if not exists alert_counts_per_minute( 
    alert_id bigserial primary key
    , unit_no integer not null
    , system_no integer not null
    , bucket_start timestamp not null
    , alerts_count integer not null
);