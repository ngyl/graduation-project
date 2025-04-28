USE anime_social;

-- 重置所有表的自增ID
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE tags AUTO_INCREMENT = 1;
ALTER TABLE posts AUTO_INCREMENT = 1;
ALTER TABLE comments AUTO_INCREMENT = 1;
ALTER TABLE resources AUTO_INCREMENT = 1;
ALTER TABLE friendships AUTO_INCREMENT = 1;
ALTER TABLE messages AUTO_INCREMENT = 1;
ALTER TABLE favorites AUTO_INCREMENT = 1;
ALTER TABLE content_tags AUTO_INCREMENT = 1;
ALTER TABLE user_tags AUTO_INCREMENT = 1;
ALTER TABLE post_likes AUTO_INCREMENT = 1;
ALTER TABLE events AUTO_INCREMENT = 1;
ALTER TABLE resource_likes AUTO_INCREMENT = 1;
ALTER TABLE event_participations AUTO_INCREMENT = 1;

-- 插入管理员用户 (密码为 'admin@123')
INSERT INTO users (username, password, is_admin, status, register_time) 
VALUES ('admin', '$2a$10$pyf6.DXj/5aCtPPlgFRShuAHy.sP1qzQE/KFMAaiW0o63oJOd44qm', 1, 1, NOW())
ON DUPLICATE KEY UPDATE id = id;

-- 插入普通测试用户 (密码为 'User@123')
INSERT INTO users (username, password, status, register_time, bio, avatar) 
VALUES 
    ('user1', '$2a$10$byykKmTOgq/wQR4d0XGwJu3Lf5aGaJiHzL8FjXp1yiviXPtESX2CO', 1, NOW(), '热爱动漫的普通用户', '/images/avatars/user1.jpg'),
    ('user2', '$2a$10$byykKmTOgq/wQR4d0XGwJu3Lf5aGaJiHzL8FjXp1yiviXPtESX2CO', 1, NOW(), '漫画爱好者，收藏控', '/images/avatars/user2.jpg'),
    ('user3', '$2a$10$byykKmTOgq/wQR4d0XGwJu3Lf5aGaJiHzL8FjXp1yiviXPtESX2CO', 1, NOW(), '轻小说达人，偏爱校园类题材', '/images/avatars/user3.jpg'),
    ('user4', '$2a$10$byykKmTOgq/wQR4d0XGwJu3Lf5aGaJiHzL8FjXp1yiviXPtESX2CO', 1, NOW(), '资深动漫评论家，热爱分享', '/images/avatars/user4.jpg'),
    ('user5', '$2a$10$byykKmTOgq/wQR4d0XGwJu3Lf5aGaJiHzL8FjXp1yiviXPtESX2CO', 1, NOW(), '视频剪辑爱好者，专注AMV制作', '/images/avatars/user5.jpg')
ON DUPLICATE KEY UPDATE id = id;

-- 插入帖子标签
INSERT INTO tags (name, category, type) 
VALUES 
    ('动画', '作品类型', 'post'),
    ('漫画', '作品类型', 'post'),
    ('轻小说', '作品类型', 'post'),
    ('热门讨论', '讨论类型', 'post'),
    ('新番推荐', '讨论类型', 'post'),
    ('经典回顾', '讨论类型', 'post'),
    ('萌系', '风格类型', 'post'),
    ('战斗热血', '风格类型', 'post'),
    ('科幻', '风格类型', 'post'),
    ('悬疑', '风格类型', 'post'),
    ('进击的巨人', '动漫作品', 'anime'),
    ('鬼灭之刃', '动漫作品', 'anime'),
    ('间谍过家家', '动漫作品', 'anime'),
    ('初音未来', '虚拟角色', 'character'),
    ('cosplay', '兴趣爱好', 'hobby'),
    ('原创插画', '创作', 'creation'),
    ('教程', '内容类型', 'content_type'),
    ('壁纸', '资源类型', 'resource_type'),
    ('角色分析', '内容类型', 'content_type'),
    ('社区活动', '活动', 'event');

