import { useEffect, useRef, useState } from 'react';
import { websocketService, type MessageHandler, type TypingHandler, type ReadHandler } from '../../services/websocket';

export const useWebSocket = () => {
  const [connected, setConnected] = useState(false);
  const [connecting, setConnecting] = useState(false);
  const connectionAttempted = useRef(false);
  const isConnecting = useRef(false);

  useEffect(() => {
    if (connectionAttempted.current || isConnecting.current) {
      return;
    }
    
    connectionAttempted.current = true;
    isConnecting.current = true;
    setConnecting(true);

    websocketService.connect()
      .then(() => {
        setConnected(true);
        setConnecting(false);
        isConnecting.current = false;
      })
      .catch((error) => {
        setConnecting(false);
        setConnected(false);
        isConnecting.current = false;
        connectionAttempted.current = false;
      });

    return () => {
      console.log('Cleanup de useWebSocket');
    };
  }, []);

  useEffect(() => {
    const handleBeforeUnload = () => {
      websocketService.disconnect();
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, []);

  return { connected, connecting };
};

export const useConversationSubscription = (
  idConversacion: number | null,
  onMessage: MessageHandler,
  onTyping?: TypingHandler,
  onRead?: ReadHandler
) => {
  const unsubscribeRefs = useRef<(() => void)[]>([]);
  const currentConversationId = useRef<number | null>(null);

  useEffect(() => {
    if (idConversacion === null || idConversacion === undefined) {
      return;
    }

    if (typeof idConversacion !== 'number') {
      return;
    }

    if (idConversacion <= 0) {
      return;
    }

    if (!websocketService.isConnected()) {
      return;
    }

    if (!onMessage || typeof onMessage !== 'function') {
      return;
    }

    if (currentConversationId.current === idConversacion) {
      return;
    }

    unsubscribeRefs.current.forEach(unsubscribe => {
      try {
        unsubscribe();
      } catch (error) {
        console.warn('Error al desuscribirse:', error);
      }
    });
    unsubscribeRefs.current = [];

    currentConversationId.current = idConversacion;

    try {
      const unsubscribeMessages = websocketService.subscribeToConversation(
        idConversacion, 
        onMessage
      );
      unsubscribeRefs.current.push(unsubscribeMessages);

      if (onTyping && typeof onTyping === 'function') {
        const unsubscribeTyping = websocketService.subscribeToTyping(
          idConversacion, 
          onTyping
        );
        unsubscribeRefs.current.push(unsubscribeTyping);
      }

      if (onRead && typeof onRead === 'function') {
        const unsubscribeRead = websocketService.subscribeToRead(
          idConversacion, 
          onRead
        );
        unsubscribeRefs.current.push(unsubscribeRead);
      }

    } catch (error) {
      unsubscribeRefs.current.forEach(unsubscribe => {
        try {
          unsubscribe();
        } catch (cleanupError) {
          console.warn('Error en cleanup después de error:', cleanupError);
        }
      });
      unsubscribeRefs.current = [];
      currentConversationId.current = null;
    }

    return () => {
      unsubscribeRefs.current.forEach(unsubscribe => {
        try {
          unsubscribe();
        } catch (error) {
          console.warn('Error al desuscribirse en cleanup:', error);
        }
      });
      unsubscribeRefs.current = [];
      currentConversationId.current = null;
    };
  }, [idConversacion, onMessage, onTyping, onRead]);
};