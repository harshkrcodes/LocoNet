// File: src/components/FormMessage.js
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing } from '../theme';

export default function FormMessage({ type = 'error', message }) {
  if (!message) return null;
  const isError = type === 'error';

  return (
    <View style={[styles.box, isError ? styles.errorBox : styles.successBox]} accessibilityLiveRegion="polite">
      <Text style={[styles.text, { color: isError ? colors.error : colors.success }]}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  box: { padding: spacing.md - 4, borderRadius: radius.md, marginBottom: spacing.md },
  errorBox: { backgroundColor: colors.errorBg },
  successBox: { backgroundColor: colors.successBg },
  text: { fontSize: 14, fontWeight: '500' },
});
