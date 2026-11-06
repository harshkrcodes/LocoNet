import React, { useState, useRef, useEffect } from 'react';
import {
  View, Text, TextInput, StyleSheet, FlatList,
  KeyboardAvoidingView, Platform, Pressable
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import useStompChat from '../hooks/useStompChat';
import { colors, radius, spacing } from '../theme';

export default function ChatScreen({ route, navigation }) {
  const { receiverId, receiverName } = route.params;
  const insets = useSafeAreaInsets();

  const { messages, isConnected, sendMessage } = useStompChat(receiverId);
  const [inputText, setInputText] = useState('');
  const flatListRef = useRef();

  const handleSend = () => {
    if (inputText.trim() === '') return;
    sendMessage(inputText);
    setInputText('');
  };

  const renderMessage = ({ item }) => {
    const isMe = item.receiverId === receiverId;
    return (
      <View style={[styles.msgWrapper, isMe ? styles.msgMe : styles.msgThem]}>
        <View style={[styles.msgBubble, isMe ? styles.bubbleMe : styles.bubbleThem]}>
          <Text style={[styles.msgText, isMe ? styles.textMe : styles.textThem]}>
            {item.content}
            {isMe && <Text style={styles.tickText}>  ✓</Text>}
          </Text>
        </View>
      </View>
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      // iOS mein 'padding' aur Android mein 'height' sabse best kaam karta hai har phone ke liye
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
  msgWrapper: { width: '100%', flexDirection: 'row', marginVertical: 4 },
  msgMe: { justifyContent: 'flex-end' },
  msgThem: { justifyContent: 'flex-start' },
  msgBubble: { maxWidth: '80%', padding: 12, borderRadius: radius.lg },
  bubbleMe: { backgroundColor: colors.primary, borderBottomRightRadius: 4 },
  bubbleThem: { backgroundColor: colors.surface, borderBottomLeftRadius: 4 },
  msgText: { fontSize: 15, lineHeight: 20 },
  textMe: { color: '#FFFFFF' },
  textThem: { color: colors.text },
  tickText: { color: '#BBDEFB', fontSize: 12 },
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