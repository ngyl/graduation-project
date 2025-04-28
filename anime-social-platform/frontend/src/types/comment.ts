/**
 * 评论DTO类型定义
 */
export interface CommentDTO {
  /**
   * 评论ID
   */
  id: number;
  
  /**
   * 帖子ID
   */
  postId: number;
  
  /**
   * 评论用户ID
   */
  userId: number;
  
  /**
   * 评论用户名
   */
  username: string;
  
  /**
   * 评论用户头像
   */
  userAvatar: string;
  
  /**
   * 评论内容
   */
  content: string;
  
  /**
   * 评论时间
   */
  createdAt: string;
  
  /**
   * 父评论ID
   * 如果是对评论的回复，则此字段不为空
   */
  parentId: number | null;
  
  /**
   * 子评论/回复列表
   */
  replies?: CommentDTO[];
}

/**
 * 创建评论请求类型
 */
export interface CreateCommentRequest {
  /**
   * 帖子ID
   */
  postId: number;
  
  /**
   * 评论内容
   */
  content: string;
  
  /**
   * 父评论ID
   * 如果是对评论的回复，则此字段不为空
   */
  parentId?: number;
} 