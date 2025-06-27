import { useState, useCallback } from 'react';
import { apiService } from '../../services/apiService';
import type { ChatConversation } from '../../types/api';
import { transformConversacionResumenToChatConversation } from '../../utils/dataTransformers';

export const useConversations = (currentUserId: number) => {
  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Cargar conversaciones
  const loadConversations = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const conversationsData = await apiService.listarResumenesConversaciones(currentUserId);
      const transformedConversations = conversationsData.map(resumen => 
        transformConversacionResumenToChatConversation(resumen, currentUserId)
      );
      setConversations(transformedConversations);
    } catch (err) {
      setError('Error al cargar conversaciones');
    } finally {
      setLoading(false);
    }
  }, [currentUserId]);

  const reloadConversations = useCallback(async () => {
    try {
      const conversationsData = await apiService.listarResumenesConversaciones(currentUserId);
      const transformedConversations = conversationsData.map(resumen => 
        transformConversacionResumenToChatConversation(resumen, currentUserId)
      );
      
      setConversations(transformedConversations);
    } catch (err) {
    }
  }, [currentUserId]);

  const updateConversation = useCallback((conversationId: number, updates: Partial<ChatConversation>) => {
    setConversations(prev => 
      prev.map(conv => 
        conv.id === conversationId 
          ? { ...conv, ...updates }
          : conv
      ).sort((a, b) => b.updatedAt.getTime() - a.updatedAt.getTime())
    );
  }, []);

  const incrementUnreadCount = useCallback((conversationId: number, message: any) => {
    setConversations(prev => 
      prev.map(conv => {
        if (conv.id === conversationId) {
          const newUnreadCount = (conv.unreadCount || 0) + 1;
          
          return {
            ...conv,
            unreadCount: newUnreadCount,
            lastMessage: message,
            updatedAt: new Date()
          };
        }
        return conv;
      }).sort((a, b) => b.updatedAt.getTime() - a.updatedAt.getTime())
    );
  }, []);

  const markConversationAsRead = useCallback((conversationId: number) => {
    updateConversation(conversationId, { unreadCount: 0 });
  }, [updateConversation]);

  return {
    conversations,
    loading,
    error,
    loadConversations,
    reloadConversations,
    updateConversation,
    markConversationAsRead,
    incrementUnreadCount,
    setConversations,
    setError
  };
};