alter table public.users
    add column if not exists nickname varchar(20)
        check (nickname is null or char_length(btrim(nickname)) between 2 and 20);
