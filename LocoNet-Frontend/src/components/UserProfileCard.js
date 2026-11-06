// File: src/components/UserProfileCard.js
// Bottom card for the user selected on the map. Pure React Native (no
// react-native-maps import). Positioning is the parent's job; this only
// animates itself in when it first mounts.
import React, { useEffect, useRef } from 'react';
import { Animated, Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { formatDistance, getIntentMeta } from '../utils/nearby';
import PrimaryButton from './PrimaryButton';

function formatTier(tier) {
  if (!tier) return 'Standard';
  return tier.charAt(0).toUpperCase() + tier.slice(1).toLowerCase();
}

export default function UserProfileCard({ user, onClose, onMessage }) {
  const meta = getIntentMeta(user.intent);
  const initial = user.fullName?.trim()?.charAt(0)?.toUpperCase() || '?';

  // Slide up + fade in on first mount. Switching between users keeps this
  // component mounted, so only the content swaps (no re-animation).
  const appear = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.spring(appear, { toValue: 1, friction: 9, tension: 70, useNativeDriver: true }).start();
  }, [appear]);

  const translateY = appear.interpolate({ inputRange: [0, 1], outputRange: [60, 0] });

  return (
    <Animated.View style={[styles.card, { opacity: appear, transform: [{ translateY }] }]}>
      <View style={styles.topRow}>
        <View style={[styles.avatar, { backgroundColor: meta.color }]}>
          <Text style={styles.avatarText}>{initial}</Text>
        </View>

        <View style={styles.identity}>
          <Text style={styles.name} numberOfLines={1}>
            {user.fullName}
          </Text>
          <Text style={styles.distance}>{formatDistance(user.distanceInMeters)} away</Text>
        </View>

        <Pressable
          onPress={onClose}
          hitSlop={12}
          style={styles.close}
          accessibilityRole="button"
          accessibilityLabel="Close profile"
        >
          <Text style={styles.closeText}>✕</Text>
        </Pressable>
      </View>

      <View style={styles.badges}>
        <View style={[styles.badge, { backgroundColor: `${meta.color}1F` }]}>
          <View style={[styles.dot, { backgroundColor: meta.color }]} />
          <Text style={[styles.badgeText, { color: meta.color }]}>{meta.label}</Text>
        </View>
        <View style={[styles.badge, styles.tierBadge]}>
          <Text style={[styles.badgeText, styles.tierText]}>{formatTier(user.userTier)}</Text>
        </View>
      </View>

      <PrimaryButton title="Message" onPress={() => onMessage?.(user)} style={styles.button} />
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.md,
    shadowColor: '#16162A',
    shadowOpacity: 0.16,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 6 },
    elevation: 8,
  },
  topRow: { flexDirection: 'row', alignItems: 'center' },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { color: '#FFFFFF', fontSize: 24, fontWeight: '800' },
  identity: { flex: 1, marginLeft: spacing.md },
  name: { fontSize: 20, fontWeight: '800', color: colors.text },
  distance: { fontSize: 14, color: colors.textMuted, marginTop: 2 },
  close: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
    marginLeft: spacing.sm,
  },
  closeText: { fontSize: 14, color: colors.textMuted, fontWeight: '700' },
  badges: { flexDirection: 'row', gap: spacing.sm, marginTop: spacing.md },
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    height: 30,
    borderRadius: 15,
  },
  dot: { width: 8, height: 8, borderRadius: 4, marginRight: 6 },
  badgeText: { fontSize: 13, fontWeight: '700' },
  tierBadge: { backgroundColor: colors.background, borderWidth: 1, borderColor: colors.border },
  tierText: { color: colors.text },
  button: { marginTop: spacing.md },
});
