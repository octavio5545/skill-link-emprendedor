import React from 'react';
import { ChatInterface } from './ChatInterface';
import { useAuth } from '../../../../context/AuthContext';
import { useChat } from '../../hooks/chat/useChat';

export const ChatPage: React.FC = () => {
  const { user } = useAuth();
  
  const currentUserId = user?.userId ? Number(user.userId) : 1;
  const chatData = useChat(currentUserId);

  return (
    <div className="h-full">
      <ChatInterface chatData={chatData} />
    </div>
  );
};

export default ChatPage;