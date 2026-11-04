// File: src/components/UserMarker.js
// NATIVE ONLY: imports react-native-maps, which has no web build. Only
// MapScreen.js imports this; Metro uses MapScreen.web.js on web instead.
import React, { memo, useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Marker } from 'react-native-maps';
import { formatDistance, getIntentMeta } from '../utils/nearby';

function UserMarker({ user }) {
  const meta = getIntentMeta(user.intent);

  // Custom-view markers are rasterized to a bitmap. Tracking view changes
  // forever re-rasterizes every frame (very slow with many markers on
  // Android), but turning it off immediately can leave the marker blank, so
  // leave it on just long enough to draw once.
  const [tracksViewChanges, setTracksViewChanges] = useState(true);
  useEffect(() => {
    const timer = setTimeout(() => setTracksViewChanges(false), 400);
    return () => clearTimeout(timer);
  }, []);

  return (
    <Marker
      coordinate={{ latitude: user.latitude, longitude: user.longitude }}
      title={user.fullName}
      description={`${meta.label} · ${formatDistance(user.distanceInMeters)}`}
      anchor={{ x: 0.5, y: 0.5 }}
      tracksViewChanges={tracksViewChanges}
    >
      <View style={[styles.pin, { backgroundColor: meta.color }]}>
        <Text style={styles.initial}>{user.fullName?.trim()?.charAt(0)?.toUpperCase() || '?'}</Text>
      </View>
    </Marker>
  );
}

export default memo(UserMarker);

const styles = StyleSheet.create({
  pin: {
    width: 40,
    height: 40,
    borderRadius: 20,
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
  initial: { color: '#FFFFFF', fontSize: 16, fontWeight: '800' },
});
