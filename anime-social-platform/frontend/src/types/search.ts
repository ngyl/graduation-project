import type { Post } from './post';
import type { Resource } from './resource';
import type { UserDTO } from './user';

/**
 * 搜索结果项类型
 */
export interface SearchResultItem {
  id: number;
  type: 'post' | 'resource' | 'user';
  title: string;
  content: string;
  url: string;
  author: string;
  tags: string[];
  createdAt: string;
}

/**
 * 搜索结果类型
 */
export interface SearchResult {
  totalResults: number;
  posts: SearchResultItem[];
  totalPosts: number;
  resources: SearchResultItem[];
  totalResources: number;
  users: SearchResultItem[];
  totalUsers: number;
} 