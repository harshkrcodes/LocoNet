// File: src/utils/inbox.js
// Pure helpers for the inbox: normalizing the API response into render-ready
// items, cheap equality for skipping no-op updates, and chat-list time labels.

const AVATAR_COLORS = ['#5B5BD6', '#0D9488', '#E8548C', '#F59E0B', '#16A34A', '#3B82F6', '#8B5CF6', '#EF4444'];
const WEEKDAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const DAY_MS = 86400000;

/** Same user always gets the same avatar color. */
function avatarColorFor(id) {
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) {
    hash = (hash * 31 + id.charCodeAt(i)) | 0;
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
}

/**
 * ISO string / epoch ms -> epoch ms, or null. The backend sends microsecond
 * precision ("…12.123456") and some JS engines only accept 3 fractional
 * digits, so extra digits are trimmed first. Strings without a zone offset
 * are interpreted in the device's local time.
 */
export function parseTimestamp(value) {
  if (value == null || value === '') return null;
  if (typeof value === 'number') return Number.isFinite(value) ? value : null;
  const ms = new Date(String(value).replace(/(\.\d{3})\d+/, '$1')).getTime();
  return Number.isNaN(ms) ? null : ms;
}

/**
 * Raw API array -> render-ready items, newest conversation first. Drops
 * entries without a userId and duplicate userIds (FlatList keys must be
 * unique). Everything the row needs is derived once here, not per render.
 */
export function prepareInbox(raw) {
  if (!Array.isArray(raw)) return [];

  const seen = new Set();
  const items = [];

  for (const entry of raw) {
    if (!entry?.userId) continue;
    const userId = String(entry.userId);
    if (seen.has(userId)) continue;
    seen.add(userId);

    const fullName = String(entry.fullName ?? '').trim() || 'Unknown';
    const unread = Number(entry.unreadCount);

    items.push({
      userId,
      fullName,
      lastMessage: String(entry.lastMessage ?? ''),
      timestampMs: parseTimestamp(entry.timestamp),
      unreadCount: Number.isFinite(unread) && unread > 0 ? Math.floor(unread) : 0,
      initial: fullName.charAt(0).toUpperCase(),
      avatarColor: avatarColorFor(userId),
    });
  }

  items.sort((a, b) => {
    const at = a.timestampMs ?? -Infinity;
    const bt = b.timestampMs ?? -Infinity;
    if (at === bt) return 0;
    return bt > at ? 1 : -1;
  });

  return items;
}

/** True when two prepared lists would render identically. */
export function isSameInbox(a, b) {
  if (a === b) return true;
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i += 1) {
    const x = a[i];
    const y = b[i];
    if (
      x.userId !== y.userId ||
      x.fullName !== y.fullName ||
      x.lastMessage !== y.lastMessage ||
      x.timestampMs !== y.timestampMs ||
      x.unreadCount !== y.unreadCount
    ) {
      return false;
    }
  }
  return true;
}

function formatClock(date) {
  const hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours % 12 || 12}:${minutes} ${hours >= 12 ? 'PM' : 'AM'}`;
}

/**
 * WhatsApp-style label: clock time today, "Yesterday", weekday within the
 * last week, otherwise "6 Nov" (with the year if it isn't this year).
 */
export function formatInboxTime(timestampMs, nowMs = Date.now()) {
  if (timestampMs == null) return '';

  const date = new Date(timestampMs);
  const now = new Date(nowMs);

  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  const startOfThatDay = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  // Rounded so 23/25-hour daylight-saving days still count as one day.
  const dayDiff = Math.round((startOfToday - startOfThatDay) / DAY_MS);

  if (dayDiff <= 0) return formatClock(date); // today (or a slightly-future clock-skewed time)
  if (dayDiff === 1) return 'Yesterday';
  if (dayDiff < 7) return WEEKDAYS[date.getDay()];

  const label = `${date.getDate()} ${MONTHS[date.getMonth()]}`;
  return date.getFullYear() === now.getFullYear() ? label : `${label} ${date.getFullYear()}`;
}
