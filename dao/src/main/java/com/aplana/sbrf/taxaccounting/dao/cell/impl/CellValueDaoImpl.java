package com.aplana.sbrf.taxaccounting.dao.cell.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.cell.CellValueDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;


@Repository
@Transactional(readOnly=true)
public class CellValueDaoImpl extends AbstractDao implements CellValueDao {
	@Override
	public void fillCellValue(Long formDataId, final Map<Long, DataRow<Cell>> rowIdMap) {
		for (int i = 0; i < CELL_VALUE_TABLES.length; i++) {
			String sqlQuery ="select * from " + CELL_VALUE_TABLES[i] +
					" v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)";

			getJdbcTemplate().query(sqlQuery, new Object[] { formDataId }, new int[] { Types.NUMERIC }, new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					Object value = rs.getObject("value");

					if (value != null) {
						for (Map.Entry<Long, DataRow<Cell>> rowId : rowIdMap.entrySet()) {
							if (rowId.getKey() == rs.getLong("row_id")) {
								for (String alias : rowId.getValue().keySet()) {
									Column col = rowId.getValue().getCell(alias).getColumn();
									if (col.getId() == rs.getInt("column_id")) {
										// TODO: думаю, стоит зарефакторить
										if (value instanceof java.sql.Date) {
											value = new java.util.Date(
													((java.sql.Date) value)
															.getTime());
										}
										rowId.getValue().put(alias, value);
									}
								}
							}
						}
					}
				}
			});
		}
	}

	@Override
	@Transactional(readOnly=false)
	public void saveCellValue(Map<Long, DataRow<Cell>> rowIdMap) {
		final List<ValueRecord<BigDecimal>> numericValues = new ArrayList<ValueRecord<BigDecimal>>();
		final List<ValueRecord<String>> stringValues = new ArrayList<ValueRecord<String>>();
		final List<ValueRecord<Date>> dateValues = new ArrayList<ValueRecord<Date>>();

		for (Map.Entry<Long, DataRow<Cell>> rowId : rowIdMap.entrySet()) {
			for (String alias : rowId.getValue().keySet()) {
				Column col = rowId.getValue().getCell(alias).getColumn();
				Object val = rowId.getValue().get(alias);
				if (val == null) {
					continue;
				} else if (val instanceof BigDecimal) {
					numericValues.add(new ValueRecord<BigDecimal>(
							(BigDecimal) val, rowId.getKey(), col.getId()));
				} else if (val instanceof String) {
					stringValues.add(new ValueRecord<String>((String) val,
							rowId.getKey(), col.getId()));
				} else if (val instanceof Date) {
					dateValues.add(new ValueRecord<Date>((Date) val,
							rowId.getKey(), col.getId()));
				} else {
					throw new IllegalArgumentException("Несовместимые типы колонки и значения");
				}
			}
		}

		insertValues(CELL_VALUE_TABLES[0], numericValues);
		insertValues(CELL_VALUE_TABLES[1], stringValues);
		insertValues(CELL_VALUE_TABLES[2], dateValues);
	}

	private <T> void insertValues(String tableName,
								  final List<ValueRecord<T>> values) {
		if (values.isEmpty()) {
			return;
		}
		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				ValueRecord<T> rec = values.get(index);
				// В строках order начинается с 1 (см. OrderUtils.reorder), а в
				// List индексы начинаются с нуля
				ps.setLong(1, rec.rowId);
				ps.setInt(2, rec.columnId);
				if (rec.value instanceof Date) {
					java.sql.Date sqlDate = new java.sql.Date(
							((Date) rec.value).getTime());
					ps.setDate(3, sqlDate);
				} else if (rec.value instanceof BigDecimal) {
					// TODO: Добавить округление данных в соответствии с
					// точностью, указанной в объекте Column
					ps.setBigDecimal(3, (BigDecimal) rec.value);
				} else if (rec.value instanceof String) {
					ps.setString(3, (String) rec.value);
				} else {
					assert false;
				}
			}

			public int getBatchSize() {
				return values.size();
			}
		};

		getJdbcTemplate().batchUpdate(
				"insert into " + tableName
						+ " (row_id, column_id, value) values (?, ?, ?)", bpss);
	}

	/**
	 * Список таблиц для значений ячеек
	 */
	private static final String[] CELL_VALUE_TABLES = {"numeric_value", "string_value", "date_value"};

	/**
	 * Запись в таблице cell_editable
	 */
	private static class ValueRecord<T> {
		private T value;
		private Long rowId;
		private int columnId;

		public ValueRecord(T value, Long rowId, int columnId) {
			this.value = value;
			this.rowId = rowId;
			this.columnId = columnId;
		}
	}
}
