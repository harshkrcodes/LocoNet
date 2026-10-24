// File: src/screens/SignupScreen.js
import React, { useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { CommonActions } from '@react-navigation/native';
import AppTextInput from '../components/AppTextInput';
import AuthLayout from '../components/AuthLayout';
import FormMessage from '../components/FormMessage';
import PrimaryButton from '../components/PrimaryButton';
import { register } from '../services/authService';
import { colors, spacing } from '../theme';
import { getApiErrorMessage } from '../utils/errors';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function SignupScreen({ navigation }) {
  const emailRef = useRef(null);
  const passwordRef = useRef(null);
  const confirmRef = useRef(null);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Mirrors the backend's UserRegistrationRequest constraints
  // (fullName <= 100, valid email, password 8-72 chars).
  const validate = () => {
    const next = {};
    if (!fullName.trim()) next.fullName = 'Enter your full name.';
    else if (fullName.trim().length > 100) next.fullName = 'Name must be 100 characters or fewer.';

    if (!EMAIL_REGEX.test(email.trim())) next.email = 'Enter a valid email address.';

    if (password.length < 8) next.password = 'Use at least 8 characters.';
    else if (password.length > 72) next.password = 'Use 72 characters or fewer.';

    if (confirmPassword !== password) next.confirmPassword = 'Passwords do not match.';

    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSignup = async () => {
    if (submitting || !validate()) return;
    setSubmitting(true);
    setFormError('');
    try {
      await register({ fullName: fullName.trim(), email: email.trim(), password });
      // Reset (rather than navigate) so the stack is exactly [Login] on every
      // React Navigation version, with the email prefilled.
      navigation.dispatch(
        CommonActions.reset({
          index: 0,
          routes: [{ name: 'Login', params: { email: email.trim(), justRegistered: true } }],
        })
      );
    } catch (error) {
      setFormError(getApiErrorMessage(error, { 409: 'An account with this email already exists.' }));
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Meet people and communities right around you."
      footer={
        <View style={styles.footer}>
          <Text style={styles.footerText}>Already have an account? </Text>
          <Pressable onPress={() => navigation.goBack()} hitSlop={8}>
            <Text style={styles.link}>Log in</Text>
          </Pressable>
        </View>
      }
    >
      <FormMessage type="error" message={formError} />

      <AppTextInput
        label="Full name"
        placeholder="Alex Sharma"
        value={fullName}
        onChangeText={setFullName}
        error={errors.fullName}
        autoCapitalize="words"
        autoComplete="name"
        returnKeyType="next"
        onSubmitEditing={() => emailRef.current?.focus()}
      />
      <AppTextInput
        ref={emailRef}
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
        placeholder="At least 8 characters"
        value={password}
        onChangeText={setPassword}
        error={errors.password}
        secureTextEntry
        autoComplete="new-password"
        returnKeyType="next"
        onSubmitEditing={() => confirmRef.current?.focus()}
      />
      <AppTextInput
        ref={confirmRef}
        label="Confirm password"
        placeholder="Re-enter your password"
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        error={errors.confirmPassword}
        secureTextEntry
        autoComplete="new-password"
        returnKeyType="go"
        onSubmitEditing={handleSignup}
      />

      <PrimaryButton title="Create account" onPress={handleSignup} loading={submitting} style={styles.button} />
    </AuthLayout>
  );
}

const styles = StyleSheet.create({
  button: { marginTop: spacing.sm },
  footer: { flexDirection: 'row', justifyContent: 'center', marginTop: spacing.lg },
  footerText: { color: colors.textMuted, fontSize: 15 },
  link: { color: colors.primary, fontSize: 15, fontWeight: '700' },
});