-- 插入资源标签
INSERT INTO tags (name, category, type) 
VALUES 
    ('图片', '资源类型', 'resource'),
    ('视频', '资源类型', 'resource'),
    ('音乐', '资源类型', 'resource'),
    ('文档', '资源类型', 'resource'),
    ('头像', '资源类型', 'resource'),
    ('原声带', '音乐类型', 'resource'),
    ('高清', '画质类型', 'resource'),
    ('免费', '获取方式', 'resource'),
    ('独家', '稀有程度', 'resource');

-- 插入帖子数据
INSERT INTO posts (user_id, title, content, created_at, updated_at, view_count, like_count, is_top) VALUES
(1, '《鬼灭之刃》最新季讨论', '大家对《鬼灭之刃》最新季有什么看法？画风和战斗场景都非常精彩！', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY, 320, 45, 1),
(2, '漫画《海贼王》1000话纪念', '海贼王连载突破1000话，来讨论一下这部经典作品的精彩瞬间吧！', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 560, 120, 1),
(3, '轻小说《刀剑神域》评测', '这是一部将虚拟现实游戏和现实世界结合的优秀作品，剧情扣人心弦。', NOW() - INTERVAL 7 DAY, NULL, 230, 28, 0),
(4, '2023年新番推荐', '分享几部2023年值得关注的新番动画，包括《间谍过家家》第二季等。', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 5 DAY, 780, 95, 1),
(5, 'AMV制作经验分享', '分享我制作动漫音乐视频的一些经验和技巧，新手可以参考。', NOW() - INTERVAL 5 DAY, NULL, 150, 35, 0),
(1, '经典动漫《命运石之门》回顾', '《命运石之门》是科幻题材的巅峰之作，剧情环环相扣，值得多次回味。', NOW() - INTERVAL 4 DAY, NULL, 320, 67, 0),
(2, '最受欢迎的动漫女主角排行', '根据最新投票，来看看哪些动漫女主角最受大家喜爱！', NOW() - INTERVAL 3 DAY, NULL, 430, 53, 0),
(3, '《86-不存在的战区》解析', '这部作品深刻反映了战争与歧视主题，角色塑造非常成功。', NOW() - INTERVAL 2 DAY, NULL, 210, 40, 0),
(4, '动漫中的经典台词收集', '分享动漫中那些令人印象深刻的经典台词，欢迎大家补充。', NOW() - INTERVAL 1 DAY, NULL, 180, 25, 0),
(5, '如何看待动漫产业的发展趋势', '近年来动漫产业快速发展，未来可能会有哪些新的变化？', NOW(), NULL, 120, 18, 0);

-- 插入评论数据
INSERT INTO comments (post_id, user_id, content, created_at, parent_id) VALUES
(1, 2, '我最喜欢炭治郎与上弦之一的战斗场景，动画制作非常精良！', NOW() - INTERVAL 9 DAY, NULL),
(1, 3, '音效和配乐也很到位，增强了战斗的紧张感。', NOW() - INTERVAL 9 DAY, 1),
(1, 4, '不过剧情发展有些快，感觉省略了原作的一些细节。', NOW() - INTERVAL 8 DAY, NULL),
(2, 1, '海贼王塑造的世界观非常宏大，1000话的成就令人敬佩。', NOW() - INTERVAL 7 DAY, NULL),
(2, 3, '完全同意，尾田老师的世界构建能力无人能及！', NOW() - INTERVAL 7 DAY, 4),
(2, 5, '希望最终大秘宝揭晓时不会让粉丝失望。', NOW() - INTERVAL 6 DAY, 4),
(3, 2, '刀剑神域前几季还不错，但后期质量有所下降。', NOW() - INTERVAL 6 DAY, NULL),
(3, 4, '我觉得Alicization篇还是很有深度的。', NOW() - INTERVAL 6 DAY, 7),
(3, 1, '桐人的角色塑造确实有提升空间。', NOW() - INTERVAL 5 DAY, 7),
(4, 3, '《间谍过家家》确实很有意思，轻松搞笑又不失温情。', NOW() - INTERVAL 5 DAY, NULL),
(4, 5, '推荐《咒术回战》第二季，正在热播中。', NOW() - INTERVAL 4 DAY, NULL),
(5, 1, '请问剪辑用什么软件比较好？新手想学习。', NOW() - INTERVAL 4 DAY, NULL),
(5, 5, '我推荐使用Adobe Premiere Pro，功能强大且有很多教程。', NOW() - INTERVAL 3 DAY, 12),
(6, 2, '《命运石之门》的时间线设计非常巧妙，每次看都有新发现。', NOW() - INTERVAL 3 DAY, NULL),
(6, 4, '凶真的角色塑造堪称经典，中二病设定很有趣。', NOW() - INTERVAL 2 DAY, NULL),
(7, 3, '我最喜欢《鬼灭之刃》中的祢豆子，设计很独特。', NOW() - INTERVAL 2 DAY, NULL),
(8, 1, '这部作品确实很有深度，对战争题材的处理很成熟。', NOW() - INTERVAL 1 DAY, NULL),
(9, 5, '"人类的心是由记忆编织而成的"——《天空之城》', NOW() - INTERVAL 1 DAY, NULL),
(10, 2, '我认为元宇宙概念会给动漫产业带来新的发展方向。', NOW(), NULL),
(10, 4, 'AI技术在动画制作中的应用也会越来越广泛。', NOW(), 19);

