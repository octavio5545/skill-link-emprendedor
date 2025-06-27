import { useCallback } from 'react';
import { sendReaction } from '../api/postsApi';

const REACTION_TYPE_MAP: { [key: string]: number } = {
  "Me gusta": 1,  
  "Me encanta": 2,    
  "Celebrar": 3,     
  "Interesante": 4,    
  "De acuerdo": 5,    
  "Hacer gracia": 6, 
};

interface UseReactionsOptions {
  currentUserId: string | null;
  onOptimisticPostReaction?: (postId: string, reactionType: string) => void;
  onOptimisticCommentReaction?: (commentId: string, reactionType: string) => void;
}

export const useReactions = ({ 
  currentUserId, 
  onOptimisticPostReaction,
  onOptimisticCommentReaction 
}: UseReactionsOptions) => {
  const handlePostReaction = useCallback(async (postId: string, reactionType: string) => {
    const reactionTypeId = REACTION_TYPE_MAP[reactionType];

    if (!currentUserId) {
      console.warn("No currentUserId disponible. Por favor, inicia sesión para reaccionar.");
      return;
    }
    
    if (!reactionTypeId) {
      console.error(`Tipo de reacción inválido: ${reactionType}. No se encontró ID correspondiente.`);
      console.error('Tipos válidos:', Object.keys(REACTION_TYPE_MAP));
      return;
    }

    if (onOptimisticPostReaction) {
      onOptimisticPostReaction(postId, reactionType);
    }

    try {
      await sendReaction(currentUserId, postId, 'POST', reactionType, reactionTypeId);
    } catch (error) {
      console.error("❌ Error al enviar la reacción al post:", error);
    }
  }, [currentUserId, onOptimisticPostReaction]);

  const handleCommentReaction = useCallback(async (commentId: string, reactionType: string) => {
    const reactionTypeId = REACTION_TYPE_MAP[reactionType];

    if (!currentUserId) {
      console.warn("No currentUserId disponible. Por favor, inicia sesión para reaccionar.");
      return;
    }
    
    if (!reactionTypeId) {
      console.error(`Tipo de reacción inválido: ${reactionType}. No se encontró ID correspondiente.`);
      console.error('Tipos válidos:', Object.keys(REACTION_TYPE_MAP));
      return;
    }

    if (onOptimisticCommentReaction) {
      onOptimisticCommentReaction(commentId, reactionType);
    }

    try {
      await sendReaction(currentUserId, commentId, 'COMMENT', reactionType, reactionTypeId);
    } catch (error) {
      console.error("❌ Error al enviar la reacción al comentario:", error);
    }
  }, [currentUserId, onOptimisticCommentReaction]);

  const handleReaction = handlePostReaction;

  return { 
    handleReaction,
    handlePostReaction, 
    handleCommentReaction 
  };
};