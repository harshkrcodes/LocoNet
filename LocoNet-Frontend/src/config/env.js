// File: src/config/env.js
import { Platform } from 'react-native';
import Constants from 'expo-constants';

const API_PORT = 8080;

function resolveBaseUrl() {
  if (process.env.EXPO_PUBLIC_API_URL) {
    return process.env.EXPO_PUBLIC_API_URL.replace(/\/$/, '');
  }

  // Web platform ke liye explicit check (laptop browser access)
  if (Platform.OS === 'web') {
    return `http://localhost:${API_PORT}`;
  }

  const hostUri = Constants.expoConfig?.hostUri;
  if (hostUri) {
    const host = hostUri.split(':')[0];
    return `http://${host}:${API_PORT}`;
  }

  return Platform.OS === 'android'
    ? `http://10.0.2.2:${API_PORT}`
    : `http://localhost:${API_PORT}`;
}

export const API_BASE_URL = resolveBaseUrl();