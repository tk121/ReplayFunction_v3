drop table if exists plant_data_log cascade; 

create table if not exists plant_data_log( 
    data_id bigserial primary key
    , unit_no integer not null
    , occurred_at timestamp not null
    , symbol text not null
    , value_locator text not null
    , value_type text not null
    , ai_value numeric(10, 6)
    , di_value integer
    , status text not null
);

