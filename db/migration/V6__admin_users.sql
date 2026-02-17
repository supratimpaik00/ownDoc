create table if not exists admin_users (
    username text primary key,
    password_hash text not null,
    phone text not null,
    created_at timestamp without time zone default now()
);

create unique index if not exists admin_users_phone_idx on admin_users (phone);
