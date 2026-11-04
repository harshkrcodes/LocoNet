import api from './api';

export async function login(email, password) {
  const { data } = await api.post('/api/auth/login', { email, password });
  return data;
}

export async function register({ fullName, email, password }) {
  const { data } = await api.post('/api/users/register', { fullName, email, password });
  return data;
}