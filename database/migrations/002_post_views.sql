-- Apply before testing post detail view recording.
CREATE TABLE IF NOT EXISTS public.post_views (
 post_id bigint NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
 visit_id uuid NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now(),
 PRIMARY KEY(post_id,visit_id)
);
ALTER TABLE public.post_views ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.post_views FROM anon, authenticated, public;
CREATE INDEX IF NOT EXISTS post_views_created_idx ON public.post_views(created_at);
