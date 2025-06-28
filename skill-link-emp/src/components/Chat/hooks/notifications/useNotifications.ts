import { useState, useEffect, useCallback } from 'react';
import type { ChatConversation } from '../../types/api';

export interface Notification {
  id: string;
  conversationId: number;
  senderName: string;
  senderAvatar: string;
  message: string;
  timestamp: Date;
  read: boolean;
}

export const useNotifications = (conversations: ChatConversation[], activeConversationId: number | null) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Procesar notificaciones basándose en las conversaciones
  useEffect(() => {
    const newNotifications: Notification[] = [];
    let totalUnread = 0;

    conversations.forEach(conversation => {
      // Solo crear notificaciones para conversaciones que NO están activas y tienen mensajes sin leer
      if (conversation.unreadCount > 0 && conversation.id !== activeConversationId) {
        totalUnread += conversation.unreadCount;
        
        const otherParticipant = conversation.participants.find(p => p.name !== 'Tú');
        
        if (otherParticipant && conversation.lastMessage) {
          newNotifications.push({
            id: `${conversation.id}-${conversation.lastMessage.id}`,
            conversationId: conversation.id,
            senderName: otherParticipant.name,
            senderAvatar: otherParticipant.avatar || '',
            message: conversation.lastMessage.content,
            timestamp: conversation.lastMessage.timestamp,
            read: false
          });
        }
      }
    });

    // Ordenar por timestamp más reciente
    newNotifications.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
    
    setNotifications(newNotifications);
    setUnreadCount(totalUnread);
  }, [conversations, activeConversationId]);

  const markNotificationAsRead = useCallback((notificationId: string) => {
    // Solo actualizar el estado local - el unreadCount real se actualiza desde las conversaciones
    setNotifications(prev => 
      prev.map(notification => 
        notification.id === notificationId 
          ? { ...notification, read: true }
          : notification
      )
    );
  }, []);

  const clearAllNotifications = useCallback(() => {
    setNotifications([]);
    setUnreadCount(0);
  }, []);

  return {
    notifications,
    unreadCount,
    markNotificationAsRead,
    clearAllNotifications
  };
};