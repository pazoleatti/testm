package com.aplana.sbrf.taxaccounting.dao.impl.refbook.main;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

class DereferenceMapper implements RowCallbackHandler {
    private RefBookAttribute attribute;
    private Map<Long, RefBookValue> result;

    DereferenceMapper(RefBookAttribute attribute) {
        this.attribute = attribute;
        result = new HashMap<>(); // все атрибуты не нужны, только справочные и ссылочные
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {
        Long recordId = rs.getLong(RefBook.RECORD_ID_ALIAS);
        Object value = parseRefBookValue(rs, "value", attribute);
        result.put(recordId, new RefBookValue(attribute.getAttributeType(), value));
    }

    /**
     * Разыменовывает текущую строку resultSet в графе columnName используя информацию об атрибуте справочника attribute
     */
    private Object parseRefBookValue(ResultSet resultSet, String columnName, RefBookAttribute attribute) throws SQLException {
        if (resultSet.getObject(columnName) != null) {
            switch (attribute.getAttributeType()) {
                case STRING: {
                    return resultSet.getString(columnName);
                }
                case NUMBER: {
                    return resultSet.getBigDecimal(columnName).setScale(attribute.getPrecision(), BigDecimal.ROUND_HALF_UP);
                }
                case DATE: {
                    return resultSet.getDate(columnName);
                }
                case REFERENCE: {
                    return SqlUtils.getLong(resultSet, columnName);
                }
            }
        }
        return null;
    }

    public Map<Long, RefBookValue> getResult() {
        return result;
    }
}
