import { useState, useEffect, useCallback } from 'react';
import type { Post, Comment, NotificationReaction } from '../types/post';

import { fetchPosts, fetchUserReaction, createComment } from './api/postsApi';
import { useWebSocket } from './websocket/useWebSocket';
import { useReactions } from './reactions/useReactions';
import { 
  parsePostDates, 
  updateCommentReactionsRecursive, 
  addCommentToPosts 
} from './utils/postUtils';

import { getUserAvatar } from '../utils/avatarUtils';
import { usePostsContext } from '../../../context/PostsContext';

interface UsePostsOptions {
  currentUserId: string | null;
}

interface UsePostsReturn {
  posts: Post[];
  loading: boolean;
  error: string | null;
  fetchPosts: () => Promise<void>;
  handleReaction: (postId: string, reactionType: string) => Promise<void>;
  handleCommentReaction: (commentId: string, reactionType: string) => Promise<void>;
  handleNewComment: (postId: string, content: string, parentCommentId?: string) => Promise<void>;
  loadMorePosts: () => Promise<void>;
  hasMore: boolean;
  loadingMore: boolean;
}

export const usePosts = ({ currentUserId }: UsePostsOptions): UsePostsReturn => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [hasInitialLoad, setHasInitialLoad] = useState(false);

  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const { shouldRefresh, markAsRefreshed } = usePostsContext();

  const handleOptimisticPostReaction = useCallback((postId: string, reactionType: string) => {
    setPosts(prevPosts => {
      return prevPosts.map(post => {
        if (post.id === postId) {
          const newReactions = { ...post.reactions };
          const currentUserReaction = post.userReaction;
          
          // Si el usuario ya tenía una reacción, la quitamos
          if (currentUserReaction) {
            newReactions[currentUserReaction] = Math.max(0, (newReactions[currentUserReaction] || 0) - 1);
          }
          
          // Si es la misma reacción, la quitamos (toggle)
          const newUserReaction = currentUserReaction === reactionType ? null : reactionType;
          
          // Si es una nueva reacción, la agregamos
          if (newUserReaction) {
            newReactions[newUserReaction] = (newReactions[newUserReaction] || 0) + 1;
          }
                    
          return {
            ...post,
            reactions: newReactions,
            userReaction: newUserReaction,
            _lastUpdate: Date.now()
          };
        }
        return post;
      });
    });
  }, []);

  const handleOptimisticCommentReaction = useCallback((commentId: string, reactionType: string) => {
    setPosts(prevPosts => {
      return prevPosts.map(post => {
        const updateCommentOptimistic = (comments: Comment[]): Comment[] => {
          return comments.map(comment => {
            if (comment.id === commentId) {
              const newReactions = { ...comment.reactions };
              const currentUserReaction = comment.userReaction;
              if (currentUserReaction) {
                newReactions[currentUserReaction] = Math.max(0, (newReactions[currentUserReaction] || 0) - 1);
              }
              const newUserReaction = currentUserReaction === reactionType ? null : reactionType;
              if (newUserReaction) {
                newReactions[newUserReaction] = (newReactions[newUserReaction] || 0) + 1;
              }              
              return {
                ...comment,
                reactions: newReactions,
                userReaction: newUserReaction
              };
            }
            
            if (comment.replies && comment.replies.length > 0) {
              return {
                ...comment,
                replies: updateCommentOptimistic(comment.replies)
              };
            }
            
            return comment;
          });
        };
        
        return {
          ...post,
          comments: updateCommentOptimistic(post.comments),
          _lastUpdate: Date.now()
        };
      });
    });
  }, []);

  const { handlePostReaction, handleCommentReaction } = useReactions({ 
    currentUserId,
    onOptimisticPostReaction: handleOptimisticPostReaction,
    onOptimisticCommentReaction: handleOptimisticCommentReaction
  });

  // Función para asegurar que los usuarios tengan avatares
  const ensureUserHasAvatar = useCallback((user: any) => {
    if (!user.avatar || user.avatar === 'https://default-avatar.url/path') {
      return {
        ...user,
        avatar: getUserAvatar(user.id)
      };
    }
    return user;
  }, []);

  const loadPosts = useCallback(async (pageToLoad: number = 0, isLoadMore: boolean = false) => {
    if (!isLoadMore && !hasInitialLoad) {
      setLoading(true);
    }
    
    if (isLoadMore) {
      setLoadingMore(true);
    }
    
    setError(null);
    
    try {
      const data = await fetchPosts(currentUserId, pageToLoad, 5);
      const normalizedPosts: Post[] = data.map(post => {
        const parsedPost = parsePostDates(post);
        parsedPost.author = ensureUserHasAvatar(parsedPost.author);
        const ensureCommentAvatars = (comments: Comment[]): Comment[] => {
          return comments.map(comment => ({
            ...comment,
            author: ensureUserHasAvatar(comment.author),
            replies: comment.replies ? ensureCommentAvatars(comment.replies) : []
          }));
        };
        
        parsedPost.comments = ensureCommentAvatars(parsedPost.comments);
        
        return parsedPost;
      });

      const hasMorePosts = normalizedPosts.length === 5; // Si recibimos menos de 5, no hay más
      setHasMore(hasMorePosts);

      if (isLoadMore) {
        // Agregar posts a la lista existente
        setPosts(prevPosts => {
          // Evitar duplicados
          const existingIds = new Set(prevPosts.map(p => p.id));
          const newPosts = normalizedPosts.filter(p => !existingIds.has(p.id));
          return [...prevPosts, ...newPosts];
        });
      } else {
        setPosts(normalizedPosts);
        setPage(0); // Reset page
      }

      if (!hasInitialLoad) {
        setHasInitialLoad(true);
      }
      if (shouldRefresh) {
        markAsRefreshed();
      }
      
    } catch (err: any) {
      setError("No se pudieron cargar los posts. Intenta de nuevo más tarde.");
      console.error('Error cargando posts:', err);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [currentUserId, ensureUserHasAvatar, hasInitialLoad, shouldRefresh, markAsRefreshed]);

  const loadMorePosts = useCallback(async () => {
    if (loadingMore || !hasMore) {
      return;
    }

    const nextPage = page + 1;
    await loadPosts(nextPage, true);
    setPage(nextPage);
  }, [page, loadingMore, hasMore, loadPosts]);

  // Función original para compatibilidad
  const fetchPostsOriginal = useCallback(async () => {
    await loadPosts(0, false);
  }, [loadPosts]);

  const handleNewComment = useCallback(async (
    postId: string, 
    content: string, 
    parentCommentId?: string
  ) => {
    if (!currentUserId) {
      console.warn('No hay usuario logueado para crear comentario');
      throw new Error('Debes estar logueado para comentar');
    }

    const optimisticComment: Comment = {
      id: `temp-${Date.now()}`,
      author: {
        id: currentUserId,
        name: 'Usuario Actual',
        avatar: getUserAvatar(currentUserId),
        title: 'Emprendedor'
      },
      content,
      createdAt: new Date(),
      reactions: {},
      userReaction: null,
      replies: [],
      postId,
      parentCommentId
    };

    setTimeout(() => {
      setPosts(prevPosts => {
        return prevPosts.map(post => {
          if (post.id === postId) {
            if (parentCommentId) {
              const updateRepliesRecursive = (comments: Comment[]): Comment[] => {
                return comments.map(comment => {
                  if (comment.id === parentCommentId) {
                    return {
                      ...comment,
                      replies: [...(comment.replies || []), optimisticComment]
                    };
                  } else if (comment.replies && comment.replies.length > 0) {
                    return {
                      ...comment,
                      replies: updateRepliesRecursive(comment.replies)
                    };
                  }
                  return comment;
                });
              };
              
              return {
                ...post,
                comments: updateRepliesRecursive(post.comments)
              };
            } else {
              return {
                ...post,
                comments: [...post.comments, optimisticComment]
              };
            }
          }
          return post;
        });
      });
    }, 300);

    try {
      const newCommentDTO = await createComment(content, postId, currentUserId, parentCommentId);
      const realComment: Comment = {
        id: newCommentDTO.id,
        author: ensureUserHasAvatar(newCommentDTO.author),
        content: newCommentDTO.content,
        createdAt: new Date(newCommentDTO.createdAt),
        reactions: newCommentDTO.reactions || {},
        userReaction: newCommentDTO.userReaction || null,
        replies: newCommentDTO.replies || []
      };

      setPosts(prevPosts => {
        return prevPosts.map(post => {
          if (post.id === postId) {
            if (parentCommentId) {
              const replaceInReplies = (comments: Comment[]): Comment[] => {
                return comments.map(comment => {
                  if (comment.id === parentCommentId) {
                    const updatedReplies = (comment.replies || []).map(reply => 
                      reply.id === optimisticComment.id ? realComment : reply
                    );
                    return {
                      ...comment,
                      replies: updatedReplies
                    };
                  } else if (comment.replies && comment.replies.length > 0) {
                    return {
                      ...comment,
                      replies: replaceInReplies(comment.replies)
                    };
                  }
                  return comment;
                });
              };
              
              return {
                ...post,
                comments: replaceInReplies(post.comments)
              };
            } else {
              const updatedComments = post.comments.map(comment => 
                comment.id === optimisticComment.id ? realComment : comment
              );
              return {
                ...post,
                comments: updatedComments
              };
            }
          }
          return post;
        });
      });

    } catch (error) {
      setPosts(prevPosts => {
        return prevPosts.map(post => {
          if (post.id === postId) {
            if (parentCommentId) {
              const removeFromReplies = (comments: Comment[]): Comment[] => {
                return comments.map(comment => {
                  if (comment.id === parentCommentId) {
                    return {
                      ...comment,
                      replies: (comment.replies || []).filter(reply => reply.id !== optimisticComment.id)
                    };
                  } else if (comment.replies && comment.replies.length > 0) {
                    return {
                      ...comment,
                      replies: removeFromReplies(comment.replies)
                    };
                  }
                  return comment;
                });
              };
              
              return {
                ...post,
                comments: removeFromReplies(post.comments)
              };
            } else {
              return {
                ...post,
                comments: post.comments.filter(comment => comment.id !== optimisticComment.id)
              };
            }
          }
          return post;
        });
      });
      
      console.error('Error al crear comentario, removiendo optimista');
      throw error;
    }
  }, [currentUserId, ensureUserHasAvatar]);

  const handleNewCommentFromWS = useCallback((newComment: Comment) => {
    const commentWithAvatar = {
      ...newComment,
      author: ensureUserHasAvatar(newComment.author)
    };
    
    setPosts(prevPosts => {
      const hasOptimisticComment = prevPosts.some(post => 
        post.comments.some(comment => 
          comment.content === commentWithAvatar.content && 
          comment.author.id === commentWithAvatar.author.id &&
          comment.id.startsWith('temp-')
        )
      );
      
      if (hasOptimisticComment) {
        return prevPosts;
      }
      
      return addCommentToPosts(prevPosts, commentWithAvatar);
    });
  }, [ensureUserHasAvatar]);

  const handleCommentUpdateFromWS = useCallback((updatedComment: Comment) => {
    const commentWithAvatar = {
      ...updatedComment,
      author: ensureUserHasAvatar(updatedComment.author)
    };
    
    setPosts(prevPosts => {
      return prevPosts.map(post => {
        const updateCommentsRecursive = (comments: Comment[]): Comment[] => {
          return comments.map(comment => {
            if (comment.id === commentWithAvatar.id) {
              return commentWithAvatar;
            } else if (comment.replies && comment.replies.length > 0) {
              return {
                ...comment,
                replies: updateCommentsRecursive(comment.replies)
              };
            }
            return comment;
          });
        };
        
        return {
          ...post,
          comments: updateCommentsRecursive(post.comments)
        };
      });
    });
  }, [ensureUserHasAvatar]);

  const handleCommentDeleteFromWS = useCallback((deletedCommentId: string) => {
    setPosts(prevPosts => {
      return prevPosts.map(post => {
        const removeCommentsRecursive = (comments: Comment[]): Comment[] => {
          return comments
            .filter(comment => comment.id !== deletedCommentId)
            .map(comment => ({
              ...comment,
              replies: comment.replies ? removeCommentsRecursive(comment.replies) : []
            }));
        };
        
        return {
          ...post,
          comments: removeCommentsRecursive(post.comments)
        };
      });
    });
  }, []);

  const handlePostUpdateFromWS = useCallback((updatedPost: Post) => {
    const postWithAvatar = {
      ...updatedPost,
      author: ensureUserHasAvatar(updatedPost.author)
    };
    
    setPosts(prevPosts => {
      return prevPosts.map(post => {
        if (post.id === postWithAvatar.id) {
          return {
            ...postWithAvatar,
            comments: post.comments,
            reactions: post.reactions,
            userReaction: post.userReaction
          };
        }
        return post;
      });
    });
  }, [ensureUserHasAvatar]);

  const handlePostDeleteFromWS = useCallback((deletedPostId: string) => {
    setPosts(prevPosts => {
      return prevPosts.filter(post => post.id !== deletedPostId);
    });
  }, []);

  const handleReactionChange = useCallback(async (reactionNotification: NotificationReaction) => {
    if (reactionNotification.targetType === 'POST') {
      let userReaction: string | null = null;
      
      if (currentUserId) {
        try {
          userReaction = await fetchUserReaction(currentUserId, reactionNotification.targetId, 'POST');
        } catch (error) {
          console.error('Error consultando userReaction de POST:', error);
        }
      }

      setPosts((prevPosts: Post[]) => {
        return prevPosts.map((post: Post) => {
          if (post.id === reactionNotification.targetId) {
            const hasOptimisticUpdate = post._lastUpdate && (Date.now() - post._lastUpdate) < 5000;
            
            if (hasOptimisticUpdate) {
              return post;
            }
            
            return {
              ...post,
              reactions: { ...reactionNotification.reactionCounts },
              userReaction: userReaction
            };
          }
          return post;
        });
      });

    } else if (reactionNotification.targetType === 'COMMENT') {
      let userReaction: string | null = null;
      
      if (currentUserId) {
        try {
          userReaction = await fetchUserReaction(currentUserId, reactionNotification.targetId, 'COMMENT');
        } catch (error) {
          console.error('Error consultando userReaction del comentario:', error);
        }
      }

      setPosts((prevPosts: Post[]) => {
        const timestamp = Date.now();
        return prevPosts.map((post: Post) => {
          const hasOptimisticUpdate = post._lastUpdate && (timestamp - post._lastUpdate) < 5000;
          
          if (hasOptimisticUpdate) {
            return post;
          }
          
          const updatedPost = {
            ...post,
            comments: updateCommentReactionsRecursive(
              post.comments,
              reactionNotification,
              userReaction
            ),
            _lastUpdate: timestamp
          };
          
          return updatedPost;
        });
      });
    }
  }, [currentUserId]);

  useWebSocket({
    onNewComment: handleNewCommentFromWS,
    onReactionChange: handleReactionChange,
    onCommentUpdate: handleCommentUpdateFromWS,
    onCommentDelete: handleCommentDeleteFromWS,
    onPostUpdate: handlePostUpdateFromWS, 
    onPostDelete: handlePostDeleteFromWS  
  });

  useEffect(() => {
    if (!hasInitialLoad || shouldRefresh) {
      loadPosts(0, false);
    }
  }, [loadPosts, hasInitialLoad, shouldRefresh]);

  return { 
    posts, 
    loading, 
    error, 
    fetchPosts: fetchPostsOriginal, 
    handleReaction: handlePostReaction,
    handleCommentReaction,
    handleNewComment,
    loadMorePosts,
    hasMore,
    loadingMore
  };
};