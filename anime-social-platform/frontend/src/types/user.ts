// 用户统计信息
export interface UserStats {
    postCount: number;
    favoriteCount: number;
    followingCount: number;
}

// 帖子信息
export interface Post {
    id: number;
    title: string;
    content: string;
    created_at: string;
    view_count: number;
    like_count: number;
}

// 收藏资源信息
export interface FavoriteResource {
    id: number;
    title: string;
    description: string;
    file_url: string;
    file_type: string;
    created_at: string;
}

// 关注用户信息
export interface FollowingUser {
    id: number;
    username: string;
    avatar: string;
    bio: string;
}

// 用户详情响应
export interface UserDetailResponse {
    user: {
        id: number;
        username: string;
        avatar: string;
        bio: string;
        isAdmin: boolean;
    };
    stats: UserStats;
} 