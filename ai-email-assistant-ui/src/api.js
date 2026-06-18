const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function readCookie(name) {
  const match = document.cookie.match(new RegExp("(?:^|; )" + name + "=([^;]*)"));
  return match ? decodeURIComponent(match[1]) : null;
}

async function request(path, options = {}) {
  const method = options.method || "GET";
  const headers = { ...(options.headers || {}) };

  if (method !== "GET" && method !== "HEAD") {
    const csrfToken = readCookie("XSRF-TOKEN");
    if (csrfToken) {
      headers["X-XSRF-TOKEN"] = csrfToken;
    }
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    method,
    headers,
    credentials: "include",
  });

  if (response.status === 401) {
    const error = new Error("Unauthenticated");
    error.status = 401;
    throw error;
  }

  if (!response.ok) {
    const text = await response.text().catch(() => "");
    const error = new Error(text || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }
  return null;
}

export function loginUrl() {
  return `${API_BASE_URL}/oauth2/authorization/google`;
}

export function getCurrentUser() {
  return request("/api/auth/me");
}

export function logout() {
  return request("/logout", { method: "POST" });
}

export function getInbox(maxResults = 10) {
  return request(`/gmail/inbox?maxResults=${maxResults}`);
}

export function getMessage(messageId) {
  return request(`/gmail/message/${messageId}`);
}

export function summarizeMessage(messageId) {
  return request("/ai/summarize", {
    method: "POST",
    body: JSON.stringify({ messageId }),
  });
}

export function generateReply(messageId) {
  return request("/ai/reply", {
    method: "POST",
    body: JSON.stringify({ messageId }),
  });
}

export function classifyMessage(messageId) {
  return request("/ai/classify", {
    method: "POST",
    body: JSON.stringify({ messageId }),
  });
}

export function sendEmail(to, subject, body) {
  return request("/gmail/send", {
    method: "POST",
    body: JSON.stringify({ to, subject, body }),
  });
}

export function createDraft(messageId) {
  return request("/gmail/draft", {
    method: "POST",
    body: JSON.stringify({ messageId }),
  });
}
