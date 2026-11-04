import { Platform } from 'react-native';
import Constants from 'expo-constants';

const API_PORT = 8080;
// Note: Agar router restart hone par connection fail ho, toh yahan apna naya Wi-Fi IP daal dena
const LAN_IP = '192.168.29.217';

function resolveBaseUrl() {
  if (process.env.EXPO_PUBLIC_API_URL) {
    return process.env.EXPO_PUBLIC_API_URL.replace(/\/$/, '');
  }

  // Web ke liye strict LAN IP taaki Chrome localhost ko block na kare
  if (Platform.OS === 'web') {
    return `http://${LAN_IP}:${API_PORT}`;
  }

  const hostUri = Constants.expoConfig?.hostUri;
  if (hostUri) {
    const host = hostUri.split(':')[0];
    return `http://${host}:${API_PORT}`;
  }

  return Platform.OS === 'android'
    ? `http://10.0.2.2:${API_PORT}`
    : `http://${LAN_IP}:${API_PORT}`;
}

export const API_BASE_URL = resolveBaseUrl();