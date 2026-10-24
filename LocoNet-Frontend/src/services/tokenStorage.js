// File: src/services/tokenStorage.js
import * as SecureStore from 'expo-secure-store';

const SESSION_KEY = 'loconet.session';

// In-memory copy so the axios request interceptor doesn't hit the
// keychain/keystore on every single request.
let cachedToken = null;

export async function saveSession(session) {
  cachedToken = session.token;
  await SecureStore.setItemAsync(SESSION_KEY, JSON.stringify(session));
}

export async function getSession() {
  const raw = await SecureStore.getItemAsync(SESSION_KEY);
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw);
    if (!parsed?.token) return null;
    cachedToken = parsed.token;
    return parsed;
  } catch {
    return null;
  }
}

export async function getToken() {
  if (cachedToken) return cachedToken;
  const session = await getSession();
  return session?.token ?? null;
}

export async function clearSession() {
  cachedToken = null;
  await SecureStore.deleteItemAsync(SESSION_KEY);
}
