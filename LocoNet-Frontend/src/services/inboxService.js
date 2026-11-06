// File: src/services/inboxService.js
import api from './api';

/**
 * GET /api/inbox -> [{ userId, fullName, lastMessage, timestamp, unreadCount }]
 * `signal` is an AbortSignal so a stale request can be cancelled when a
 * newer one starts or the screen loses focus. The JWT is attached by api.js.
 */
export async function fetchInbox(signal) {
  const { data } = await api.get('/api/inbox', { signal });
  return data;
}
