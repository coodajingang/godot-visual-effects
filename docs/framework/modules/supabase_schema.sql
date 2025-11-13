-- Minimal Supabase schema for the Godot4 Universal Game Framework

create table if not exists public.profiles (
    user_id uuid primary key default gen_random_uuid(),
    email text unique,
    nickname text not null default 'Adventurer',
    level integer not null default 1,
    experience integer not null default 0,
    vip integer not null default 0,
    currencies jsonb not null default jsonb_build_object('gold', 0, 'gems', 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.saves (
    user_id uuid references public.profiles(user_id) on delete cascade,
    snapshot jsonb not null,
    updated_at timestamptz not null default now(),
    primary key(user_id)
);

create table if not exists public.purchases (
    id bigserial primary key,
    user_id uuid references public.profiles(user_id) on delete cascade,
    product_id text not null,
    price numeric(10,2) not null default 0,
    currency text not null default 'USD',
    rewards jsonb not null default '{}'::jsonb,
    platform text not null,
    receipt text,
    created_at timestamptz not null default now()
);

create table if not exists public.leaderboards (
    board_id text not null,
    user_id uuid references public.profiles(user_id) on delete cascade,
    score integer not null,
    metadata jsonb not null default '{}'::jsonb,
    recorded_at timestamptz not null default now(),
    primary key(board_id, user_id)
);

-- Recommended security policies (adjust per project)
-- enable row level security
alter table public.profiles enable row level security;
alter table public.saves enable row level security;
alter table public.purchases enable row level security;
alter table public.leaderboards enable row level security;

-- authenticated users can select/update their own profile
create policy "Profiles are editable by owner" on public.profiles
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

create policy "Saves are accessible by owner" on public.saves
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);
