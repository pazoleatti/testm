package com.aplana.sbrf.taxaccounting.model.util;

import lombok.experimental.Delegate;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Обертка над ResultSet, добавляющий prefix к наименованию столбцов, позволяющий переиспользование RowMapper`ов для данных связываемых таблиц запроса
 */
public class SubResultSet implements ResultSet {

    private String prefix;
    @Delegate(excludes=ResultSetExclude.class)
    private ResultSet resultSet;

    public SubResultSet(String prefix, ResultSet resultSet) {
        this.prefix = prefix;
        this.resultSet = resultSet;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return resultSet.getString(prefix + columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return resultSet.getBoolean(prefix + columnLabel);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return resultSet.getInt(prefix + columnLabel);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return resultSet.getLong(prefix + columnLabel);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return resultSet.getDouble(prefix + columnLabel);
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return resultSet.getDate(prefix + columnLabel);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return resultSet.getTimestamp(prefix + columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return resultSet.getBigDecimal(prefix + columnLabel);
    }

    private interface ResultSetExclude {
        String getString(String columnLabel);
        boolean getBoolean(String columnLabel);
        int getInt(String columnLabel);
        long getLong(String columnLabel);
        double getDouble(String columnLabel);
        Date getDate(String columnLabel);
        Timestamp getTimestamp(String columnLabel);
        BigDecimal getBigDecimal(String columnLabel);
    }
}
