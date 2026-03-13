package ai.skills.api.hotsearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建时间：2026/03/13
 * 功能：热搜采集记录持久化实体，对应 {@code hot_search_record} 表。
 * 作者：Devil
 */
@Data
@TableName("hot_search_record")
public class HotSearchRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台标识（baidu / weibo / douyin） */
    private String platform;

    /** 排名 */
    private Integer rankNum;

    /** 热搜标题 */
    private String title;

    /** 热度值 */
    private Long hotScore;

    /** 链接地址 */
    private String url;

    /** 热搜标签（热/新/沸等） */
    private String hotTag;

    /** 采集时间（同一批次相同） */
    private LocalDateTime collectedAt;

    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