-- 插入资源数据
INSERT INTO resources (user_id, title, description, file_url, file_type, file_size, upload_time, download_count, like_count) VALUES
(1, '鬼灭之刃高清壁纸合集', '收集了鬼灭之刃主要角色的高清壁纸，分辨率为1920x1080', '/resources/images/kimetsu_wallpapers.zip', 'image/zip', 25600, NOW() - INTERVAL 15 DAY, 450, 120),
(2, '进击的巨人第四季OST', '进击的巨人最终季原声带，包含所有背景音乐', '/resources/audio/aot_s4_ost.zip', 'audio/zip', 102400, NOW() - INTERVAL 12 DAY, 320, 85),
(3, '轻小说《为美好的世界献上祝福》PDF', '为美好的世界献上祝福轻小说1-5卷PDF合集', '/resources/docs/konosuba_vol1-5.pdf', 'application/pdf', 15360, NOW() - INTERVAL 10 DAY, 280, 65),
(4, '动漫人物绘画教程', '从基础到进阶的动漫人物绘画技巧教程视频', '/resources/videos/anime_drawing_tutorial.mp4', 'video/mp4', 307200, NOW() - INTERVAL 8 DAY, 180, 42),
(5, '鬼灭之刃AMV - 炎柱VS上弦之三', '炎柱战斗场景的精彩剪辑，配乐为LiSA的Gurenge', '/resources/videos/rengoku_amv.mp4', 'video/mp4', 153600, NOW() - INTERVAL 6 DAY, 520, 130),
(1, '海贼王人物头像包', '海贼王主要角色的头像图片，适合社交媒体使用', '/resources/images/onepiece_avatars.zip', 'image/zip', 12800, NOW() - INTERVAL 5 DAY, 210, 48),
(2, '动漫配乐精选集', '收集了多部经典动漫的配乐，高音质无损格式', '/resources/audio/anime_soundtracks.zip', 'audio/zip', 204800, NOW() - INTERVAL 4 DAY, 150, 37),
(3, '《咒术回战》漫画合集', '咒术回战漫画最新160话合集，高清扫描版', '/resources/docs/jujutsu_kaisen_manga.zip', 'application/zip', 256000, NOW() - INTERVAL 3 DAY, 380, 95),
(4, '动漫场景背景素材', '各种动漫场景的背景图片，可用于创作和设计', '/resources/images/anime_backgrounds.zip', 'image/zip', 51200, NOW() - INTERVAL 2 DAY, 140, 33),
(5, '宫崎骏动画电影剪辑集', '宫崎骏经典作品的精彩场景剪辑，怀旧向', '/resources/videos/ghibli_compilation.mp4', 'video/mp4', 409600, NOW() - INTERVAL 1 DAY, 290, 76),
(1, '动漫社交平台使用指南.pdf', '详细介绍平台各项功能的使用方法', '/files/guide.pdf', 'pdf', 2048, '2023-02-10 12:00:00', 512, 256),
(3, '原创插画合集.zip', '包含10张高清原创动漫插画', '/files/illustrations.zip', 'zip', 15360, '2023-02-13 18:00:00', 384, 192),
(2, '进击的巨人1080P无水印壁纸.zip', '20张高清壁纸，适合桌面和手机使用', '/files/aot_wallpapers.zip', 'zip', 30720, '2023-02-14 10:30:00', 640, 320),
(5, '动漫角色分析文章合集.docx', '详细分析多部热门动漫的主要角色', '/files/character_analysis.docx', 'docx', 1536, '2023-02-17 14:15:00', 192, 96),
(4, 'Cosplay妆容教程视频.mp4', '详细讲解如何化妆成为初音未来', '/files/cosplay_tutorial.mp4', 'mp4', 102400, '2023-02-23 09:45:00', 448, 224);

