package ai.skills.api.stockindex.service;

import ai.skills.api.common.api.ResponseCode;
import ai.skills.api.common.exception.BizException;
import ai.skills.api.stockindex.collector.StockIndexCollector;
import ai.skills.api.stockindex.config.StockIndexProperties;
import ai.skills.api.stockindex.config.StockIndexProperties.IndexGroupConfig;
import ai.skills.api.stockindex.model.IndexGroup;
import ai.skills.api.stockindex.model.StockIndexResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2026/03/18
 * 功能：股票指数服务层，负责解析分组配置并调用采集器获取行情数据。
 * 作者：Devil
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockIndexService {

    private final StockIndexCollector collector;

    private final StockIndexProperties properties;

    /**
     * 功能：获取所有已启用分组的指数行情数据。
     *
     * @return 全部分组行情结果列表
     */
    public List<StockIndexResult> getAll() {
        List<StockIndexResult> results = new ArrayList<>();
        Map<String, IndexGroupConfig> groups = properties.getGroups();

        for (Map.Entry<String, IndexGroupConfig> entry : groups.entrySet()) {
            String groupCode = entry.getKey();
            IndexGroupConfig config = entry.getValue();

            if (!config.isEnabled() || config.getCodes() == null || config.getCodes().isEmpty()) {
                continue;
            }

            StockIndexResult result = collector.collect(groupCode, config.getName(), config.getCodes());
            results.add(result);
        }

        return results;
    }

    /**
     * 功能：获取指定分组的指数行情数据。
     *
     * @param group 分组枚举
     * @return 分组行情结果
     */
    public StockIndexResult getByGroup(IndexGroup group) {
        Map<String, IndexGroupConfig> groups = properties.getGroups();
        IndexGroupConfig config = groups.get(group.getCode());

        if (config == null || !config.isEnabled()) {
            throw new BizException(ResponseCode.BIZ_ERROR, "分组 [" + group.getName() + "] 未启用或不存在");
        }

        if (config.getCodes() == null || config.getCodes().isEmpty()) {
            throw new BizException(ResponseCode.BIZ_ERROR, "分组 [" + group.getName() + "] 未配置股票代码");
        }

        return collector.collect(group.getCode(), group.getName(), config.getCodes());
    }
}
