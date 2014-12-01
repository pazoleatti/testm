package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.RefBookEmailConfigDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
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
public class RefBookEmailConfigDaoImpl  extends AbstractDao implements RefBookEmailConfigDao {

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords() {
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query("select NAME, VALUE, DESCRIPTION from configuration_email order by id", new RowMapper<Map<String, RefBookValue>>() {
            @Override
            public Map<String, RefBookValue> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, RefBookValue> map = new HashMap<String, RefBookValue>();
                map.put("NAME", new RefBookValue(RefBookAttributeType.STRING, rs.getString("NAME")));
                map.put("VALUE", new RefBookValue(RefBookAttributeType.STRING, rs.getString("VALUE")));
                map.put("DESCRIPTION", new RefBookValue(RefBookAttributeType.STRING, rs.getString("DESCRIPTION")));
                return map;
            }
        });
        return new PagingResult<Map<String, RefBookValue>>(records);
    }

    @Override
    public void updateRecords(final List<Map<String, RefBookValue>> records) {
        getJdbcTemplate().batchUpdate("update configuration_email set value = ? where name = ?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, RefBookValue> record = records.get(i);
                ps.setString(1, record.get("VALUE").getStringValue());
                ps.setString(2, record.get("NAME").getStringValue());
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }
}
