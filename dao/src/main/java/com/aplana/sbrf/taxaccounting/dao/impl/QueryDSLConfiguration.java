package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import com.querydsl.sql.types.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
import java.sql.Connection;

@Configuration
public class QueryDSLConfiguration extends AbstractDao {
    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration() {
        SQLTemplates templates = OracleTemplates.builder().build();
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.register(new LocalDateTimeType());
        configuration.register(new InputStreamType());
        configuration.register(new NumericBooleanType());
        configuration.register(new EnumByOrdinalType<>(DepartmentType.class));
        configuration.register(new EnumByOrdinalType<>(State.class));
        configuration.register(new EnumByOrdinalType<>(NotificationType.class));
        configuration.register(new EnumByOrdinalType<>(LogLevel.class));

        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory() {
        Provider<Connection> provider = new SpringConnectionProvider(getJdbcTemplate().getDataSource());
        return new SQLQueryFactory(querydslConfiguration(), provider);
    }
}