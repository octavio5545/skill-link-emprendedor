import { useEffect, useCallback } from 'react';
import { websocketService } from '../../services/websocket';
import type { ChatConversation, ChatMessage, Mensaje } from '../../types/api';

interface UseWebSocketSubscriptionsProps {
  conversations: ChatConversation[];
  currentUserId: number;
  activeConversationId: number | null;
  onMessageReceived: (conversationId: number, message: any) => ChatMessage;
  onConversationUpdate: (conversationId: number, updates: Partial<ChatConversation>) => void;
  onNewConversationDetected: () => Promise<void>;
  markAsRead: (conversationId: number) => Promise<void>;
}

export const useWebSocketSubscriptions = ({
  conversations,
  currentUserId,
  activeConversationId,
  onMessageReceived,
  onConversationUpdate,
  onNewConversationDetected,
  markAsRead
}: UseWebSocketSubscriptionsProps) => {

  useEffect(() => {
    if (!websocketService.isConnected() || conversations.length === 0) {
      return;
    }
    const unsubscribeFunctions: (() => void)[] = [];
    conversations.forEach(conversation => {
      try {
        const unsubscribe = websocketService.subscribeToConversation(
          conversation.id,
          async (mensaje: Mensaje) => {
            const chatMessage = onMessageReceived(conversation.id, mensaje);
            const isFromOtherUser = chatMessage.senderId !== currentUserId;
            const isChatActive = activeConversationId === conversation.id;
            
            if (isFromOtherUser && isChatActive) {
              setTimeout(() => {
                markAsRead(conversation.id);
              }, 100);
            }

            const shouldIncrementUnread = isFromOtherUser && !isChatActive;
            onConversationUpdate(conversation.id, {
              lastMessage: chatMessage,
              updatedAt: chatMessage.timestamp,
              unreadCount: shouldIncrementUnread ? conversation.unreadCount + 1 : conversation.unreadCount
            });
          }
        );

        unsubscribeFunctions.push(unsubscribe);
      } catch (error) {
        console.error('Error suscribiéndose a conversación:', conversation.id, error);
      }
    });

    return () => {
      unsubscribeFunctions.forEach(unsubscribe => {
        try {
          unsubscribe();
        } catch (error) {
          console.warn('Error en cleanup:', error);
        }
      });
    };
  }, [conversations.length, currentUserId, activeConversationId, onMessageReceived, onConversationUpdate, markAsRead]);

  useEffect(() => {
    if (!websocketService.isConnected()) {
      return;
    }

    const globalTopic = `/topic/user/${currentUserId}/new-conversation`;
    
    const unsubscribeGlobal = websocketService.subscribeToConversation(
      0, // ID especial para suscripción global
      async (mensaje: Mensaje) => {
        console.log('NUEVA CONVERSACIÓN DETECTADA:', mensaje);
        await onNewConversationDetected();
      }
    );

    return () => {
      console.log('Limpiando suscripción global');
      unsubscribeGlobal();
    };
  }, [currentUserId, onNewConversationDetected]);
};