package com.tuum.banking.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    @Value("${tuum.datasource.es.url}")
    String esDatasourceUrl;
    @Value("${tuum.datasource.db.url}")
    String readDbDatasourceUrl;
    @Value("${spring.datasource.username}")
    String datasourceUser;
    @Value("${spring.datasource.password}")
    String datasourcePassword;

    private static final String EVENT_STORE_MIGRATION_DIR = "filesystem:./src/main/resources/migration/es";
    private static final String READ_DB_MIGRATION_DIR = "filesystem:./src/main/resources/migration/rdb";

    @Bean
    public Flyway esFlyway() {
        return getFlyway(esDatasourceUrl, EVENT_STORE_MIGRATION_DIR);
    }

    @Bean
    public Flyway rDbFlyway() {
        return getFlyway(readDbDatasourceUrl, READ_DB_MIGRATION_DIR);
    }

    private Flyway getFlyway(String jdbcUrl, String migrationLocation) {
        var flyway = Flyway
                .configure()
                .dataSource(jdbcUrl, datasourceUser, datasourcePassword)
                .locations(migrationLocation)
                .load();
        flyway.migrate();

        return flyway;
    }
}
