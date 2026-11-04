import axios from 'axios';
import { getSession } from './tokenStorage';

const FIXED_BASE_URL = 'http://192.168.29.217:8080';
console.log("🚀 AXIOS INITIALIZED WITH:", FIXED_BASE_URL);

const api = axios.create({
  baseURL: FIXED_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

api.interceptors.request.use(async (config) => {
  try {
    const session = await getSession();
    // 🔥 FIX: Check if session is an object {token: ...} or a direct string
    const token = session?.token ? session.token : (typeof session === 'string' ? session : null);

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`✅ TOKEN ATTACHED -> ${config.method.toUpperCase()} ${config.url}`);
    } else {
      console.warn(`⚠️ NO TOKEN ATTACHED -> ${config.method.toUpperCase()} ${config.url}`);
    }
  } catch (error) {
    console.error("❌ Token Fetch Error:", error);
  }
  return config;
});

let onUnauthorized = null;
export const setUnauthorizedHandler = (handler) => {
  onUnauthorized = handler;
};

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("❌ AXIOS RAW ERROR:", error.message);
    console.error("❌ AXIOS RESPONSE STATUS:", error.response?.status);
    console.error("❌ FAILED URL:", error.config?.url);

    if (error.response?.status === 401 && onUnauthorized) {
      console.warn("🔒 401 Unauthorized - Triggering auto-logout");
      onUnauthorized();
    }
    return Promise.reject(error);
  }
);

export default api;