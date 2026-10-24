// File: src/context/AuthContext.js
import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as authService from '../services/authService';
import { setUnauthorizedHandler } from '../services/api';
import { clearSession, getSession, saveSession } from '../services/tokenStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(null); // { token, userId, email } | null
  const [isRestoring, setIsRestoring] = useState(true);

  // Restore a previous login from secure storage on app start.
  useEffect(() => {
    (async () => {
      try {
        setSession(await getSession());
      } catch {
        setSession(null);
      } finally {
        setIsRestoring(false);
      }
    })();
  }, []);

  const signIn = useCallback(async (email, password) => {
    const data = await authService.login(email, password); // throws on failure
    await saveSession(data);
    setSession(data);
  }, []);

  const signOut = useCallback(async () => {
    await clearSession();
    setSession(null);
  }, []);

  // Let the axios layer force a logout when the server rejects our token.
  useEffect(() => {
    setUnauthorizedHandler(signOut);
    return () => setUnauthorizedHandler(null);
  }, [signOut]);

  const value = useMemo(
    () => ({ session, isRestoring, isAuthenticated: Boolean(session?.token), signIn, signOut }),
    [session, isRestoring, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
