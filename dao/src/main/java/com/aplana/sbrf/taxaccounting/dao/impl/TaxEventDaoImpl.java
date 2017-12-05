package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TaxEventDao;
import com.aplana.sbrf.taxaccounting.model.TaxChangesEvent;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TaxEventDaoImpl extends AbstractDao implements TaxEventDao {
    private static final class TaxEventMapper implements RowMapper<TaxChangesEvent> {

        @Override
        public TaxChangesEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            TaxChangesEvent result = new TaxChangesEvent();
            result.setId(rs.getLong("id"));
            result.setTableName(rs.getString("table_name"));
            result.setOperationName(rs.getString("operation_name"));
            result.setRefBookId(rs.getLong("ref_book_id"));
            result.setTableRowId(rs.getLong("table_row_id"));
            result.setLogDateTime(rs.getTimestamp("log_datetime"));
            return result;
        }
    }

    @Override
    public List<TaxChangesEvent> getNewTaxEvents() {
        return getJdbcTemplate().query("select tax.* from VW_LOG_TABLE_CHANGE tax \n" +
                "left join LOG_TABLE_CHANGE_PROCESSED ndfl on ndfl.id = tax.id \n" +
                "where ndfl.id is null", new TaxEventMapper());
    }

    @Override
    public void processTaxEvent(TaxChangesEvent event) {
        getJdbcTemplate().update("insert into LOG_TABLE_CHANGE_PROCESSED (id) values (?)", event.getId());
    }
}
