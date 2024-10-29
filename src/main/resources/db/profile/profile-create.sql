
-- User profiles workload
create table if not exists pc_user_profile
(
    id        uuid                 default gen_random_uuid(),
    version   int         not null default 0,
    payload   jsonb       not null,
    expire_at timestamptz not null,

    primary key (id, version)
);

-- insert into pc_user_profile (payload, expire_at) values ('{}', current_timestamp());