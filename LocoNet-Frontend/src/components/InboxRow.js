// File: src/components/InboxRow.js
import React, { memo, useCallback, useMemo } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../theme';
import { formatInboxTime } from '../utils/inbox';

// Fixed height lets the FlatList skip measuring rows (getItemLayout).
export const INBOX_ROW_HEIGHT = 76;

const AVATAR_SIZE = 52;

/**
 * Takes primitives (not an item object) so React.memo's shallow compare is
 * enough: a refresh that returns brand-new-but-identical objects re-renders
 * nothing, and a change to one conversation re-renders only its row.
 * `onPress` must be a stable callback.
 */
function InboxRow({ userId, fullName, lastMessage, timestampMs, unreadCount, initial, avatarColor, onPress }) {
  const hasUnread = unreadCount > 0;
  const timeLabel = useMemo(() => formatInboxTime(timestampMs), [timestampMs]);
  const handlePress = useCallback(() => onPress(userId, fullName), [onPress, userId, fullName]);

  const preview = lastMessage || 'No messages yet';
  const accessibilityLabel = `${fullName}. ${hasUnread ? `${unreadCount} unread. ` : ''}${preview}`;

  return (
    <Pressable
      onPress={handlePress}
      android_ripple={{ color: colors.border }}
      style={({ pressed }) => [styles.row, pressed && styles.pressed]}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
    >
      <View style={[styles.avatar, { backgroundColor: avatarColor }]}>
        <Text style={styles.avatarText}>{initial}</Text>
      </View>

      <View style={styles.body}>
        <View style={styles.line}>
          <Text style={[styles.name, hasUnread && styles.nameUnread]} numberOfLines={1}>
            {fullName}
          </Text>
          <Text style={[styles.time, hasUnread && styles.timeUnread]}>{timeLabel}</Text>
        </View>

        <View style={styles.line}>
          <Text
            style={[styles.preview, hasUnread && styles.previewUnread, !lastMessage && styles.previewEmpty]}
            numberOfLines={1}
          >
            {preview}
          </Text>
          {hasUnread && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{unreadCount > 99 ? '99+' : unreadCount}</Text>
            </View>
          )}
        </View>
      </View>

      <View style={styles.divider} />
    </Pressable>
  );
}

export default memo(InboxRow);

const styles = StyleSheet.create({
  row: {
    height: INBOX_ROW_HEIGHT,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    backgroundColor: colors.surface,
  },
  pressed: { backgroundColor: colors.background },
  avatar: {
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { color: '#FFFFFF', fontSize: 22, fontWeight: '800' },
  body: { flex: 1, marginLeft: spacing.md, justifyContent: 'center' },
  line: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  name: { flex: 1, fontSize: 16, fontWeight: '600', color: colors.text, marginRight: spacing.sm },
  nameUnread: { fontWeight: '800' },
  time: { fontSize: 12, color: colors.textMuted },
  timeUnread: { color: colors.primary, fontWeight: '700' },
  preview: { flex: 1, fontSize: 14, color: colors.textMuted, marginTop: 4, marginRight: spacing.sm },
  previewUnread: { color: colors.text, fontWeight: '600' },
  previewEmpty: { fontStyle: 'italic' },
  badge: {
    minWidth: 22,
    height: 22,
    borderRadius: 11,
    paddingHorizontal: 6,
    marginTop: 4,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.primary,
  },
  badgeText: { color: '#FFFFFF', fontSize: 12, fontWeight: '800' },
  // Inset past the avatar, like WhatsApp/Telegram. Drawn inside the row so
  // the row height stays exactly INBOX_ROW_HEIGHT.
  divider: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    left: spacing.md + AVATAR_SIZE + spacing.md,
    height: StyleSheet.hairlineWidth,
    backgroundColor: colors.border,
  },
});
