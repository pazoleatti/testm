package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Перенёс mapper Ф. Марата(из RefBookUniverseDataProvider) чтобы использовать его в других RefBookDataProvider
 * User: ekuvshinov
 */
public class RefBookValueMapper implements RowMapper<Map<String, RefBookValue>> {

    private final RefBook refBook;

    public RefBookValueMapper(RefBook refBook) {
        this.refBook = refBook;
    }
    public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, rs.getLong(RefBook.RECORD_ID_ALIAS)));
        for (RefBookAttribute attribute: refBook.getAttributes()) {
            Object value = null;
            if (rs.getObject(attribute.getAlias()) != null) {
                switch (attribute.getAttributeType()) {
                    case STRING: {
                        value = rs.getString(attribute.getAlias());
                    }
                    break;
                    case NUMBER: {
                        value = rs.getBigDecimal(attribute.getAlias()).setScale(attribute.getPrecision());
                    }
                    break;
                    case DATE: {
                        value = rs.getDate(attribute.getAlias());
                    }
                    break;
                    case REFERENCE: {
                        value = rs.getLong(attribute.getAlias());
                    }
                    break;
                }
            }
            result.put(attribute.getAlias(), new RefBookValue(attribute.getAttributeType(), value));
        }
        return result;
    }
}
