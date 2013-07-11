package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

/**
 * Реализация ДАО для работы со строками НФ
 * 
 * @author sgoryachkin
 * 
 */
@Repository
public class DataRowDaoImpl extends AbstractDao implements DataRowDao {

	@Override
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return phisicalGetRows(fd,
				new TypeFlag[] { TypeFlag.DEL, TypeFlag.SAME }, filter, range);
	}

	@Override
	public int getSavedSize(FormData fd, DataRowFilter filter) {
		return phisicalGetSize(fd,
				new TypeFlag[] { TypeFlag.DEL, TypeFlag.SAME }, filter);
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return phisicalGetRows(fd,
				new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, filter, range);
	}

	@Override
	public int getSize(FormData fd, DataRowFilter filter) {
		return phisicalGetSize(fd,
				new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, filter);
	}

	@Override
	public void updateRows(FormData fd, Collection<DataRow<Cell>> rows) {
		System.out.println("Update");
		// Если строка помечена как ADD, необходимо обновление
		// Если строка помечена как SAME, то помечаем её как DEL создаем новую с
		// тем же значением ORD
		List<DataRow<Cell>> forUpdate = new ArrayList<DataRow<Cell>>();
		List<DataRow<Cell>> forCreate = new ArrayList<DataRow<Cell>>();
		List<Long> forCreateOrder = new ArrayList<Long>();
		for (DataRow<Cell> dataRow : rows) {
			Long id = dataRow.getId();
			Pair<Integer, Long> typeAndOrd = getTypeAndOrdById(fd,
					new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, id);
			if (TypeFlag.ADD.getKey() == typeAndOrd.getFirst()) {
				forUpdate.add(dataRow);
			} else {
				forCreate.add(dataRow);
				forCreateOrder.add(typeAndOrd.getSecond());
			}
		}

		batchRemoveCells(forUpdate);
		batchInsertCells(forUpdate);

		phisicalUpdateRowsType(fd, forCreate, TypeFlag.DEL);
		phisicalInsertRows(fd, forCreate, null, null, forCreateOrder);

	}

	@Override
	public void removeRows(FormData fd, final List<DataRow<Cell>> rows) {
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE

		getJdbcTemplate().batchUpdate(
				"delete from DATA_ROW where ID=? and TYPE=?",
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						Long rowId = rows.get(i).getId();
						if (rowId == null) {
							throw new IllegalArgumentException();
						}
						ps.setLong(1, rows.get(i).getId());
						ps.setInt(2, TypeFlag.ADD.getKey());
					}

					@Override
					public int getBatchSize() {
						return rows.size();
					}

				});

		getJdbcTemplate().batchUpdate(
				"update DATA_ROW set TYPE=? where ID=? and TYPE=?",
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, TypeFlag.DEL.getKey());
						ps.setLong(2, rows.get(i).getId());
						ps.setInt(3, TypeFlag.SAME.getKey());
					}

					@Override
					public int getBatchSize() {
						return rows.size();
					}
				});

	}

	@Override
	public void removeRows(final FormData fd, final int idxFrom, final int idxTo) {
		if ((idxFrom < 1) || (idxTo < idxFrom)) {
			throw new IllegalArgumentException();
		}
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		String idsSQL = "select ID from (select rownum as IDX, ID, TYPE from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId order by ORD) RR where IDX between :from and :to";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("types", TypeFlag.rtsToKeys(new TypeFlag[] { TypeFlag.ADD,
				TypeFlag.SAME }));
		params.put("formDataId", fd.getId());
		params.put("from", idxFrom);
		params.put("to", idxTo);
		params.put("remType", TypeFlag.ADD.getKey());
		params.put("updType", TypeFlag.SAME.getKey());
		params.put("setType", TypeFlag.DEL.getKey());

		getNamedParameterJdbcTemplate().update(
				"delete from DATA_ROW where ID in (" + idsSQL
						+ ") and TYPE=:remType", params);
		getNamedParameterJdbcTemplate().update(
				"update DATA_ROW set TYPE=:setType where ID in (" + idsSQL
						+ ") and TYPE=:updType", params);

	}

	@Override
	public void removeRows(FormData fd) {
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE

		getJdbcTemplate().update(
				"delete from DATA_ROW where FORM_DATA_ID=? and TYPE=?",
				new Object[] { fd.getId(), TypeFlag.ADD.getKey() });
		getJdbcTemplate().update(
				"update DATA_ROW set TYPE=? where FORM_DATA_ID=? and TYPE=?",
				new Object[] { TypeFlag.DEL.getKey(), fd.getId(),
						TypeFlag.SAME.getKey() });
	}

	@Override
	public void saveRows(final FormData fd, final List<DataRow<Cell>> dataRows) {
		// Полностью чистим временный срез строк.
		removeRows(fd);
		// Вставляем строки
		phisicalInsertRows(fd, dataRows,
				DataRowDaoImplUtils.DEFAULT_ORDER_STEP,
				DataRowDaoImplUtils.DEFAULT_ORDER_STEP, null);
	}

	@Override
	public void insertRows(FormData fd, int index, List<DataRow<Cell>> rows) {
		index--;
		Long ordBegin = getOrd(fd,
				new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, index);
		if (ordBegin == null) {
			ordBegin = 0l;
		}
		insertRows(fd, index, ordBegin, rows);
	}

	@Override
	public void insertRows(FormData fd, DataRow<Cell> afterRow,
			List<DataRow<Cell>> rows) {
		Pair<Long, Integer> ordAndIndex = getOrdAndIndex(fd, new TypeFlag[] {
				TypeFlag.ADD, TypeFlag.SAME }, afterRow.getId());
		insertRows(fd, ordAndIndex.getSecond(), ordAndIndex.getFirst(), rows);
	}

	@Override
	public void commit(FormData fd) {
		phisicalRemoveRows(fd, TypeFlag.DEL);
		phisicalUpdateRowsType(fd, TypeFlag.ADD, TypeFlag.SAME);
	}

	@Override
	public void rollback(FormData fd) {
		phisicalRemoveRows(fd, TypeFlag.ADD);
		phisicalUpdateRowsType(fd, TypeFlag.DEL, TypeFlag.SAME);
	}

	private void insertRows(FormData fd, int index, long ordBegin,
			List<DataRow<Cell>> rows) {

		Long ordEnd = getOrd(fd,
				new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, index + 1);
		long ordStep = ordEnd == null ? DataRowDaoImplUtils.DEFAULT_ORDER_STEP
				: DataRowDaoImplUtils
						.calcOrdStep(ordBegin, ordEnd, rows.size());

		if (ordStep == 0) {
			// TODO: Реализовать перепаковку п
			// Слишком маленькие значения ORD. В промежуток нельзя вставить
			// такое количество строк
			throw new IllegalAccessError("Необходима перепаковка поля ORD");
		}

		phisicalInsertRows(fd, rows, ordBegin, ordStep, null);
	}

	private void phisicalRemoveRows(FormData fd, TypeFlag type) {
		getJdbcTemplate().update(
				"delete from DATA_ROW where FORM_DATA_ID = ? and TYPE = ?",
				fd.getId(), type.getKey());
	}

	private void phisicalUpdateRowsType(FormData fd, TypeFlag fromType,
			TypeFlag toType) {
		getJdbcTemplate()
				.update("update DATA_ROW set TYPE = ? where FORM_DATA_ID = ? and TYPE = ?",
						toType.getKey(), fd.getId(), fromType.getKey());
	}

	private void phisicalUpdateRowsType(FormData fd,
			final List<DataRow<Cell>> dataRows, final TypeFlag toType) {

		if (!dataRows.isEmpty()) {

			getJdbcTemplate().batchUpdate(
					"update DATA_ROW set TYPE = ? where ID = ?",
					new BatchPreparedStatementSetter() {

						@Override
						public void setValues(PreparedStatement ps, int i)
								throws SQLException {
							ps.setInt(1, toType.getKey());
							ps.setLong(2, dataRows.get(i).getId());
						}

						@Override
						public int getBatchSize() {
							return dataRows.size();
						}
					});
		}
	}

	private List<DataRow<Cell>> phisicalGetRows(FormData fd, TypeFlag[] types,
			DataRowFilter filter, DataRowRange range) {
		DataRowMapper dataRowMapper = new DataRowMapper(fd, types, filter,
				range);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql();
		List<DataRow<Cell>> dataRows = getNamedParameterJdbcTemplate().query(
				sql.getFirst(), sql.getSecond(), dataRowMapper);
		// SBRFACCTAX-2082
		//FormDataUtils.setValueOners(dataRows);
		return dataRows;
	}

	private int phisicalGetSize(FormData fd, TypeFlag[] types,
			DataRowFilter filter) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		return getNamedParameterJdbcTemplate()
				.queryForInt(
						"select count(ID) from DATA_ROW where FORM_DATA_ID = :formDataId and TYPE in (:types)",
						params);
	}

	private void phisicalInsertRows(final FormData fd,
			final List<DataRow<Cell>> dataRows, final Long ordBegin,
			final Long ordStep, final List<Long> orders) {

		if (dataRows.isEmpty()) {
			return;
		}

		// SBRFACCTAX-2201, SBRFACCTAX-2082
		FormDataUtils.cleanValueOners(dataRows);

		// TODO: Необходимо оптимизировать insert. Oracle не поддерживает
		// получение ключей при использовании батч операции
		// http://stackoverflow.com/questions/9065894/jdbc-preparedstatement-batch-update-and-generated-keys
		// Функция SqlUtils.batchUpdate работает как последовательный вызов
		// executeUpdate, а не batchUpdate.
		// Хотя с запросе на stackoverflow написано, что падение
		// производительности не значительное.

		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				DataRow<Cell> dr = dataRows.get(index);
				String rowAlias = dr.getAlias();
				ps.setLong(1, fd.getId());
				ps.setString(2, rowAlias);
				Long order = null;
				if (orders != null) {
					order = orders.get(index);
				} else {
					order = (index + 1) * ordStep + ordBegin;
				}
				ps.setLong(3, order);
				ps.setInt(4, TypeFlag.ADD.getKey());
			}

			@Override
			public int getBatchSize() {
				return dataRows.size();
			}
		};

		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		SqlUtils.batchUpdate(getJdbcTemplate(), new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {

				return con
						.prepareStatement(
								"insert into DATA_ROW (ID, FORM_DATA_ID, ALIAS, ORD, TYPE) values (SEQ_DATA_ROW.NEXTVAL, ?, ?, ?, ?)",
								new String[] { "ID" });
			}
		}, bpss, generatedKeyHolder);

		List<Map<String, Object>> keysList = generatedKeyHolder.getKeyList();
		if (keysList.size() != dataRows.size()) {
			throw new IllegalStateException();
		}
		final Iterator<DataRow<Cell>> iterator = dataRows.iterator();
		for (Map<String, Object> map : keysList) {
			iterator.next().setId(((BigDecimal) map.get("ID")).longValue());
		}

		batchInsertCells(dataRows);

	}

	/**
	 * Получаем значение ORD для строки по индексу
	 * 
	 * @param fd
	 * @param types
	 * @param dataRowIndex
	 * @return
	 */
	private Long getOrd(FormData fd, TypeFlag[] types, int dataRowIndex) {
		String sql = "select ORD from (select rownum as IDX, ORD from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId order by ORD) RR where IDX = :dataRowIndex";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		params.put("dataRowIndex", dataRowIndex);
		List<Long> list = getNamedParameterJdbcTemplate().queryForList(sql,
				params, Long.class);
		return list.isEmpty() ? null : DataAccessUtils
				.requiredSingleResult(list);
	}

	private Pair<Long, Integer> getOrdAndIndex(FormData fd, TypeFlag[] types,
			long dataRowId) {
		String sql = "select ORD, IDX from (select rownum as IDX, ORD, ID from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId order by ORD) RR where ID = :dataRowId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		params.put("dataRowId", dataRowId);
		return DataAccessUtils
				.requiredSingleResult(getNamedParameterJdbcTemplate().query(
						sql, params, new RowMapper<Pair<Long, Integer>>() {
							@Override
							public Pair<Long, Integer> mapRow(ResultSet rs,
									int rowNum) throws SQLException {
								return new Pair<Long, Integer>(rs
										.getLong("ORD"), rs.getInt("IDX"));
							}
						}));
	}

	private Pair<Integer, Long> getTypeAndOrdById(FormData fd,
			TypeFlag[] types, long dataRowId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		params.put("dataRowId", dataRowId);

		return DataAccessUtils
				.requiredSingleResult(getNamedParameterJdbcTemplate()
						.query("select TYPE, ORD, ID from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId and ID = :dataRowId",
								params, new RowMapper<Pair<Integer, Long>>() {

									@Override
									public Pair<Integer, Long> mapRow(
											ResultSet rs, int rowNum)
											throws SQLException {
										return new Pair<Integer, Long>(rs
												.getInt("TYPE"), rs
												.getLong("ORD"));
									}
								}));
	}

	/**
	 * Метод сохраняет параметры Cell
	 * 
	 * @param dataRows
	 */
	private void batchInsertCells(List<DataRow<Cell>> dataRows) {

		// Values
		Map<String, List<Object[]>> valueParamsMap = new HashMap<String, List<Object[]>>();
		for (String tableName : DataRowDaoImplUtils.CELL_VALUE_TABLE_NAMES) {
			valueParamsMap.put(tableName, new ArrayList<Object[]>());
		}
		// SpanInfo
		List<Object[]> spanInfoParams = new ArrayList<Object[]>();
		// Editable
		List<Object[]> editableParams = new ArrayList<Object[]>();
		// Styles
		List<Object[]> stylesParams = new ArrayList<Object[]>();

		for (DataRow<Cell> dataRow : dataRows) {
			for (String alias : dataRow.keySet()) {
				Cell cell = dataRow.getCell(alias);
				Column c = cell.getColumn();
				Object val = cell.getValue();
				// Values
				if (val != null) {
					String tableName = DataRowDaoImplUtils
							.getCellValueTableName(c);
					List<Object[]> batchList = valueParamsMap.get(tableName);
					batchList.add(new Object[] { dataRow.getId(), c.getId(),
							val });
				}
				// Span Info
				if (cell.getColSpan() > 1 || cell.getRowSpan() > 1) {
					spanInfoParams.add(new Object[] { dataRow.getId(),
							c.getId(), cell.getColSpan(), cell.getRowSpan() });
				}
				// Editable
				if (cell.isEditable()) {
					editableParams.add(new Object[] { dataRow.getId(),
							c.getId() });
				}
				// Styles
				FormStyle style = cell.getStyle();
				if (style != null) {
					stylesParams.add(new Object[] { dataRow.getId(), c.getId(),
							style.getId() });
				}

			}
		}

		// Values
		for (String tableName : DataRowDaoImplUtils.CELL_VALUE_TABLE_NAMES) {
			List<Object[]> batchList = valueParamsMap.get(tableName);
			if (!batchList.isEmpty()) {
				getJdbcTemplate()
						.batchUpdate(
								"insert into "
										+ tableName
										+ " (row_id, column_id, value) values (?, ?, ?)",
								valueParamsMap.get(tableName));
			}
		}
		// Span Info
		if (!spanInfoParams.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_span_info (row_id, column_id, colspan, rowspan) values (?, ?, ?, ?)",
							spanInfoParams);
		}
		// Editable
		if (!editableParams.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_editable (row_id, column_id) values (?, ?)",
							editableParams);
		}
		// Styles
		if (!stylesParams.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_style (row_id, column_id, style_id) values (?, ?, ?)",
							stylesParams);
		}

	}

	private void batchRemoveCells(final List<DataRow<Cell>> dataRows) {
		if (!dataRows.isEmpty()) {
			BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {

				@Override
				public void setValues(PreparedStatement ps, int i)
						throws SQLException {
					ps.setLong(1, dataRows.get(i).getId());
				}

				@Override
				public int getBatchSize() {
					return dataRows.size();
				}
			};

			for (String tableName : DataRowDaoImplUtils.CELL_VALUE_TABLE_NAMES) {
				getJdbcTemplate().batchUpdate(
						"delete from " + tableName + " where row_id = ?", bpss);
			}
			getJdbcTemplate().batchUpdate(
					"delete from cell_span_info where row_id = ?", bpss);
			getJdbcTemplate().batchUpdate(
					"delete from cell_editable where row_id = ?", bpss);
			getJdbcTemplate().batchUpdate(
					"delete from cell_style where row_id = ?", bpss);
		}
	}

}
