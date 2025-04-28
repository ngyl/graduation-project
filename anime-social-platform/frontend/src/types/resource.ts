/**
 * 资源数据类型
 */
export interface Resource {
  /** 资源ID */
  id: number;
  
  /** 上传用户ID */
  userId?: number;
  
  /** 上传用户名 */
  username?: string;
  
  /** 上传用户头像 */
  userAvatar?: string;
  
  /** 资源标题 */
  title: string;
  
  /** 资源描述 */
  description: string;
  
  /** 文件类型，也用作资源类型 */
  fileType?: string;
  
  /** 文件大小(KB) */
  fileSize?: number;
  
  /** 文件URL/下载链接 */
  fileUrl?: string;

  /** 封面URL */
  coverUrl?: string;
    
  /** 上传时间 */
  uploadTime: string;
  
  /** 下载次数 */
  downloadCount?: number;
  
  /** 收藏次数 */
  favoriteCount: number;
  
  /** 点赞次数 */
  likeCount: number;
  
  /** 标签列表 */
  tags: {
    id: number;
    name: string;
  }[];
  
  /** 当前用户是否已收藏该资源 */
  isFavorited?: boolean;

  /** 当前用户是否已点赞该资源 */
  isLiked?:boolean;
}

export interface ResourceListResponse {
  items: Resource[];
  total: number;
}



