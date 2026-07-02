package com.example.refactor.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * memory 저장소 모드에서는 DB/JPA 자동 설정을 끄고 메모리 Repository만 사용한다.
 */
@Configuration
@ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
public class MemoryRepositoryAutoConfiguration {
}