-- 添加好友关系数据
INSERT INTO friendships (user_id, friend_id, status, created_at) VALUES
(1, 2, 1, NOW() - INTERVAL 20 DAY),
(2, 1, 1, NOW() - INTERVAL 20 DAY),
(1, 3, 1, NOW() - INTERVAL 18 DAY),
(3, 1, 1, NOW() - INTERVAL 18 DAY),
(2, 3, 1, NOW() - INTERVAL 15 DAY),
(3, 2, 1, NOW() - INTERVAL 15 DAY),
(4, 1, 1, NOW() - INTERVAL 12 DAY),
(1, 4, 1, NOW() - INTERVAL 12 DAY),
(5, 1, 1, NOW() - INTERVAL 10 DAY),
(1, 5, 1, NOW() - INTERVAL 10 DAY),
(4, 2, 0, NOW() - INTERVAL 5 DAY),
(3, 5, 1, NOW() - INTERVAL 8 DAY),
(5, 3, 1, NOW() - INTERVAL 8 DAY),
(2, 5, 0, NOW() - INTERVAL 3 DAY),
(4, 5, 1, NOW() - INTERVAL 6 DAY),
(5, 4, 1, NOW() - INTERVAL 6 DAY);

-- 添加私信测试数据
INSERT INTO messages (sender_id, receiver_id, content, send_time, read_status) VALUES
(1, 2, '你好，你最近在看什么动漫？', NOW() - INTERVAL 5 DAY, 1),
(2, 1, '我最近在看《进击的巨人》最终季，你呢？', NOW() - INTERVAL 5 DAY, 1),
(1, 2, '我在看《鬼灭之刃》，听说很精彩', NOW() - INTERVAL 4 DAY, 1),
(2, 1, '是的，画风和战斗场景都很出色', NOW() - INTERVAL 4 DAY, 1),
(1, 3, '嘿，有推荐的新番吗？', NOW() - INTERVAL 3 DAY, 1),
(3, 1, '《间谍过家家》不错，轻松有趣', NOW() - INTERVAL 3 DAY, 1),
(4, 1, '听说你是动漫达人，能推荐点经典作品吗？', NOW() - INTERVAL 2 DAY, 0),
(1, 4, '《命运石之门》、《死亡笔记》都是经典之作', NOW() - INTERVAL 1 DAY, 0),
(3, 2, '你喜欢什么类型的动漫？', NOW() - INTERVAL 6 DAY, 1),
(2, 3, '我偏好热血战斗类，比如《海贼王》', NOW() - INTERVAL 6 DAY, 0),
(5, 1, '请问有没有好的AMV制作教程？', NOW() - INTERVAL 2 DAY, 1),
(1, 5, '我可以分享一些我收集的教程给你', NOW() - INTERVAL 2 DAY, 1),
(5, 1, '太感谢了！期待你的分享', NOW() - INTERVAL 1 DAY, 1),
(4, 3, '你对《咒术回战》第二季有什么看法？', NOW() - INTERVAL 5 DAY, 1),
(3, 4, '制作质量很高，但节奏有点赶，建议还是看漫画', NOW() - INTERVAL 4 DAY, 1),
(2, 5, '看到你上传的AMV，制作得很精彩！', NOW() - INTERVAL 3 DAY, 0),
(5, 3, '你知道哪里可以找到高质量的动漫原声带吗？', NOW() - INTERVAL 4 DAY, 1),
(3, 5, '可以尝试一下Spotify或者网站AniPlaylist', NOW() - INTERVAL 3 DAY, 1);

