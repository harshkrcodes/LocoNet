// File: src/screens/HomeScreen.js
// Placeholder landing screen for Phase 1 — replaced by the real tab
// navigator (nearby users, chats, societies) in later phases.
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import PrimaryButton from '../components/PrimaryButton';
import { useAuth } from '../context/AuthContext';
import { colors, radius, spacing } from '../theme';

export default function HomeScreen() {
  const { session, signOut } = useAuth();

  return (
    <SafeAreaView style={styles.safe}>
      <View style={styles.card}>
        <Text style={styles.title}>You're signed in</Text>
        <Text style={styles.subtitle}>{session?.email}</Text>
        <PrimaryButton title="Sign out" variant="secondary" onPress={signOut} style={styles.button} />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.background, justifyContent: 'center', padding: spacing.lg },
  card: { backgroundColor: colors.surface, borderRadius: radius.lg, padding: spacing.lg },
  title: { fontSize: 24, fontWeight: '800', color: colors.text },
  subtitle: { fontSize: 15, color: colors.textMuted, marginTop: spacing.xs },
  button: { marginTop: spacing.lg },
});
