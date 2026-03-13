-- ============================================================
-- 热搜采集记录表
-- 创建时间：2026/03/13
-- 功能：存储各平台定时采集的热搜数据，每次采集为一个批次（同一 collected_at）。
-- 作者：Devil
-- ============================================================

CREATE TABLE IF NOT EXISTS `hot_search_record`
(
    `id`           BIGINT       NOT NULL COMMENT '主键ID（ASSIGN_ID 策略）',
    `platform`     VARCHAR(32)  NOT NULL COMMENT '平台标识（baidu / weibo / douyin）',
    `rank_num`     INT          NOT NULL COMMENT '排名',
    `title`        VARCHAR(512) NOT NULL COMMENT '热搜标题',
    `hot_score`    BIGINT       NOT NULL DEFAULT 0 COMMENT '热度值',
    `url`          VARCHAR(1024)         DEFAULT '' COMMENT '链接地址',
    `hot_tag`      VARCHAR(32)           DEFAULT '' COMMENT '热搜标签（热/新/沸等）',
    `collected_at` DATETIME     NOT NULL COMMENT '采集时间（同一批次相同）',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_platform_collected` (`platform`, `collected_at`),
    INDEX `idx_collected_at` (`collected_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '热搜采集记录';
