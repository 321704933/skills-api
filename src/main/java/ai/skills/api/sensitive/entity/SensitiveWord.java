package ai.skills.api.sensitive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词持久化实体，对应 {@code sensitive_word} 表。
 * 作者：Devil
 */
@Data
@TableName("sensitive_word")
public class SensitiveWord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 违禁词 */
    private String word;

    /** 分类（政治/色情/暴力/广告/其他） */
    private String category;

    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
