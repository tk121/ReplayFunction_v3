drop table if exists vdu_operations_per_minute cascade; 

create table if not exists vdu_operations_per_minute( 
    operation_id bigserial primary key
    , unit_no integer not null
    , vdu_no integer not null
    , bucket_start timestamp not null
    , operations_count integer not null
);