-- 添加收藏数据
INSERT INTO favorites (user_id, resource_id, created_at) VALUES
(1, 2, NOW() - INTERVAL 10 DAY),
(1, 5, NOW() - INTERVAL 9 DAY),
(2, 1, NOW() - INTERVAL 11 DAY),
(2, 3, NOW() - INTERVAL 8 DAY),
(3, 5, NOW() - INTERVAL 7 DAY),
(3, 7, NOW() - INTERVAL 6 DAY),
(4, 8, NOW() - INTERVAL 5 DAY),
(4, 10, NOW() - INTERVAL 4 DAY),
(5, 4, NOW() - INTERVAL 9 DAY),
(5, 9, NOW() - INTERVAL 3 DAY),
(1, 8, NOW() - INTERVAL 2 DAY),
(2, 10, NOW() - INTERVAL 1 DAY),
(3, 9, NOW() - INTERVAL 8 DAY),
(4, 7, NOW() - INTERVAL 7 DAY),
(5, 3, NOW() - INTERVAL 6 DAY),
(2, 1, '2023-02-10 14:20:00'),
(3, 1, '2023-02-10 16:35:00'),
(4, 1, '2023-02-11 10:45:00'),
(5, 1, '2023-02-11 15:30:00'),
(1, 2, '2023-02-13 19:10:00'),
(2, 3, '2023-02-14 12:25:00'),
(3, 4, '2023-02-17 16:40:00'),
(1, 5, '2023-02-23 11:15:00'),
(5, 3, '2023-02-24 14:50:00'),
(4, 2, '2023-02-25 09:30:00');

-- 添加内容标签关联数据
INSERT INTO content_tags (tag_id, content_type, content_id) VALUES
(1, 'post', 1), -- 动画标签关联到鬼灭之刃帖子
(8, 'post', 1), -- 战斗热血标签关联到鬼灭之刃帖子
(2, 'post', 2), -- 漫画标签关联到海贼王帖子
(8, 'post', 2), -- 战斗热血标签关联到海贼王帖子
(3, 'post', 3), -- 轻小说标签关联到刀剑神域评测
(9, 'post', 3), -- 科幻标签关联到刀剑神域评测
(5, 'post', 4), -- 新番推荐标签关联到2023年新番推荐
(12, 'resource', 1), -- 图片标签关联到鬼灭之刃壁纸
(15, 'resource', 1), -- 壁纸标签关联到鬼灭之刃壁纸
(13, 'resource', 2), -- 音乐标签关联到进击的巨人OST
(17, 'resource', 2), -- 原声带标签关联到进击的巨人OST
(14, 'resource', 3), -- 文档标签关联到轻小说PDF
(19, 'resource', 3), -- 免费标签关联到轻小说PDF
(12, 'resource', 4), -- 图片标签关联到动漫人物绘画教程
(13, 'resource', 4), -- 视频标签关联到动漫人物绘画教程
(12, 'resource', 5), -- 视频标签关联到鬼灭之刃AMV
(18, 'resource', 5), -- 高清标签关联到鬼灭之刃AMV
(6, 'post', 6), -- 经典回顾标签关联到命运石之门回顾
(9, 'post', 6), -- 科幻标签关联到命运石之门回顾
(4, 'post', 7), -- 热门讨论标签关联到动漫女主角排行
(7, 'post', 7), -- 萌系标签关联到动漫女主角排行
(1, 'post', 8), -- 动画标签关联到86解析
(10, 'post', 8), -- 悬疑标签关联到86解析
(4, 'post', 9), -- 热门讨论标签关联到经典台词收集
(4, 'post', 10), -- 热门讨论标签关联到动漫产业发展
(16, 'resource', 6), -- 头像标签关联到海贼王头像包
(19, 'resource', 6), -- 免费标签关联到海贼王头像包
(13, 'resource', 7), -- 音乐标签关联到动漫配乐精选集
(20, 'resource', 7), -- 独家标签关联到动漫配乐精选集
(14, 'resource', 8), -- 文档标签关联到咒术回战漫画
(19, 'resource', 8), -- 免费标签关联到咒术回战漫画
(12, 'resource', 9), -- 图片标签关联到动漫场景背景
(15, 'resource', 9), -- 壁纸标签关联到动漫场景背景
(12, 'resource', 10), -- 视频标签关联到宫崎骏剪辑
(18, 'resource', 10), -- 高清标签关联到宫崎骏剪辑
(10, 'post', 1),
(1, 'post', 2),
(6, 'post', 3),
(5, 'post', 4),
(4, 'post', 4),
(3, 'post', 5),
(9, 'post', 5),
(7, 'post', 7),
(8, 'resource', 3),
(1, 'resource', 3),
(9, 'resource', 4),
(5, 'resource', 5),
(4, 'resource', 5);

