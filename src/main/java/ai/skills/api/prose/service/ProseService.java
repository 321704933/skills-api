package ai.skills.api.prose.service;

import ai.skills.api.prose.model.ProseSentenceResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 创建时间：2026/03/13
 * 功能：散文句子服务层，启动时从 JSON 文件加载数据，提供随机句子查询。
 * 作者：Devil
 */
@Slf4j
@Service
public class ProseService {

    private static final String DATA_FILE = "data/prose-sentences.json";

    private List<ProseSentenceResult> sentences;

    private final ObjectMapper objectMapper;

    public ProseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 功能：应用启动时从 JSON 文件加载全部散文句子。
     */
    @PostConstruct
    public void init() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_FILE).getInputStream()) {
            sentences = objectMapper.readValue(is, new TypeReference<>() {});
        }
        log.info("散文句子加载完成，共 {} 条", sentences.size());
    }

    /**
     * 功能：随机获取一条散文句子。
     *
     * @return 随机句子（数据为空返回 null）
     */
    public ProseSentenceResult getRandomSentence() {
        if (sentences == null || sentences.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(sentences.size());
        return sentences.get(index);
    }
}
