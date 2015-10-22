package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.ColumnType;
import com.aplana.sbrf.taxaccounting.model.DataRowType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@Repository("formDataCacheDao")
public class FormDataCacheDaoImpl extends AbstractDao implements FormDataCacheDao {

	private static final Log LOG = LogFactory.getLog(FormDataCacheDaoImpl.class);

    private static final String REF_BOOK_VALUE_FOR_FORM_DATA =
			"SELECT v.record_id, v.number_value, v.string_value, v.date_value, v.reference_value, a.type, a.precision, a.alias, a.ref_book_id " +
			"FROM ref_book_value v join ref_book_attribute a ON a.id = v.attribute_id " +
            "WHERE record_id IN ";

    private class RefBookCacheMapper implements RowMapper<Pair<String, Pair<String, RefBookValue>>> {

        @Override
        public Pair<String, Pair<String, RefBookValue>> mapRow(ResultSet rs, int rowNum) throws SQLException {

            RefBookAttributeType type = null;
            Object value = null;

            switch (SqlUtils.getInteger(rs, "type")) {
                case 1:
                    type = RefBookAttributeType.STRING;
                    value = rs.getString("string_value");
                    break;
                case 2:
                    type = RefBookAttributeType.NUMBER;
                    BigDecimal val = rs.getBigDecimal("number_value");
                    if (val != null) {
                        value = val.setScale(SqlUtils.getInteger(rs,"precision"));
                    }
                    break;
                case 3:
                    type = RefBookAttributeType.DATE;
                    value = rs.getDate("date_value");
                    break;
                case 4:
                    type = RefBookAttributeType.REFERENCE;
                    value = SqlUtils.getLong(rs,"reference_value");
                    break;
            }

            RefBookValue rbValue = new RefBookValue(type, value);

            String key = SqlUtils.getLong(rs, "ref_book_id") + "_" + SqlUtils.getLong(rs, "record_id");
            return new Pair<String, Pair<String, RefBookValue>>(key, new Pair<String, RefBookValue>(rs.getString("alias"), rbValue));
        }
    }

	/**
	 * Формирует запрос для разыменовывания ссылок НФ
 	 * @param formData
	 * @return
	 */
	private String createSql(FormData formData) {
		StringBuilder sql = new StringBuilder("(");
		int count = 0;
		for (Column column : formData.getFormColumns()){
			if (column.getColumnType() == ColumnType.REFBOOK) {
				if (count++ > 0) {
					sql.append(" UNION ALL\n");
				}
				sql.append("SELECT DISTINCT c");
				sql.append(column.getId());
				sql.append(" FROM form_data_");
				sql.append(formData.getFormTemplateId());
				sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual");
			}
		}
		sql.append(')');
		if (count > 0) {
			return sql.toString();
		}
		return null;
	}

    @Override
    public Map<String, Map<String, RefBookValue>> getRefBookMap(FormData formData) {
		Map<String, Map<String, RefBookValue>> retVal = new HashMap<String, Map<String, RefBookValue>>();
		String sql = createSql(formData);
		if (sql == null) {
			return retVal; // нет ссылок - нет работы
		}
		LOG.debug(REF_BOOK_VALUE_FOR_FORM_DATA + sql);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

        List<Pair<String, Pair<String, RefBookValue>>> valuesList = getNamedParameterJdbcTemplate().query(
				REF_BOOK_VALUE_FOR_FORM_DATA + sql, params, new RefBookCacheMapper());

        for (Pair<String, Pair<String, RefBookValue>> pair : valuesList) {
            if (retVal.containsKey(pair.getFirst())) {
                retVal.get(pair.getFirst()).put(pair.getSecond().getFirst(), pair.getSecond().getSecond());
            } else {
                Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                map.put(pair.getSecond().getFirst(), pair.getSecond().getSecond());
                retVal.put(pair.getFirst(), map);
            }
        }
        return retVal;
    }
}
