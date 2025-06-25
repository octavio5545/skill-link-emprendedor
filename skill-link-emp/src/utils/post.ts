import { IPost } from "../types/IPost";

export const generateUniqueId = (): string => {
  return Math.random().toString(36).slice(2, 9);
};

export const getAllTags = (posts: IPost[]): string[] => {
  const tagSet = new Set<string>();
  posts.forEach(post => {
    post.tags.forEach((tag: string) => tagSet.add(tag));
  });
  return Array.from(tagSet).sort();
};

export const filterPosts = (posts: IPost[], searchTerm: string, selectedTags: string[]) => {
  return posts.filter(post => {
    const matchesSearch = searchTerm === '' || 
      post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      post.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      post.author.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesTags = selectedTags.length === 0 || 
      selectedTags.some(tag => post.tags.includes(tag));
    
    return matchesSearch && matchesTags;
  });
};