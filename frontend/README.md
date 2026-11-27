# Moodtoon frontend

Session cookies default to Secure, HttpOnly, and SameSite=Lax. For local HTTP development only, set `SESSION_COOKIE_SECURE=false` on the Spring Boot process if the browser does not retain the session cookie. On Render, use the same origin for the frontend and API and keep the secure defaults.

React + Vite. Run `npm ci`, then `npm run dev` in frontend. Start Spring Boot with its database environment variables first. The frontend uses the real `/api` endpoints; Vite proxies local requests to `http://localhost:8080`. Optionally set `VITE_API_BASE_URL=/api` in `.env.local`. Run `npm run build` to check production bundling. Render serves frontend and API from the same origin. See ../docs/api-contract.md. KMAS key and database credentials belong only on the backend.
