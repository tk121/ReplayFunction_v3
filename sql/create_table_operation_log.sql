drop table if exists operation_log cascade; 

create table if not exists operation_log( 
    operation_id bigserial primary key
    , unit_no integer not null
    , graphic_type text not null
    , vdu_no integer
    , occurred_at timestamp not null
    , action_type text not null
    , page_id text
    , control_id text
    , button_id text
    , value numeric (10, 6)
    , created_at timestamp default current_timestamp
);

