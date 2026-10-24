// File: src/screens/LoginScreen.js
import React, { useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import AppTextInput from '../components/AppTextInput';
import AuthLayout from '../components/AuthLayout';
import FormMessage from '../components/FormMessage';
import PrimaryButton from '../components/PrimaryButton';
import { useAuth } from '../context/AuthContext';
import { colors, spacing } from '../theme';
import { getApiErrorMessage } from '../utils/errors';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function LoginScreen({ navigation, route }) {
  const { signIn } = useAuth();
  const passwordRef = useRef(null);

  const [email, setEmail] = useState(route.params?.email ?? '');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const validate = () => {
    const next = {};
    if (!EMAIL_REGEX.test(email.trim())) next.email = 'Enter a valid email address.';
    if (!password) next.password = 'Enter your password.';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleLogin = async () => {
    if (submitting || !validate()) return;
    setSubmitting(true);
    setFormError('');
    try {
      await signIn(email.trim(), password);
      // No navigate() needed: RootNavigator swaps to Home once the session is set.
    } catch (error) {
      setFormError(getApiErrorMessage(error, { 401: 'Invalid email or password.' }));
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Log in to see who's around you."
      footer={
        <View style={styles.footer}>
          <Text style={styles.footerText}>New to LocoNet? </Text>
          <Pressable onPress={() => navigation.navigate('Signup')} hitSlop={8}>
            <Text style={styles.link}>Create account</Text>
          </Pressable>
        </View>
      }
    >
      {route.params?.justRegistered && (
        <FormMessage type="success" message="Account created! Log in to continue." />
      )}
      <FormMessage type="error" message={formError} />

      <AppTextInput
        label="Email"
        placeholder="you@example.com"
        value={email}
        onChangeText={setEmail}
        error={errors.email}
        keyboardType="email-address"
        autoComplete="email"
        returnKeyType="next"
        onSubmitEditing={() => passwordRef.current?.focus()}
      />
      <AppTextInput
        ref={passwordRef}
        label="Password"
        placeholder="Your password"
        value={password}
        onChangeText={setPassword}
        error={errors.password}
        secureTextEntry
        autoComplete="current-password"
        returnKeyType="go"
        onSubmitEditing={handleLogin}
      />

      <PrimaryButton title="Log in" onPress={handleLogin} loading={submitting} style={styles.button} />
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  button: { marginTop: spacing.sm },
  footer: { flexDirection: 'row', justifyContent: 'center', marginTop: spacing.lg },
  footerText: { color: colors.textMuted, fontSize: 15 },
  link: { color: colors.primary, fontSize: 15, fontWeight: '700' },
});
