package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository("formDataDao")
@Transactional(readOnly = true)
public class FormDataDaoImpl extends AbstractDao implements FormDataDao {
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormStyleDao formStyleDao;

	private static class ValueRecord<T> {
		private T value;
		private int order;
		private int columnId;

		public ValueRecord(T value, int order, int columnId) {
			this.value = value;
			this.order = order;
			this.columnId = columnId;
		}
	}

	/**
	 * Запись в таблице cell_style
	 */
	private static class StyleRecord {
		private int columnId;
		private Long rowId;
		private Integer id;

		private StyleRecord(int columnId, Long rowId, Integer id) {
			this.columnId = columnId;
			this.rowId = rowId;
			this.id = id;
		}

		public int getColumnId() {
			return columnId;
		}

		public void setColumnId(int columnId) {
			this.columnId = columnId;
		}

		public Long getRowId() {
			return rowId;
		}

		public void setRowId(Long rowId) {
			this.rowId = rowId;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

	}

	/**
	 * Запись в таблице cell_span_info
	 * 
	 * @author sgoryachkin
	 */
	private static class SpanRecord {
		private int colSpan;
		private int rowSpan;
		private int columnId;
		private Long rowId;

		public SpanRecord(int colSpan, int rowSpan, int columnId, Long rowId) {
			super();
			this.colSpan = colSpan;
			this.rowSpan = rowSpan;
			this.columnId = columnId;
			this.rowId = rowId;
		}

		public int getColSpan() {
			return colSpan;
		}

		public void setColSpan(int colSpan) {
			this.colSpan = colSpan;
		}

		public int getRowSpan() {
			return rowSpan;
		}

		public void setRowSpan(int rowSpan) {
			this.rowSpan = rowSpan;
		}

		public int getColumnId() {
			return columnId;
		}

		public void setColumnId(int columnId) {
			this.columnId = columnId;
		}

		public Long getRowId() {
			return rowId;
		}

		public void setRowId(Long rowId) {
			this.rowId = rowId;
		}

	}

	private static class RowMapperResult {
		FormData formData;
		FormTemplate formTemplate;
	}

	private class FormDataRowMapper implements RowMapper<RowMapperResult> {
		public RowMapperResult mapRow(ResultSet rs, int index)
				throws SQLException {
			RowMapperResult result = new RowMapperResult();

			int formTemplateId = rs.getInt("form_id");
			FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

			FormData fd = new FormData();
			fd.initFormTemplateParams(formTemplate);
			fd.setId(rs.getLong("id"));
			fd.setDepartmentId(rs.getInt("department_id"));
			fd.setState(WorkflowState.fromId(rs.getInt("state")));
			fd.setKind(FormDataKind.fromId(rs.getInt("kind")));
			fd.setReportPeriodId(rs.getInt("report_period_id"));

			result.formData = fd;
			result.formTemplate = formTemplate;
			return result;
		}

	}

	private class FormDataWithOutRowRowMapper implements RowMapper<FormData> {
		public FormData mapRow(ResultSet rs, int index)
				throws SQLException {
			FormData result = new FormData();
			result.setId(rs.getLong("id"));
			result.setDepartmentId(rs.getInt("department_id"));
			result.setState(WorkflowState.fromId(rs.getInt("state")));
			result.setKind(FormDataKind.fromId(rs.getInt("kind")));
			result.setReportPeriodId(rs.getInt("report_period_id"));
			return result;
		}

	}

	public FormData get(final long formDataId) {
		JdbcTemplate jt = getJdbcTemplate();
		final FormData formData;
		final FormTemplate formTemplate;
		try {
			RowMapperResult res = jt.queryForObject(
					"select * from form_data where id = ?",
					new Object[] { formDataId }, new int[] { Types.NUMERIC },
					new FormDataRowMapper());
			formData = res.formData;
			formTemplate = res.formTemplate;
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Записи в таблице FORM_DATA с id = "
					+ formDataId + " не найдено");
		}

		final Map<Long, DataRow> rowIdToAlias = new HashMap<Long, DataRow>();

