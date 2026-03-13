package ai.skills.api.sensitive.mapper;

import ai.skills.api.sensitive.entity.SensitiveWord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词数据访问层。
 * 作者：Devil
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
}
