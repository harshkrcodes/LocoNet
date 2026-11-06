import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { colors, spacing, radius } from '../theme';

export const INBOX_ROW_HEIGHT = 76;

export default function InboxRow({ userId, fullName, lastMessage, timestampMs, unreadCount, initial, avatarColor, onPress }) {
  // Time formatting directly in component
  const timeString = new Date(timestampMs).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  return (
    <Pressable style={styles.container} onPress={() => onPress(userId, fullName)}>
      <View style={[styles.avatar, { backgroundColor: avatarColor || colors.primary }]}>
        <Text style={styles.avatarText}>{initial || '?'}</Text>
      </View>
      <View style={styles.content}>
        <View style={styles.header}>
          <Text style={styles.name} numberOfLines={1}>{fullName}</Text>
          <Text style={styles.time}>{timeString}</Text>
        </View>
        <View style={styles.footer}>
          <Text style={[styles.message, unreadCount > 0 && styles.messageUnread]} numberOfLines={1}>
            {lastMessage}
          </Text>
          {unreadCount > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{unreadCount}</Text>
            </View>
          )}
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: { flexDirection: 'row', height: INBOX_ROW_HEIGHT, paddingHorizontal: spacing.md, alignItems: 'center', backgroundColor: colors.surface },
  avatar: { width: 50, height: 50, borderRadius: 25, justifyContent: 'center', alignItems: 'center', marginRight: spacing.md },
  avatarText: { color: '#FFF', fontSize: 20, fontWeight: 'bold' },
  content: { flex: 1, borderBottomWidth: 1, borderBottomColor: '#E5E5E5', height: '100%', justifyContent: 'center' },
  header: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 4 },
  name: { fontSize: 16, fontWeight: '600', color: colors.text, flex: 1 },
  time: { fontSize: 12, color: colors.textMuted, marginLeft: spacing.sm },
  footer: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  message: { fontSize: 14, color: colors.textMuted, flex: 1 },
  messageUnread: { color: colors.text, fontWeight: '600' },
  badge: { backgroundColor: colors.primary, borderRadius: 10, paddingHorizontal: 6, paddingVertical: 2, marginLeft: spacing.sm },
  badgeText: { color: '#FFF', fontSize: 10, fontWeight: 'bold' }
});