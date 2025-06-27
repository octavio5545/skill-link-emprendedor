import React, { useState, useEffect } from 'react';
import { ConversationList } from './conversation/ConversationList';
import { ChatWindow } from './window/ChatWindow';
import { useWebSocket } from '../../hooks/websocket/useWebSocket';
import { MessageCircle, Wifi, WifiOff, RefreshCw } from 'lucide-react';
import type { ChatUser } from '../../types/api';

interface ChatInterfaceProps {
  chatData: any;
}

export const ChatInterface: React.FC<ChatInterfaceProps> = ({ chatData }) => {
  const [activeConversationId, setActiveConversationId] = useState<number | null>(null);
  const [showChat, setShowChat] = useState<boolean>(false);
  const [loadingMessages, setLoadingMessages] = useState<{ [conversationId: number]: boolean }>({});

  const { connected: wsConnected, connecting: wsConnecting } = useWebSocket();
  
  const {
    conversations,
    messages,
    loading,
    error,
    typingUsers,
    currentUserId,
    hasMoreMessages,
    loadingMoreMessages,
    loadConversations,
    loadMessages,
    loadMoreMessages,
    sendMessage,
    markAsRead,
    sendTypingNotification,
    setActiveConversation,
    setError,
    activeConversationId: hookActiveConversationId
  } = chatData;

  useEffect(() => {
    if (hookActiveConversationId && hookActiveConversationId !== activeConversationId) {
      handleSelectConversation(hookActiveConversationId);
    }
  }, [hookActiveConversationId]);

  const handleSendMessage = async (content: string) => {
    if (!activeConversationId) {
      console.error('No hay conversación activa');
      return;
    }

    if (!wsConnected) {
      setError('No hay conexión WebSocket. Los mensajes requieren conexión en tiempo real.');
      return;
    }
    
    await sendMessage(activeConversationId, content);
  };

  const handleSelectConversation = async (conversationId: number) => {
    if (!conversationId || typeof conversationId !== 'number' || conversationId <= 0) {
      console.error('ID de conversación inválido:', conversationId);
      return;
    }
    setActiveConversationId(conversationId);
    setActiveConversation(conversationId);
    setShowChat(true);
    
    if (!messages[conversationId]) {
      setLoadingMessages(prev => ({ ...prev, [conversationId]: true }));
      
      try {
        await loadMessages(conversationId);
      } finally {
        setLoadingMessages(prev => ({ ...prev, [conversationId]: false }));
      }
    }
    
    await markAsRead(conversationId);
  };

  const handleBackToList = () => {
    setShowChat(false);
    setActiveConversationId(null);
    setActiveConversation(null);
  };

  const handleLoadMoreMessages = async (conversationId: number, page: number) => {
    return await loadMoreMessages(conversationId, page);
  };

  useEffect(() => {
    return () => {
      setActiveConversation(null);
    };
  }, [setActiveConversation]);

  const activeConversation = conversations.find((c: { id: number | null; }) => c.id === activeConversationId);

  const currentUser: ChatUser = {
    id: currentUserId,
    name: 'Joseph',
    email: 'prueba@email.com',
    avatar: 'https://images.pexels.com/photos/1040880/pexels-photo-1040880.jpeg?auto=compress&cs=tinysrgb&w=150&h=150&dpr=2',
    role: 'colaborador',
    status: 'online',
    expertise: ['Desarrollo Web', 'MVP', 'Lean Startup']
  };

  if (loading && conversations.length === 0) {
    return (
      <div className="h-[calc(100vh-4rem)] w-full flex items-center justify-center bg-white">
        <div className="text-center max-w-md mx-auto px-6">
          <div className="relative mb-8">
            <div className="w-12 h-12 mx-auto">
              <div className="absolute inset-0 border-3 border-emerald-200 rounded-full"></div>
              <div className="absolute inset-0 border-3 border-transparent border-t-emerald-500 rounded-full animate-spin"></div>
            </div>
            <div className="absolute inset-0 w-12 h-12 mx-auto">
              <div className="absolute inset-1 border-2 border-transparent border-t-emerald-300 rounded-full animate-spin" 
                   style={{ animationDirection: 'reverse', animationDuration: '1.5s' }}></div>
            </div>
          </div>

          <div className="mb-6">
            <div className="w-16 h-16 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-full flex items-center justify-center mx-auto shadow-lg">
              <MessageCircle className="w-8 h-8 text-white" />
            </div>
          </div>

          <h3 className="text-xl font-semibold text-gray-900 mb-3">Cargando conversaciones</h3>
          <p className="text-gray-600 mb-6">Conectando con tu red de colaboradores...</p>
          
          {wsConnecting && (
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-700 rounded-lg text-sm font-medium">
              <div className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></div>
              <span>Estableciendo conexión</span>
            </div>
          )}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-[calc(100vh-4rem)] w-full flex items-center justify-center bg-white">
        <div className="text-center max-w-md mx-auto px-6">
          <div className="w-16 h-16 bg-gradient-to-br from-red-500 to-red-600 rounded-full flex items-center justify-center mx-auto mb-6 shadow-lg">
            <WifiOff className="w-8 h-8 text-white" />
          </div>
          
          <h3 className="text-xl font-semibold text-gray-900 mb-3">Error de conexión</h3>
          <p className="text-gray-600 mb-6 leading-relaxed">{error}</p>
          
          <button
            onClick={() => {
              setError(null);
              loadConversations();
            }}
            className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-600 text-white rounded-lg hover:from-emerald-600 hover:to-teal-700 transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
          >
            <RefreshCw className="w-4 h-4" />
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-4rem)] w-full flex bg-white">
      <div className={`${showChat ? 'hidden lg:block' : 'block'} w-full lg:w-96 flex-shrink-0 bg-white h-full`}>
        <ConversationList
          conversations={conversations}
          activeConversationId={activeConversationId}
          onSelectConversation={handleSelectConversation}
          currentUser={currentUser}
          loading={loading}
        />
      </div>

      <div className={`${showChat ? 'block' : 'hidden lg:block'} flex-1 min-w-0 bg-white h-full`}>
        {activeConversation ? (
          <ChatWindow
            conversation={activeConversation}
            messages={messages[activeConversationId!] || []}
            currentUser={currentUser}
            onSendMessage={handleSendMessage}
            onBack={handleBackToList}
            typingUsers={typingUsers[activeConversationId!] || []}
            onTyping={() => activeConversationId && sendTypingNotification(activeConversationId)}
            disabled={!wsConnected}
            onLoadMoreMessages={handleLoadMoreMessages}
            hasMoreMessages={hasMoreMessages[activeConversationId!] ?? true}
            loadingMoreMessages={loadingMoreMessages[activeConversationId!] ?? false}
            loadingMessages={loadingMessages[activeConversationId!] ?? false}
          />
        ) : (
          <div className="h-full flex items-center justify-center bg-white">
            <div className="text-center max-w-sm mx-auto px-6">
              <div className="w-20 h-20 bg-gradient-to-br from-emerald-500 to-teal-600 rounded-full flex items-center justify-center mx-auto mb-6 shadow-lg">
                <MessageCircle className="w-10 h-10 text-white" />
              </div>
              
              <h3 className="text-xl font-semibold text-gray-900 mb-3">
                Selecciona una conversación
              </h3>
              <p className="text-gray-600 leading-relaxed mb-6">
                {conversations.length === 0 
                  ? 'No hay conversaciones disponibles. Conecta con mentores y colaboradores para comenzar.'
                  : 'Elige una conversación para comenzar a intercambiar ideas y conocimientos.'
                }
              </p>
              
              {wsConnected && (
                <div className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-700 rounded-lg text-sm font-medium">
                  <Wifi className="w-4 h-4" />
                  <span>Conectado</span>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};