import type { Post } from '../../types/post';

const API_BASE_URL = 'https://skill-link-emprendedor-pjof.onrender.com/api';

const getAuthHeaders = (): HeadersInit => {
  const token = sessionStorage.getItem('jwt_token');
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` })
  };
};

const authenticatedRequest = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...getAuthHeaders(),
      ...options.headers,
    },
  });

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      sessionStorage.removeItem('jwt_token');
      sessionStorage.removeItem('user_info');
      window.location.href = '/';
      throw new Error('Retornado a la página de inicio debido a autenticación fallida');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const contentType = response.headers.get('content-type');
  const hasContent = response.status !== 204 && response.headers.get('content-length') !== '0';
  
  if (hasContent && contentType && contentType.includes('application/json')) {
    return await response.json();
  }
  
  return undefined as T;
};

export const fetchPosts = async (
  currentUserId: string | null, 
  page: number = 0, 
  size: number = 5
): Promise<Post[]> => {
  const userIdParam = currentUserId ? `currentUserId=${currentUserId}&` : '';
  const url = `${API_BASE_URL}/posts?${userIdParam}page=${page}&size=${size}`;
  
  const posts = await authenticatedRequest<Post[]>(url);
  
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
  const url = `${API_BASE_URL}/reactions?userId=${currentUserId}&targetId=${targetId}&targetType=${targetType}&reactionTypeId=${reactionTypeId}`;
  
  await authenticatedRequest<void>(url, {
    method: 'POST'
  });
};

export const fetchUserReaction = async (
  currentUserId: string,
  targetId: string,
  targetType: 'POST' | 'COMMENT'
): Promise<string | null> => {
  try {
    const url = `${API_BASE_URL}/reactions/user-reaction?userId=${currentUserId}&targetId=${targetId}&targetType=${targetType}`;
    
    const response = await fetch(url, {
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        sessionStorage.removeItem('jwt_token');
        sessionStorage.removeItem('user_info');
        window.location.href = '/';
        return null;
      }
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

  return await authenticatedRequest<any>(url.toString(), {
    method: 'POST',
    body: JSON.stringify({
      contenido: content
    })
  });
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

  return await authenticatedRequest<any>(url.toString(), {
    method: 'PUT',
    body: JSON.stringify({
      titulo: title,
      contenido: content
    })
  });
};

export const deletePost = async (postId: string): Promise<void> => {
  const url = `${API_BASE_URL}/posts/${postId}`;
  
  await authenticatedRequest<void>(url, {
    method: 'DELETE'
  });
};

export const updateComment = async (
  commentId: string,
  content: string
): Promise<any> => {
  const url = `${API_BASE_URL}/comments/${commentId}`;
  
  return await authenticatedRequest<any>(url, {
    method: 'PUT',
    body: JSON.stringify({
      contenido: content
    })
  });
};

export const deleteComment = async (commentId: string): Promise<void> => {
  const url = `${API_BASE_URL}/comments/${commentId}`;
  
  await authenticatedRequest<void>(url, {
    method: 'DELETE'
  });
};