-- 添加用户标签关联数据（用户兴趣）
INSERT INTO user_tags (user_id, tag_id, created_at) VALUES
(1, 1, NOW() - INTERVAL 30 DAY), -- user1对动画感兴趣
(1, 8, NOW() - INTERVAL 29 DAY), -- user1对战斗热血感兴趣
(1, 5, NOW() - INTERVAL 28 DAY), -- user1对新番推荐感兴趣
(2, 2, NOW() - INTERVAL 30 DAY), -- user2对漫画感兴趣
(2, 8, NOW() - INTERVAL 29 DAY), -- user2对战斗热血感兴趣
(2, 6, NOW() - INTERVAL 28 DAY), -- user2对经典回顾感兴趣
(3, 3, NOW() - INTERVAL 30 DAY), -- user3对轻小说感兴趣
(3, 7, NOW() - INTERVAL 29 DAY), -- user3对萌系感兴趣
(3, 9, NOW() - INTERVAL 28 DAY), -- user3对科幻感兴趣
(4, 1, NOW() - INTERVAL 30 DAY), -- user4对动画感兴趣
(4, 5, NOW() - INTERVAL 29 DAY), -- user4对新番推荐感兴趣
(4, 10, NOW() - INTERVAL 28 DAY), -- user4对悬疑感兴趣
(5, 12, NOW() - INTERVAL 30 DAY), -- user5对视频感兴趣
(5, 13, NOW() - INTERVAL 29 DAY), -- user5对音乐感兴趣
(5, 17, NOW() - INTERVAL 28 DAY); -- user5对原声带感兴趣

-- 添加帖子点赞数据
INSERT INTO post_likes (post_id, user_id, created_at) VALUES
(1, 2, NOW() - INTERVAL 9 DAY),
(1, 3, NOW() - INTERVAL 9 DAY),
(1, 4, NOW() - INTERVAL 8 DAY),
(1, 5, NOW() - INTERVAL 8 DAY),
(2, 1, NOW() - INTERVAL 7 DAY),
(2, 3, NOW() - INTERVAL 7 DAY),
(2, 4, NOW() - INTERVAL 6 DAY),
(2, 5, NOW() - INTERVAL 6 DAY),
(3, 1, NOW() - INTERVAL 6 DAY),
(3, 2, NOW() - INTERVAL 5 DAY),
(4, 1, NOW() - INTERVAL 5 DAY),
(4, 2, NOW() - INTERVAL 5 DAY),
(4, 3, NOW() - INTERVAL 4 DAY),
(5, 1, NOW() - INTERVAL 4 DAY),
(5, 4, NOW() - INTERVAL 3 DAY),
(6, 2, NOW() - INTERVAL 3 DAY),
(6, 3, NOW() - INTERVAL 3 DAY),
(6, 5, NOW() - INTERVAL 2 DAY),
(7, 1, NOW() - INTERVAL 2 DAY),
(7, 4, NOW() - INTERVAL 2 DAY),
(8, 2, NOW() - INTERVAL 1 DAY),
(8, 5, NOW() - INTERVAL 1 DAY),
(9, 3, NOW() - INTERVAL 1 DAY),
(10, 1, NOW()),
(10, 4, NOW());

