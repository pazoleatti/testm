package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookVzlHistoryDao;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
public class RefBookVzlHistoryDaoImpl extends AbstractDao implements RefBookVzlHistoryDao {

    @Autowired
    private RefBookDao refBookDao;

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        StringBuilder sql = new StringBuilder("SELECT id ");
        sql.append(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM "+TABLE_NAME+" WHERE id = :id");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", recordId);
        List<Map<String, RefBookValue>> records = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RefBookValueMapper(refBook));
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        if (records.size() == 1) {
            result = records.get(0);
        }
        return result;
    }

    @Override
    public void createRecords(@NotNull final List<Map<String, RefBookValue>> records) {
        if (records != null && !records.isEmpty()) {
            //Формируем запрос
            final RefBook refBook = refBookDao.get(REF_BOOK_ID);
            StringBuilder attributesPart = new StringBuilder();
            StringBuilder paramsPart = new StringBuilder();
            //Генерим новый id
            attributesPart.append("ID");
            paramsPart.append("seq_ref_book_vzl_history.nextval");
            if (refBook.getAttributes().size() > 0) {
                paramsPart.append(",");
                attributesPart.append(",");
            }
            for (Iterator<RefBookAttribute> it = refBook.getAttributes().iterator(); it.hasNext();) {
                RefBookAttribute attribute = it.next();
                attributesPart.append(attribute.getAlias());
                paramsPart.append("?");
                if (it.hasNext()) {
                    attributesPart.append(",");
                    paramsPart.append(",");
                }
            }
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    TABLE_NAME, attributesPart.toString(), paramsPart.toString());
            getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int paramIndex = 1;
                    Map<String, RefBookValue> record = records.get(i);
                    for (RefBookAttribute attribute : refBook.getAttributes()) {
                        RefBookValue value = record.get(attribute.getAlias());
                        switch (attribute.getAttributeType()) {
                            case STRING: {
                                ps.setString(paramIndex, value.getStringValue());
                            }
                            break;
                            case NUMBER: {
                                if (value.getNumberValue() != null) {
                                    BigDecimal v = new BigDecimal(value.getNumberValue().toString());
                                    ps.setBigDecimal(paramIndex, v.setScale(attribute.getPrecision(), RoundingMode.HALF_UP));
                                }
                            }
                            break;
                            case DATE: {
                                ps.setDate(paramIndex, new java.sql.Date(value.getDateValue().getTime()));
                            }
                            break;
                            case REFERENCE: {
                                ps.setLong(paramIndex, value.getReferenceValue());
                            }
                            break;
                        }
                        paramIndex++;
                    }
                }

                @Override
                public int getBatchSize() {
                    return records.size();
                }
            });
        }
    }

    @Override
    public void updateRecord(Long uniqueRecordId, Map<String, RefBookValue> record) {
        if (record != null && !record.isEmpty()) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            StringBuilder attributesPart = new StringBuilder();

            final RefBook refBook = refBookDao.get(REF_BOOK_ID);
            int i = 1;
            for (Map.Entry<String, RefBookValue> value : record.entrySet()) {
                String alias = value.getKey();
                attributesPart.append(alias).append("= :").append(alias);
                if (i < record.size()) {
                    attributesPart.append(", ");
                }

                switch (value.getValue().getAttributeType()) {
                    case STRING: {
                        params.addValue(alias, value.getValue().getStringValue());
                    }
                    break;
                    case NUMBER: {
                        if (value.getValue().getNumberValue() != null) {
                            BigDecimal v = new BigDecimal(value.getValue().getNumberValue().toString());
                            params.addValue(alias, v.setScale(refBook.getAttribute(alias).getPrecision(), RoundingMode.HALF_UP));
                        }
                    }
                    break;
                    case DATE: {
                        params.addValue(alias, value.getValue().getDateValue().getTime());
                    }
                    break;
                    case REFERENCE: {
                        params.addValue(alias, value.getValue().getReferenceValue());
                    }
                    break;
                }
                i++;
            }
            params.addValue("id", uniqueRecordId);
            String sql = String.format("UPDATE %s SET %s where id = :id", TABLE_NAME, attributesPart.toString());
            getNamedParameterJdbcTemplate().update(sql, params);
        }
    }
}
