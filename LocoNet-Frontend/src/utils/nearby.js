// File: src/utils/nearby.js
export const RADIUS_OPTIONS = [1000, 2000, 5000, 10000]; // meters

export const INTENT_META = {
  DATING: { label: 'Dating', color: '#E8548C' },
  NETWORKING: { label: 'Networking', color: '#3B82F6' },
  SPORTS: { label: 'Sports', color: '#16A34A' },
  STUDY: { label: 'Study', color: '#F59E0B' },
  GENERAL: { label: 'Open to chat', color: '#5B5BD6' },
};

export function getIntentMeta(intent) {
  return INTENT_META[intent] ?? INTENT_META.GENERAL;
}

export function formatRadius(meters) {
  return `${meters / 1000} km`;
}

// Deliberately coarse: nobody needs to know someone is exactly 37 m away.
export function formatDistance(meters) {
  if (meters == null) return '';
  if (meters < 100) return 'Under 100 m';
  if (meters < 1000) return `${Math.round(meters / 50) * 50} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}
