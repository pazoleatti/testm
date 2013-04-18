package com.aplana.sbrf.taxaccounting.dao.cell.impl;

import com.aplana.sbrf.taxaccounting.dao.cell.CellStyleDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.*;
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
public class CellStyleDaoImpl extends AbstractDao implements CellStyleDao {
	@Override
	public void fillCellStyle(Long formDataId, final Map<Long, DataRow> rowIdMap, final List<FormStyle> styles) {
		String sqlQuery = "SELECT row_id, column_id, style_id FROM cell_style cs "
				+ "WHERE exists (SELECT 1 from data_row r WHERE r.id = cs.row_id and r.form_data_id = ?)";

		getJdbcTemplate().query(sqlQuery, new Object[] { formDataId },
				new int[] { Types.NUMERIC }, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				for (Map.Entry<Long, DataRow> rowId : rowIdMap.entrySet()) {
					if (rs.getLong("row_id") == rowId.getKey()) {
						for (String alias : rowId.getValue().keySet()) {
							Cell cell = rowId.getValue().getCell(alias);
							if (rs.getInt("column_id") == rowId.getValue().getCell(alias).getColumn().getId()) {
								cell.setStyleAlias(ModelUtils.findByProperties(styles, rs.getInt("style_id"),
										new ModelUtils.GetPropertiesFunc<FormStyle, Integer>() {
											@Override
											public Integer getProperties(FormStyle object) {
												return object.getId();
											}
										}).getAlias());
							}
						}
					}
				}
			}
		});
	}

	@Override
	@Transactional(readOnly=false)
	public void saveCellStyle(Map<Long, DataRow> rowIdMap) {
		final List<StyleRecord> records = new ArrayList<StyleRecord>();
		for (Map.Entry<Long, DataRow> rowId : rowIdMap.entrySet()) {
			for (String alias : rowId.getValue().keySet()) {
				if (rowId.getValue().getCell(alias).getStyle() != null) {
					records.add(new StyleRecord(rowId.getKey(), rowId.getValue().getCell(alias).getColumn().getId(),
							rowId.getValue().getCell(alias).getStyle().getId()));
				}
			}
		}

		if (!records.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_style (row_id, column_id, style_id) values (?, ?, ?)",
							new BatchPreparedStatementSetter() {
								public void setValues(PreparedStatement ps,
													  int index) throws SQLException {
									StyleRecord rec = records.get(index);
									ps.setLong(1, rec.getRowId());
									ps.setInt(2, rec.getColumnId());
									ps.setInt(3, rec.getId());
								}

								public int getBatchSize() {
									return records.size();
								}
							});
		}
	}

	/**
	 * Запись в таблице cell_style
	 */
	private static final class StyleRecord {
		private int columnId;
		private Long rowId;
		private Integer id;

		private StyleRecord(Long rowId, Integer columnId , Integer id) {
			this.columnId = columnId;
			this.rowId = rowId;
			this.id = id;
		}

		public int getColumnId() {
			return columnId;
		}

		public Long getRowId() {
			return rowId;
		}

		public Integer getId() {
			return id;
		}
	}

}
