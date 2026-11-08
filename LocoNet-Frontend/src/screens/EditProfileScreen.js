// File: src/screens/EditProfileScreen.js
import React, { useState, useEffect } from 'react';
import {
  View, Text, TextInput, StyleSheet, KeyboardAvoidingView,
  Platform, Pressable, ActivityIndicator, Alert, ScrollView
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import api from '../services/api';
import { colors, radius, spacing } from '../theme';

const INTENT_OPTIONS = ['GENERAL', 'DATING', 'NETWORKING', 'SPORTS', 'STUDY'];

export default function EditProfileScreen({ navigation }) {
  const insets = useSafeAreaInsets();

  const [organization, setOrganization] = useState('');
  const [intent, setIntent] = useState('GENERAL');
  const [interests, setInterests] = useState(''); // Comma separated for now

  const [isFetching, setIsFetching] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    fetchCurrentProfile();
  }, []);

  const fetchCurrentProfile = async () => {
    try {
      setIsFetching(true);
      // 🔥 FIX: Added '/api' prefix to the URL
      const response = await api.get('/api/users/me');
      const user = response.data;

      setOrganization(user.organization || '');
      setIntent(user.intent || 'GENERAL');
      setInterests(user.interests ? user.interests.join(', ') : '');
    } catch (error) {
      console.error("Failed to load profile:", error);
      Alert.alert("Error", "Could not load your profile data.");
    } finally {
      setIsFetching(false);
    }
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      // Convert comma-separated string back to array
      const interestsArray = interests.split(',').map(i => i.trim()).filter(i => i.length > 0);

      // 🔥 FIX: Added '/api' prefix to the URL
      await api.put('/api/users/profile', {
        organization: organization,
        intent: intent,
        interests: interestsArray
      });

      Alert.alert("Success", "Profile updated successfully!", [
        { text: "OK", onPress: () => navigation.goBack() }
      ]);
    } catch (error) {
      console.error("Failed to save profile:", error);
      Alert.alert("Error", "Failed to save profile changes.");
    } finally {
      setIsSaving(false);
    }
  };

  if (isFetching) {
    return (
      <View style={[styles.container, styles.centerBox]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={[styles.header, { paddingTop: insets.top + spacing.sm }]}>
        <Pressable onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Text style={styles.backText}>← Back</Text>
        </Pressable>
        <Text style={styles.headerTitle}>Edit Profile</Text>
        <View style={{ width: 60 }} />
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>

        <View style={styles.formGroup}>
          <Text style={styles.label}>Organization / College</Text>
          <TextInput
            style={styles.input}
            placeholder="e.g. Software Engineer @ Amazon"
            placeholderTextColor={colors.textMuted}
            value={organization}
            onChangeText={setOrganization}
            maxLength={100}
          />
        </View>

        <View style={styles.formGroup}>
          <Text style={styles.label}>Your Primary Intent</Text>
          <View style={styles.intentGrid}>
            {INTENT_OPTIONS.map((opt) => (
              <Pressable
                key={opt}
                style={[
                  styles.intentChip,
                  intent === opt && styles.intentChipActive
                ]}
                onPress={() => setIntent(opt)}
              >
                <Text style={[
                  styles.intentChipText,
                  intent === opt && styles.intentChipTextActive
                ]}>
                  {opt}
                </Text>
              </Pressable>
            ))}
          </View>
        </View>

        <View style={styles.formGroup}>
          <Text style={styles.label}>Interests (Comma Separated)</Text>
          <TextInput
            style={[styles.input, styles.textArea]}
            placeholder="e.g. Coding, Football, Startup"
            placeholderTextColor={colors.textMuted}
            value={interests}
            onChangeText={setInterests}
            multiline
            numberOfLines={3}
          />
        </View>

        <Pressable
          style={[styles.saveBtn, isSaving && styles.saveBtnDisabled]}
          onPress={handleSave}
          disabled={isSaving}
        >
          {isSaving ? (
            <ActivityIndicator size="small" color="#FFF" />
          ) : (
            <Text style={styles.saveBtnText}>Save Changes</Text>
          )}
        </Pressable>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  centerBox: { justifyContent: 'center', alignItems: 'center' },
  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: spacing.md, paddingBottom: spacing.sm,
    backgroundColor: colors.surface, borderBottomWidth: 1, borderBottomColor: '#E5E5E5'
  },
  backBtn: { paddingRight: spacing.md, width: 60 },
  backText: { color: colors.primary, fontSize: 16, fontWeight: '600' },
  headerTitle: { fontSize: 18, fontWeight: '700', color: colors.text },
  content: { padding: spacing.md },
  formGroup: { marginBottom: spacing.lg },
  label: { fontSize: 14, fontWeight: '700', color: colors.text, marginBottom: spacing.sm },
  input: {
    backgroundColor: colors.surface, borderRadius: radius.md,
    borderWidth: 1, borderColor: '#E0E0E0',
    paddingHorizontal: spacing.md, height: 50, fontSize: 15, color: colors.text
  },
  textArea: { height: 100, paddingTop: 12, textAlignVertical: 'top' },
  intentGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  intentChip: {
    paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20,
    backgroundColor: '#F5F5F5', borderWidth: 1, borderColor: '#E0E0E0'
  },
  intentChipActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  intentChipText: { fontSize: 13, fontWeight: '600', color: '#555' },
  intentChipTextActive: { color: '#FFF' },
  saveBtn: {
    backgroundColor: colors.primary, borderRadius: radius.md,
    height: 50, justifyContent: 'center', alignItems: 'center',
    marginTop: spacing.md
  },
  saveBtnDisabled: { opacity: 0.7 },
  saveBtnText: { color: '#FFF', fontSize: 16, fontWeight: '700' }
});