package com.aplana.sbrf.taxaccounting.dao.impl.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Используется когда нужна прямая передача null в объект
 *
 * @author Alexander Ivanov
 */
public final class MapperUtils {

    private MapperUtils() {
    }

    public static Long getLong(ResultSet rs, Integer colNum) throws SQLException {
        long nValue = rs.getLong(colNum);
        return rs.wasNull() ? null : nValue;
    }

    public static BigDecimal getBD(ResultSet rs, Integer colNum) throws SQLException {
        BigDecimal nValue = rs.getBigDecimal(colNum);
        return rs.wasNull() ? null : (nValue.scale() < 2 ? nValue.setScale(2) : nValue);
    }

    public static String getString(ResultSet rs, Integer colNum) throws SQLException {
        String nValue = rs.getString(colNum);
        return rs.wasNull() ? null : nValue;
    }

    public static Date getDate(ResultSet rs, Integer colNum) throws SQLException {
        Date nValue = rs.getDate(colNum);
        return rs.wasNull() ? null : nValue;
    }
}
