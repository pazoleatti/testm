package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.NdflReferenceDao;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional
public class NdflReferenceDaoImpl extends AbstractDao implements NdflReferenceDao {

    @Override
    public int updateField(final List<Long> uniqueRecordIds, String alias, final String value) {
        getJdbcTemplate().batchUpdate("update ndfl_references set " + alias + " = ? where ID = ?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long uniqueRecordId = uniqueRecordIds.get(i);
                ps.setString(1, value);
                ps.setLong(2, uniqueRecordId);
            }

            @Override
            public int getBatchSize() {
                return uniqueRecordIds.size();
            }
        });
        return uniqueRecordIds.size();
    }
}
