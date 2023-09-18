package com.tuum.banking.config;

import com.tuum.banking.es.repository.AccountEventStoreMapper;
import com.tuum.banking.query.repository.AccountQueryMapper;
import com.tuum.banking.utils.UUIDTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.UUID;

@Configuration
public class MyBatisConfig {

    private static final String EVENT_STORE_SESSION_FACTORY = "eventStoreSessionFactory";
    private static final String READ_DB_SESSION_FACTORY = "readDbSessionFactory";

    @Bean(name = EVENT_STORE_SESSION_FACTORY)
    @Primary
    public SqlSessionFactoryBean esSqlSessionFactory(@Qualifier(DataSourceConfig.EVENT_STORE) final DataSource eventStore)
            throws Exception {
        return getSqlSessionFactoryBean(eventStore, AccountEventStoreMapper.class);
    }

    @Bean
    public MapperFactoryBean<AccountEventStoreMapper> esMapper(
            @Qualifier(EVENT_STORE_SESSION_FACTORY) final SqlSessionFactory esSqlSessionFactoryBean
    ) {
        MapperFactoryBean<AccountEventStoreMapper> factoryBean = new MapperFactoryBean<>(AccountEventStoreMapper.class);
        factoryBean.setSqlSessionFactory(esSqlSessionFactoryBean);

        return factoryBean;
    }

    @Bean(name = READ_DB_SESSION_FACTORY)
    public SqlSessionFactoryBean readDbSqlSessionFactory(@Qualifier(DataSourceConfig.READ_DATABASE) final DataSource readDb)
            throws Exception {
        return getSqlSessionFactoryBean(readDb, AccountQueryMapper.class);
    }

    @Bean
    public MapperFactoryBean<AccountQueryMapper> readDbMapper(
            @Qualifier(READ_DB_SESSION_FACTORY) final SqlSessionFactory readDbSqlSessionFactoryBean
    ) {
        MapperFactoryBean<AccountQueryMapper> factoryBean = new MapperFactoryBean<>(AccountQueryMapper.class);
        factoryBean.setSqlSessionFactory(readDbSqlSessionFactoryBean);

        return factoryBean;
    }

    private SqlSessionFactoryBean getSqlSessionFactoryBean(DataSource dataSource, Class mapper) throws Exception {
        final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        final SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();

        if (sqlSessionFactory != null) {
            var configuration = sqlSessionFactory.getConfiguration();

            if (configuration != null) {
                configuration.getTypeHandlerRegistry().register(UUID.class, new UUIDTypeHandler());
                configuration.addMapper(mapper);
                sqlSessionFactoryBean.setConfiguration(configuration);
            }
        }

        return sqlSessionFactoryBean;
    }
}
