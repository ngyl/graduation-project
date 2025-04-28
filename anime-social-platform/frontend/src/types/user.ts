import type { TagDTO } from './tag';

// 用户DTO接口
export interface UserDTO {
    id: number;
    username: string;
    avatar: string;
    bio: string;
    isAdmin: boolean;
    status: number;
    registerTime: string;
    lastLoginTime: string;
    tags: TagDTO[];
}

// 帖子信息
export interface Post {
    id: number;
    title: string;
    content: string;
    createdAt: string;
    viewCount: number;
    likeCount: number;
}

// 资源信息
export interface Resource {
    id: number;
    title: string;
    description: string;
    fileUrl?: string;
    fileType?: string;
    uploadTime: string;
}

// 收藏资源信息
export interface FavoriteResource {
    id: number;
    title: string;
    description: string;
    fileUrl?: string;
    fileType?: string;
    uploadTime: string;
}

// 关注用户信息
export interface FollowingUser {
    id: number;
    username: string;
    avatar: string;
    bio: string;
    isFollowing?: boolean;
}

// 用户详情响应
export interface UserDetail extends UserDTO {
    postCount: number;
    favoriteCount: number;
    followerCount: number;
    followingCount: number;
    isFollowing?: boolean;
} 