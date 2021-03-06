package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public abstract class RefBookAbstractValueMapper implements RowMapper<Map<String, RefBookValue>> {
    /**
     * Справочник для которого был создан маппер
     */
    protected RefBook refBook;

    @Override
    public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
        Map<String, RefBookValue> result = createResult(rs);

        // Получаем список столбцов, вернувшихся из запроса
        List<String> columnNames = new ArrayList<String>();
        for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
            columnNames.add(rs.getMetaData().getColumnName(i).toLowerCase());
        }
        // Бизнес-идентификатор
        if (columnNames.contains(RefBook.BUSINESS_ID_ALIAS.toLowerCase())) {
            result.put(RefBook.BUSINESS_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, SqlUtils.getLong(rs, RefBook.BUSINESS_ID_ALIAS)));
        };
        // Начало периода действия записи
        if (columnNames.contains(RefBook.RECORD_VERSION_FROM_ALIAS.toLowerCase())) {
            Timestamp value = rs.getTimestamp(RefBook.RECORD_VERSION_FROM_ALIAS);
            if (value != null) {
                result.put(RefBook.RECORD_VERSION_FROM_ALIAS, new RefBookValue(RefBookAttributeType.DATE, new Date(value.getTime())));
            }
        };
        // Конец периода действия записи
        if (columnNames.contains(RefBook.RECORD_VERSION_TO_ALIAS.toLowerCase())) {
            Timestamp value = rs.getTimestamp(RefBook.RECORD_VERSION_TO_ALIAS);
            if (value != null) {
                result.put(RefBook.RECORD_VERSION_TO_ALIAS, new RefBookValue(RefBookAttributeType.DATE, new Date(value.getTime())));
            }
        };

        for (RefBookAttribute attribute: refBook.getAttributes()) {
            Object value = null;
            String alias = attribute.getAlias();
            if (rs.getObject(alias) != null) {
                switch (attribute.getAttributeType()) {
                    case STRING: {
                        value = rs.getString(alias);
                    }
                    break;
                    case NUMBER: {
                        value = rs.getBigDecimal(alias).setScale(attribute.getPrecision(), BigDecimal.ROUND_HALF_UP);
                    }
                    break;
                    case DATE: {
                        value = new Date(rs.getTimestamp(alias).getTime());
                    }
                    break;
                    case REFERENCE: {
                        value = SqlUtils.getLong(rs, alias);
                    }
                    break;
                    default:
                }
            }
            result.put(alias, new RefBookValue(attribute.getAttributeType(), value));
        }
        return result;
    }

    protected abstract Map<String, RefBookValue> createResult(ResultSet rs) throws SQLException;
}
