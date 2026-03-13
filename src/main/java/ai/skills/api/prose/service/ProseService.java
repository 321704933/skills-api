package ai.skills.api.prose.service;

import ai.skills.api.prose.entity.ProseSentence;
import ai.skills.api.prose.mapper.ProseSentenceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子服务层，提供随机句子查询。
 * 作者：Devil
 */
@Service
public class ProseService extends ServiceImpl<ProseSentenceMapper, ProseSentence> {

    /**
     * 功能：随机获取一条散文句子。
     *
     * @return 随机句子（表为空返回 null）
     */
    public ProseSentence getRandomSentence() {
        return baseMapper.selectRandom();
    }
}
