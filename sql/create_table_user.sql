drop table if exists "user" cascade;

create table if not exists "user" (
    user_id bigserial primary key,
    user_name varchar(100) not null unique,
    password varchar(255) not null,
    can_control boolean not null default false,
    enabled boolean not null default true
);

insert into "user" (
    user_name,
    password,
    can_control,
    enabled
) values
    ('operator1', 'pass1', true,  true),
    ('operator2', 'pass2', true,  true),
    ('guest1',    'pass3', false, true),
    ('guest2',    'pass4', false, true);