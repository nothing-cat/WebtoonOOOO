-- Run after the favorite_webtoons base table has been created.
BEGIN;
ALTER TABLE public.favorite_webtoons ADD COLUMN IF NOT EXISTS last_clicked_at timestamptz;
UPDATE public.favorite_webtoons SET last_clicked_at = created_at WHERE last_clicked_at IS NULL;
ALTER TABLE public.favorite_webtoons ALTER COLUMN last_clicked_at SET DEFAULT now();
ALTER TABLE public.favorite_webtoons ALTER COLUMN last_clicked_at SET NOT NULL;
CREATE INDEX IF NOT EXISTS favorite_webtoons_user_recent_idx ON public.favorite_webtoons (user_id, last_clicked_at DESC);
COMMIT;
