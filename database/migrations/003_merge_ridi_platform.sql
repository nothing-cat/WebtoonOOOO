BEGIN;
LOCK TABLE public.platforms, public.webtoon_platforms IN SHARE ROW EXCLUSIVE MODE;
INSERT INTO public.webtoon_platforms(webtoon_id,platform_id)
SELECT wp.webtoon_id, canonical.id
FROM public.webtoon_platforms wp
JOIN public.platforms alias ON alias.id=wp.platform_id AND alias.name='리디북스'
CROSS JOIN public.platforms canonical
WHERE canonical.id='ridi' AND canonical.name='리디'
ON CONFLICT DO NOTHING;
DELETE FROM public.webtoon_platforms wp USING public.platforms alias
WHERE wp.platform_id=alias.id AND alias.name='리디북스'
AND EXISTS (SELECT 1 FROM public.webtoon_platforms target WHERE target.webtoon_id=wp.webtoon_id AND target.platform_id='ridi');
DELETE FROM public.platforms p WHERE p.name='리디북스'
AND NOT EXISTS (SELECT 1 FROM public.webtoon_platforms wp WHERE wp.platform_id=p.id)
AND EXISTS (SELECT 1 FROM public.platforms canonical WHERE canonical.id='ridi' AND canonical.name='리디');
COMMIT;
