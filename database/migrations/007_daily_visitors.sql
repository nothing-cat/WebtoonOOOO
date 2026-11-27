create table if not exists public.daily_visitors (
    visit_date date not null,
    visitor_id uuid not null,
    primary key (visit_date, visitor_id)
);

alter table public.daily_visitors enable row level security;
revoke all on public.daily_visitors from anon, authenticated, public;
