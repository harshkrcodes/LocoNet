// File: src/components/UserMarker.js
// NATIVE ONLY: imports react-native-maps, which has no web build. Only
// MapScreen.js imports this; Metro uses MapScreen.web.js on web instead.
import React, { memo, useCallback, useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Marker } from 'react-native-maps';
import { getIntentMeta } from '../utils/nearby';

function UserMarker({ user, selected = false, onSelect }) {
  const meta = getIntentMeta(user.intent);

  // Custom-view markers are rasterized to a bitmap. Tracking view changes
  // forever re-rasterizes every frame (very slow with many markers on
  // Android), but turning it off immediately can leave the marker blank.
  // So: track for a moment on mount and whenever the look changes
  // (selected / color), then switch it off.
  const [tracksViewChanges, setTracksViewChanges] = useState(true);
  useEffect(() => {
    setTracksViewChanges(true);
    const timer = setTimeout(() => setTracksViewChanges(false), 400);
    return () => clearTimeout(timer);
  }, [selected, meta.color]);

  const handlePress = useCallback(
    (event) => {
      // Android also fires the MapView's onPress for marker taps; stop it
      // so the map doesn't immediately deselect what we just selected.
      event?.stopPropagation?.();
      onSelect?.(user);
    },
    [onSelect, user]
  );

  const initial = user.fullName?.trim()?.charAt(0)?.toUpperCase() || '?';

  // No title/description on purpose: setting them makes the OS show its own
  // callout, which would fight with the profile card.
  return (
    <Marker
      coordinate={{ latitude: user.latitude, longitude: user.longitude }}
      anchor={{ x: 0.5, y: 0.5 }}
      onPress={handlePress}
      tracksViewChanges={tracksViewChanges}
      zIndex={selected ? 10 : 1}
    >
      {/* Fixed-size container so selecting (bigger pin + ring) never changes
          the marker's bounds or shifts its anchor point. */}
      <View style={styles.container}>
        {selected && <View style={[styles.ring, { borderColor: meta.color, backgroundColor: `${meta.color}33` }]} />}
        <View style={[styles.pin, selected && styles.pinSelected, { backgroundColor: meta.color }]}>
          <Text style={[styles.initial, selected && styles.initialSelected]}>{initial}</Text>
        </View>
      </View>
    </Marker>
  );
}

export default memo(UserMarker);

const styles = StyleSheet.create({
  container: {
    width: 60,
    height: 60,
    alignItems: 'center',
    justifyContent: 'center',
  },
  ring: {
    position: 'absolute',
    width: 58,
    height: 58,
    borderRadius: 29,
    borderWidth: 2,
  },
  pin: {
    width: 38,
    height: 38,
    borderRadius: 19,
    borderWidth: 3,
    borderColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000000',
    shadowOpacity: 0.25,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
    elevation: 4,
  },
  pinSelected: {
    width: 46,
    height: 46,
    borderRadius: 23,
  },
  initial: { color: '#FFFFFF', fontSize: 16, fontWeight: '800' },
  initialSelected: { fontSize: 19 },
});
