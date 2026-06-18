# Ledger — AI Email Assistant UI

A React (Vite) front end for the `ai-email-assistant` Spring Boot backend. Sign in
with Google, then summarize, draft a reply for, or classify any message in your
inbox.

## Setup

```
npm install
cp .env.example .env   # adjust VITE_API_BASE_URL if your backend isn't on :8080
npm run dev
```

The app runs at `http://localhost:5173` by default.

## Backend requirements

This UI talks to the Spring Boot backend over `fetch` with cookies (session-based
OAuth2 login + CSRF cookie), so the backend must be running and configured with:

```
app.frontend.url=http://localhost:5173
```

That property drives both the CORS allow-list and the post-login redirect target,
so make sure it matches wherever this app is actually served from.

## How login works

1. The "Continue with Google" button does a full-page redirect to
   `{backend}/oauth2/authorization/google` — this can't be done via `fetch`,
   since Google's login page won't load in an iframe or XHR context.
2. Google redirects back to the backend, which completes the OAuth2 login,
   starts a session, and redirects the browser back to this app.
3. On load, the app calls `GET /api/auth/me` (with `credentials: 'include'`) to
   check whether a session exists. A 401 means "show the login screen."
4. POST requests (summarize, reply, classify, send, draft) attach the
   `XSRF-TOKEN` cookie value as an `X-XSRF-TOKEN` header, matching the
   backend's cookie-based CSRF setup.

## Build

```
npm run build
```

Outputs a static bundle to `dist/`, deployable to any static host — just make
sure `app.frontend.url` on the backend points at that host's URL.
