alter table public.users
    add column if not exists is_blocked boolean not null default false;

create table if not exists public.banners (
    id integer primary key check (id = 1),
    image_url text not null,
    alt_text text not null,
    link_url text,
    updated_at timestamptz not null default now()
);

alter table public.banners enable row level security;
revoke all on public.banners from anon, authenticated, public;
