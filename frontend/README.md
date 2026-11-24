# Moodtoon frontend

React + Vite. Run `npm ci`, then `npm run dev` in frontend. `npm run build` checks production bundling.

Default demo mode: demo/admin and arbitrary test password; never enter real credentials. Set VITE_USE_MOCK=false and VITE_API_BASE_URL=/api in .env.local for real API (backend pending). See ../docs/api-contract.md and ../database/migrations/001_favorite_last_clicked_at.sql. KMAS key belongs only on backend. No direct Supabase SDK is used.
