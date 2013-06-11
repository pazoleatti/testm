package com.aplana.sbrf.taxaccounting.dao.cell.impl;

import com.aplana.sbrf.taxaccounting.dao.cell.CellSpanInfoDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
@Transactional(readOnly=true)
public class CellSpanInfoDaoImpl extends AbstractDao implements CellSpanInfoDao {
	@Override
	public void fillCellSpanInfo(Long formDataId, final Map<Long, DataRow<Cell>> rowIdMap) {
		String sqlQuery = "select column_id, row_id, colspan, rowspan from cell_span_info v where exists" +
				" (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)";

		getJdbcTemplate().query(sqlQuery, new Object[] { formDataId },
				new int[] { Types.NUMERIC }, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				DataRow<Cell> rowValue = rowIdMap.get(rs.getLong("row_id"));
				if (rowValue!=null){
					for (String alias : rowValue.keySet()) {
						Cell cell = rowValue.getCell(alias);
						if (rs.getInt("column_id") == rowValue.getCell(alias).getColumn().getId()) {
							cell.setColSpan(rs.getInt("colspan"));
							cell.setRowSpan(rs.getInt("rowspan"));
						}
					}
					
				}
			}
		});
	}

	@Override
	@Transactional(readOnly=false)
	public void saveCellSpanInfo(Map<Long, DataRow<Cell>> rowIdMap) {
		final List<SpanRecord> records = new ArrayList<SpanRecord>();
		for (Map.Entry<Long, DataRow<Cell>> rowId : rowIdMap.entrySet()) {
			for (String alias : rowId.getValue().keySet()) {
				if (rowId.getValue().getCell(alias).getColSpan() > 1 || rowId.getValue().getCell(alias).getRowSpan() > 1) {
					records.add(new SpanRecord(rowId.getKey(), rowId.getValue().getCell(alias).getColumn().getId(),
							rowId.getValue().getCell(alias).getColSpan(), rowId.getValue().getCell(alias).getRowSpan()));
				}
			}
		}

		if (!records.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_span_info (row_id, column_id, colspan, rowspan) values (?, ?, ?, ?)",
							new BatchPreparedStatementSetter() {
								public void setValues(PreparedStatement ps,
													  int index) throws SQLException {
									SpanRecord rec = records.get(index);
									ps.setLong(1, rec.getRowId());
									ps.setInt(2, rec.getColumnId());
									ps.setInt(3, rec.getColSpan());
									ps.setInt(4, rec.getRowSpan());
								}

								public int getBatchSize() {
									return records.size();
								}
							});
		}
	}

	/**
	 * Запись в таблице cell_span_info
	 */
	private static class SpanRecord {
		private int colSpan;
		private int rowSpan;
		private int columnId;
		private Long rowId;

		public SpanRecord(Long rowId, int columnId, int colSpan, int rowSpan) {
			super();
			this.colSpan = colSpan;
			this.rowSpan = rowSpan;
			this.columnId = columnId;
			this.rowId = rowId;
		}

		public int getColSpan() {
			return colSpan;
		}

		public int getRowSpan() {
			return rowSpan;
		}

		public int getColumnId() {
			return columnId;
		}

		public Long getRowId() {
			return rowId;
		}
	}
}
