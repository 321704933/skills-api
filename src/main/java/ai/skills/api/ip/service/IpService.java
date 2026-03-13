package ai.skills.api.ip.service;

import ai.skills.api.ip.model.IpQueryResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 创建时间：2026/03/13
 * 功能：IP 地理位置查询服务，基于 ip2region 离线数据库实现纯内存查询。
 * 作者：Devil
 */
@Slf4j
@Service
public class IpService {

    private Searcher searcher;

    @PostConstruct
    void init() throws Exception {
        var resource = new ClassPathResource("ip2region/ip2region_v4.xdb");
        Path tmpFile = Files.createTempFile("ip2region_v4", ".xdb");
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        }
        var cBuff = Searcher.loadContentFromFile(tmpFile.toString());
        this.searcher = Searcher.newWithBuffer(Version.IPv4, cBuff);
        Files.deleteIfExists(tmpFile);
        log.info("ip2region 数据库加载完成，全内存查询就绪");
    }

    /**
     * 功能：查询 IP 地址的地理位置信息。
     *
     * @param ip IPv4 地址
     * @return 地理位置查询结果
     */
    public IpQueryResult query(String ip) {
        try {
            String region = searcher.search(ip);
            // 格式：国家|区域|省份|城市|运营商，例如 "中国|0|上海|上海市|电信"
            String[] parts = region.split("\\|", -1);
            return new IpQueryResult(
                    ip,
                    clean(parts.length > 0 ? parts[0] : ""),
                    clean(parts.length > 2 ? parts[2] : ""),
                    clean(parts.length > 3 ? parts[3] : ""),
                    clean(parts.length > 4 ? parts[4] : "")
            );
        } catch (Exception e) {
            log.warn("IP 查询失败: ip={}, error={}", ip, e.getMessage());
            return new IpQueryResult(ip, "", "", "", "");
        }
    }

    private String clean(String value) {
        return "0".equals(value) ? "" : value;
    }
}
