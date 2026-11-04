import React, { useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View, TextInput, ActivityIndicator, KeyboardAvoidingView, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '../context/AuthContext';
import { getApiErrorMessage } from '../utils/errors';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function LoginScreen({ navigation, route }) {
  const { signIn } = useAuth();
  const passwordRef = useRef(null);

  const [email, setEmail] = useState(route.params?.email ?? '');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleLogin = async () => {
    setErrorMsg('');
    if (!EMAIL_REGEX.test(email.trim()) || !password) {
      setErrorMsg('Please enter a valid email and password.');
      return;
    }

    setSubmitting(true);
    try {
      await signIn(email.trim(), password);
    } catch (error) {
      setErrorMsg(getApiErrorMessage(error, { 401: 'Invalid email or password.' }));
      setSubmitting(false);
    }
  };

  return (
    <LinearGradient colors={['#0f172a', '#1e293b', '#334155']} style={styles.container}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.content}>

        <View style={styles.headerContainer}>
          <View style={styles.logoCircle}>
            <Text style={styles.logoText}>L</Text>
          </View>
          <Text style={styles.title}>Welcome to LocoNet</Text>
          <Text style={styles.subtitle}>Connect with people around you</Text>
        </View>

        <View style={styles.card}>
          {errorMsg ? <Text style={styles.errorText}>{errorMsg}</Text> : null}

          <TextInput
            style={styles.input}
            placeholder="Email Address"
            placeholderTextColor="#94a3b8"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            returnKeyType="next"
            onSubmitEditing={() => passwordRef.current?.focus()}
          />

          <TextInput
            ref={passwordRef}
            style={styles.input}
            placeholder="Password"
            placeholderTextColor="#94a3b8"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            returnKeyType="go"
            onSubmitEditing={handleLogin}
          />

          <Pressable
            style={({ pressed }) => [styles.button, pressed && styles.buttonPressed]}
            onPress={handleLogin}
            disabled={submitting}
          >
            {submitting ? (
              <ActivityIndicator color="#ffffff" />
            ) : (
              <Text style={styles.buttonText}>Log In</Text>
            )}
          </Pressable>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Don't have an account? </Text>
          <Pressable onPress={() => navigation.navigate('Signup')}>
            <Text style={styles.link}>Sign Up Now</Text>
          </Pressable>
        </View>

      </KeyboardAvoidingView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { flex: 1, justifyContent: 'center', padding: 24 },
  headerContainer: { alignItems: 'center', marginBottom: 40 },
  logoCircle: { width: 70, height: 70, borderRadius: 35, backgroundColor: '#3b82f6', justifyContent: 'center', alignItems: 'center', marginBottom: 20, elevation: 10, shadowColor: '#3b82f6', shadowOpacity: 0.5, shadowRadius: 15 },
  logoText: { color: '#ffffff', fontSize: 36, fontWeight: '900' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#ffffff', marginBottom: 8 },
  subtitle: { fontSize: 16, color: '#cbd5e1' },
  card: { backgroundColor: 'rgba(255, 255, 255, 0.05)', padding: 24, borderRadius: 20, borderWidth: 1, borderColor: 'rgba(255, 255, 255, 0.1)' },
  errorText: { color: '#ef4444', backgroundColor: 'rgba(239, 68, 68, 0.1)', padding: 12, borderRadius: 8, marginBottom: 16, textAlign: 'center', overflow: 'hidden' },
  input: { backgroundColor: 'rgba(15, 23, 42, 0.5)', color: '#ffffff', height: 55, borderRadius: 12, paddingHorizontal: 16, fontSize: 16, marginBottom: 16, borderWidth: 1, borderColor: 'rgba(255, 255, 255, 0.1)' },
  button: { backgroundColor: '#3b82f6', height: 55, borderRadius: 12, justifyContent: 'center', alignItems: 'center', marginTop: 8, shadowColor: '#3b82f6', shadowOpacity: 0.3, shadowRadius: 8, elevation: 5 },
  buttonPressed: { opacity: 0.8, transform: [{ scale: 0.98 }] },
  buttonText: { color: '#ffffff', fontSize: 18, fontWeight: 'bold' },
  footer: { flexDirection: 'row', justifyContent: 'center', marginTop: 32 },
  footerText: { color: '#94a3b8', fontSize: 15 },
  link: { color: '#3b82f6', fontSize: 15, fontWeight: 'bold' },
});