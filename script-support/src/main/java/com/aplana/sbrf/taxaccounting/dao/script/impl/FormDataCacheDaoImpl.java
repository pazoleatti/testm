package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@Repository("formDataCacheDao")
public class FormDataCacheDaoImpl extends AbstractDao implements FormDataCacheDao {

    private static final String REF_BOOK_VALUE_FOR_FORM_DATA = "select v.record_id, v.number_value, v.string_value, " +
            "v.date_value, v.reference_value, a.type, a.precision, a.alias from ref_book_value v, ref_book_attribute a " +
            "where record_id in (select distinct value from numeric_value where row_id in (select id from data_row " +
            "where form_data_id = ? and type = 0) and column_id in (select id from form_column where form_template_id = " +
            "(select form_template_id from form_data where id = ?) and type = 'R')) and a.id = v.attribute_id";

    private class RefBookCacheMapper implements RowMapper<Pair<Long, Pair<String, RefBookValue>>> {

        @Override
        public Pair<Long, Pair<String, RefBookValue>> mapRow(ResultSet rs, int rowNum) throws SQLException {

            RefBookAttributeType type = null;
            Object value = null;

            switch (rs.getInt("type")) {
                case 1:
                    type = RefBookAttributeType.STRING;
                    value = rs.getString("string_value");
                    break;
                case 2:
                    type = RefBookAttributeType.NUMBER;
                    BigDecimal val = rs.getBigDecimal("number_value");
                    if (val != null) {
                        value = val.setScale(rs.getInt("precision"));
                    }
                    break;
                case 3:
                    type = RefBookAttributeType.DATE;
                    value = rs.getDate("date_value");
                    break;
                case 4:
                    type = RefBookAttributeType.REFERENCE;
                    value = rs.getLong("reference_value");
                    break;
            }

            RefBookValue rbValue = new RefBookValue(type, value);

            return new Pair<Long, Pair<String, RefBookValue>>(rs.getLong("record_id"), new Pair<String, RefBookValue>(rs.getString("alias"), rbValue));
        }
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRefBookMap(Long formDataId) {
        List<Pair<Long, Pair<String, RefBookValue>>> valuesList = getJdbcTemplate().query(REF_BOOK_VALUE_FOR_FORM_DATA,
                new Long[]{formDataId, formDataId}, new int[]{Types.NUMERIC, Types.NUMERIC},
                new RefBookCacheMapper());

        Map<Long, Map<String, RefBookValue>> retVal = new HashMap<Long, Map<String, RefBookValue>>();

        for (Pair<Long, Pair<String, RefBookValue>> pair : valuesList) {
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
