// File: src/screens/ShoutsScreen.js
import React, { useState, useEffect, useCallback } from 'react';
import {
  View, Text, TextInput, StyleSheet, FlatList,
  KeyboardAvoidingView, Platform, Pressable, ActivityIndicator, Alert
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { fetchNearbyShouts, postShout } from '../services/shoutService';
import { colors, radius, spacing } from '../theme';

/**
 * Formats timestamp to a detailed local string.
 * Example output: "3h ago at 3:14 PM" or "Just now at 3:14 PM"
 */
const formatShoutTime = (timestamp) => {
  if (!timestamp) return 'Just now';
  const date = new Date(timestamp);
  const timeStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  const seconds = Math.floor((new Date() - date) / 1000);
  let interval = seconds / 3600;

  if (interval >= 1 && interval < 24) {
    return `${Math.floor(interval)}h ago at ${timeStr}`;
  }
  interval = seconds / 60;
  if (interval >= 1 && interval < 60) {
    return `${Math.floor(interval)}m ago at ${timeStr}`;
  }
  if (seconds < 60) {
    return `Just now at ${timeStr}`;
  }

  return `${date.toLocaleDateString()} at ${timeStr}`;
};

export default function ShoutsScreen({ route, navigation }) {
  const { coords } = route.params || {};
  const insets = useSafeAreaInsets();

  const [shouts, setShouts] = useState([]);
  const [inputText, setInputText] = useState('');
  const [isFetching, setIsFetching] = useState(true);
  const [isPosting, setIsPosting] = useState(false);

  const loadShouts = useCallback(async () => {
    if (!coords) {
      setIsFetching(false);
      return;
    }
    try {
      setIsFetching(true);
      const data = await fetchNearbyShouts(coords.latitude, coords.longitude);
      setShouts(data);
    } catch (error) {
      Alert.alert("Error", "Could not load local feed.");
    } finally {
      setIsFetching(false);
    }
  }, [coords]);

  useEffect(() => {
    loadShouts();
  }, [loadShouts]);

  const handlePost = async () => {
    if (!inputText.trim() || !coords) return;
    try {
      setIsPosting(true);
      const newShout = await postShout(inputText.trim(), coords.latitude, coords.longitude);
      setShouts([newShout, ...shouts]);
      setInputText('');
    } catch (error) {
      Alert.alert("Failed to post", "Please try again later.");
    } finally {
      setIsPosting(false);
    }
  };

  /**
   * Navigates to ChatScreen and passes the shout content as a quote parameter.
   */
  const openChatWithQuote = (authorId, authorName, shoutContent) => {
    if (!authorId) return;
    navigation.navigate('ChatScreen', {
      receiverId: authorId,
      receiverName: authorName,
      shoutQuote: shoutContent
    });
  };

  const renderShout = ({ item }) => (
    <View style={styles.shoutCard}>
      <View style={styles.shoutHeader}>
        <View style={styles.avatarPlaceholder}>
          <Text style={styles.avatarInitial}>{item.authorName ? item.authorName.charAt(0).toUpperCase() : '?'}</Text>
        </View>
        <View style={styles.authorInfo}>
          <Text style={styles.authorName}>{item.authorName || 'Anonymous'}</Text>
          <Text style={styles.timeText}>{formatShoutTime(item.createdAt)}</Text>
        </View>
        <Pressable
          style={styles.messageBtn}
          onPress={() => openChatWithQuote(item.authorId, item.authorName, item.content)}
        >
          <Text style={styles.messageBtnText}>💬 Message</Text>
        </Pressable>
      </View>
      <Text style={styles.shoutContent}>{item.content}</Text>
    </View>
  );

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={[styles.header, { paddingTop: insets.top + spacing.sm }]}>
        <Pressable onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Text style={styles.backText}>← Map</Text>
        </Pressable>
        <Text style={styles.headerTitle}>Local Feed</Text>
        <View style={{ width: 70 }} />
      </View>

      {isFetching ? (
        <View style={styles.centerBox}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : shouts.length === 0 ? (
        <View style={styles.centerBox}>
          <Text style={styles.emptyText}>It's quiet around here...</Text>
          <Text style={styles.emptySubText}>Be the first to shout something locally!</Text>
        </View>
      ) : (
        <FlatList
          data={shouts}
          keyExtractor={(item) => item.id ? item.id.toString() : Math.random().toString()}
          renderItem={renderShout}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          refreshing={isFetching}
          onRefresh={loadShouts}
        />
      )}

      <View style={[styles.inputContainer, { paddingBottom: Math.max(insets.bottom, spacing.sm) }]}>
        <TextInput
          style={styles.input}
          placeholder="What's happening nearby?"
          placeholderTextColor={colors.textMuted}
          value={inputText}
          onChangeText={setInputText}
          multiline
          maxLength={280}
        />
        <Pressable
          style={[styles.postBtn, (!inputText.trim() || isPosting) && styles.postBtnDisabled]}
          onPress={handlePost}
          disabled={!inputText.trim() || isPosting}
        >
          {isPosting ? (
            <ActivityIndicator size="small" color="#FFF" />
          ) : (
            <Text style={styles.postBtnText}>Post</Text>
          )}
        </Pressable>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingHorizontal: spacing.md, paddingBottom: spacing.sm,
    backgroundColor: colors.surface, borderBottomWidth: 1, borderBottomColor: '#E5E5E5'
  },
  backBtn: { paddingRight: spacing.md, width: 70 },
  backText: { color: colors.primary, fontSize: 16, fontWeight: '600' },
  headerTitle: { fontSize: 18, fontWeight: '700', color: colors.text },
  centerBox: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  emptyText: { fontSize: 18, fontWeight: '600', color: colors.text, marginBottom: 8 },
  emptySubText: { fontSize: 14, color: colors.textMuted },
  listContent: { padding: spacing.md },
  shoutCard: {
    backgroundColor: colors.surface, borderRadius: radius.md,
    padding: spacing.md, marginBottom: spacing.md,
    shadowColor: '#000', shadowOpacity: 0.05, shadowRadius: 4, shadowOffset: { width: 0, height: 2 }, elevation: 2
  },
  shoutHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: spacing.sm },
  avatarPlaceholder: { width: 36, height: 36, borderRadius: 18, backgroundColor: colors.primary, justifyContent: 'center', alignItems: 'center', marginRight: spacing.sm },
  avatarInitial: { color: '#FFF', fontWeight: 'bold', fontSize: 16 },
  authorInfo: { flex: 1 },
  authorName: { fontSize: 15, fontWeight: '700', color: colors.text },
  timeText: { fontSize: 12, color: colors.textMuted, marginTop: 2 },
  messageBtn: { backgroundColor: '#E8F5E9', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 15 },
  messageBtnText: { color: '#2E7D32', fontWeight: '700', fontSize: 12 },
  shoutContent: { fontSize: 15, lineHeight: 22, color: colors.text },
  inputContainer: {
    flexDirection: 'row', padding: spacing.sm, paddingHorizontal: spacing.md,
    backgroundColor: colors.surface, borderTopWidth: 1, borderTopColor: '#E5E5E5', alignItems: 'flex-end'
  },
  input: {
    flex: 1, backgroundColor: colors.background, borderRadius: radius.md,
    paddingHorizontal: 12, paddingTop: 10, paddingBottom: 10, fontSize: 15, color: colors.text, maxHeight: 100
  },
  postBtn: {
    marginLeft: spacing.sm, backgroundColor: colors.primary, borderRadius: radius.md,
    paddingHorizontal: 16, height: 40, justifyContent: 'center', alignItems: 'center'
  },
  postBtnDisabled: { opacity: 0.5 },
  postBtnText: { color: '#FFF', fontWeight: '700', fontSize: 15 }
});