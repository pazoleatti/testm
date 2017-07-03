package com.aplana.sbrf.taxaccounting.dao.impl;


import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import com.querydsl.sql.types.DateTimeType;
import com.querydsl.sql.types.LocalDateType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
import java.sql.Connection;

@Configuration
public class QueryDSLConfiguration extends AbstractDao{
    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration() {
        SQLTemplates templates = OracleTemplates.builder().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.register(new DateTimeType());
        configuration.register(new LocalDateType());
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory() {
        Provider<Connection> provider = new SpringConnectionProvider(getJdbcTemplate().getDataSource());
        return new SQLQueryFactory(querydslConfiguration(), provider);
    }
}
