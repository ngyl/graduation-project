export interface Post {
    id: number;
    title: string;
    content: string;
    created_at: string;
    updated_at: string;
    view_count: number;
    like_count: number;
    comment_count: number;
    user: {
        id: number;
        username: string;
        avatar: string;
    };
    tags: {
        id: number;
        name: string;
    }[];
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