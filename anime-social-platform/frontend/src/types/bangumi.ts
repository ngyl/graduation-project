// Bangumi API 数据类型定义

// 条目类型
export enum SubjectType {
  Book = 1,
  Anime = 2,
  Music = 3,
  Game = 4,
  Real = 6
}

// 图片信息
export interface Images {
  large?: string;
  common?: string;
  medium?: string;
  small?: string;
  grid?: string;
}

// 标签信息
export interface Tag {
  name: string;
  count: number;
}

// 动漫条目
export interface AnimeSubject {
  id: number;
  type: SubjectType;
  name: string;
  name_cn: string;
  summary: string;
  nsfw: boolean;
  locked: boolean;
  date?: string;
  platform?: string;
  images?: Images;
  tags?: Tag[];
  rating?: {
    total: number;
    count: {
      [key: string]: number;
    };
    score: number;
  };
  collection?: {
    wish: number;
    collect: number;
    doing: number;
    on_hold: number;
    dropped: number;
  };
  volumes?: number;
  eps?: number;
  rank?: number;
}

// 日历项
export interface CalendarItem {
  weekday: {
    id: number;
    cn: string;
    en: string;
    ja: string;
  };
  items: AnimeSubject[];
}

// 分页结果
export interface PagedResult<T> {
  total: number;
  limit: number;
  offset: number;
  data: T[];
} 