import { useState, useCallback } from 'react';
import { apiService } from '../../services/apiService';
import type { ChatMessage } from '../../types/api';
import { transformMensajesDTOToChatMessages, transformMensajeToChatMessage } from '../../utils/dataTransformers';

export const useMessages = () => {
  const [messages, setMessages] = useState<{ [conversationId: number]: ChatMessage[] }>({});
  const [hasMoreMessages, setHasMoreMessages] = useState<{ [conversationId: number]: boolean }>({});
  const [loadingMoreMessages, setLoadingMoreMessages] = useState<{ [conversationId: number]: boolean }>({});
  const [messagePage, setMessagePage] = useState<{ [conversationId: number]: number }>({});

  // Cargar mensajes iniciales
  const loadMessages = useCallback(async (conversationId: number) => {
    try {
      const mensajesDTO = await apiService.obtenerMensajes(conversationId);
      const transformedMessages = transformMensajesDTOToChatMessages(mensajesDTO);
      setMessages(prev => ({
        ...prev,
        [conversationId]: transformedMessages
      }));

      setMessagePage(prev => ({ ...prev, [conversationId]: 0 }));
      setHasMoreMessages(prev => ({ ...prev, [conversationId]: mensajesDTO.length >= 20 }));
      setLoadingMoreMessages(prev => ({ ...prev, [conversationId]: false }));
      
    } catch (err) {
      console.error('Error loading messages:', err);
      throw err;
    }
  }, []);

  const loadMoreMessages = useCallback(async (conversationId: number, page: number): Promise<ChatMessage[]> => {
    try {
      setLoadingMoreMessages(prev => ({ ...prev, [conversationId]: true }));
      const mensajesDTO = await apiService.obtenerMensajesPaginados(conversationId, page, 20);
      if (mensajesDTO.length === 0) {
        setHasMoreMessages(prev => ({ ...prev, [conversationId]: false }));
        return [];
      }
      
      const transformedMessages = transformMensajesDTOToChatMessages(mensajesDTO);
      setMessages(prev => ({
        ...prev,
        [conversationId]: [...transformedMessages, ...(prev[conversationId] || [])]
      }));
      
      setMessagePage(prev => ({ ...prev, [conversationId]: page }));
      
      if (mensajesDTO.length < 20) {
        setHasMoreMessages(prev => ({ ...prev, [conversationId]: false }));
      }
      
      return transformedMessages;
      
    } catch (err) {
      console.error('Error loading more messages:', err);
      return [];
    } finally {
      setLoadingMoreMessages(prev => ({ ...prev, [conversationId]: false }));
    }
  }, []);

  const addMessage = useCallback((conversationId: number, message: any) => {
    const chatMessage = transformMensajeToChatMessage(message);
    
    setMessages(prev => {
      const currentMessages = prev[conversationId] || [];
      if (currentMessages.some(m => m.id === chatMessage.id)) {
        return prev;
      }

      return {
        ...prev,
        [conversationId]: [...currentMessages, chatMessage]
      };
    });

    return chatMessage;
  }, []);

  const hasConversationMessages = useCallback((conversationId: number) => {
    return Boolean(messages[conversationId]);
  }, [messages]);

  return {
    messages,
    hasMoreMessages,
    loadingMoreMessages,
    messagePage,
    loadMessages,
    loadMoreMessages,
    addMessage,
    hasConversationMessages,
    setMessages
  };
};