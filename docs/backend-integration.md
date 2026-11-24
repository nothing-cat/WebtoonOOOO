# Backend integration
Run the existing Spring Boot app with DB_PASSWORD and SPRING_JPA_HIBERNATE_DDL_AUTO=validate.
Apply database/migrations/002_post_views.sql in Supabase SQL Editor before testing board detail visits.
Set APP_FRONTEND_ORIGIN to the exact frontend origin(s), comma-separated. Local default http://localhost:5173.
Frontend .env.local:
VITE_USE_MOCK=false
VITE_API_BASE_URL=http://localhost:8080/api
Restart Vite. Keep mock mode until backend startup is verified.
KMAS import is disabled by default. Set KMAS_API_KEY (secret), KMAS_IMPORT_ENABLED=true, KMAS_IMPORT_PAGES=1, KMAS_IMPORT_START_PAGE=0; run backend once, then stop and remove import flag. Platform-filtered requests use pltfomCdNm; default platforms 네이버웹툰,카카오웹툰,리디. Set KMAS_IMPORT_PLATFORMS to override. Page size 100. No automatic repeated bulk import. Imported IDs use normalized title + picture writer + story writer; same metadata merges platform links, imperfect identity matching remains possible. Missing authors use platform plus ISBN or cover URL fallback. Age filter accepts recognized non-adult labels only; verify additional labels before expanding. Platform name stored as supplied, no aliases normalized. API response shape and live call must be tested with approved key.
Auth uses server session with BCrypt, not Supabase Auth. No password hash in DTO. Local cookie works on localhost; for deployment prefer same-origin proxy and HTTPS, configure cookie security and CSRF before public use. Current CORS allowlist is not full CSRF protection. No rate limiting yet. Lists currently unpaginated to match React array contract; optimize before large imports. Comment empathy counts calculated from comment_likes; legacy cached column not used. Post visits persist UUID keys; retention and abuse prevention pending. 

Verified PowerShell API response uses the case-sensitive JSON key `itemList` and returns an array of 100 records. Importer also accepts the lowercase variant. All three platform searches succeeded; Java database import still needs a runtime test.

KMAS offset behavior verified by overlapping results at pageNo 100/101 and 200/201. Import requests now use offsets 0,100,200,... and stop at totalCount or a short response. KMAS_IMPORT_PAGES is the maximum number of requests per platform, and KMAS_IMPORT_START_PAGE is the initial offset (despite the historical setting name). Existing ID generation remains unchanged. Ridi platform alias is normalized to 리디 on import. Apply database/migrations/003_merge_ridi_platform.sql to move existing links safely without changing work IDs.
