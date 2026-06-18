import React from "react";
import { loginUrl } from "../api.js";

export default function Login() {
  return (
    <div className="screen-center login-screen">
      <div className="login-card">
        <span className="eyebrow">AI Email Assistant</span>
        <h1 className="display">Your inbox, read and answered.</h1>
        <p className="muted login-copy">
          Sign in with Google to let the assistant summarize, draft replies
          for, and classify what lands in your inbox.
        </p>
        <a className="btn btn-primary login-btn" href={loginUrl()}>
          <span className="login-btn-glyph" aria-hidden="true">
            ✉
          </span>
          Continue with Google
        </a>
        <p className="fine-print">
          We only ever read what you ask us to. Nothing is sent or filed
          without your say-so.
        </p>
      </div>
    </div>
  );
}
