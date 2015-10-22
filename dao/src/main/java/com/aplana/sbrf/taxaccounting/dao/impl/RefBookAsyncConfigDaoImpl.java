package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookAsyncConfigDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RefBookAsyncConfigDaoImpl extends AbstractDao implements RefBookAsyncConfigDao {

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords() {
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query("select ID, NAME, LIMIT_KIND, TASK_LIMIT, SHORT_QUEUE_LIMIT from async_task_type where dev_mode = 0 order by NAME ", new RowMapper<Map<String, RefBookValue>>() {
            @Override
            public Map<String, RefBookValue> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                map.put(ConfigurationParamModel.ASYNC_TYPE_ID, new RefBookValue(RefBookAttributeType.STRING, rs.getString(ConfigurationParamModel.ASYNC_TYPE_ID)));
                map.put(ConfigurationParamModel.ASYNC_TYPE, new RefBookValue(RefBookAttributeType.STRING, rs.getString(ConfigurationParamModel.ASYNC_TYPE)));
                map.put(ConfigurationParamModel.ASYNC_LIMIT_KIND, new RefBookValue(RefBookAttributeType.STRING, rs.getString(ConfigurationParamModel.ASYNC_LIMIT_KIND)));
                Integer limit = SqlUtils.getInteger(rs, (ConfigurationParamModel.ASYNC_LIMIT));
                if (rs.wasNull()) {
                    map.put(ConfigurationParamModel.ASYNC_LIMIT, new RefBookValue(RefBookAttributeType.NUMBER, null));
                } else {
                    map.put(ConfigurationParamModel.ASYNC_LIMIT, new RefBookValue(RefBookAttributeType.NUMBER, limit));
                }
                Integer shortLimit = SqlUtils.getInteger(rs, (ConfigurationParamModel.ASYNC_SHORT_LIMIT));
                if (rs.wasNull()) {
                    map.put(ConfigurationParamModel.ASYNC_SHORT_LIMIT, new RefBookValue(RefBookAttributeType.NUMBER, null));
                } else {
                    map.put(ConfigurationParamModel.ASYNC_SHORT_LIMIT, new RefBookValue(RefBookAttributeType.NUMBER, shortLimit));
                }
                return map;
            }
        });
        return new PagingResult<Map<String, RefBookValue>>(records);
    }

    @Override
    public void updateRecords(final List<Map<String, RefBookValue>> records) {
        getJdbcTemplate().batchUpdate("update async_task_type set TASK_LIMIT = ?, SHORT_QUEUE_LIMIT = ? where ID = ?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, RefBookValue> record = records.get(i);
                ps.setString(1, record.get(ConfigurationParamModel.ASYNC_LIMIT).getStringValue());
                ps.setString(2, record.get(ConfigurationParamModel.ASYNC_SHORT_LIMIT).getStringValue());
                ps.setInt(3, Integer.parseInt(record.get(ConfigurationParamModel.ASYNC_TYPE_ID).getStringValue()));
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }
}
