import { useState, useEffect, useCallback } from 'react';
import { useConversations } from './useConversations';
import { useMessages } from './useMessages';
import { useWebSocketSubscriptions } from './useWebSocketSubscriptions';
import { useChatActions } from './useChatActions';
import { useTypingUsers } from './useTypingUsers';

export const useChat = (currentUserId: number) => {
  const [activeConversationId, setActiveConversationId] = useState<number | null>(null);
  const conversationsHook = useConversations(currentUserId);
  const messagesHook = useMessages();
  const actionsHook = useChatActions(currentUserId);
  const typingHook = useTypingUsers(currentUserId);

  const setActiveConversation = useCallback((conversationId: number | null) => {
    setActiveConversationId(conversationId);
  }, []);

  const handleMessageReceived = useCallback((conversationId: number, message: any) => {
    const conversationExists = conversationsHook.conversations.some(conv => conv.id === conversationId);
    if (!conversationExists) {
      setTimeout(() => {
        conversationsHook.reloadConversations();
      }, 500);
    } else {
      if (conversationId !== activeConversationId) {
        conversationsHook.incrementUnreadCount(conversationId, message);
      } else {
        conversationsHook.updateConversation(conversationId, {
          lastMessage: message,
          updatedAt: new Date()
        });
      }
    }
    
    return messagesHook.addMessage(conversationId, message);
  }, [
    conversationsHook.conversations, 
    conversationsHook.reloadConversations, 
    conversationsHook.incrementUnreadCount,
    conversationsHook.updateConversation,
    messagesHook.addMessage, 
    activeConversationId
  ]);

  const handleConversationUpdate = useCallback((conversationId: number, updates: any) => {
    conversationsHook.updateConversation(conversationId, updates);
  }, [conversationsHook.updateConversation]);

  const handleNewConversationDetected = useCallback(async () => {
    await conversationsHook.reloadConversations();
  }, [conversationsHook.reloadConversations]);

  const markAsRead = useCallback(async (conversationId: number) => {
    await actionsHook.markAsRead(conversationId);
    conversationsHook.markConversationAsRead(conversationId);
  }, [actionsHook.markAsRead, conversationsHook.markConversationAsRead]);

  const sendMessage = useCallback(async (conversationId: number, content: string) => {
    await actionsHook.sendMessage(conversationId, content);
    setTimeout(async () => {
      await conversationsHook.reloadConversations();
    }, 500);
  }, [actionsHook.sendMessage, conversationsHook.reloadConversations]);

  useWebSocketSubscriptions({
    conversations: conversationsHook.conversations,
    currentUserId,
    activeConversationId,
    onMessageReceived: handleMessageReceived,
    onConversationUpdate: handleConversationUpdate,
    onNewConversationDetected: handleNewConversationDetected,
    markAsRead
  });

  useEffect(() => {
    conversationsHook.loadConversations();
  }, [conversationsHook.loadConversations]);

  return {
    conversations: conversationsHook.conversations,
    messages: messagesHook.messages,
    loading: conversationsHook.loading,
    error: conversationsHook.error,
    typingUsers: typingHook.typingUsers,
    activeConversationId,
    currentUserId,
    hasMoreMessages: messagesHook.hasMoreMessages,
    loadingMoreMessages: messagesHook.loadingMoreMessages,

    loadConversations: conversationsHook.loadConversations,
    loadMessages: messagesHook.loadMessages,
    loadMoreMessages: messagesHook.loadMoreMessages,
    sendMessage,
    markAsRead,
    sendTypingNotification: actionsHook.sendTypingNotification,
    handleTypingNotification: typingHook.handleTypingNotification,
    setActiveConversation,
    setError: conversationsHook.setError,
    reloadConversations: conversationsHook.reloadConversations
  };
};