package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.ColumnType;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRowType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация ДАО для работы со строками НФ
 *
 * @author sgoryachkin
 */
@Repository
public class DataRowDaoImpl extends AbstractDao implements DataRowDao {

	private static final Log LOG = LogFactory.getLog(DataRowDaoImpl.class);
	private static final byte COL_VALUE_IDX = 0;
	private static final byte COL_STYLE_IDX = 1;

	@Autowired
	private BDUtils bdUtils;

	@Autowired
	private FormDataDao formDataDao;

	@Override
	public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, DataRowRange range, String key, boolean isCaseSensitive, boolean manual) {
        if (isSupportOver()) {
            getJdbcTemplate().update("alter session set NLS_NUMERIC_CHARACTERS = '. '");
        }

		Pair<String, Map<String, Object>> sql = getSearchQuery(formDataId, key, isCaseSensitive, manual);
		// get query and params
		Map<String, Object> params = sql.getSecond();

        String query = sql.getFirst();
		// calculate count
		String countQuery = "SELECT COUNT(*) FROM (" + query + ")";

		int count = getNamedParameterJdbcTemplate().queryForObject(countQuery, params, Integer.class);

		List<FormDataSearchResult> dataRows;

		if (count != 0) {
			String dataQuery = "SELECT idx, column_index, row_index, raw_value FROM (" + query + ") WHERE idx BETWEEN :from AND :to";
			params.put("from", 1);
			params.put("to", range.getCount());

			dataRows = getNamedParameterJdbcTemplate().query(dataQuery, params, new RowMapper<FormDataSearchResult>() {
				@Override
				public FormDataSearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
					FormDataSearchResult result = new FormDataSearchResult();
					result.setIndex(SqlUtils.getLong(rs, "idx"));
					result.setColumnIndex(SqlUtils.getLong(rs, "column_index"));
					result.setRowIndex(SqlUtils.getLong(rs, "row_index"));
					result.setStringFound(rs.getString("raw_value"));
					return result;
				}
			});
		} else {
			dataRows = new ArrayList<FormDataSearchResult>();
		}

		return new PagingResult<FormDataSearchResult>(dataRows, count);
	}

	/**
	 * Метод возвращает пару - строку запроса и параметры
	 *
	 * @return
	 */
	private Pair<String, Map<String, Object>> getSearchQuery(Long formDataId, String key, boolean isCaseSensitive, boolean manual) {
        FormData formData = formDataDao.get(formDataId, manual);
        StringBuilder stringBuilder = new StringBuilder("");
        String strUnion = "union all \n";
        boolean f = false;
        for(Column column: formData.getFormColumns()) {
            switch (column.getColumnType()) {
                case NUMBER:
                    if (f) stringBuilder.append(strUnion);
                    stringBuilder.append("select ord as row_index, ").append(column.getId()).append(" as column_id, ltrim(to_char(to_number(c").append(column.getDataOrder()).append("),'99999999999999990");
                    if (((NumericColumn)column).getPrecision()>0) {
                        stringBuilder.append(".");
                        for(int i=0; i<((NumericColumn)column).getPrecision(); i++)
                            stringBuilder.append("0");
                    }
                    stringBuilder.append("')) as raw_value, ").append(column.getOrder()).append(" as column_index from t ");
                    f = true;
                    break;
                case STRING:
                    if (f) stringBuilder.append(strUnion);
                    stringBuilder.append("select ord as row_index, ").append(column.getId()).append(" as column_id, c").append(column.getDataOrder()).append(" as raw_value, ")
                            .append(column.getOrder()).append(" as column_index from t ");
                    f = true;
                    break;
                case AUTO:
                    if (f) stringBuilder.append(strUnion);
                    stringBuilder.append("select ord as row_index, ").append(column.getId()).append(" as column_id, (case when (alias IS NULL OR alias LIKE '%{wan}%') then to_char((row_number() over(PARTITION BY CASE WHEN (alias IS NULL OR alias LIKE '%{wan}%') THEN 1 ELSE 0 END ORDER BY ord)) + ").append(formData.getPreviousRowNumber()).append(") else null end) as raw_value, ")
                            .append(column.getOrder()).append(" as column_index from t ");
                    f = true;
                    break;
            }
        }

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fdId", formDataId);
		params.put("key", "%" + key + "%");
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", manual);

		String sql =
				"SELECT row_number() OVER (ORDER BY a.row_index, a.column_index) AS idx, row_index, column_index, raw_value\n" +
						"FROM (\n" +
						"    WITH t AS (\n" +
						"        SELECT * FROM form_data_row fd WHERE fd.form_data_id = :fdId and fd.temporary = :temporary and fd.manual = :manual)\n" +
                        stringBuilder.toString() +
						"  ) a\n" +

						// check case sensitive
						(
								isCaseSensitive ?
										"            WHERE raw_value like :key \n" :
										"            WHERE LOWER(raw_value) like LOWER(:key) \n"
						);

		return new Pair<String, Map<String, Object>>(sql, params);
	}

	@Override
	public void copyRows(long formDataSourceId, long formDataDestinationId) {
		FormData formDataSource = formDataDao.get(formDataSourceId, false);
		FormData formDataTarget = formDataDao.get(formDataDestinationId, false);
		// Проверяем, что макеты одинаковые
		if (formDataTarget.getFormTemplateId() != formDataSource.getFormTemplateId()) {
			throw new IllegalArgumentException("Макеты НФ должны совпадать");
		}
		// Очистка постоянного среза НФ-приемника
		removeRows(formDataTarget);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formDataTarget.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("form_data_source_id", formDataSource.getId());
		params.put("temporary_source", DataRowType.SAVED.getCode());
		params.put("manual", DataRowType.AUTO.getCode());

		// Копирование данных из постоянного среза источника в постоянный срез приемника
		Map<Integer, String[]> columnNames = DataRowMapper.getColumnNames(formDataTarget);
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_row");
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		for (Column column : formDataTarget.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name);
			}
		}
		sql.append(")\nSELECT seq_form_data_row.nextval, :form_data_id, :temporary, manual, ord, alias");
		for (Column column : formDataTarget.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name);
			}
		}
		sql.append("\nFROM form_data_row");
		sql.append("\nWHERE form_data_id = :form_data_source_id AND temporary = :temporary_source AND manual = :manual");

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
        }
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
		// копирование спанов из одной НФ в другую
		copySpan(formDataSource, formDataTarget, DataRowType.SAVED, DataRowType.AUTO);
	}

	@Override
	public void reorderRows(FormData formData, final List<DataRow<Cell>> rows) {
		if (rows == null || rows.isEmpty()) {
			return;
		}
		// сдвигаем все строки, чтобы обойти ограничение уникального индекса при сортировке
		shiftRows(formData, new DataRowRange(1, rows.size()), DataRowType.SAVED);
		// обновляем данные в бд
		String sql = "UPDATE form_data_row SET ord = :ord WHERE id = :id";

		List<Map<String, Object>> params = new ArrayList<Map<String, Object>>(rows.size());
		int i = 1;
		for (DataRow<Cell> row : rows) {
			row.setIndex(i++);

			Map<String, Object> values = new HashMap<String, Object>();
			values.put("ord", row.getIndex());
			values.put("id", row.getId());
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql);
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.intern(), params.toArray(new Map[0]));
		// обновить все спаны
		removeSpanAll(formData, DataRowType.SAVED);
		insertSpan(formData, DataRowType.SAVED, rows, false);
	}

	private static final String SQL_DELETE_SPAN_ALL = "DELETE FROM form_data_row_span WHERE " +
			" form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual";
	/**
	 * Удаляем всю информацию по объединениям ячеек для указанного среза
	 * @param formData код НФ + ручной\автоматическтй срез
	 * @param temporary постоянный\резервный срез
	 */
	void removeSpanAll(FormData formData, DataRowType temporary) {
		if (temporary != DataRowType.SAVED && temporary != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong value of 'temporary' argument");
		}
		final Map<String, Object> rowParams = new HashMap<String, Object>();
		rowParams.put("form_data_id", formData.getId());
		rowParams.put("temporary", temporary.getCode());
		rowParams.put("manual", formData.isManual() ? 1 : 0);
		getNamedParameterJdbcTemplate().update(SQL_DELETE_SPAN_ALL, rowParams);
	}

	private static final String SQL_INSERT_SPAN = "INSERT INTO form_data_row_span (row_id, form_data_id, temporary, manual, data_ord, ord, colspan, rowspan) " +
			"VALUES (:row_id, :form_data_id, :temporary, :manual, :data_ord, :ord, :colspan, :rowspan)";
	/**
	 * Вставляем информацию об объединениях ячеек для указанных строк
	 * @param rows строки, должны быть уже в БД
	 * @param needShift требуется ли расширять спаны? true - для новых записей
	 */
	void insertSpan(FormData formData, DataRowType temporary, List<DataRow<Cell>> rows, boolean needShift) {
		if (rows.isEmpty()) {
			return;
		}
		if (temporary != DataRowType.SAVED && temporary != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong value of 'temporary' argument");
		}
		// расширяем старые спаны
		DataRowRange range = new DataRowRange(rows.get(0).getIndex(), rows.size());
		if (needShift) {
			shiftSpan(formData, range, temporary);
		}
		// запрашиваем информацию о типе среза
		final Map<String, Object> rowParams = new HashMap<String, Object>();
		rowParams.put("form_data_id", formData.getId());
		rowParams.put("temporary", temporary.getCode());
		rowParams.put("manual", formData.isManual() ? 1 : 0);

		// устанавливаем значения для спанов
		List<Map<String, Object>> spanParams = new ArrayList<Map<String, Object>>(rows.size());
		for (DataRow<Cell> row : rows) {
			for(Cell cell : row.getCells()) {
				int colspan = cell.getColSpan();
				int rowspan = cell.getRowSpan();
				if (colspan + rowspan > 2) { // если значения отличаются от значений по умолчанию
					Map<String, Object> values = new HashMap<String, Object>();
					values.putAll(rowParams);
					values.put("row_id", row.getId());
					values.put("data_ord", cell.getColumn().getDataOrder());
					values.put("ord", row.getIndex());
					values.put("colspan", null);
					values.put("rowspan", null);
					if (colspan > 1) {
						values.put("colspan", colspan);
					}
					if (rowspan > 1) {
						values.put("rowspan", rowspan);
					}
					spanParams.add(values);
				}
			}
		}
		if (!spanParams.isEmpty()) {
			getNamedParameterJdbcTemplate().batchUpdate(SQL_INSERT_SPAN, spanParams.toArray(new Map[0]));
		}
	}

	private static final String SQL_SHIFT_SPAN = "UPDATE form_data_row_span SET rowspan = rowspan + :count " +
			"WHERE rowspan > 1 AND ord + rowspan >= :offset AND ord < :offset AND temporary = :temporary AND " +
			"manual = :manual AND form_data_id = :form_data_id";
	/**
	 * Раздвигает спаны по вертикали
	 * @param formData
	 * @param range
	 * @param temporary
	 */
	void shiftSpan(FormData formData, DataRowRange range, DataRowType temporary) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", temporary.getCode());
		params.put("manual", formData.isManual() ? 1 : 0);
		params.put("offset", range.getOffset());
		params.put("count", range.getCount());
		getNamedParameterJdbcTemplate().update(SQL_SHIFT_SPAN, params);
	}

	/**
	 * Вызывается при обновлении значений набора строк.
	 * Обновляем родительские ячейки, удаляем текущие спаны, вставляем новые спаны
	 * @param dataRowMapper
	 * @param rows
	 */
	private void updateSpan(DataRowMapper dataRowMapper, List<DataRow<Cell>> rows) {
		if (rows.isEmpty()) {
			return;
		}
		// найти строку с наименьшим ord
		DataRow<Cell> row = rows.get(0);
		int ord = row.getIndex();
		for(DataRow<Cell> r : rows) {
			if (r.getIndex() < ord) {
				row = r;
				ord = r.getIndex();
			}
		}
        for (Cell cell: row.getCells()) {
            if (cell.getRowSpan() == 0 && cell.getColSpan() == 0)
                // данный метод работает только для первой строки из пейджинга
                return;
        }
        // взять самого первого и обновить у его родителей значения + спаны
        Map<String, Object> rowParams = getRowParams(rows.get(0));
        DataRowType temporary = (Integer) rowParams.get("temporary") == 1 ? DataRowType.TEMP : DataRowType.SAVED;
        updateParentCells(dataRowMapper, temporary, row);
        //обновление спанов у существующих строк
        removeSpan(rows);
        insertSpan(dataRowMapper.getFormData(), temporary, rows, false);
	}

	private static final String SQL_GET_ROW_PARAMS = "SELECT form_data_id, temporary, manual FROM form_data_row WHERE id = :row_id";
	/**
	 * Запрашиваем информацию о типе среза, в котором находится строка НФ
	 * @param row
	 * @return
	 */
	private Map<String, Object> getRowParams(DataRow<Cell> row) {
		final Map<String, Object> rowParams = new HashMap<String, Object>();
		rowParams.put("row_id", row.getId());
		getNamedParameterJdbcTemplate().query(SQL_GET_ROW_PARAMS, rowParams, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				rowParams.put("form_data_id", rs.getLong("form_data_id"));
				rowParams.put("temporary", rs.getInt("temporary"));
				rowParams.put("manual", rs.getInt("manual"));
			}
		});
		return rowParams;
	}

	private static final String SQL_MOVE_SPAN = "UPDATE form_data_row_span SET temporary = :new_temporary " +
			"WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual";
	/**
	 * Копирует спаны из постоянного среза в резервный и наоборот
	 * @param formData
	 * @param from
	 * @param to
	 */
	void moveSpan(FormData formData, DataRowType from, DataRowType to) {
		if (from != DataRowType.SAVED && from != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong value of 'from' argument");
		}
		if (to != DataRowType.SAVED && to != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong value of 'to' argument");
		}
		if (to == from) {
			throw new IllegalArgumentException("Wrong value of 'from' and 'to' arguments");
		}
		// вставляем новые
		final Map<String, Object> rowParams = new HashMap<String, Object>();
		rowParams.put("form_data_id", formData.getId());
		rowParams.put("temporary", from.getCode());
		rowParams.put("new_temporary", to.getCode());
		rowParams.put("manual", formData.isManual() ? 1 : 0);
		getNamedParameterJdbcTemplate().update(SQL_MOVE_SPAN, rowParams);
	}

	private static final String SQL_COPY_SPAN = "INSERT INTO form_data_row_span (row_id, form_data_id, temporary, manual, data_ord, ord, colspan, rowspan) " +
			"SELECT (SELECT id FROM form_data_row WHERE ord = s.ord AND form_data_id = :form_data_id_target AND temporary = :temporary_target AND manual = :manual_target) as row_id, " +
			":form_data_id_target, :temporary_target, :manual_target, data_ord, ord, colspan, rowspan FROM form_data_row_span s WHERE " +
			"form_data_id = :form_data_id_source AND temporary = :temporary_source AND manual = :manual_source";
	/**
	 * Копирует спаны в постоянном срезе из одного экземпляра НФ в другую, либо в рамках одной НФ, но из автоматической
	 * версии в версию ручного ввода.
	 * <br /><br />
	 * Варианты использования: <br />
	 * <ul>
	 *     <li>createCheckPoint - SAVED, AUTO -> TEMP, AUTO</li>
	 *     <li>copyRows - SAVED, AUTO -> SAVED, AUTO</li>
	 *     <li>createManual - SAVED, AUTO -> SAVED, MANUAL</li>
	 * </ul>
	 * @param formDataSource
	 * @param formDataTarget
	 * @param temporary тип среза, откуда и куда копируются данные
	 * @param targetManual в какую версию ввода копировать данные (авто или ручную)
	 */
	void copySpan(FormData formDataSource, FormData formDataTarget, DataRowType temporary, DataRowType targetManual) {
		if (temporary != DataRowType.SAVED && temporary != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong value of 'temporary' argument");
		}
		if (targetManual != DataRowType.MANUAL && targetManual != DataRowType.AUTO) {
			throw new IllegalArgumentException("Wrong value of 'temporary' argument");
		}
		// вставляем новые
		final Map<String, Object> rowParams = new HashMap<String, Object>();
		rowParams.put("form_data_id_source", formDataSource.getId());
		rowParams.put("form_data_id_target", formDataTarget.getId());
		rowParams.put("temporary_source", DataRowType.SAVED.getCode());
		rowParams.put("temporary_target", temporary.getCode());
		rowParams.put("manual_source", DataRowType.AUTO.getCode());
		rowParams.put("manual_target", targetManual.getCode());
		getNamedParameterJdbcTemplate().update(SQL_COPY_SPAN, rowParams);
	}

	/**
	 * После удаления строк надо поправить rowspan у оставшихся строк. Выполняется до основного
	 * удаления строк
	 * @param rows
	 */
	private void updateSpanOnRemove(FormData formData, List<DataRow<Cell>> rows) {
		if (rows.isEmpty()) {
			return;
		}
		for (DataRow<Cell> row : rows) {
			DataRowRange range = new DataRowRange(row.getIndex(), -1); // сжимаем спаны на олну строку
			shiftSpan(formData, range, DataRowType.SAVED);
		}
	}

	private static final String SQL_DELETE_SPAN = "DELETE FROM form_data_row_span WHERE row_id = :row_id";
	/**
	 * Удаляем всю информацию по объединениям ячеек для указанных строк
	 * @param rows
	 */
	void removeSpan(Collection<DataRow<Cell>> rows) {
		if (rows.isEmpty()) {
			return;
		}
		List<Map<String, Object>> spanParams = new ArrayList<Map<String, Object>>(rows.size());
		for (DataRow<Cell> row : rows) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("row_id", row.getId());
			spanParams.add(values);
		}
		getNamedParameterJdbcTemplate().batchUpdate(SQL_DELETE_SPAN, spanParams.toArray(new Map[0]));
	}

	@Override
	public boolean isDataRowsCountChanged(FormData formData) {
		// сравниваем кол-во реальных строк с числом, хранящимся в form_data.number_current_row
		StringBuilder sql = new StringBuilder("SELECT (SELECT COUNT(*) FROM form_data_row");
		sql.append(" WHERE form_data_id = :form_data_id AND (alias IS NULL OR alias LIKE '%\n");
		sql.append(DataRowMapper.ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%')");
		sql.append(" AND temporary = :temporary AND manual = :manual");
		sql.append(") - (SELECT COALESCE(number_current_row, 0) FROM form_data WHERE id = :form_data_id) FROM DUAL");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		int difference = getNamedParameterJdbcTemplate().queryForObject(sql.toString().intern(), params, Integer.class);
		return difference != 0;
	}

	@Override
    public void insertRows(FormData formData, int index, List<DataRow<Cell>> rows) {
        insertRows(formData, index, rows, DataRowType.SAVED);
    }

	private void insertRows(FormData formData, int index, List<DataRow<Cell>> rows, DataRowType temporary) {
        DataRowMapper dataRowMapper = new DataRowMapper(formData);
		int size = getRowCount(formData);
		if (index < 1 || index > size + 1) {
			throw new IllegalArgumentException(String.format("Вставка записей допустима только в диапазоне индексов [1; %s]. index = %s", size + 1, index));
		}
		if (rows == null) {
			throw new IllegalArgumentException("Аргумент \"rows\" должен быть задан");
		}
		// сдвигаем строки
		shiftRows(formData, new DataRowRange(index, rows.size()), temporary);
		// вставляем новые в образовавшийся промежуток
		Map<Integer, String[]> columnNames = DataRowMapper.getColumnNames(formData);

		StringBuilder sql = new StringBuilder("INSERT INTO form_data_row");
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		for (Column column : formData.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name);
			}
		}
		sql.append(")\n VALUES (:id, :form_data_id, :temporary, :manual, :ord, :alias");
		for (Column column : formData.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", :").append(name);
			}
		}
		sql.append(')');
		// формируем список параметров для батча
		List<Long> ids = bdUtils.getNextDataRowIds(Long.valueOf(rows.size()));
		int manual = formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode();
		List<Map<String, Object>> params = new ArrayList<Map<String, Object>>(rows.size());
		int i = 0;
		for (DataRow<Cell> row : rows) {
			row.setId(ids.get(i));
			row.setIndex(index + i++);

			Map<String, Object> values = new HashMap<String, Object>();
			values.put("id", row.getId());
			values.put("form_data_id", formData.getId());
			values.put("temporary", temporary.getCode());
			values.put("manual", manual);
			values.put("ord", row.getIndex());
			values.put("alias", row.getAlias());
			for (Column column : formData.getFormColumns()) {
				String[] names = columnNames.get(column.getId());
				Cell cell = row.getCell(column.getAlias());
				values.put(names[COL_VALUE_IDX], dataRowMapper.formatCellValue(cell));
				values.put(names[COL_STYLE_IDX], dataRowMapper.formatCellStyle(cell));
			}
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
		// вставляем спаны
		insertSpan(formData, temporary, rows, true);
	}

	@Override
	public void saveRows(final FormData formData, final List<DataRow<Cell>> dataRows) {
		// полностью удаляем строки
		removeRows(formData);
		// вставляем новый набор данных
		insertRows(formData, 1, dataRows);
	}

    @Override
    public void saveTempRows(final FormData formData, final List<DataRow<Cell>> dataRows) {
        // полностью удаляем строки
        removeCheckPoint(formData);
        // вставляем новый набор данных
        insertRows(formData, 1, dataRows, DataRowType.TEMP);
    }

	@Override
	public void updateRows(FormData formData, List<DataRow<Cell>> rows) {
        DataRowMapper dataRowMapper = new DataRowMapper(formData);
		// обновляем спаны до того, как обновим значения указанных строк, так как часть данных должна быть записана
		// в родительские строки, а не напрямую в указанные строки
		updateSpan(dataRowMapper, rows);

        StringBuilder sql = new StringBuilder("UPDATE form_data_row");
		sql.append(" SET alias = :alias");
		Map<Integer, String[]> columnNames = DataRowMapper.getColumnNames(formData);
		for (Column column : formData.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name).append(" = :").append(name);
			}
		}
		sql.append(" WHERE id = :id");
		if (LOG.isTraceEnabled()) {
			LOG.trace("updateRows: " + sql.toString().intern());
		}
		// формируем список параметров для батча
		List<Map<String, Object>> params = new ArrayList<Map<String, Object>>(rows.size());
		for (DataRow<Cell> row : rows) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("id", row.getId());
			values.put("alias", row.getAlias());
			for (Column column : formData.getFormColumns()) {
				String[] names = columnNames.get(column.getId());
				Cell cell = row.getCell(column.getAlias());
				values.put(names[COL_VALUE_IDX], dataRowMapper.formatCellValue(cell));
				values.put(names[COL_STYLE_IDX], dataRowMapper.formatCellStyle(cell));
			}
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
	}

	private static final String DELETE_BY_ROW_ID = "DELETE FROM form_data_row WHERE id = :id";
	@Override
	public void removeRows(FormData formData, final List<DataRow<Cell>> rows) {
		if (rows.isEmpty()) {
			return;
		}
		// примечание: строки надо удалять так, чтобы не нарушалась последовательность ORD = 1, 2, 3, ...

		// формируем список параметров для батча
		List<Map<String, Long>> params = new ArrayList<Map<String, Long>>(rows.size());
		for (DataRow<Cell> row : rows) {
			Map<String, Long> values = new HashMap<String, Long>();
			values.put("id", row.getId());
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(DELETE_BY_ROW_ID);
		}
		getNamedParameterJdbcTemplate().batchUpdate(DELETE_BY_ROW_ID.intern(), params.toArray(new Map[0]));
		// упорядочиваем заново строки
		reorderRows(formData);
		// обновляем пропуски в спанах
		updateSpanOnRemove(formData, rows);
		// синхронизируем form_data_row_span.ord
		reorderSpan(formData);
	}

	/**
	 * Переупорядочивает строки, восстанавливает последовательность ORD = 1, 2, 3, ...
	 *
	 * @param formData ссылка на экземпляр НФ
	 */
	private void reorderRows(FormData formData) {
		StringBuilder sql = new StringBuilder("MERGE INTO form_data_row");
		sql.append(" t USING\n(SELECT id, ROW_NUMBER() ");
		if (isSupportOver()) {
			sql.append("OVER (ORDER BY ord)");
		} else {
			sql.append("OVER ()");
		}
		sql.append(" AS neword FROM form_data_row");
		sql.append("\nWHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual) s");
		sql.append("\nON (t.id = s.id) WHEN MATCHED THEN UPDATE SET t.ord = s.neword");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	private static final String SQL_REORDER_SPAN = "MERGE INTO form_data_row_span t USING \n" +
			"(SELECT id, ord FROM form_data_row \n" +
			"WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual) s \n" +
			"ON (t.row_id = s.id) WHEN MATCHED THEN UPDATE SET t.ord = s.ord";
	/**
	 * Синхронизирует form_data_row_span.ord со значениями из form_data_row.ord
	 * @param formData
	 */
	private void reorderSpan(FormData formData) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(SQL_REORDER_SPAN);
		}
		getNamedParameterJdbcTemplate().update(SQL_REORDER_SPAN, params);
	}

	@Override
	public void removeCheckPoint(FormData formData) {
		// удаляем точку восстановления (временный срез)
		removeRowsInternal(formData, DataRowType.TEMP);
	}

	private static final String SQL_MOVE_ROWS = "UPDATE form_data_row SET temporary = :temporary WHERE form_data_id = :form_data_id AND manual = :manual";
	@Override
	public void restoreCheckPoint(FormData formData) {
		// удаляем постоянный срез
		removeRowsInternal(formData, DataRowType.SAVED);
		// переносим данные из временного среза - восстановление контрольной точки
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(SQL_MOVE_ROWS);
		}
		getNamedParameterJdbcTemplate().update(SQL_MOVE_ROWS, params);
		refreshRefBookLinks(formData);
		// переносим спаны
		moveSpan(formData, DataRowType.TEMP, DataRowType.SAVED);
	}

	@Override
	public void createManual(FormData formData) {
		if (!formData.isManual()) {
			throw new IllegalArgumentException("Форма должна иметь признак ручного ввода");
		}
		// удаляет данные ручного ввода, formData.isManual() == true
		removeRowsInternal(formData, DataRowType.SAVED);
		// формируем запрос на копирование среза
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_row");
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append(") \nSELECT seq_form_data_row.nextval, form_data_id, :temporary AS temporary, :manual AS manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append("\nFROM form_data_row");
		sql.append("\nWHERE form_data_id = :form_data_id AND temporary = :temporary_src");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary_src", DataRowType.SAVED.getCode());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", DataRowType.MANUAL.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
		// копирование спанов источника
		copySpan(formData, formData, DataRowType.SAVED, DataRowType.MANUAL);
	}

	@Override
	public void createCheckPoint(FormData formData) {
		removeRowsInternal(formData, DataRowType.TEMP);
		// формируем запрос на копирование среза
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_row");
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append(") \nSELECT seq_form_data_row.nextval, form_data_id, :temporary AS temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append("\nFROM form_data_row");
		sql.append("\nWHERE form_data_id = :form_data_id AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.TEMP.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
		// копируем спаны в резервный срез
		copySpan(formData, formData, DataRowType.TEMP, formData.isManual() ? DataRowType.MANUAL : DataRowType.AUTO);
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowRange range) {
		return getRowsInternal(fd, range, DataRowType.SAVED);
	}

	@Override
	public int getRowCount(FormData formData) {
		return getSizeInternal(formData, DataRowType.SAVED);
	}

    @Override
    public List<DataRow<Cell>> getTempRows(FormData fd, DataRowRange range) {
        return getRowsInternal(fd, range, DataRowType.TEMP);
    }

    @Override
    public int getTempRowCount(FormData formData) {
        return getSizeInternal(formData, DataRowType.TEMP);
    }

	@Override
	public int getAutoNumerationRowCount(FormData formData) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_row");
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND (alias IS NULL");
		sql.append(" OR alias LIKE '%").append(DataRowMapper.ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%')");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		return getNamedParameterJdbcTemplate().queryForObject(sql.toString().intern(), params, Integer.class);
	}

	/**
	 * @param formData    НФ для которой требуется получить строки
	 * @param range       параметры пейджинга, может быть null
	 * @return список строк, != null
	 */
	private List<DataRow<Cell>> getRowsInternal(FormData formData, DataRowRange range, DataRowType temporary) {
		if (temporary != DataRowType.SAVED && temporary != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong value of 'temporary' argument");
		}
		DataRowMapper dataRowMapper = new DataRowMapper(formData);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql(range, temporary);
		if (!isSupportOver()) {
			sql.setFirst(sql.getFirst().replaceAll("OVER \\(PARTITION BY.{0,}ORDER BY ord\\)", "OVER ()"));
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.getSecond());
			LOG.trace(sql.getFirst());
		}
		List<DataRow<Cell>> rows = getNamedParameterJdbcTemplate().query(sql.getFirst(), sql.getSecond(), dataRowMapper);
		if (rows.size() > 0) {
			getSpanInfo(dataRowMapper, range, temporary, rows);
			// для постраничного отображения данных необходимо актуализировать ячейки с разорванным rowspan
			// https://jira.aplana.com/browse/SBRFACCTAX-15026
			if (range != null) {
				updateChildCells(dataRowMapper, temporary, rows);
			}
		}
		return rows;
	}

	/**
	 * Так как информация об объединении ячеек сейчас хранится в отдельной таблице, то необходимо выполнить
	 * дополнительный запрос и проставить значения для colspan и rowspan
	 * @param dataRowMapper
	 * @param range
	 * @param temporary
	 * @param rows
	 */
	void getSpanInfo(DataRowMapper dataRowMapper, DataRowRange range, DataRowType temporary, final List<DataRow<Cell>> rows) {
		if (temporary != DataRowType.TEMP && temporary != DataRowType.SAVED) {
			throw new IllegalArgumentException("Value of argument 'temporary' is incorrect");
		}
		FormData formData = dataRowMapper.getFormData();
		final List<Column> columns = formData.getFormColumns();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT data_ord, ord, colspan, rowspan FROM form_data_row_span ");
		sb.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual ");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", temporary.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		final int offset = range == null ? 1 : range.getOffset();
		// если постранично, то добавляем орграничение на выборку
		if (range != null) {
			sb.append(" AND ord >= :start AND ord < :end");

			params.put("start", offset);
			params.put("end", offset + range.getCount());
		}
		sb.append(" ORDER BY ord, data_ord");

		// так как надо обработать большое количество ячеек, то заводим кэш
		final Map<Integer, Column> columnCache = new HashMap<Integer, Column>();
		getNamedParameterJdbcTemplate().query(sb.toString(), params, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				int dataOrd = rs.getInt("data_ord");
				int ord = rs.getInt("ord");
				Integer colSpan = SqlUtils.getInteger(rs, "colspan");
				Integer rowSpan = SqlUtils.getInteger(rs, "rowspan");
				// поиск графы
				Column column = columnCache.get(dataOrd);
				if(column == null) {
					column = getColumnByDataOrd(columns, dataOrd); //todo кэш
					columnCache.put(dataOrd, column);
				}
				// установка значений
				DataRow<Cell> row = rows.get(ord - offset);
				Cell cell = row.getCell(column.getAlias());
				cell.setColSpan(colSpan);
				cell.setRowSpan(rowSpan);
                if (colSpan == null) colSpan = 1;
                if (rowSpan == null) rowSpan = 1;
                for(int i = 0;i < rowSpan; i++)
                    for(int j = 0;j < colSpan; j++){
                        if (i != 0 || j != 0) {
                            if (rows.size() > (ord - offset + i)) {
                                DataRow<Cell> childRow = rows.get(ord - offset + i);
                                Cell childCell = childRow.getCells().get(column.getOrder() + j - 1);
                                childCell.setColSpan(0);
                                childCell.setRowSpan(0);
                            }
                        }
                    }
			}
		});
	}

	/**
	 * Возвращает количество строк в налоговой форме, включая итоговые (alias != null)
	 *
	 * @param formData ссылка на экземпляр НФ
	 * @param dataRowType тип среза
	 * @return количество записей в указанном срезе
	 */
	private int getSizeInternal(FormData formData, DataRowType dataRowType) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_row");
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", dataRowType.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		return getNamedParameterJdbcTemplate().queryForObject(sql.toString().intern(), params, Integer.class);
	}

	@Override
	public void removeRows(FormData formData) {
		removeRowsInternal(formData, DataRowType.SAVED);
	}

	private static final String SQL_DELETE_MANUAL_ROWS = "DELETE FROM form_data_row WHERE form_data_id = :form_data_id AND manual = :manual";
	@Override
	public void removeAllManualRows(FormData formData) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("manual", DataRowType.MANUAL.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(SQL_DELETE_MANUAL_ROWS);
		}
		getNamedParameterJdbcTemplate().update(SQL_DELETE_MANUAL_ROWS, params);
	}

	/**
	 * Удаляет все строки из основного\резервного срезов. Для основного среза удаляет строки как в версии ручного
	 * ввода, так и в автоматической версии. Признак formData.isManual учитывается только при удалении из
	 * основного среза
	 *
	 * @param formData          НФ
	 * @param temporary признак из какого среза удалить строки NO NULL
	 */
	private void removeRowsInternal(FormData formData, DataRowType temporary) {
		if (temporary != DataRowType.TEMP && temporary != DataRowType.SAVED) {
			throw new IllegalArgumentException("Value of argument 'temporary' is incorrect");
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", temporary.getCode());
		if (temporary == DataRowType.SAVED) {
			params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		}
		// удаление данных
		StringBuilder sql = new StringBuilder("DELETE FROM form_data_row");
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary");
		if (temporary == DataRowType.SAVED) {
			sql.append(" AND manual = :manual");
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	private static final String SQL_SHIFT_ROW_ORD = "UPDATE form_data_row " +
			"SET ord = ord + :shift WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord >= :offset";
	private static final String SQL_SHIFT_SPAN_ORD = "UPDATE form_data_row_span " +
			"SET ord = ord + :shift WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord >= :offset";
	/**
	 * Сдвигает строки на "range.count" позиций начиная с позиции "range.offset".
	 *
	 * @param formData экземпляр НФ для строк которых осуществляется сдвиг
	 * @param range диапазон для указания с какого индекса и на сколько осуществляем сдвиг
	 * @param temporary тип среза в котором осуществляется сдвиг строк
	 * @return количество смещенных строк
	 */
    private int shiftRows(FormData formData, DataRowRange range, DataRowType temporary) {
		// спаны сдвигать не требуется, их сдвиг осуществляется в вызываемых методах
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", temporary.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		params.put("offset", range.getOffset());
		params.put("shift", range.getCount());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(SQL_SHIFT_ROW_ORD);
		}
		getNamedParameterJdbcTemplate().update(SQL_SHIFT_SPAN_ORD, params);
		return getNamedParameterJdbcTemplate().update(SQL_SHIFT_ROW_ORD, params);
	}

	/**
	 * Актуализирует список ссылок НФ на элементы справочника. Ссылки выставляются только для строк постоянного среза
	 * (автоматическая или версия ручного ввода)
	 * @param formData экземпляр НФ, ссылки которого требуется актуализировать
	 */
	@Override
	public void refreshRefBookLinks(FormData formData) {
		// составляем список справочных граф
		List<String> names = new ArrayList<String>();
		for (Column column : formData.getFormColumns()) {
			if (column.getColumnType() == ColumnType.REFBOOK) {
				names.add("c" + column.getDataOrder());
			}
		}
		// если справочных граф нет, то выходим - делать нечего
		if (names.isEmpty()) {
			return;
		}
		// запросы предоставлены Леной http://jira.aplana.com/browse/SBRFACCTAX-11367
		// формируем WITH часть запросов
		StringBuilder withSql = new StringBuilder("(WITH TAB AS (SELECT ");
		withSql.append(names.get(0));
		for (int i = 1; i < names.size(); i++) {
			withSql.append(", ").append(names.get(i));
		}
		withSql.append(" FROM form_data_row");
		withSql.append(" WHERE temporary = :temporary AND form_data_id = :form_data_id), DATA AS(\n");

		withSql.append("SELECT DISTINCT '").append(names.get(0)).append("' AS column_name, TO_NUMBER(").append(names.get(0)).append(") AS value FROM TAB");
		for (int i = 1; i < names.size(); i++) {
			withSql.append(" UNION \n SELECT DISTINCT '").append(names.get(i)).append("' AS column_name, TO_NUMBER(").append(names.get(i)).append(") AS value FROM TAB");
		}

		withSql.append(")\nSELECT DISTINCT rba.ref_book_id, data.value AS record_id FROM data\n");
		withSql.append("JOIN form_column fc ON('c'||fc.data_ord = data.column_name AND fc.parent_column_id IS NULL ");
		withSql.append("AND form_template_id = :form_template_id)\n");
		withSql.append("JOIN ref_book_attribute rba ON rba.id = fc.attribute_id\n");
		withSql.append("WHERE data.value IS NOT NULL)");
		// запрос на удаление лишних ссылок
		StringBuilder deleteSql = new StringBuilder("DELETE FROM form_data_ref_book WHERE form_data_id = :form_data_id ");
		deleteSql.append("AND (ref_book_id, record_id) NOT IN\n").append(withSql);
		// запрос на вставку недостающих ссылок
		StringBuilder insertSql = new StringBuilder("MERGE INTO form_data_ref_book tgt USING\n");
		insertSql.append(withSql).append("src\n");
		insertSql.append("ON (tgt.form_data_id = :form_data_id AND tgt.ref_book_id = src.ref_book_id AND tgt.record_id = src.record_id)\n");
		insertSql.append("WHEN NOT MATCHED THEN\n");
		insertSql.append("INSERT (tgt.form_data_id, tgt.ref_book_id, tgt.record_id) VALUES (:form_data_id, src.ref_book_id, src.record_id)");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("form_template_id", formData.getFormTemplateId());


		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(deleteSql.toString());
			LOG.trace(insertSql.toString());
		}
		getNamedParameterJdbcTemplate().update(deleteSql.toString(), params);
		getNamedParameterJdbcTemplate().update(insertSql.toString(), params);
	}

	/**
	 * Запрос для получения информации об отрезанном пейджингом объединении ячеек по вертикали (ROW_SPAN)
	 * Результат в виде List<row_id, x, y, colspan, rowspan, value, style>
	 */
	private static final String ROW_SPAN_QUERY =
			" with t as ( " +
			"\n	select fc.data_ord as x, span.ord as y, span.colspan, span.rowspan, ref.* " +
			"\n 	from form_data fd " +
			"\n 	join form_column fc on fc.form_template_id = fd.form_template_id " +
			"\n 	join form_data_row data on data.form_data_id = fd.id " +
			"\n 	join form_data_row_span span on span.form_data_id = data.form_data_id and span.temporary = data.temporary and " +
			"\n   span.manual = data.manual and span.data_ord = fc.data_ord " +
			"\n 	and data.ord between span.ord and span.ord + span.rowspan - 1 " +
			"\n 	join form_data_row ref on ref.form_data_id = span.form_data_id and ref.temporary = span.temporary and " +
			"\n   ref.manual = span.manual and span.ord = ref.ord " +
			"\n 	where fd.id = :form_data_id and data.temporary = :temporary and data.manual = :manual and data.ord = :row_ord" +
			"		and span.ord <> data.ord) " +
		"\n select id row_id, x, y, colspan, rowspan, cell_value, cell_style from t " +
		"\n unpivot include nulls ((cell_value, cell_style) for form_data_row_data_ord in " +
			"\n ((C0, C0_STYLE) as 0, (C1, C1_STYLE) as 1, (C2, C2_STYLE) as 2, (C3, C3_STYLE) as 3, (C4, C4_STYLE) as 4, (C5, C5_STYLE) as 5, " +
			"\n (C6, C6_STYLE) as 6, (C7, C7_STYLE) as 7, (C8, C8_STYLE) as 8, (C9, C9_STYLE) as 9, (C10, C10_STYLE) as 10, " +
			"\n (C11, C11_STYLE) as 11, (C12, C12_STYLE) as 12, (C13, C13_STYLE) as 13, (C14, C14_STYLE) as 14, (C15, C15_STYLE) as 15, " +
			"\n (C16, C16_STYLE) as 16, (C17, C17_STYLE) as 17, (C18, C18_STYLE) as 18, (C19, C19_STYLE) as 19, (C20, C20_STYLE) as 20, " +
			"\n (C21, C21_STYLE) as 21, (C22, C22_STYLE) as 22, (C23, C23_STYLE) as 23, (C24, C24_STYLE) as 24, (C25, C25_STYLE) as 25, " +
			"\n (C26, C26_STYLE) as 26, (C27, C27_STYLE) as 27, (C28, C28_STYLE) as 28, (C29, C29_STYLE) as 29, (C30, C30_STYLE) as 30, " +
			"\n (C31, C31_STYLE) as 31, (C32, C32_STYLE) as 32, (C33, C33_STYLE) as 33, (C34, C34_STYLE) as 34, (C35, C35_STYLE) as 35, " +
			"\n (C36, C36_STYLE) as 36, (C37, C37_STYLE) as 37, (C38, C38_STYLE) as 38, (C39, C39_STYLE) as 39, (C40, C40_STYLE) as 40, " +
			"\n (C41, C41_STYLE) as 41, (C42, C42_STYLE) as 42, (C43, C43_STYLE) as 43, (C44, C44_STYLE) as 44, (C45, C45_STYLE) as 45, " +
			"\n (C46, C46_STYLE) as 46, (C47, C47_STYLE) as 47, (C48, C48_STYLE) as 48, (C49, C49_STYLE) as 49, (C50, C50_STYLE) as 50, " +
			"\n (C51, C51_STYLE) as 51, (C52, C52_STYLE) as 52, (C53, C53_STYLE) as 53, (C54, C54_STYLE) as 54, (C55, C55_STYLE) as 55, " +
			"\n (C56, C56_STYLE) as 56, (C57, C57_STYLE) as 57, (C58, C58_STYLE) as 58, (C59, C59_STYLE) as 59, (C60, C60_STYLE) as 60, " +
			"\n (C61, C61_STYLE) as 61, (C62, C62_STYLE) as 62, (C63, C63_STYLE) as 63, (C64, C64_STYLE) as 64, (C65, C65_STYLE) as 65, " +
			"\n (C66, C66_STYLE) as 66, (C67, C67_STYLE) as 67, (C68, C68_STYLE) as 68, (C69, C69_STYLE) as 69, (C70, C70_STYLE) as 70, " +
			"\n (C71, C71_STYLE) as 71, (C72, C72_STYLE) as 72, (C73, C73_STYLE) as 73, (C74, C74_STYLE) as 74, (C75, C75_STYLE) as 75, " +
			"\n (C76, C76_STYLE) as 76, (C77, C77_STYLE) as 77, (C78, C78_STYLE) as 78, (C79, C79_STYLE) as 79, (C80, C80_STYLE) as 80, " +
			"\n (C81, C81_STYLE) as 81, (C82, C82_STYLE) as 82, (C83, C83_STYLE) as 83, (C84, C84_STYLE) as 84, (C85, C85_STYLE) as 85, " +
			"\n (C86, C86_STYLE) as 86, (C87, C87_STYLE) as 87, (C88, C88_STYLE) as 88, (C89, C89_STYLE) as 89, (C90, C90_STYLE) as 90, " +
			"\n (C91, C91_STYLE) as 91, (C92, C92_STYLE) as 92, (C93, C93_STYLE) as 93, (C94, C94_STYLE) as 94, (C95, C95_STYLE) as 95, " +
			"\n (C96, C96_STYLE) as 96, (C97, C97_STYLE) as 97, (C98, C98_STYLE) as 98, (C99, C99_STYLE) as 99 ))" +
		"\n where x = form_data_row_data_ord ";
	/**
	 * Ищет для первой строки (row) родительские ячейки и извлекает из них значения ячеек и спанов
	 */
	private void updateChildCells(final DataRowMapper dataRowMapper, DataRowType temporary, final List<DataRow<Cell>> rows) {
		parentSpanExecute(dataRowMapper, temporary, rows.get(0), new DataRowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				updateChildCell(dataRowMapper, rs, rows, this);
			}
		});
	}

	/**
	 * Отдаем обратно родителям значения ячеек и спаны. Что передали, то занулили в самой строке, чтобы дважды не
	 * сохранять значения
	 * @param dataRowMapper
	 * @param temporary
	 * @param row
	 */
	private void updateParentCells(final DataRowMapper dataRowMapper, DataRowType temporary, final DataRow<Cell> row) {
		parentSpanExecute(dataRowMapper, temporary, row, new DataRowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				updateParentCell(dataRowMapper, rs, row, this);
			}
		});
	}

	/**
	 * Простая обертка с параметрами запроса
	 */
	static abstract class DataRowCallbackHandler implements RowCallbackHandler {
		private Map<String, Object> params = new HashMap<String, Object>();
		public Map<String, Object> getParams() {
			return params;
		}
		public void addParams(Map<String, Object> params) {
			this.params.putAll(params);
		}
	}
	/**
	 * Осуществляет поиск родительских ячеек для строки и выполняет заложенные в handler действия
	 * @param dataRowMapper
	 * @param temporary
	 * @param row
	 * @param handler
	 */
	private void parentSpanExecute(final DataRowMapper dataRowMapper, DataRowType temporary, final DataRow<Cell> row, DataRowCallbackHandler handler) {
		if (!isSupportOver()) { // отключаем метод для юнит-тестов
			return;
		}
		if (!DataRowType.SAVED.equals(temporary) && !DataRowType.TEMP.equals(temporary)) {
			throw new IllegalArgumentException("Argument ");
		}
		// выполняем запрос для поиска родителей
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", dataRowMapper.getFormData().getId());
		params.put("temporary", temporary.getCode());
		params.put("manual", dataRowMapper.getFormData().isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		params.put("row_ord", row.getIndex());
		handler.addParams(params);
		getNamedParameterJdbcTemplate().query(ROW_SPAN_QUERY, params, handler);
	}

	private static final String SQL_UPDATE_PARENT_VALUE = "UPDATE form_data_row SET c%1$s = :value, c%1$s_style = :style WHERE " +
			"form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord = :ord";
	private static final String SQL_UPDATE_PARENT_SPAN = "UPDATE form_data_row_span SET rowspan = :rowspan " +
			"WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord = :ord AND " +
			"data_ord = :data_ord";

	/**
	 *  Обновляем родительские ячейки и удаляем данные из дочерних, если нашли родителя
	 * @param dataRowMapper
	 * @param rs
	 * @param childRow
	 * @param handler
	 * @throws SQLException
	 */
	void updateParentCell(DataRowMapper dataRowMapper, ResultSet rs, DataRow<Cell> childRow, DataRowCallbackHandler handler) throws SQLException {
		final List<Column> columns = dataRowMapper.getFormData().getFormColumns();
		int childRowOrd = childRow.getIndex();

		int dataOrd = rs.getInt("x");
		int parentRowOrd = rs.getInt("y");
		int parentColSpan = getSpan(rs.getInt("colspan"));
		int parentRowSpan = getSpan(rs.getInt("rowspan"));

		Column column = getColumnByDataOrd(columns, dataOrd);
		Cell cell = childRow.getCell(column.getAlias());
		// обновляем спан родителя
		int childColSpan = cell.getColSpan();
		int childRowSpan = cell.getRowSpan();
		if (childColSpan != parentColSpan) {
			throw new IllegalArgumentException(String.format("В дочерней ячейке нельзя изменять colspan родительской {column %s, ord %s-%s, span %s-%s}",
					dataOrd, parentRowOrd, childRow.getIndex(), parentColSpan, childColSpan));
		}
		Map<String, Object> params = handler.getParams();
		params.put("rowspan", parentRowSpan + (childRowOrd + childRowSpan) - (parentRowOrd + parentRowSpan));
		params.put("ord", parentRowOrd);
		params.put("data_ord", dataOrd);

		if (parentRowOrd + parentRowSpan - childRowOrd != childRowSpan) {
			getNamedParameterJdbcTemplate().update(SQL_UPDATE_PARENT_SPAN, params);
		}
		// обновляем стиль и значение родителя
		params.put("value", dataRowMapper.formatCellValue(cell));
		params.put("style", DataRowMapper.formatCellStyle(cell));
		getNamedParameterJdbcTemplate().update(String.format(SQL_UPDATE_PARENT_VALUE, dataOrd), params);
		// обнуляем дочернюю ячейку
		cell.setStyle(null);
		cell.setValue(null, null);
		cell.setColSpan(null);
		cell.setRowSpan(null);
	}

	private static int getSpan(int span) {
		return span == 0 ? 1 : span;
	}

	/**
	 * Обновляет дочерние ячейки значениями из родительских
	 * @param dataRowMapper маппер значений из результата запроса
	 * @param rs результат запроса родительских ячеек
	 * @param rows
	 * @throws SQLException
	 */
	static void updateChildCell(DataRowMapper dataRowMapper, ResultSet rs, List<DataRow<Cell>> rows, DataRowCallbackHandler handler) throws SQLException {
		final List<Column> columns = dataRowMapper.getFormData().getFormColumns();
		int childRowOrd = rows.get(0).getIndex();

		int dataOrd = rs.getInt("x");
		int parentRowOrd = rs.getInt("y");
		int parentColSpan = rs.getInt("colspan");
		int parentRowSpan = rs.getInt("rowspan");
		String parentValue = rs.getString("cell_value");
		String parentStyle = rs.getString("cell_style");

		Column column = getColumnByDataOrd(columns, dataOrd);
		Cell cell = rows.get(0).getCell(column.getAlias());
		// переносим из родительской ячейки стили и значения
		if (parentColSpan != 0) {
			cell.setColSpan(parentColSpan);
		}
		if (parentRowSpan != 0) {
			cell.setRowSpan(parentRowOrd + parentRowSpan - childRowOrd);
		}
		DataRowMapper.parseCellStyle(cell, parentStyle);
		cell.setValue(dataRowMapper.parseCellValue(column.getColumnType(), parentValue), childRowOrd);
        int colSpan = cell.getColSpan();
        int rowSpan = cell.getRowSpan();
        for (int i = 0; i < rowSpan; i++)
            for (int j = 0; j < colSpan; j++) {
                if (i != 0 || j != 0) {
                    if (rows.size() > i) {
                        DataRow<Cell> childRow = rows.get(i);
                        Cell childCell = childRow.getCell(columns.get(column.getOrder() + j - 1).getAlias());
                        childCell.setColSpan(0);
                        childCell.setRowSpan(0);
                    }
                }
            }
	}

	/**
	 * Поиск столбца по dataOrd
	 * @param columns список граф НФ
	 * @param dataOrd порядковый номер столбца в FORM_DATA_ROW
	 * @return
	 * @throws IllegalAccessException если указан неправильный порядко
	 */
	static Column getColumnByDataOrd(List<Column> columns, final int dataOrd) {
		Column column = IterableUtils.find(columns, new Predicate<Column>() {
			@Override
			public boolean evaluate(Column column) {
				return dataOrd == column.getDataOrder();
			}
		});
		if (column == null) {
			throw new IllegalArgumentException("Wrong value for \"dataOrd\" argument. " + dataOrd);
		}
		return column;
	}
}