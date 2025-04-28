import type { UserDTO } from './user';
import type { TagDTO } from './tag';

export interface Post {
    id: number;
    title: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    viewCount: number;
    likeCount: number;
    commentCount: number;
    isTop: boolean;
    isLiked: boolean;
    userDTO: UserDTO;
    tags: TagDTO[];
}

export interface CreatePostRequest {
    title: string;
    content: string;
    tagIds: number[];
}

export interface GetPostsParams {
    page: number;
    size: number;
    type?: string;
    tagId?: number | null;
    sort?: string;
}

export interface PostListResponse {
    items: Post[];
    total: number;
} 