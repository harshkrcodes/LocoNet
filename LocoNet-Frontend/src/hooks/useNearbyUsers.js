import { useState, useEffect, useCallback } from 'react';
import * as Location from 'expo-location';
import api from '../services/api';

export default function useNearbyUsers(initialRadius = 5000) {
    const [users, setUsers] = useState([]);
    const [isFetching, setIsFetching] = useState(true);
    const [error, setError] = useState(null);
    const [coords, setCoords] = useState(null);
    const [radius, setRadius] = useState(initialRadius);
    const [status, setStatus] = useState('pending'); // 'granted', 'denied', 'error'

    const syncLocationAndFetch = useCallback(async (isRetry = false) => {
        try {
            setIsFetching(true);
            setError(null);

            // 1. Permission Check
            const { status: permStatus } = await Location.requestForegroundPermissionsAsync();
            if (permStatus !== 'granted') {
                setStatus('denied');
                setError('Location permission denied. Please enable it in settings.');
                setIsFetching(false);
                return;
            }
            setStatus('granted');

            // 2. Fetch Live GPS Coordinates
            const location = await Location.getCurrentPositionAsync({
                accuracy: Location.Accuracy.Balanced,
            });
            const { latitude, longitude } = location.coords;
            setCoords({ latitude, longitude });

            // 3. Send Location to Backend (Matching backend DTO exact variable names: lat, lon)
            await api.post('/api/location/update', {
                lat: latitude,
                lon: longitude
            });

            // 4. Fetch Nearby Users based on updated location
            const response = await api.get('/api/location/nearby', {
                params: {
                    lat: latitude,
                    lon: longitude,
                    radiusMeters: radius
                }
            });

            setUsers(response.data);

        } catch (err) {
            // Auto-retry once on 409 Conflict
            if (err.response?.status === 409 && !isRetry) {
                console.log('Location stale on server. Auto-syncing...');
                return syncLocationAndFetch(true);
            }

            setStatus('error');
            console.error("Fetch users error:", err);
            setError(err.response?.data?.message || 'Failed to fetch nearby users.');
        } finally {
            setIsFetching(false);
        }
    }, [radius]);

    // Screen load hone par pehli baar chalega
    useEffect(() => {
        syncLocationAndFetch();
    }, [syncLocationAndFetch]);

    // MapScreen.js exactly inhi variables ka wait kar raha hai
    return {
        radius,
        setRadius,
        coords,
        users,
        status,
        error,
        isFetching,
        refresh: () => syncLocationAndFetch(false)
    };
}