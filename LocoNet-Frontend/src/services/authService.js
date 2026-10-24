// File: src/services/authService.js
import api from './api';

/** POST /api/auth/login -> { token, userId, email } */
export async function login(email, password) {
  const { data } = await api.post('/api/auth/login', { email, password });
  return data;
}

/** POST /api/users/register -> UserDTO (201) */
export async function register({ fullName, email, password }) {
  const { data } = await api.post('/api/users/register', { fullName, email, password });
  return data;
}
