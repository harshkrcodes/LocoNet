import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as authService from '../services/authService';
import { setUnauthorizedHandler } from '../services/api';
import { clearSession, getSession, saveSession } from '../services/tokenStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(null);
  const [isRestoring, setIsRestoring] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const storedSession = await getSession();
        setSession(storedSession);
      } catch {
        setSession(null);
      } finally {
        setIsRestoring(false);
      }
    })();
  }, []);

  const signIn = useCallback(async (email, password) => {
    const data = await authService.login(email, password);
    await saveSession(data);
    setSession(data);
  }, []);

  const signOut = useCallback(async () => {
    await clearSession();
    setSession(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(signOut);
    return () => setUnauthorizedHandler(null);
  }, [signOut]);

  const value = useMemo(
    () => ({
        session,
        isRestoring,
        // 🔥 FIX: Ensure we accurately check boolean state regardless of session data structure
        isAuthenticated: Boolean(session?.token || (typeof session === 'string' && session)),
        signIn,
        signOut
    }),
    [session, isRestoring, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}