		jt.query("select * from data_row where form_data_id = ? order by ord",
				new Object[] { formDataId }, new int[] { Types.NUMERIC },
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						Long rowId = rs.getLong("id");
						String alias = rs.getString("alias");
						DataRow row = formData.appendDataRow(alias);
						rowIdToAlias.put(rowId, row);
						row.setOrder(rs.getInt("ord"));
						row.setManagedByScripts(rs.getInt("managed_by_scripts") == 1);
					}
				});

		readStyle(formTemplate, rowIdToAlias, formData.getId());
		readSpan(formTemplate, rowIdToAlias, formData.getId());
		readValues("numeric_value", formTemplate, rowIdToAlias, formData);
		readValues("string_value", formTemplate, rowIdToAlias, formData);
		readValues("date_value", formTemplate, rowIdToAlias, formData);
		return formData;
	}

	private boolean checkValueType(Object value,
			Class<? extends Column> columnType) {
		// TODO: в будущем возможны спец-ячейки, тип которых отличается от типа
		// столбца
		if (value == null) {
			return true;
		} else {
			return value instanceof BigDecimal
					&& NumericColumn.class.equals(columnType)
					|| value instanceof String
					&& StringColumn.class.equals(columnType)
					|| value instanceof Date
					&& DateColumn.class.equals(columnType);
		}
	}

	private void readStyle(final FormTemplate formTemplate,
			final Map<Long, DataRow> rowMap, Long formDataId) {

		String sqlQuery = "SELECT row_id, column_id, style_id FROM cell_style cs "
				+ "WHERE exists (SELECT 1 from data_row r WHERE r.id = cs.row_id and r.form_data_id = ?)";

		getJdbcTemplate().query(sqlQuery, new Object[] { formDataId },
				new int[] { Types.NUMERIC }, new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						Long rowId = rs.getLong("row_id");
						DataRow row = rowMap.get(rowId);
						Column col = formTemplate.getColumn(rs
								.getInt("column_id"));
						Cell cell = row.getCell(col.getAlias());
						cell.setStyleAlias(ModelUtils.findByProperties(
								formTemplate.getStyles(),
								rs.getInt("style_id"),
								new ModelUtils.GetPropertiesFunc<FormStyle, Integer>() {
									@Override
									public Integer getProperties(FormStyle object) {
										return object.getId();
									}
								}).getAlias());
					}
				});
	}

	/**
	 * Получение данных о диапазоне ячейки
	 * 
	 * @param formTemplate
	 * @param rowMap
	 * @param formDataId
	 */
	private void readSpan(final FormTemplate formTemplate,
			final Map<Long, DataRow> rowMap, Long formDataId) {
		getJdbcTemplate()
				.query("select column_id, row_id, colspan, rowspan from cell_span_info v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
						new Object[] { formDataId },
						new int[] { Types.NUMERIC }, new RowCallbackHandler() {
							public void processRow(ResultSet rs)
									throws SQLException {
								SpanRecord spanRecord = new SpanRecord(rs
										.getInt("colspan"), rs
										.getInt("rowspan"), rs
										.getInt("column_id"), rs
										.getLong("row_id"));
								Long rowId = rs.getLong("row_id");

								DataRow row = rowMap.get(rowId);
								Column col = formTemplate.getColumn(spanRecord
										.getColumnId());
								Cell cellValue = row.getCell(col.getAlias());

								cellValue.setColSpan(spanRecord.getColSpan());
								cellValue.setRowSpan(spanRecord.getRowSpan());
							}
						});
	}

	private void readValues(String tableName, final FormTemplate formTemplate,
			final Map<Long, DataRow> rowMap, final FormData formData) {
		getJdbcTemplate()
				.query("select * from "
						+ tableName
						+ " v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
						new Object[] { formData.getId() },
						new int[] { Types.NUMERIC }, new RowCallbackHandler() {
							public void processRow(ResultSet rs)
									throws SQLException {
								int columnId = rs.getInt("column_id");
								Long rowId = rs.getLong("row_id");
								Object value = rs.getObject("value");
								if (value != null) {
									DataRow row = rowMap.get(rowId);
									Column col = formTemplate
											.getColumn(columnId);
									String columnAlias = col.getAlias();
									// TODO: думаю, стоит зарефакторить
									if (value instanceof java.sql.Date) {
										value = new java.util.Date(
												((java.sql.Date) value)
														.getTime());
									}

									boolean typeOk = checkValueType(value,
											col.getClass());
									if (!typeOk) {
										logger.warn("Cannot assign value '"
												+ value + "'("
												+ value.getClass().getName()
												+ ") to column '" + columnAlias
												+ "'("
												+ col.getClass().getName()
												+ ")");
										value = null;
									}
									row.put(columnAlias, value);
								}
							}
						});
	}

	@Override
	@Transactional(readOnly = false)
	public long save(final FormData formData) {
		if (formData.getState() == null) {
			throw new DaoException("Не указана стадия жизненного цикла");
		}

		if (formData.getKind() == null) {
			throw new DaoException("Не указан тип налоговой формы");
		}

		if (formData.getDepartmentId() == null) {
			throw new DaoException(
					"Не указано подразделение, к которому относится налоговая форма");
		}

		if (formData.getReportPeriodId() == null) {
			throw new DaoException("Не указан идентификатор отчётного периода");
		}

		Long formDataId;
		JdbcTemplate jt = getJdbcTemplate();
		if (formData.getId() == null) {
			formDataId = generateId("seq_form_data", Long.class);
			jt.update(
					"insert into form_data (id, form_id, department_id, kind, state, report_period_id) values (?, ?, ?, ?, ?, ?)",
					formDataId, formData.getFormTemplateId(),
					formData.getDepartmentId(), formData.getKind().getId(),
					formData.getState().getId(), formData.getReportPeriodId());
			formData.setId(formDataId);
		} else {
			formDataId = formData.getId();
			jt.update("delete from data_row where form_data_id = ?",
					new Object[] { formDataId }, new int[] { Types.NUMERIC });
		}

		insertRows(formData);
		return formDataId;
	}

	private void insertRows(final FormData formData) {
		final List<DataRow> dataRows = formData.getDataRows();
		if (dataRows.isEmpty()) {
			return;
		}

		final long formDataId = formData.getId();

		OrderUtils.reorder(dataRows);
		// Теперь мы уверены, что order везде заполнен, уникален и идёт, начиная
		// с 1, возрастая без пропусков.
		final List<ValueRecord<BigDecimal>> numericValues = new ArrayList<ValueRecord<BigDecimal>>();
		final List<ValueRecord<String>> stringValues = new ArrayList<ValueRecord<String>>();
		final List<ValueRecord<Date>> dateValues = new ArrayList<ValueRecord<Date>>();

		final List<SpanRecord> spanValues = new ArrayList<SpanRecord>();
		final List<StyleRecord> styleValues = new ArrayList<StyleRecord>();
		final List<Integer> spanOrders = new ArrayList<Integer>();
		final List<Integer> styleOrders = new ArrayList<Integer>();
		// final Map<String, FormStyle> styleAliasToId =
		// formStyleDao.getAliasToFormStyleMap(formData.getFormTemplateId());

		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				DataRow dr = dataRows.get(index);
				String rowAlias = dr.getAlias();
				int rowOrder = dr.getOrder();
				ps.setString(1, rowAlias);
				ps.setInt(2, rowOrder);
				ps.setInt(3, dr.isManagedByScripts() ? 1 : 0);

				for (Column col : formData.getFormColumns()) {
					Object val = dr.get(col.getAlias());
					Cell cellValue = dr.getCell(col.getAlias());

					if (val == null) {
						continue;
					} else if (val instanceof BigDecimal) {
						numericValues.add(new ValueRecord<BigDecimal>(
								(BigDecimal) val, rowOrder, col.getId()));
					} else if (val instanceof String) {
						stringValues.add(new ValueRecord<String>((String) val,
								rowOrder, col.getId()));
					} else if (val instanceof Date) {
						dateValues.add(new ValueRecord<Date>((Date) val,
								rowOrder, col.getId()));
					}

					if (cellValue.getColSpan() > 1
							|| cellValue.getRowSpan() > 1) {
						spanValues.add(new SpanRecord(cellValue.getColSpan(),
								cellValue.getRowSpan(), col.getId(), null));
						spanOrders.add(rowOrder);
					}
					if (cellValue.getStyle() != null) {
						styleValues.add(new StyleRecord(col.getId(), null,
								cellValue.getStyle().getId()));
						styleOrders.add(rowOrder);
					}
				}
			}

			@Override
			public int getBatchSize() {
				return dataRows.size();
			}
		};

		JdbcTemplate jt = getJdbcTemplate();

		jt.batchUpdate(
				"insert into data_row (id, form_data_id, alias, ord, managed_by_scripts) values (seq_data_row.nextval, "
						+ formDataId + ", ?, ?, ?)", bpss);

		// Получаем массив идентификаторов строк, индекс записи в массиве
		// соответствует порядковому номеру строки (меньше на единицу)
		final List<Long> rowIds = jt.queryForList(
				"select id from data_row where form_data_id = ? order by ord",
				new Object[] { formDataId }, new int[] { Types.NUMERIC },
				Long.class);

		insertValues("numeric_value", numericValues, rowIds);
		insertValues("string_value", stringValues, rowIds);
		insertValues("date_value", dateValues, rowIds);

		insertStyles(styleValues, styleOrders, rowIds);
		insertSpans(spanValues, spanOrders, rowIds);
	}

	private <T> void insertValues(String tableName,
			final List<ValueRecord<T>> values, final List<Long> rowIds) {
		if (values.isEmpty()) {
			return;
		}
		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				ValueRecord<T> rec = values.get(index);
				// В строках order начинается с 1 (см. OrderUtils.reorder), а в
				// List индексы начинаются с нуля
				ps.setLong(1, rowIds.get(rec.order - 1));
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

	private void insertStyles(final List<StyleRecord> values,
			final List<Integer> orders, final List<Long> rowIds) {
		if (!values.isEmpty()) {
			getJdbcTemplate()
					.batchUpdate(
							"insert into cell_style (row_id, column_id, style_id) values (?, ?, ?)",
							new BatchPreparedStatementSetter() {
								public void setValues(PreparedStatement ps,
										int index) throws SQLException {

									StyleRecord rec = values.get(index);
									rec.setRowId(rowIds.get(orders.get(index) - 1));

									ps.setLong(1, rec.getRowId());
									ps.setInt(2, rec.getColumnId());
									ps.setInt(3, rec.getId());

								}

								public int getBatchSize() {
									return values.size();
								}
							});
		}
	}

	private <T> void insertSpans(final List<SpanRecord> values,
			final List<Integer> orders, final List<Long> rowIds) {
		if (values.isEmpty()) {
			return;
		}
		getJdbcTemplate()
				.batchUpdate(
						"insert into cell_span_info (row_id, column_id, colspan, rowspan) values (?, ?, ?, ?)",
						new BatchPreparedStatementSetter() {
							public void setValues(PreparedStatement ps,
									int index) throws SQLException {
								SpanRecord rec = values.get(index);
								rec.setRowId(rowIds.get(orders.get(index) - 1));

								ps.setLong(1, rec.getRowId());
								ps.setInt(2, rec.getColumnId());
								ps.setInt(3, rec.getColSpan());
								ps.setInt(4, rec.getRowSpan());
							}

							public int getBatchSize() {
								return values.size();
							}
						});
	}

	@Override
	public List<Long> listFormDataIdByType(int typeId) {
		return getJdbcTemplate()
				.queryForList(
						"select id from form_data fd where exists (select 1 from form f where f.id = fd.form_id and f.type_id = ?)",
						new Object[] { typeId }, new int[] { Types.NUMERIC },
						Long.class);
	}

	@Override
	@Transactional(readOnly = false)
	public void delete(long formDataId) {
		JdbcTemplate jt = getJdbcTemplate();

		Object[] params = { formDataId };
		int[] types = { Types.NUMERIC };

		jt.update(
				"delete from numeric_value v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
				params, types);
		jt.update(
				"delete from string_value v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
				params, types);
		jt.update(
				"delete from date_value v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
				params, types);
		jt.update(
				"delete from cell_span_info v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
				params, types);
		jt.update("delete from data_row where form_data_id = ?", params, types);
		jt.update("delete from form_data where id = ?", params, types);
	}

	/**
	 * Ищет налоговую форму по заданным параметрам.
	 * 
	 * @param formTypeId
	 *            идентификатор
	 *            {@link com.aplana.sbrf.taxaccounting.model.FormType вида
	 *            формы}.
	 * @param kind
	 *            тип формы
	 * @param departmentId
	 *            идентификатор
	 *            {@link com.aplana.sbrf.taxaccounting.model.Department
	 *            подразделения}
	 * @param periodId
	 *            идентификатор
	 *            {@link com.aplana.sbrf.taxaccounting.model.ReportPeriod
	 *            отчетного периода}
	 * @return форма или null, если не найдена
	 */
	@Override
	public FormData find(int formTypeId, FormDataKind kind, int departmentId,
			int periodId) {
		Long formDataId = getJdbcTemplate()
				.query("select fd.id from form_data fd join form f on fd.form_id=f.id "
						+ "where f.type_id=? and fd.kind=? and fd.department_id=? and fd.report_period_id=?",
						new Object[] { formTypeId, kind.getId(), departmentId,
								periodId },
						new int[] { Types.NUMERIC, Types.NUMERIC,
								Types.NUMERIC, Types.NUMERIC },
						new ResultSetExtractor<Long>() {
							@Override
							public Long extractData(ResultSet rs)
									throws SQLException, DataAccessException {
								if (rs.next()) {
									return rs.getLong("id");
								} else {
									return null;
								}
							}
						});

		if (formDataId != null) {
			return get(formDataId);
		} else {
			return null;
		}
	}

	@Override
	public FormData getWithoutRows(long formDataId){
		JdbcTemplate jt = getJdbcTemplate();
		try{
			return jt.queryForObject(
					"SELECT id, department_id, state, kind, report_period_id" +
							" FROM form_data WHERE id = ?",
					new Object[] { formDataId }, new int[] { Types.NUMERIC },
					new FormDataWithOutRowRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Записи в таблице FORM_DATA с id = "
					+ formDataId + " не найдено");
		}
	}
}
