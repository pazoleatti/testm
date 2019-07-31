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
        return getJdbcTemplateSecondary().query("SELECT tax.* FROM VW_LOG_TABLE_CHANGE tax \n" +
                "LEFT JOIN LOG_TABLE_CHANGE_PROCESSED ndfl ON ndfl.id = tax.id \n" +
                "WHERE ndfl.id IS NULL " +
                "ORDER BY tax.id asc " +
                "FOR UPDATE SKIP LOCKED", new TaxEventMapper());
    }

    @Override
    public void processTaxEvent(TaxChangesEvent event) {
        getJdbcTemplateSecondary().update("INSERT INTO LOG_TABLE_CHANGE_PROCESSED (id) VALUES (?)", event.getId());
    }
}