-- 添加资源点赞数据
INSERT INTO resource_likes (resource_id, user_id, created_at) VALUES
(1, 2, '2023-02-10 13:05:00'),
(1, 3, '2023-02-10 14:15:00'),
(1, 4, '2023-02-11 09:25:00'),
(1, 5, '2023-02-11 10:35:00'),
(2, 1, '2023-02-13 19:20:00'),
(2, 2, '2023-02-14 08:30:00'),
(2, 4, '2023-02-14 10:40:00'),
(3, 1, '2023-02-14 12:45:00'),
(3, 4, '2023-02-14 13:55:00'),
(3, 5, '2023-02-14 15:05:00');

-- 添加活动数据
INSERT INTO events (title, description, start_time, end_time, status, created_by) VALUES
('2023冬季动漫线上观影会', '一起在线观看2023冬季新番首播，并进行实时讨论', NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 3 HOUR, 1, 1),
('动漫绘画技巧分享workshop', '由专业画师分享动漫人物绘画技巧，适合初学者参加', NOW() + INTERVAL 10 DAY, NOW() + INTERVAL 10 DAY + INTERVAL 2 HOUR, 1, 4),
('经典动漫音乐鉴赏会', '一起回顾那些令人难忘的动漫原声带，分享感受', NOW() + INTERVAL 15 DAY, NOW() + INTERVAL 15 DAY + INTERVAL 2 HOUR, 1, 5),
('《鬼灭之刃》剧场版线下观影活动', '组织《鬼灭之刃》粉丝一起观看最新剧场版', NOW() + INTERVAL 20 DAY, NOW() + INTERVAL 20 DAY + INTERVAL 4 HOUR, 1, 2),
('动漫周边交换会', '带上你的闲置动漫周边来交换吧，淘到心仪的宝贝', NOW() + INTERVAL 25 DAY, NOW() + INTERVAL 25 DAY + INTERVAL 5 HOUR, 1, 3),
('线上配音大赛', '为你喜欢的动漫角色配音，赢取精美周边！', '2023-03-15 10:00:00', '2023-03-20 18:00:00', 1, 1),
('动漫插画创作大赛', '以"未来"为主题创作原创插画，优秀作品将获得实体出版机会！', '2023-04-01 00:00:00', '2023-04-30 23:59:59', 1, 3),
('《鬼灭之刃》线上观影会', '一起在线观看《鬼灭之刃》剧场版，并进行实时讨论！', '2023-03-10 19:00:00', '2023-03-10 22:00:00', 1, 5),
('动漫周边交换活动', '带上你的闲置动漫周边来交换吧！', '2023-05-05 14:00:00', '2023-05-05 17:00:00', 1, 4),
('动漫音乐鉴赏会', '分享讨论你最喜欢的动漫原声音乐！', '2023-03-25 20:00:00', '2023-03-25 22:00:00', 1, 2);

-- 插入活动参与数据
INSERT INTO event_participations (user_id, event_id, participation_time, status) VALUES
(2, 1, '2023-02-26 15:10:00', 1),
(3, 1, '2023-02-26 16:20:00', 1),
(4, 1, '2023-02-27 09:30:00', 1),
(5, 1, '2023-02-27 11:40:00', 1),
(1, 2, '2023-02-28 10:15:00', 1),
(2, 2, '2023-02-28 13:25:00', 1),
(4, 2, '2023-03-01 14:35:00', 1),
(1, 3, '2023-03-01 16:45:00', 1),
(2, 3, '2023-03-02 09:55:00', 1),
(3, 3, '2023-03-02 11:05:00', 1); 