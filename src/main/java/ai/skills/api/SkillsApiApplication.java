package ai.skills.api;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 创建时间：2026/03/12
 * 功能：Spring Boot 应用启动入口，负责加载整个 skills-api 项目上下文。
 * 作者：Devil
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan(basePackages = "ai.skills.api", annotationClass = Mapper.class)
public class SkillsApiApplication {

    /**
     * 功能：启动应用进程。
     *
     * @param args 启动参数列表
     */
    public static void main(String[] args) {
        SpringApplication.run(SkillsApiApplication.class, args);
    }

}
