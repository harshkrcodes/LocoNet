// File: src/components/CenteredState.js
import React from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../theme';
import PrimaryButton from './PrimaryButton';

export default function CenteredState({ loading = false, title, body, actionLabel, onAction }) {
  return (
    <View style={styles.container}>
      {loading && <ActivityIndicator size="large" color={colors.primary} />}
      {Boolean(title) && <Text style={[styles.title, loading && styles.titleLoading]}>{title}</Text>}
      {Boolean(body) && <Text style={styles.body}>{body}</Text>}
      {Boolean(actionLabel) && (
        <PrimaryButton title={actionLabel} onPress={onAction} style={styles.button} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.xl,
    backgroundColor: colors.background,
  },
  title: { fontSize: 20, fontWeight: '800', color: colors.text, textAlign: 'center' },
  titleLoading: { fontSize: 15, fontWeight: '500', color: colors.textMuted, marginTop: spacing.md },
  body: { fontSize: 15, color: colors.textMuted, textAlign: 'center', marginTop: spacing.sm, lineHeight: 22 },
  button: { marginTop: spacing.lg, alignSelf: 'stretch' },
});
