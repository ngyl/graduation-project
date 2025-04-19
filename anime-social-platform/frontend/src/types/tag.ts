export interface TagDTO {
    id: number;
    name: string;
    category: string;
    type: 'post' | 'resource';
}

export interface UserTagsDTO {
    postTags: TagDTO[];
    resourceTags: TagDTO[];
} 