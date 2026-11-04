// File: src/screens/MapScreen.js
import React, { useEffect, useRef } from 'react';
import { ActivityIndicator, Linking, Pressable, StyleSheet, Text, View, Platform } from 'react-native';
import MapView, { Circle } from 'react-native-maps';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import CenteredState from '../components/CenteredState';
import RadiusChips from '../components/RadiusChips';
import UserMarker from '../components/UserMarker';
// STATUS import hata diya gaya hai kyunki wo undefined thaa
import useNearbyUsers from '../hooks/useNearbyUsers';
import { colors, radius as corners, spacing } from '../theme';
import { formatRadius } from '../utils/nearby';

const regionFor = ({ latitude, longitude }, meters) => {
  const delta = (meters * 2.6) / 111000;
  return { latitude, longitude, latitudeDelta: delta, longitudeDelta: delta };
};

export default function MapScreen() {
  const insets = useSafeAreaInsets();
  const mapRef = useRef(null);
  const { radius, setRadius, coords, users, status, error, isFetching, refresh } = useNearbyUsers();

  useEffect(() => {
    if (coords) mapRef.current?.animateToRegion(regionFor(coords, radius), 350);
  }, [coords, radius]);

  // Web Browser Bypass: Chrome par blank screen hatane ke liye
  if (Platform.OS === 'web') {
    return (
      <CenteredState
        title="Map not supported on Web"
        body="LocoNet MapView only works on mobile. Please open this app in Expo Go on your phone."
        actionLabel="Understood"
        onAction={() => {}}
      />
    );
  }

  // STATUS.DENIED ko direct string se replace kiya
  if (status === 'denied' || status === 'DENIED') {
    return (
      <CenteredState
        title="Location access needed"
        body="LocoNet uses your location to show who's around you. Turn it on in Settings."
        actionLabel="Open settings"
        onAction={() => Linking.openSettings()}
      />
    );
  }

  if (status === 'error' || status === 'ERROR') {
    return <CenteredState title="Can't find you" body={error} actionLabel="Try again" onAction={refresh} />;
  }

  if (!coords) {
    return <CenteredState loading title="Finding your location…" />;
  }

  const summary = users.length
    ? `${users.length} ${users.length === 1 ? 'person' : 'people'} within ${formatRadius(radius)}`
    : `No one within ${formatRadius(radius)} yet`;

  return (
    <View style={styles.container}>
      <MapView
        ref={mapRef}
        style={StyleSheet.absoluteFill}
        initialRegion={regionFor(coords, radius)}
        showsUserLocation
        showsMyLocationButton={false}
        toolbarEnabled={false}
      >
        <Circle
          center={coords}
          radius={radius}
          strokeColor={`${colors.primary}99`}
          fillColor={`${colors.primary}1A`}
          strokeWidth={1.5}
        />
        {users.map((user) => (
          <UserMarker key={user.id} user={user} />
        ))}
      </MapView>

      <View style={[styles.header, styles.shadow, { top: insets.top + spacing.sm }]}>
        <View style={styles.headerText}>
          <Text style={styles.title}>Nearby</Text>
          <Text style={styles.subtitle}>{summary}</Text>
        </View>
        <Pressable onPress={refresh} disabled={isFetching} style={styles.refresh} accessibilityRole="button">
          {isFetching ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <Text style={styles.refreshText}>Refresh</Text>
          )}
        </Pressable>
      </View>

      <View style={[styles.sheet, styles.shadow, { bottom: insets.bottom + spacing.md }]}>
        <Text style={styles.sheetLabel}>Search radius</Text>
        <RadiusChips value={radius} onChange={setRadius} />
        {Boolean(error) && <Text style={styles.error}>{error}</Text>}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  shadow: {
    shadowColor: '#16162A',
    shadowOpacity: 0.12,
    shadowRadius: 14,
    shadowOffset: { width: 0, height: 4 },
    elevation: 6,
  },
  header: {
    position: 'absolute',
    left: spacing.md,
    right: spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: corners.lg,
    paddingVertical: spacing.sm + 2,
    paddingHorizontal: spacing.md,
  },
  headerText: { flex: 1 },
  title: { fontSize: 18, fontWeight: '800', color: colors.text },
  subtitle: { fontSize: 13, color: colors.textMuted, marginTop: 2 },
  refresh: {
    minWidth: 80,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },
  refreshText: { color: colors.primary, fontWeight: '700', fontSize: 14 },
  sheet: {
    position: 'absolute',
    left: spacing.md,
    right: spacing.md,
    backgroundColor: colors.surface,
    borderRadius: corners.lg,
    padding: spacing.md,
  },
  sheetLabel: { fontSize: 13, fontWeight: '600', color: colors.text, marginBottom: spacing.sm },
  error: { color: colors.error, fontSize: 13, marginTop: spacing.sm },
});