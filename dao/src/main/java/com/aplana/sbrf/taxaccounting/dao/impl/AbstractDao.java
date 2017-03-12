package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Абстрактный класс, позволяющий делать автовайринг с DataSource'ом
 * и содержащий вспомогательные методы, востребованные наследниками
 */
public abstract class AbstractDao {

    /**
     * из oracle.jdbc.OracleTypes
     */
    public static final int CURSOR = -10;

    @Autowired
    DBInfo dbInfo;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected JdbcTemplate getJdbcTemplate() {
        return (JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations();
    }

    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    protected <T extends Number> T generateId(String sequenceName, Class<T> resultType) {
        return getJdbcTemplate().queryForObject("select " + sequenceName + ".nextval from dual", resultType);
    }

    protected boolean isSupportOver() {
        return dbInfo.isSupportOver();
    }

    protected boolean isWithRecursive() {
        return dbInfo.isWithRecursive();
    }

    protected boolean isDateDiffNumber() {
        return dbInfo.isDateDiffNumber();
    }
}
