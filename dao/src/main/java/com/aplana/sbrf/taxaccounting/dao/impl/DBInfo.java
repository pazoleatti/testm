package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.SQLException;

/**
 * User: avanteev
 */
@Repository
public class DBInfo {
    private static final String HSQLDB_NAME_ENGINE = "HSQL Database Engine";

    private static boolean supportOver;

    private DBInfo() {
    }

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    private void init() throws SQLException {
        String dbName = ((JdbcTemplate)namedParameterJdbcTemplate.getJdbcOperations()).getDataSource().getConnection().getMetaData().getDatabaseProductName();
        supportOver = !HSQLDB_NAME_ENGINE.equals(dbName);
    }

    public static boolean isSupportOver(){
        return supportOver;
    }
}
