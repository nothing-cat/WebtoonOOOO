alter table public.users
    add column if not exists show_favorites boolean not null default false,
    add column if not exists show_posts boolean not null default false,
    add column if not exists show_comments boolean not null default false;
