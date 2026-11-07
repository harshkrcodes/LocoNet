// File: src/screens/MapScreen.js
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { ActivityIndicator, Linking, Pressable, StyleSheet, Text, View } from 'react-native';
import MapView, { Circle } from 'react-native-maps';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import CenteredState from '../components/CenteredState';
import RadiusChips from '../components/RadiusChips';
import UserMarker from '../components/UserMarker';
import UserProfileCard from '../components/UserProfileCard';
import useNearbyUsers from '../hooks/useNearbyUsers';
import { colors, radius as corners, spacing } from '../theme';
import { formatRadius } from '../utils/nearby';
import { useAuth } from '../context/AuthContext';

const regionFor = ({ latitude, longitude }, meters) => {
  const delta = (meters * 2.6) / 111000;
  return { latitude, longitude, latitudeDelta: delta, longitudeDelta: delta };
};

const SELECT_CAMERA_OFFSET = 0.18;

export default function MapScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { signOut } = useAuth();
  const mapRef = useRef(null);
  const regionRef = useRef(null);
  const { radius, setRadius, coords, users, status, error, isFetching, refresh } = useNearbyUsers();

  const [selectedId, setSelectedId] = useState(null);
  const selectedUser = useMemo(
    () => users.find((user) => user.id === selectedId) ?? null,
    [users, selectedId]
  );

  useEffect(() => {
    if (coords) mapRef.current?.animateToRegion(regionFor(coords, radius), 350);
  }, [coords, radius]);

  const handleRegionChange = useCallback((region) => {
    regionRef.current = region;
  }, []);

  const handleSelect = useCallback((user) => {
    setSelectedId(user.id);
    const region = regionRef.current;
    const latitudeDelta = region?.latitudeDelta ?? 0.02;
    const longitudeDelta = region?.longitudeDelta ?? 0.02;
    mapRef.current?.animateToRegion(
      {
        latitude: user.latitude - latitudeDelta * SELECT_CAMERA_OFFSET,
        longitude: user.longitude,
        latitudeDelta,
        longitudeDelta,
      },
      300
    );
  }, []);

  const handleMapPress = useCallback((event) => {
    if (event?.nativeEvent?.action === 'marker-press') return;
    setSelectedId(null);
  }, []);

  const handleClose = useCallback(() => setSelectedId(null), []);

  const handleMessage = useCallback((user) => {
    navigation.navigate('ChatScreen', {
      receiverId: user.id,
      receiverName: user.fullName
    });
  }, [navigation]);

  if (status === 'denied') {
    return (
      <CenteredState
        title="Location access needed"
        body="LocoNet uses your location to show who's around you. Turn it on in Settings."
        actionLabel="Open settings"
        onAction={() => Linking.openSettings()}
      />
    );
  }
  if (status === 'error') {
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
        moveOnMarkerPress={false}
        onPress={handleMapPress}
        onRegionChangeComplete={handleRegionChange}
      >
        <Circle
          center={coords}
          radius={radius}
          strokeColor={`${colors.primary}99`}
          fillColor={`${colors.primary}1A`}
          strokeWidth={1.5}
        />
        {users.map((user) => (
          <UserMarker
            key={user.id}
            user={user}
            selected={user.id === selectedId}
            onSelect={handleSelect}
          />
        ))}
      </MapView>

      <View style={[styles.header, styles.shadow, { top: insets.top + spacing.sm }]}>
        <View style={styles.headerText}>
          <Text style={styles.title}>Nearby</Text>
          <Text style={styles.subtitle}>{summary}</Text>
        </View>

        {/* NAYA LOGOUT BUTTON */}
        <Pressable onPress={signOut} style={{ marginRight: 15, justifyContent: 'center' }}>
          <Text style={{ color: colors.error || '#F44336', fontWeight: '700', fontSize: 14 }}>Logout</Text>
        </Pressable>

        <Pressable onPress={refresh} disabled={isFetching} style={styles.refresh} accessibilityRole="button">
          {isFetching ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <Text style={styles.refreshText}>Refresh</Text>
          )}
        </Pressable>
      </View>

      <View style={[styles.bottomAnchor, { bottom: insets.bottom + spacing.md }]}>
        {selectedUser ? (
          <UserProfileCard user={selectedUser} onClose={handleClose} onMessage={handleMessage} />
        ) : (
          <View style={[styles.sheet, styles.shadow]}>
            <Text style={styles.sheetLabel}>Search radius</Text>
            <RadiusChips value={radius} onChange={setRadius} />
            {Boolean(error) && <Text style={styles.error}>{error}</Text>}
          </View>
        )}
      </View>

      <Pressable
        style={[styles.inboxButton, styles.shadow]}
        onPress={() => navigation.navigate('Inbox')}
      >
        <Text style={styles.inboxButtonText}>💬 My Chats</Text>
      </Pressable>

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
    left: spacing.md, right: spacing.md,
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: corners.lg,
    paddingVertical: spacing.sm + 2, paddingHorizontal: spacing.md,
  },
  headerText: { flex: 1 },
  title: { fontSize: 18, fontWeight: '800', color: colors.text },
  subtitle: { fontSize: 13, color: colors.textMuted, marginTop: 2 },
  refresh: {
    minWidth: 80, height: 36, borderRadius: 18,
    alignItems: 'center', justifyContent: 'center',
    backgroundColor: colors.background,
  },
  refreshText: { color: colors.primary, fontWeight: '700', fontSize: 14 },
  bottomAnchor: { position: 'absolute', left: spacing.md, right: spacing.md },
  sheet: {
    backgroundColor: colors.surface,
    borderRadius: corners.lg, padding: spacing.md,
  },
  sheetLabel: { fontSize: 13, fontWeight: '600', color: colors.text, marginBottom: spacing.sm },
  error: { color: colors.error, fontSize: 13, marginTop: spacing.sm },

  inboxButton: {
    position: 'absolute',
    bottom: 200,
    right: 20,
    backgroundColor: colors.primary,
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 25,
  },
  inboxButtonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  }
});