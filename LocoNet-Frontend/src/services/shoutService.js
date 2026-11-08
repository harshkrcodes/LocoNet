import api from './api';

/**
 * Fetches active shouts within a specific radius of the given coordinates.
 */
export const fetchNearbyShouts = async (latitude, longitude) => {
  try {
    const response = await api.get(`/api/shouts/nearby?latitude=${latitude}&longitude=${longitude}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching shouts:", error);
    throw error;
  }
};

/**
 * Creates a new shout broadcasted to the local radius.
 */
export const postShout = async (content, latitude, longitude, radiusMeters = 5000) => {
  try {
    const response = await api.post('/api/shouts', {
      content,
      latitude,
      longitude,
      radiusMeters
    });
    return response.data;
  } catch (error) {
    console.error("Error posting shout:", error);
    throw error;
  }
};