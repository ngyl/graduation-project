export interface TagDTO {
    id: number;
    name: string;
    category: string;
    type: 'post' | 'resource';
    createdAt?: string;
    updatedAt?: string;
    usage?: number; // 使用次数
}

export interface UserTagsDTO {
    postTags: TagDTO[];
    resourceTags: TagDTO[];
}

export interface TagFormData {
    name: string;
    category: string;
    type: 'post' | 'resource';
} 