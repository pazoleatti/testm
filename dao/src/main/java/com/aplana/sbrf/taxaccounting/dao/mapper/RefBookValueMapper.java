package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Перенёс mapper Ф. Марата(из RefBookUniverseDataProvider) чтобы использовать его в других RefBookDataProvider
 * User: ekuvshinov
 */
public class RefBookValueMapper extends RefBookAbstractValueMapper {

	/**
	 * Маппер создается привязанным к конкретному справочнику
	 * @param refBook
	 */
    public RefBookValueMapper(RefBook refBook) {
		this.refBook = refBook;
    }

    @Override
    public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
        return super.mapRow(rs, index);
    }

    @Override
    protected Map<String, RefBookValue> createResult(ResultSet rs) throws SQLException {
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, SqlUtils.getLong(rs, RefBook.RECORD_ID_ALIAS)));
        if (SqlUtils.isExistColumn(rs, "ROW_ORD")) {// для настроек подразделений
            result.put("ROW_ORD", new RefBookValue(RefBookAttributeType.NUMBER, SqlUtils.getLong(rs, "ROW_ORD")));
        }
        if (this.refBook.isVersioned() && this.refBook.getId() != RefBook.Id.DEPARTMENT.getId()) {
            result.put(RefBook.BUSINESS_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, SqlUtils.getLong(rs, RefBook.BUSINESS_ID_ALIAS)));
        }
        return result;
    }
}