// File: src/hooks/useStompChat.js
import { useState, useEffect, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import 'text-encoding';
import { getSession } from '../services/tokenStorage';
import { decode } from 'base-64';

// Tera backend IP
const API_BASE_URL = 'http://192.168.29.217:8080';
const SOCKET_URL = 'ws://192.168.29.217:8080/ws';

const getUserId = (token) => {
  try {
    if (!token) return null;
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
    const decoded = JSON.parse(jsonPayload);
    return decoded.sub || decoded.userId || decoded.id;
  } catch (e) { return null; }
};

export default function useStompChat(receiverId) {
  const [messages, setMessages] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [myId, setMyId] = useState(null);
  const clientRef = useRef(null);

  // FAANG FIX 1: Chat khulte hi Database se purane/missed messages fetch karo
  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const session = await getSession();
        const token = session?.token ? session.token : (typeof session === 'string' ? session : '');
        if (!token) return;

        setMyId(getUserId(token));

        // Tera banaya hua backend endpoint call ho raha hai yahan
        const response = await fetch(`${API_BASE_URL}/api/chat/history?otherUserId=${receiverId}`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const history = await response.json();
          setMessages(history); // Purani chat DB se direct screen par
        }
      } catch (error) {
        console.error("Chat history fetch error:", error);
      }
    };
    fetchHistory();
  }, [receiverId]);

  // FIX 2: Live messages ke liye STOMP WebSocket connection
  useEffect(() => {
    const client = new Client({
      brokerURL: SOCKET_URL,
      forceBinaryWSFrames: true,
      appendMissingNULLonIncoming: true,
      reconnectDelay: 5000,

      beforeConnect: async () => {
        const session = await getSession();
        const token = session?.token ? session.token : (typeof session === 'string' ? session : '');
        client.connectHeaders = { Authorization: `Bearer ${token}` };
      },

      onConnect: () => {
        setIsConnected(true);
        client.subscribe('/user/queue/messages', (frame) => {
          const newMsg = JSON.parse(frame.body);
          if (newMsg.senderId === receiverId || newMsg.receiverId === receiverId) {
             setMessages((prev) => {
               // Duplicate messages rokne ke liye check
               if (prev.some(m => m.id === newMsg.id)) return prev;
               return [...prev, newMsg];
             });
          }
        });
      },
      onDisconnect: () => setIsConnected(false),
    });

    client.activate();
    clientRef.current = client;
    return () => client.deactivate();
  }, [receiverId]);

  const sendMessage = useCallback((content) => {
    if (clientRef.current && isConnected && content.trim() !== "") {
      const text = content.trim();

      // Optimistic Update: Turant screen par dikhane ke liye
      const localMsg = {
        id: Date.now().toString(),
        senderId: myId,
        receiverId: receiverId,
        content: text,
        timestamp: new Date().toISOString()
      };
      setMessages((prev) => [...prev, localMsg]);

      // Backend (aur Database) ko bhejne ke liye
      clientRef.current.publish({
        destination: '/app/chat',
        body: JSON.stringify({ receiverId, content: text }),
      });
    }
  }, [receiverId, isConnected, myId]);

  return { messages, isConnected, sendMessage };
}