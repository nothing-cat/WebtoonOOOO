# Moodtoon frontend

React + Vite. Run `npm ci`, then `npm run dev` in frontend. Start Spring Boot with its database environment variables first. The frontend uses the real `/api` endpoints; Vite proxies local requests to `http://localhost:8080`. Optionally set `VITE_API_BASE_URL=/api` in `.env.local`. Run `npm run build` to check production bundling. Render serves frontend and API from the same origin. See ../docs/api-contract.md. KMAS key and database credentials belong only on the backend.
