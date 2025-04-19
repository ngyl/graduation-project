USE anime_social;

-- 插入管理员用户 (密码为 'admin123')
INSERT INTO users (username, password, is_admin, status, register_time) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, 1, NOW())
ON DUPLICATE KEY UPDATE id = id;

-- 插入普通测试用户 (密码为 'password')
INSERT INTO users (username, password, status, register_time) 
VALUES 
    ('user1', '$2a$10$rAEJiNlRF9SjWzbGI0PswuEuQJG0lL6QZpJ4WZ7QiyPWxvaFuMJKS', 1, NOW()),
    ('user2', '$2a$10$rAEJiNlRF9SjWzbGI0PswuEuQJG0lL6QZpJ4WZ7QiyPWxvaFuMJKS', 1, NOW())
ON DUPLICATE KEY UPDATE id = id;

-- 插入帖子标签
INSERT INTO tags (name, category, type) 
VALUES 
    ('动画', '作品类型', 'post'),
    ('漫画', '作品类型', 'post'),
    ('轻小说', '作品类型', 'post'),
    ('热门讨论', '讨论类型', 'post'),
    ('新番推荐', '讨论类型', 'post'),
    ('经典回顾', '讨论类型', 'post')
ON DUPLICATE KEY UPDATE id = id;

-- 插入资源标签
INSERT INTO tags (name, category, type) 
VALUES 
    ('图片', '资源类型', 'resource'),
    ('视频', '资源类型', 'resource'),
    ('音乐', '资源类型', 'resource'),
    ('文档', '资源类型', 'resource'),
    ('壁纸', '资源类型', 'resource'),
    ('头像', '资源类型', 'resource')
ON DUPLICATE KEY UPDATE id = id;

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
(2, 3, '我偏好热血战斗类，比如《海贼王》', NOW() - INTERVAL 6 DAY, 0); 