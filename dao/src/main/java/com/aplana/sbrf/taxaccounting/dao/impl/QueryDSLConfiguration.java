package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import com.querydsl.sql.types.EnumByOrdinalType;
import com.querydsl.sql.types.InputStreamType;
import com.querydsl.sql.types.LocalDateTimeType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
import java.sql.Connection;

@Configuration
public class QueryDSLConfiguration extends AbstractDao {
    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration() {
        SQLTemplates templates = HSQLDBTemplates.builder().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.register(new LocalDateTimeType());
        configuration.register(new InputStreamType());
        configuration.register(new EnumByOrdinalType<DepartmentType>(DepartmentType.class));
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory() {
        Provider<Connection> provider = new SpringConnectionProvider(getJdbcTemplate().getDataSource());
        return new SQLQueryFactory(querydslConfiguration(), provider);
    }
}