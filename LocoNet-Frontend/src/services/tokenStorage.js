import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';

const SESSION_KEY = 'loconet_session';

export async function saveSession(sessionData) {
  const dataString = JSON.stringify(sessionData);
  if (Platform.OS === 'web') {
    localStorage.setItem(SESSION_KEY, dataString);
  } else {
    await SecureStore.setItemAsync(SESSION_KEY, dataString);
  }
}

export async function getSession() {
  try {
    let dataString = null;
    if (Platform.OS === 'web') {
      dataString = localStorage.getItem(SESSION_KEY);
    } else {
      dataString = await SecureStore.getItemAsync(SESSION_KEY);
    }
    return dataString ? JSON.parse(dataString) : null;
  } catch (error) {
    return null;
  }
}

export async function clearSession() {
  if (Platform.OS === 'web') {
    localStorage.removeItem(SESSION_KEY);
  } else {
    await SecureStore.deleteItemAsync(SESSION_KEY);
  }
}