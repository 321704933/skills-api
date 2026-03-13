package ai.skills.api.sensitive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词持久化实体，对应 {@code sensitive_word} 表。
 * 作者：Devil
 */
@Schema(name = "违禁词", description = "违禁词持久化实体")
@Data
@TableName("sensitive_word")
public class SensitiveWord {

    @Schema(description = "违禁词ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 违禁词 */
    @Schema(description = "违禁词", example = "敏感词")
    private String word;

    /** 分类（政治/色情/暴力/广告/其他） */
    @Schema(description = "分类", example = "广告")
    private String category;

    /** 记录创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
