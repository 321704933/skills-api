package ai.skills.api.hotsearch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建时间：2026/03/13
 * 功能：热搜采集记录持久化实体，对应 {@code hot_search_record} 表。
 * 作者：Devil
 */
@Schema(name = "热搜记录", description = "热搜采集记录持久化实体")
@Data
@TableName("hot_search_record")
public class HotSearchRecord {

    @Schema(description = "记录ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 平台标识（baidu / weibo / douyin / toutiao） */
    @Schema(description = "平台标识", example = "baidu")
    private String platform;

    /** 排名 */
    @Schema(description = "排名", example = "1")
    private Integer rankNum;

    /** 热搜标题 */
    @Schema(description = "热搜标题", example = "今日热搜排名第一")
    private String title;

    /** 热度值 */
    @Schema(description = "热度值", example = "9800000")
    private Long hotScore;

    /** 链接地址 */
    @Schema(description = "热搜链接")
    private String url;

    /** 热搜标签（热/新/沸等） */
    @Schema(description = "热搜标签", example = "热")
    private String hotTag;

    /** 采集时间（同一批次相同） */
    @Schema(description = "采集时间")
    private LocalDateTime collectedAt;

    /** 记录创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
