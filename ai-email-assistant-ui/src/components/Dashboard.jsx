import React, { useEffect, useState } from "react";
import {
  getInbox,
  summarizeMessage,
  generateReply,
  classifyMessage,
  sendEmail,
  createDraft,
  logout,
} from "../api.js";

const CATEGORY_CLASS = {
  IMPORTANT: "stamp-red",
  WORK: "stamp-green",
  PERSONAL: "stamp-gold",
  PROMOTION: "stamp-slate",
  SPAM: "stamp-slate",
};

function replySubjectFor(subject) {
  const trimmed = (subject || "").trim();
  return trimmed.toLowerCase().startsWith("re:") ? trimmed : `Re: ${trimmed}`;
}

function initials(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  return parts
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");
}

export default function Dashboard({ user, onLoggedOut }) {
  const [emails, setEmails] = useState([]);
  const [listLoading, setListLoading] = useState(true);
  const [listError, setListError] = useState(null);

  const [selectedId, setSelectedId] = useState(null);

  const [summary, setSummary] = useState(null);
  const [reply, setReply] = useState(null);
  const [category, setCategory] = useState(null);

  const [busy, setBusy] = useState({ summary: false, reply: false, classify: false, send: false, draft: false });
  const [actionError, setActionError] = useState(null);
  const [sendStatus, setSendStatus] = useState(null);
  const [draftStatus, setDraftStatus] = useState(null);

  useEffect(() => {
    loadInbox();
  }, []);

  function loadInbox() {
    setListLoading(true);
    setListError(null);
    getInbox(10)
      .then((data) => setEmails(data || []))
      .catch(() => setListError("Couldn't load the inbox. Try refreshing."))
      .finally(() => setListLoading(false));
  }

  function selectEmail(email) {
    setSelectedId(email.id);
    setSummary(null);
    setReply(null);
    setCategory(null);
    setActionError(null);
    setSendStatus(null);
    setDraftStatus(null);
  }

  const selectedEmail = emails.find((e) => e.id === selectedId) || null;

  async function handleSummarize() {
    if (!selectedEmail) return;
    setBusy((b) => ({ ...b, summary: true }));
    setActionError(null);
    try {
      const data = await summarizeMessage(selectedEmail.id);
      setSummary(data.summary);
    } catch (e) {
      setActionError("Couldn't reach the assistant to summarize this. Try again.");
    } finally {
      setBusy((b) => ({ ...b, summary: false }));
    }
  }

  async function handleReply() {
    if (!selectedEmail) return;
    setBusy((b) => ({ ...b, reply: true }));
    setActionError(null);
    setSendStatus(null);
    try {
      const data = await generateReply(selectedEmail.id);
      setReply(data.reply);
    } catch (e) {
      setActionError("Couldn't reach the assistant to draft a reply. Try again.");
    } finally {
      setBusy((b) => ({ ...b, reply: false }));
    }
  }

  async function handleClassify() {
    if (!selectedEmail) return;
    setBusy((b) => ({ ...b, classify: true }));
    setActionError(null);
    try {
      const data = await classifyMessage(selectedEmail.id);
      setCategory(data.category);
    } catch (e) {
      setActionError("Couldn't reach the assistant to classify this. Try again.");
    } finally {
      setBusy((b) => ({ ...b, classify: false }));
    }
  }

  async function handleSendReply() {
    if (!selectedEmail || !reply) return;
    setBusy((b) => ({ ...b, send: true }));
    setSendStatus(null);
    try {
      await sendEmail(selectedEmail.from, replySubjectFor(selectedEmail.subject), reply);
      setSendStatus("Sent.");
    } catch (e) {
      setSendStatus("Couldn't send. Try again.");
    } finally {
      setBusy((b) => ({ ...b, send: false }));
    }
  }

  async function handleSaveDraft() {
    if (!selectedEmail) return;
    setBusy((b) => ({ ...b, draft: true }));
    setDraftStatus(null);
    try {
      await createDraft(selectedEmail.id);
      setDraftStatus("Saved to Gmail drafts.");
    } catch (e) {
      setDraftStatus("Couldn't save the draft. Try again.");
    } finally {
      setBusy((b) => ({ ...b, draft: false }));
    }
  }

  async function handleLogout() {
    try {
      await logout();
    } finally {
      onLoggedOut();
    }
  }

  return (
    <div className="shell">
      <header className="topbar">
        <div className="wordmark">
          <span className="wordmark-mark">✦</span> Ledger
        </div>
        <div className="topbar-account">
          <div className="avatar">{initials(user?.name)}</div>
          <div className="account-meta">
            <span className="account-name">{user?.name || "Signed in"}</span>
            <span className="account-email mono">{user?.email}</span>
          </div>
          <button className="btn btn-ghost" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </header>

      <div className="layout">
        <aside className="manifest">
          <div className="manifest-header">
            <span className="manifest-title">Inbox</span>
            <button className="btn btn-text" onClick={loadInbox} disabled={listLoading}>
              {listLoading ? "Refreshing…" : "Refresh"}
            </button>
          </div>

          {listError && <p className="error-text">{listError}</p>}

          {!listLoading && !listError && emails.length === 0 && (
            <p className="muted manifest-empty">Nothing here yet.</p>
          )}

          <ul className="manifest-list">
            {emails.map((email) => (
              <li key={email.id}>
                <button
                  className={`manifest-item ${selectedId === email.id ? "is-selected" : ""}`}
                  onClick={() => selectEmail(email)}
                >
                  <span className="manifest-from mono">{email.from}</span>
                  <span className="manifest-subject">{email.subject || "(no subject)"}</span>
                  <span className="manifest-snippet">{email.snippet}</span>
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <main className="detail">
          {!selectedEmail && (
            <div className="screen-center detail-empty">
              <p className="muted">Pick a message from the ledger to begin.</p>
            </div>
          )}

          {selectedEmail && (
            <div className="detail-inner">
              <div className="detail-head">
                <h2 className="detail-subject">{selectedEmail.subject || "(no subject)"}</h2>
                <dl className="detail-meta">
                  <div>
                    <dt>From</dt>
                    <dd className="mono">{selectedEmail.from}</dd>
                  </div>
                  <div>
                    <dt>To</dt>
                    <dd className="mono">{selectedEmail.to}</dd>
                  </div>
                </dl>
              </div>

              <div className="detail-body">{selectedEmail.body || selectedEmail.snippet}</div>

              <div className="action-row">
                <button className="btn btn-stamp" onClick={handleSummarize} disabled={busy.summary}>
                  {busy.summary ? "Reading…" : "Summarize message"}
                </button>
                <button className="btn btn-stamp" onClick={handleReply} disabled={busy.reply}>
                  {busy.reply ? "Drafting…" : "Draft a reply"}
                </button>
                <button className="btn btn-stamp" onClick={handleClassify} disabled={busy.classify}>
                  {busy.classify ? "Classifying…" : "Classify message"}
                </button>
              </div>

              {actionError && <p className="error-text">{actionError}</p>}

              {category && (
                <div className="result-block">
                  <span className={`stamp ${CATEGORY_CLASS[category] || "stamp-slate"}`}>{category}</span>
                </div>
              )}

              {summary && (
                <div className="result-block annotation">
                  <span className="result-label">Summary</span>
                  <p>{summary}</p>
                </div>
              )}

              {reply !== null && (
                <div className="result-block annotation">
                  <span className="result-label">Suggested reply</span>
                  <textarea
                    className="reply-box"
                    value={reply}
                    onChange={(e) => setReply(e.target.value)}
                    rows={8}
                  />
                  <div className="reply-actions">
                    <button className="btn btn-primary" onClick={handleSendReply} disabled={busy.send}>
                      {busy.send ? "Sending…" : "Send this reply"}
                    </button>
                    <button className="btn btn-secondary" onClick={handleSaveDraft} disabled={busy.draft}>
                      {busy.draft ? "Saving…" : "Save as Gmail draft"}
                    </button>
                  </div>
                  {sendStatus && <p className="status-text">{sendStatus}</p>}
                  {draftStatus && <p className="status-text">{draftStatus}</p>}
                  <p className="fine-print">
                    Saving as a draft asks the assistant to write a fresh reply directly in Gmail, separate
                    from any edits made here.
                  </p>
                </div>
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
