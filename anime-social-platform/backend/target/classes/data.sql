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