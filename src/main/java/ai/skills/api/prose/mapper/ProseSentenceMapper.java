package ai.skills.api.prose.mapper;

import ai.skills.api.prose.entity.ProseSentence;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子数据访问层，提供随机查询能力。
 * 作者：Devil
 */
@Mapper
public interface ProseSentenceMapper extends BaseMapper<ProseSentence> {

    /**
     * 功能：随机返回一条散文句子。
     *
     * @return 随机句子（表为空返回 null）
     */
    @Select("SELECT * FROM prose_sentence ORDER BY RAND() LIMIT 1")
    ProseSentence selectRandom();
}
