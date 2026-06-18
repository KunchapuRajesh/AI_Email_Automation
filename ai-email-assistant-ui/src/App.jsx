import React, { useEffect, useState } from "react";
import Login from "./components/Login.jsx";
import Dashboard from "./components/Dashboard.jsx";
import { getCurrentUser } from "./api.js";

export default function App() {
  const [status, setStatus] = useState("checking"); // checking | guest | authed
  const [user, setUser] = useState(null);

  useEffect(() => {
    getCurrentUser()
      .then((data) => {
        setUser(data);
        setStatus("authed");
      })
      .catch(() => {
        setStatus("guest");
      });
  }, []);

  if (status === "checking") {
    return (
      <div className="screen-center">
        <div className="seal-spinner" aria-hidden="true" />
        <p className="muted">Opening the ledger…</p>
      </div>
    );
  }

  if (status === "guest") {
    return <Login />;
  }

  return <Dashboard user={user} onLoggedOut={() => setStatus("guest")} />;
}
