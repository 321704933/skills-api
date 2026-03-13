package ai.skills.api.sensitive.service;

import ai.skills.api.sensitive.entity.SensitiveWord;
import ai.skills.api.sensitive.mapper.SensitiveWordMapper;
import ai.skills.api.sensitive.model.SensitiveCheckResult;
import cn.hutool.dfa.WordTree;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 创建时间：2026/03/13
 * 功能：违禁词服务层，启动时加载词库构建 DFA 树，提供高效文本检测。
 * 作者：Devil
 */
@Slf4j
@Service
public class SensitiveWordService extends ServiceImpl<SensitiveWordMapper, SensitiveWord> {

    private final WordTree wordTree = new WordTree();

    /**
     * 功能：应用启动时从数据库加载全部违禁词，构建 DFA 词树。
     */
    @PostConstruct
    public void init() {
        List<SensitiveWord> words = list();
        words.forEach(w -> wordTree.addWord(w.getWord()));
        log.info("违禁词库加载完成，共 {} 个词", words.size());
    }

    /**
     * 功能：检测文本中是否包含违禁词。
     *
     * @param text 待检测文本
     * @return 检测结果（是否命中、命中词列表、过滤后文本）
     */
    public SensitiveCheckResult check(String text) {
        List<String> foundWords = wordTree.matchAll(text, -1, false, true);
        List<String> distinct = foundWords.stream().distinct().toList();

        String filteredText = text;
        for (String word : distinct) {
            filteredText = filteredText.replace(word, "*".repeat(word.length()));
        }

        return new SensitiveCheckResult(!distinct.isEmpty(), distinct, filteredText);
    }
}
