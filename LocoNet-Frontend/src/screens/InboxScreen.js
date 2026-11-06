// File: src/screens/InboxScreen.js
import React, { useCallback, useMemo, useRef, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { SafeAreaView } from 'react-native-safe-area-context';
import axios from 'axios';
import CenteredState from '../components/CenteredState';
import InboxRow, { INBOX_ROW_HEIGHT } from '../components/InboxRow';
import { fetchInbox } from '../services/inboxService';
import { colors, spacing } from '../theme';

// Must match the name your ChatScreen is registered under in the navigator.
const CHAT_ROUTE = 'ChatScreen';

// Module-level so FlatList always receives the same function references.
const keyExtractor = (item) => item.userId;
const getItemLayout = (_data, index) => ({
  length: INBOX_ROW_HEIGHT,
  offset: INBOX_ROW_HEIGHT * index,
  index,
});

function EmptyInbox() {
  return (
    <View style={styles.empty}>
      <Text style={styles.emptyTitle}>No conversations yet</Text>
      <Text style={styles.emptyBody}>
        Find people near you on the map and say hi. Your chats will show up here.
      </Text>
    </View>
  );
}

export default function InboxScreen({ navigation }) {
  const [items, setItems] = useState([]);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState('');

  const abortRef = useRef(null);

  const load = useCallback(async () => {
    // Only the newest request may update the screen.
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    try {
      const raw = await fetchInbox(controller.signal);
      if (controller.signal.aborted) return;

      // In-place logic instead of external utils
      const next = (raw || []).map(item => ({
        ...item,
        initial: item.fullName ? item.fullName.charAt(0).toUpperCase() : '?',
        avatarColor: colors.primary, // Defaulting to primary color
        // Convert ISO string to timestamp for sorting/display
        timestampMs: new Date(item.timestamp).getTime()
      }));

      // Simple check to avoid unnecessary re-renders
      setItems((prev) => {
        if (prev.length !== next.length) return next;
        const isSame = prev.every((p, i) => p.userId === next[i].userId && p.timestampMs === next[i].timestampMs);
        return isSame ? prev : next;
      });
      setError('');
    } catch (e) {
      if (controller.signal.aborted || axios.isCancel(e)) return;
      // Simple error message instead of external util
      setError(e.response?.data?.message || e.message || 'Something went wrong while fetching chats.');
    }

    setIsInitialLoading(false);
    setIsRefreshing(false);
  }, []);

  // Fetch on first focus and silently revalidate every time we come back
  // (e.g. from a chat), so unread badges stay fresh without a spinner.
  useFocusEffect(
    useCallback(() => {
      load();
      return () => {
        abortRef.current?.abort();
        setIsRefreshing(false);
      };
    }, [load])
  );

  const onRefresh = useCallback(() => {
    setIsRefreshing(true);
    load();
  }, [load]);

  const retry = useCallback(() => {
    setIsInitialLoading(true);
    setError('');
    load();
  }, [load]);

  const handleOpenChat = useCallback(
    (userId, fullName) => {
      navigation.navigate(CHAT_ROUTE, { receiverId: userId, receiverName: fullName });
    },
    [navigation]
  );

  const renderItem = useCallback(
    ({ item }) => (
      <InboxRow
        userId={item.userId}
        fullName={item.fullName}
        lastMessage={item.lastMessage}
        timestampMs={item.timestampMs}
        unreadCount={item.unreadCount}
        initial={item.initial}
        avatarColor={item.avatarColor}
        onPress={handleOpenChat}
      />
    ),
    [handleOpenChat]
  );

  const refreshControl = useMemo(
    () => (
      <RefreshControl
        refreshing={isRefreshing}
        onRefresh={onRefresh}
        tintColor={colors.primary}
        colors={[colors.primary]}
      />
    ),
    [isRefreshing, onRefresh]
  );

  const totalUnread = useMemo(() => items.reduce((sum, item) => sum + (item.unreadCount || 0), 0), [items]);

  if (isInitialLoading) {
    return <CenteredState loading title="Loading your chats…" />;
  }
  if (error && items.length === 0) {
    return <CenteredState title="Can't load your chats" body={error} actionLabel="Try again" onAction={retry} />;
  }

  const subtitle =
    totalUnread > 0
      ? `${totalUnread} unread ${totalUnread === 1 ? 'message' : 'messages'}`
      : items.length === 0
        ? 'Nothing here yet'
        : 'All caught up';

  return (
    <SafeAreaView style={styles.safe}>
      <View style={styles.header}>
        <Text style={styles.title}>Chats</Text>
        <Text style={styles.subtitle}>{subtitle}</Text>
      </View>

      {Boolean(error) && (
        <Pressable onPress={onRefresh} style={styles.banner} accessibilityRole="button">
          <Text style={styles.bannerText}>{error} Tap to retry.</Text>
        </Pressable>
      )}

      <View style={styles.sheet}>
        <FlatList
          data={items}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          getItemLayout={getItemLayout}
          refreshControl={refreshControl}
          ListEmptyComponent={EmptyInbox}
          contentContainerStyle={items.length === 0 ? styles.emptyContainer : undefined}
          initialNumToRender={12}
          maxToRenderPerBatch={10}
          windowSize={7}
        />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  header: { paddingHorizontal: spacing.lg, paddingTop: spacing.md, paddingBottom: spacing.md },
  title: { fontSize: 28, fontWeight: '800', color: colors.text },
  subtitle: { fontSize: 14, color: colors.textMuted, marginTop: 2 },
  banner: {
    marginHorizontal: spacing.md,
    marginBottom: spacing.sm,
    paddingVertical: spacing.sm + 2,
    paddingHorizontal: spacing.md,
    borderRadius: 12,
    backgroundColor: colors.errorBg,
  },
  bannerText: { color: colors.error, fontSize: 13, fontWeight: '500' },
  sheet: {
    flex: 1,
    backgroundColor: colors.surface,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    overflow: 'hidden',
  },
  emptyContainer: { flexGrow: 1 },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: spacing.xl },
  emptyTitle: { fontSize: 18, fontWeight: '800', color: colors.text, textAlign: 'center' },
  emptyBody: {
    fontSize: 15,
    color: colors.textMuted,
    textAlign: 'center',
    marginTop: spacing.sm,
    lineHeight: 22,
  },
});