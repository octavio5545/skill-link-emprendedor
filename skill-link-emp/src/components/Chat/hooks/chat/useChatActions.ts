import { useCallback } from 'react';
import { websocketService } from '../../services/websocket';
import { apiService } from '../../services/apiService';
import type { MensajeWebSocketDTO, MarcarLeidosRequest, UsuarioEscribiendoDTO } from '../../types/api';

export const useChatActions = (currentUserId: number) => {

  const sendMessage = useCallback(async (conversationId: number, content: string) => {
    try {
      const messageDto: MensajeWebSocketDTO = {
        idConversacion: conversationId,
        idEmisor: currentUserId,
        contenido: content
      };

      if (websocketService.isConnected()) {
        websocketService.sendMessage(messageDto);
      } else {
        throw new Error('WebSocket no conectado');
      }
    } catch (err) {
      console.error('Error sending message:', err);
      throw err;
    }
  }, [currentUserId]);

  const markAsRead = useCallback(async (conversationId: number) => {
    try {
      const request: MarcarLeidosRequest = {
        idConversacion: conversationId,
        idUsuario: currentUserId
      };

      if (websocketService.isConnected()) {
        websocketService.markAsRead(request);
      } else {
        await apiService.marcarMensajesComoLeidos(request);
      }
    } catch (err) {
      console.error('Error marking as read:', err);
      throw err;
    }
  }, [currentUserId]);

  const sendTypingNotification = useCallback((conversationId: number) => {
    if (websocketService.isConnected()) {
      const typingDto: UsuarioEscribiendoDTO = {
        idConversacion: conversationId,
        idUsuario: currentUserId
      };
      websocketService.sendTypingNotification(typingDto);
    }
  }, [currentUserId]);

  return {
    sendMessage,
    markAsRead,
    sendTypingNotification
  };
};