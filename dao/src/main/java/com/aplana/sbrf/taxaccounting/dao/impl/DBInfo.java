package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.SQLException;

/**
 * User: avanteev
 * Класс необходимый исключительно для совместимости sql-функции между базами данных.
 * При составлении запроса используется встроенная функция row_number() over (order ...), гарантирующая нам порядок
 * строк в отсортированной таблице. Эта фукция не поддерживается в БД hsqldb.
 * Класс работает как синглтон, единожды при инициализаци определяет тип БД и выставляет признак supportOver.
 */
@Repository
public final class DBInfo {
    private static final String HSQLDB_NAME_ENGINE = "HSQL Database Engine";

    /**
     * Признак поддержки функции row_number() over (...)
     */
    private boolean supportOver;

    /**
     * Признак добавления служебного слова 'recursive' в конструкцию рекурсивных запросов with...
     * В hsqldb используется, в oracle - нет
     */
    private boolean withRecursive;

    /**
     * Признак целочисленной разницы дат (для Oracle)
     */
    private boolean dateDiffNumber;

    private DBInfo() {
    }

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    @SuppressWarnings("unused") // https://jira.codehaus.org/browse/SONARJAVA-117
    private void init() throws SQLException {
        String dbName = ((JdbcTemplate)namedParameterJdbcTemplate.getJdbcOperations()).getDataSource().getConnection().getMetaData().getDatabaseProductName();
        supportOver = !HSQLDB_NAME_ENGINE.equals(dbName);
        withRecursive = HSQLDB_NAME_ENGINE.equals(dbName);
        dateDiffNumber = !HSQLDB_NAME_ENGINE.equals(dbName);
    }

    public boolean isSupportOver(){
        return supportOver;
    }

    public boolean isWithRecursive() {
        return withRecursive;
    }

    public boolean isDateDiffNumber() {
        return dateDiffNumber;
    }
}
