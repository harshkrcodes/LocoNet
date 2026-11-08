import React, { useState, useRef } from 'react';
import {
  View, Text, TextInput, StyleSheet, FlatList,
  KeyboardAvoidingView, Platform, Pressable
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import useStompChat from '../hooks/useStompChat';
import { colors, radius, spacing } from '../theme';

const formatMessageTime = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const formatDateBadge = (timestamp) => {
  if (!timestamp) return 'Today';
  const date = new Date(timestamp);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (date.toDateString() === today.toDateString()) return 'Today';
  if (date.toDateString() === yesterday.toDateString()) return 'Yesterday';
  return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
};

export default function ChatScreen({ route, navigation }) {
  const { receiverId, receiverName, shoutQuote } = route.params;
  const insets = useSafeAreaInsets();

  const { messages, isConnected, sendMessage } = useStompChat(receiverId);
  const [inputText, setInputText] = useState('');
  const [activeQuote, setActiveQuote] = useState(shoutQuote || null);
  const flatListRef = useRef();

  const handleSend = () => {
    if (inputText.trim() === '') return;

    let finalPayload = inputText;

    if (activeQuote) {
      finalPayload = `📢 Replying to your Local Feed:\n"${activeQuote}"\n\n${inputText}`;
      setActiveQuote(null);
    }

    sendMessage(finalPayload);
    setInputText('');
  };

  const renderMessage = ({ item, index }) => {
    const isMe = item.receiverId === receiverId;
    const timeString = formatMessageTime(item.timestamp || Date.now());

    let showDateBadge = false;
    if (index === 0) {
      showDateBadge = true;
    } else {
      const currentMsgDate = new Date(item.timestamp || Date.now()).toDateString();
      const prevMsgDate = new Date(messages[index - 1]?.timestamp || Date.now()).toDateString();
      if (currentMsgDate !== prevMsgDate) showDateBadge = true;
    }

    return (
      <View>
        {showDateBadge && (
          <View style={styles.dateBadgeContainer}>
            <Text style={styles.dateBadgeText}>{formatDateBadge(item.timestamp || Date.now())}</Text>
          </View>
        )}
        <View style={[styles.msgWrapper, isMe ? styles.msgMe : styles.msgThem]}>
          <View style={[styles.msgBubble, isMe ? styles.bubbleMe : styles.bubbleThem]}>
            <Text style={[styles.msgText, isMe ? styles.textMe : styles.textThem]}>
              {item.content}
            </Text>
            <View style={styles.timeContainer}>
              <Text style={[styles.timeText, isMe ? styles.timeTextMe : styles.timeTextThem]}>
                {timeString}
              </Text>
              {isMe && <Text style={styles.tickText}>✓✓</Text>}
            </View>
          </View>
        </View>
      </View>
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 25}
    >
      <View style={[styles.header, { paddingTop: insets.top + spacing.sm }]}>
        <Pressable onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Text style={styles.backText}>← Back</Text>
        </Pressable>
        <Text style={styles.headerTitle}>{receiverName}</Text>
        <View style={styles.statusIndicator}>
          <View style={[styles.statusDot, { backgroundColor: isConnected ? '#4CAF50' : '#F44336' }]} />
        </View>
      </View>

      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item, index) => item.id ? item.id.toString() : index.toString()}
        renderItem={renderMessage}
        contentContainerStyle={styles.listContent}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: true })}
        onLayout={() => flatListRef.current?.scrollToEnd({ animated: true })}
      />

      {activeQuote && (
        <View style={styles.quoteBanner}>
          <View style={styles.quoteContent}>
            <Text style={styles.quoteTitle}>Replying to Local Feed</Text>
            <Text style={styles.quoteText} numberOfLines={2}>"{activeQuote}"</Text>
          </View>
          <Pressable
            onPress={() => setActiveQuote(null)}
            style={styles.quoteClose}
            hitSlop={{ top: 15, bottom: 15, left: 15, right: 15 }}
          >
            <Text style={styles.quoteCloseText}>✕</Text>
          </Pressable>
        </View>
      )}

      <View style={[styles.inputContainer, { paddingBottom: Math.max(insets.bottom, spacing.sm) }]}>
        <TextInput
          style={styles.input}
          placeholder="Type a message..."
          placeholderTextColor={colors.textMuted}
          value={inputText}
          onChangeText={setInputText}
          multiline
        />
        <Pressable
          style={[styles.sendBtn, !inputText.trim() && styles.sendBtnDisabled]}
          onPress={handleSend}
          disabled={!inputText.trim()}
        >
          <Text style={styles.sendBtnText}>Send</Text>
        </Pressable>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: {
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: spacing.md,
    paddingBottom: spacing.sm, backgroundColor: colors.surface,
    borderBottomWidth: 1, borderBottomColor: '#E5E5E5'
  },
  backBtn: { paddingRight: spacing.md },
  backText: { color: colors.primary, fontSize: 16, fontWeight: '600' },
  headerTitle: { flex: 1, fontSize: 18, fontWeight: '700', color: colors.text },
  statusIndicator: { paddingLeft: spacing.sm },
  statusDot: { width: 10, height: 10, borderRadius: 5 },
  listContent: { padding: spacing.md, gap: spacing.sm },
  dateBadgeContainer: { alignItems: 'center', marginVertical: 12 },
  dateBadgeText: {
    backgroundColor: '#E0E0E0', color: '#555', paddingHorizontal: 12,
    paddingVertical: 4, borderRadius: 12, fontSize: 12, overflow: 'hidden'
  },
  msgWrapper: { width: '100%', flexDirection: 'row', marginVertical: 4 },
  msgMe: { justifyContent: 'flex-end' },
  msgThem: { justifyContent: 'flex-start' },
  msgBubble: {
    maxWidth: '80%', paddingHorizontal: 12, paddingTop: 10,
    paddingBottom: 8, borderRadius: radius.lg
  },
  bubbleMe: { backgroundColor: colors.primary, borderBottomRightRadius: 4 },
  bubbleThem: { backgroundColor: colors.surface, borderBottomLeftRadius: 4 },
  msgText: { fontSize: 15, lineHeight: 20 },
  textMe: { color: '#FFFFFF' },
  textThem: { color: colors.text },
  timeContainer: {
    flexDirection: 'row', justifyContent: 'flex-end',
    alignItems: 'center', marginTop: 4, marginLeft: 10
  },
  timeText: { fontSize: 10 },
  timeTextMe: { color: 'rgba(255, 255, 255, 0.7)' },
  timeTextThem: { color: colors.textMuted },
  tickText: { color: '#BBDEFB', fontSize: 10, marginLeft: 4, fontWeight: 'bold' },

  quoteBanner: {
    flexDirection: 'row', backgroundColor: '#F5F5F5', marginHorizontal: spacing.md,
    borderLeftWidth: 4, borderLeftColor: colors.primary, borderRadius: 4,
    padding: 8, marginBottom: 8, alignItems: 'center'
  },
  quoteContent: { flex: 1 },
  quoteTitle: { color: colors.primary, fontSize: 12, fontWeight: '700', marginBottom: 2 },
  quoteText: { color: '#555', fontSize: 13, fontStyle: 'italic' },
  quoteClose: { padding: 8, justifyContent: 'center', alignItems: 'center' },
  quoteCloseText: { color: '#888', fontSize: 18, fontWeight: 'bold' },

  inputContainer: {
    flexDirection: 'row', padding: spacing.sm, paddingHorizontal: spacing.md,
    backgroundColor: colors.surface, borderTopWidth: 1, borderTopColor: '#E5E5E5',
    alignItems: 'flex-end'
  },
  input: {
    flex: 1, backgroundColor: colors.background, borderRadius: radius.md,
    paddingHorizontal: 12, paddingTop: 10, paddingBottom: 10,
    fontSize: 15, color: colors.text, maxHeight: 100
  },
  sendBtn: {
    marginLeft: spacing.sm, backgroundColor: colors.primary,
    borderRadius: radius.md, paddingHorizontal: 16, height: 40,
    justifyContent: 'center', alignItems: 'center'
  },
  sendBtnDisabled: { opacity: 0.5 },
  sendBtnText: { color: '#FFF', fontWeight: '700', fontSize: 15 }
});