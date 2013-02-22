package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CellEditableDao;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
@Transactional(readOnly=true)
public class CellEditableDaoImpl extends AbstractDao implements CellEditableDao {
	@Override
	public void fillCellEditable(Long formDataId, final Map<Long, DataRow> rowIdMap) {
		String sqlQuery = "SELECT row_id, column_id FROM cell_editable ce "
				+ "WHERE exists (SELECT 1 from data_row r WHERE r.id = ce.row_id and r.form_data_id = ?)";

		final List<CellEditable> records = new ArrayList<CellEditable>();

		getJdbcTemplate().query(sqlQuery, new Object[] { formDataId }, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				records.add(new CellEditable(rs.getLong("row_id"), rs.getInt("column_id")));
			}
		});

		for (CellEditable cellEditable : records) {
			for (Map.Entry<Long, DataRow> rowId : rowIdMap.entrySet()) {
				if (cellEditable.getRowId().equals(rowId.getKey())) {
					for (Map.Entry<String, Object> cellAlias : rowId.getValue().entrySet()) {
						if (rowId.getValue().getCell(cellAlias.getKey()).getColumn().getId()
								.equals(cellEditable.getColumnId())) {
							rowId.getValue().getCell(cellAlias.getKey()).setEditable(true);
						}
					}
				}
			}
		}
	}

	@Override
	@Transactional(readOnly=false)
	public void saveCellEditable(Map<Long, DataRow> rowIdMap) {
		final List<CellEditable> cellEditableList = new ArrayList<CellEditable>();
		for (Map.Entry<Long, DataRow> rowId : rowIdMap.entrySet()) {
				for (String alias : rowId.getValue().keySet()) {
					if (rowId.getValue().getCell(alias).isEditable()) {
						cellEditableList.add(new CellEditable(rowId.getKey(), rowId.getValue().getCell(alias).getColumn().getId()));
					}
				}
		}

		if (cellEditableList.isEmpty()) {
			return;
		}

		getJdbcTemplate()
				.batchUpdate(
						"insert into cell_editable (row_id, column_id) values (?, ?)",
						new BatchPreparedStatementSetter() {
							public void setValues(PreparedStatement ps, int index) throws SQLException {
								CellEditable rec = cellEditableList.get(index);
								ps.setLong(1, rec.getRowId());
								ps.setInt(2, rec.getColumnId());
							}

							public int getBatchSize() {
								return cellEditableList.size();
							}
						});
	}

	/**
	 * Запись в таблице cell_editable
	 */
	private class CellEditable {
		private Long rowId;
		private Integer columnId;

		public CellEditable(Long rowId, Integer columnId) {
			this.rowId = rowId;
			this.columnId = columnId;
		}

		public Long getRowId() {
			return rowId;
		}

		public Integer getColumnId() {
			return columnId;
		}
	}

}
