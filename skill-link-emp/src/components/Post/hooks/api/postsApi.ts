import type { Post } from '../../types/post';

const API_BASE_URL = 'https://skill-link-emprendedor-pjof.onrender.com/api';

export const fetchPosts = async (
  currentUserId: string | null, 
  page: number = 0, 
  size: number = 5
): Promise<Post[]> => {
  const userIdParam = currentUserId ? `currentUserId=${currentUserId}&` : '';
  const response = await fetch(
    `${API_BASE_URL}/posts?${userIdParam}page=${page}&size=${size}`
  );

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const posts = await response.json();
  
  const sortedPosts = posts.sort((a: any, b: any) => {
    const dateA = new Date(a.createdAt).getTime();
    const dateB = new Date(b.createdAt).getTime();
    return dateB - dateA;
  });

  return sortedPosts;
};

export const sendReaction = async (
  currentUserId: string,
  targetId: string,
  targetType: 'POST' | 'COMMENT',
  reactionType: string,
  reactionTypeId: number
): Promise<void> => {
  const response = await fetch(
    `${API_BASE_URL}/reactions?userId=${currentUserId}&targetId=${targetId}&targetType=${targetType}&reactionTypeId=${reactionTypeId}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );
  if (!response.ok && response.status !== 409) {
    const errorText = await response.text();
    throw new Error(`Fallo al enviar la reacción: ${response.status} ${response.statusText} - ${errorText}`);
  }
};

export const fetchUserReaction = async (
  currentUserId: string,
  targetId: string,
  targetType: 'POST' | 'COMMENT'
): Promise<string | null> => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/reactions/user-reaction?userId=${currentUserId}&targetId=${targetId}&targetType=${targetType}`
    );
    
    if (!response.ok) {
      if (response.status === 404) {
        return null;
      }
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }
    
    const userReaction = await response.text();
    return userReaction || null;
  } catch (error) {
    console.error('Error fetching user reaction:', error);
    return null;
  }
};

export const createComment = async (
  content: string,
  postId: string,
  userId: string,
  parentCommentId?: string
): Promise<any> => {
  const url = new URL(`${API_BASE_URL}/comments`);
  url.searchParams.append('postId', postId);
  url.searchParams.append('userId', userId);
  if (parentCommentId) {
    url.searchParams.append('parentCommentId', parentCommentId);
  }

  const response = await fetch(url.toString(), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      contenido: content
    })
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error al crear comentario: ${response.status} ${response.statusText} - ${errorText}`);
  }

  const result = await response.json();
  return result;
};

export const updatePost = async (
  postId: string,
  title: string,
  content: string,
  currentUserId?: string
): Promise<any> => {
  const url = new URL(`${API_BASE_URL}/posts/${postId}`);
  if (currentUserId) {
    url.searchParams.append('currentUserId', currentUserId);
  }

  const response = await fetch(url.toString(), {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      titulo: title,
      contenido: content
    })
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error al editar post: ${response.status} ${response.statusText} - ${errorText}`);
  }

  const result = await response.json();
  return result;
};


export const deletePost = async (postId: string): Promise<void> => {

  const response = await fetch(`${API_BASE_URL}/posts/${postId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    }
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error al eliminar post: ${response.status} ${response.statusText} - ${errorText}`);
  }

};


export const updateComment = async (
  commentId: string,
  content: string
): Promise<any> => {
  const response = await fetch(`${API_BASE_URL}/comments/${commentId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      contenido: content
    })
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error al editar comentario: ${response.status} ${response.statusText} - ${errorText}`);
  }

  const result = await response.json();
  return result;
};

export const deleteComment = async (commentId: string): Promise<void> => {

  const response = await fetch(`${API_BASE_URL}/comments/${commentId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    }
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error al eliminar comentario: ${response.status} ${response.statusText} - ${errorText}`);
  }
};