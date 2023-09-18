package com.tuum.banking.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    static final String EVENT_STORE = "eventStore";
    static final String READ_DATABASE = "readDb";

    @Value("${tuum.datasource.es.url}")
    String esDatasourceUrl;
    @Value("${tuum.datasource.db.url}")
    String readDbDatasourceUrl;
    @Value("${spring.datasource.username}")
    String datasourceUser;
    @Value("${spring.datasource.password}")
    String datasourcePassword;

    @Bean(name = EVENT_STORE)
    @ConfigurationProperties("tuum.datasource.es")
    public DataSource eventStore() {
        return getDataSource(esDatasourceUrl);
    }

    @Bean(name = READ_DATABASE)
    @ConfigurationProperties("tuum.datasource.db")
    public DataSource readDatabase() {
        return getDataSource(readDbDatasourceUrl);
    }

    private DataSource getDataSource(String jdcUrl) {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdcUrl);
        dataSource.setUsername(datasourceUser);
        dataSource.setPassword(datasourcePassword);

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager eventStoreTransactionManager() {
        var manager = new DataSourceTransactionManager();
        manager.setDataSource(eventStore());

        return manager;
    }

    @Bean
    public PlatformTransactionManager readDbTransactionManager() {
        var manager = new DataSourceTransactionManager();
        manager.setDataSource(readDatabase());

        return manager;
    }


    @Bean(name = "chainedTransactionManager")
    public ChainedTransactionManager transactionManager(
            @Qualifier("eventStoreTransactionManager") final PlatformTransactionManager eventStoreTransactionManager,
            @Qualifier("readDbTransactionManager") final PlatformTransactionManager readDbTransactionManager)
    {
        var tm = new ChainedTransactionManager(eventStoreTransactionManager, readDbTransactionManager);

        return tm;
    }
}
