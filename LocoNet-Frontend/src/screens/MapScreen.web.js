// File: src/screens/MapScreen.web.js
// Metro picks this over MapScreen.js on web. react-native-maps has no web
// implementation (importing it crashes the web bundle), so web gets the same
// data as a list.
import React from 'react';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import CenteredState from '../components/CenteredState';
import RadiusChips from '../components/RadiusChips';
import useNearbyUsers, { STATUS } from '../hooks/useNearbyUsers';
import { colors, radius as corners, spacing } from '../theme';
import { formatDistance, formatRadius, getIntentMeta } from '../utils/nearby';

function UserRow({ user }) {
  const meta = getIntentMeta(user.intent);
  return (
    <View style={styles.row}>
      <View style={[styles.avatar, { backgroundColor: meta.color }]}>
        <Text style={styles.avatarText}>{user.fullName?.trim()?.charAt(0)?.toUpperCase() || '?'}</Text>
      </View>
      <View style={styles.rowText}>
        <Text style={styles.name}>{user.fullName}</Text>
        <Text style={styles.meta}>
          {meta.label} · {formatDistance(user.distanceInMeters)}
        </Text>
      </View>
    </View>
  );
}

export default function MapScreen() {
  const { radius, setRadius, users, status, error, isFetching, refresh } = useNearbyUsers();

  if (status === STATUS.DENIED) {
    return (
      <CenteredState
        title="Location is blocked"
        body="Allow location for this site in your browser settings, then try again. Browsers only share location on HTTPS or localhost."
        actionLabel="Try again"
        onAction={refresh}
      />
    );
  }
  if (status === STATUS.ERROR) {
    return <CenteredState title="Can't find you" body={error} actionLabel="Try again" onAction={refresh} />;
  }
  if (status === STATUS.LOADING) {
    return <CenteredState loading title="Finding your location…" />;
  }

  return (
    <SafeAreaView style={styles.safe}>
      <View style={styles.header}>
        <View style={styles.headerText}>
          <Text style={styles.title}>Nearby</Text>
          <Text style={styles.subtitle}>
            {users.length} {users.length === 1 ? 'person' : 'people'} within {formatRadius(radius)}
          </Text>
        </View>
        <Pressable onPress={refresh} disabled={isFetching} style={styles.refresh} accessibilityRole="button">
          {isFetching ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <Text style={styles.refreshText}>Refresh</Text>
          )}
        </Pressable>
      </View>

      <View style={styles.chips}>
        <RadiusChips value={radius} onChange={setRadius} />
      </View>
      {Boolean(error) && <Text style={styles.error}>{error}</Text>}

      <FlatList
        data={users}
        keyExtractor={(user) => user.id}
        renderItem={({ item }) => <UserRow user={item} />}
        contentContainerStyle={styles.list}
        ListEmptyComponent={<Text style={styles.empty}>No one nearby yet. Try a larger radius.</Text>}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background },
  header: { flexDirection: 'row', alignItems: 'center', padding: spacing.lg, paddingBottom: spacing.md },
  headerText: { flex: 1 },
  title: { fontSize: 26, fontWeight: '800', color: colors.text },
  subtitle: { fontSize: 14, color: colors.textMuted, marginTop: 2 },
  refresh: {
    minWidth: 84,
    height: 38,
    borderRadius: 19,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.surface,
    borderWidth: 1.5,
    borderColor: colors.border,
  },
  refreshText: { color: colors.primary, fontWeight: '700', fontSize: 14 },
  chips: { paddingHorizontal: spacing.lg, marginBottom: spacing.md },
  error: { color: colors.error, fontSize: 13, paddingHorizontal: spacing.lg, marginBottom: spacing.sm },
  list: { paddingHorizontal: spacing.lg, paddingBottom: spacing.xl, gap: spacing.sm },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: corners.lg,
    padding: spacing.md,
  },
  avatar: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  rowText: { flex: 1, marginLeft: spacing.md },
  name: { fontSize: 16, fontWeight: '700', color: colors.text },
  meta: { fontSize: 13, color: colors.textMuted, marginTop: 2 },
  empty: { textAlign: 'center', color: colors.textMuted, marginTop: spacing.xl },
});
