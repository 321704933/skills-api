package ai.skills.api.prose.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子持久化实体，对应 {@code prose_sentence} 表。
 * 作者：Devil
 */
@Data
@TableName("prose_sentence")
public class ProseSentence {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 句子正文 */
    private String content;

    /** 出处（书名/集名） */
    private String source;

    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
