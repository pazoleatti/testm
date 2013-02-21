package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CellEditableDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;


@Repository
@Transactional(readOnly=true)
public class CellEditableDaoImpl extends AbstractDao implements CellEditableDao{

	public List<CellEditable> getFormCellEditable(Long formDataId) {

		String sqlQuery = "SELECT row_id, column_id FROM cell_editable ce "
				+ "WHERE exists (SELECT 1 from data_row r WHERE r.id = ce.row_id and r.form_data_id = ?)";

		final List<CellEditable> records = new ArrayList<CellEditable>();

		getJdbcTemplate().query(sqlQuery, new Object[] { formDataId },
				new int[] { Types.NUMERIC }, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				records.add(new CellEditable(rs.getLong("row_id"), rs.getInt("column_id")));
			}
		});

		return records;
	}

	@Transactional(readOnly=false)
	@Override
	public void saveFormEditableCells(final List<CellEditable> cellEditableList, final List<Integer> orders, final List<Long> rowIds) {
		if (!cellEditableList.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_editable (row_id, column_id) values (?, ?)",
							new BatchPreparedStatementSetter() {
								public void setValues(PreparedStatement ps,
													  int index) throws SQLException {

									CellEditable rec = cellEditableList.get(index);
									rec.setRowId(rowIds.get(orders.get(index) - 1));

									ps.setLong(1, rec.getRowId());
									ps.setInt(2, rec.getColumnId());
								}

								public int getBatchSize() {
									return cellEditableList.size();
								}
							});
		}
	}
}
