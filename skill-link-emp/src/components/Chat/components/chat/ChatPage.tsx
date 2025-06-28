import React from 'react';
import { ChatInterface } from './ChatInterface';
import { useAuth } from '../../../../context/AuthContext';
import { useChat } from '../../hooks/chat/useChat';
import { useSearchParams } from 'react-router-dom';

export const ChatPage: React.FC = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  
  const currentUserId = user?.userId ? Number(user.userId) : 1;
  const chatData = useChat(currentUserId);

  // Obtener conversationId de la URL si existe
  const conversationIdFromUrl = searchParams.get('conversation');
  const initialConversationId = conversationIdFromUrl ? Number(conversationIdFromUrl) : null;

  console.log('🔗 ChatPage - conversationId desde URL:', initialConversationId);

  return (
    <div className="h-full">
      <ChatInterface 
        chatData={chatData} 
        initialConversationId={initialConversationId}
      />
    </div>
  );
};

export default ChatPage;