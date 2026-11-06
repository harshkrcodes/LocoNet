// File: src/services/inboxService.js
import api from './api';

export const fetchInbox = async (signal) => {
  try {
    // Tera api.js already baseURL aur token automatically handle kar lega
    const response = await api.get('/api/inbox', {
      signal
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};