-- MyForum 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS `forum` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `forum`;

CREATE TABLE `user` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username`   VARCHAR(64)  NOT NULL UNIQUE,
    `email`      VARCHAR(128) NOT NULL UNIQUE,
    `password`   VARCHAR(256) NOT NULL,
    `avatar`     VARCHAR(512) DEFAULT '',
    `bio`        VARCHAR(512) DEFAULT '',
    `role`       TINYINT      DEFAULT 0 COMMENT '0-普通用户 1-管理员',
    `status`     TINYINT      DEFAULT 1 COMMENT '1-正常 0-禁用',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `category` (
    `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(64) NOT NULL,
    `slug`        VARCHAR(64) NOT NULL UNIQUE,
    `description` VARCHAR(256) DEFAULT '',
    `sort`        INT DEFAULT 0,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `post` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT UNSIGNED NOT NULL,
    `category_id` INT UNSIGNED NOT NULL,
    `title`       VARCHAR(256) NOT NULL,
    `content`     LONGTEXT     NOT NULL,
    `content_raw` LONGTEXT     NOT NULL COMMENT '原始 Markdown',
    `status`      TINYINT DEFAULT 1 COMMENT '1-正常 0-删除 2-置顶',
    `view_count`  BIGINT DEFAULT 0,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category_id`),
    INDEX `idx_created_at` (`created_at`),
    FULLTEXT INDEX `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comment` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `post_id`    BIGINT UNSIGNED NOT NULL,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `parent_id`  BIGINT UNSIGNED DEFAULT 0 COMMENT '0-回复帖子 其他-回复评论',
    `reply_to`   BIGINT UNSIGNED DEFAULT 0 COMMENT '被回复的用户ID',
    `content`    TEXT NOT NULL,
    `status`     TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `like` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT UNSIGNED NOT NULL,
    `target_type` TINYINT NOT NULL COMMENT '1-帖子 2-回复',
    `target_id`   BIGINT UNSIGNED NOT NULL,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `idx_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `favorite` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `post_id`    BIGINT UNSIGNED NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX `idx_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `from_id`    BIGINT UNSIGNED DEFAULT 0,
    `type`       TINYINT NOT NULL COMMENT '1-回复 2-点赞 3-系统通知',
    `content`    VARCHAR(512) NOT NULL,
    `target_id`  BIGINT UNSIGNED DEFAULT 0,
    `is_read`    TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `message` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `from_id`    BIGINT UNSIGNED NOT NULL,
    `to_id`      BIGINT UNSIGNED NOT NULL,
    `content`    TEXT NOT NULL,
    `is_read`    TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_from_to` (`from_id`, `to_id`),
    INDEX `idx_to_read` (`to_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `category` (`name`, `slug`, `description`, `sort`) VALUES
('技术讨论', 'tech', '技术相关话题', 1),
('项目反馈', 'feedback', '项目建议与反馈', 2),
('职场交流', 'career', '职场经验分享', 3),
('资源分享', 'resources', '学习资源与工具', 4),
('灌水闲聊', 'chat', '轻松闲聊区', 5);
