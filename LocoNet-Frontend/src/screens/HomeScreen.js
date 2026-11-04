import React from 'react';
import { View, Text, Button } from 'react-native';
import { useAuth } from '../context/AuthContext';

export default function HomeScreen({ navigation }) {
  const { signOut } = useAuth();

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text style={{ fontSize: 20, marginBottom: 20 }}>LocoNet Home</Text>

      {/* Yeh darwaza hai MapScreen par jaane ka */}
      <Button
        title="Open Live Map"
        onPress={() => navigation.navigate('Map')}
      />

      <View style={{ marginTop: 20 }}>
        <Button title="Logout" color="red" onPress={signOut} />
      </View>
    </View>
  );
}