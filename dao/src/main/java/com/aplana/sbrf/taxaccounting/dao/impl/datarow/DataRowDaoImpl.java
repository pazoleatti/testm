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
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
	private static final byte COL_EDIT_IDX = 2;
	private static final byte COL_COLSPAN_IDX = 3;
	private static final byte COL_ROWSPAN_IDX = 4;

	@Autowired
	private BDUtils bdUtils;

	@Autowired
	private FormDataDao formDataDao;

	private RowMapper formDataSearchMapper = new RowMapper<FormDataSearchResult>() {
		@Override
		public FormDataSearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
			FormDataSearchResult result = new FormDataSearchResult();
			result.setIndex(SqlUtils.getLong(rs, "idx"));
			result.setColumnIndex(SqlUtils.getLong(rs, "column_index"));
			result.setRowIndex(SqlUtils.getLong(rs, "row_index"));
			result.setStringFound(rs.getString("raw_value"));
			return result;
		}
	};

	@Override
	public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, int sessionId,
														  boolean isCaseSensitive, boolean manual, boolean correctionDiff) {
		Pair<String, Map<String, Object>> sql = getSearchQuery(formDataId, formTemplateId, key, isCaseSensitive, manual, correctionDiff);
		// get query and params
		String query = sql.getFirst();
		Map<String, Object> params = sql.getSecond();
		return null;
	}

	/**
	 * Метод возвращает пару - строку запроса и параметры
	 *
	 * @return
	 */
	@Override
	public Pair<String, Map<String, Object>> getSearchQuery(Long formDataId, Integer formTemplateId, String key, boolean isCaseSensitive, boolean manual, boolean correctionDiff) {
		FormData formData = formDataDao.get(formDataId, manual);
		StringBuilder stringBuilder = new StringBuilder("");
		StringBuilder listColumnIndex = new StringBuilder("");
		boolean firstEntry = false;

		stringBuilder.append("select ord as row_index ");

		boolean isNumber = false;
		try {
			Double.parseDouble(key);
			isNumber = true;
		} catch (NumberFormatException ignored) {}

		for(Column column: formData.getFormColumns()) {
			String colIndex = "\"" + column.getOrder() + "\"";
			switch (column.getColumnType()) {
				case NUMBER:
					if (isNumber) {
						if (firstEntry) listColumnIndex.append(",");
						listColumnIndex.append(colIndex);
						stringBuilder.append(", ltrim(to_char(to_number(c").append(column.getId()).append("),'99999999999999990");
						if (((NumericColumn) column).getPrecision() > 0) {
							stringBuilder.append(".");
							for (int i = 0; i < ((NumericColumn) column).getPrecision(); i++)
								stringBuilder.append("0");
						}
						stringBuilder.append("')) as ").append(colIndex);
						firstEntry = true;
					}
					break;
				case STRING:
					if (firstEntry) listColumnIndex.append(",");
					listColumnIndex.append(colIndex);
					stringBuilder.append(", c").append(column.getId()).append(" as ").append(colIndex);
					firstEntry = true;
					break;
				case AUTO:
					if (isNumber) {
						if (firstEntry) listColumnIndex.append(",");
						listColumnIndex.append(colIndex);
						stringBuilder.append(", (case when (alias IS NULL OR alias LIKE '%{wan}%') then to_char((row_number() over(PARTITION BY CASE WHEN (alias IS NULL OR alias LIKE '%{wan}%') THEN 1 ELSE 0 END ORDER BY ord)) + ").append(formData.getPreviousRowNumber()).append(") else null end)")
								.append(" as ").append(colIndex);
						firstEntry = true;
					}
					break;
			}
		}
		stringBuilder.append(" FROM form_data_" + formTemplateId + " fd ");
		stringBuilder.append(" WHERE fd.form_data_id = :form_data_id and fd.temporary = :temporary and fd.manual = :manual ");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formDataId);
		params.put("pattern", "%" + key + "%");
		params.put("temporary", correctionDiff ? DataRowType.TEMP.getCode() : DataRowType.SAVED.getCode());
		params.put("manual", manual);

		String sql =
				"WITH t AS (" +
						stringBuilder.toString() +
						")" +
						" SELECT row_index,\n" +
						"  column_index,\n" +
						"  raw_value\n" +
						" FROM t UNPIVOT INCLUDE NULLS (raw_value FOR column_index IN (" + listColumnIndex.toString() + "))" +
						// check case sensitive
						(
								isCaseSensitive ?
										"            WHERE raw_value like :pattern \n" :
										"            WHERE LOWER(raw_value) like LOWER(:pattern) \n"
						);

		return new Pair<String, Map<String, Object>>(sql, params);
	}

	private String FORM_SEARCH_DATA_RESULT = "FORM_SEARCH_DATA_RESULT";
	private String FORM_SEARCH_RESULT = "FORM_SEARCH_RESULT";
	private String FORM_SEARCH_DATA_RESULT_TMP = "FORM_SEARCH_DATA_RESULT_TMP";

	@Override
	@SuppressWarnings("unchecked")
	public PagingResult<FormDataSearchResult> getSearchResult(int sessionId, long formDataId, String key, DataRowRange range) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("session_id", sessionId);
		params.put("form_data_id", formDataId);
		params.put("key", key);

		Pair<Integer, Integer> pair = null;
		Integer id;
		Integer count;
		try {
			pair = (Pair<Integer, Integer>) getNamedParameterJdbcTemplate().queryForObject(
					"SELECT id, rows_count FROM " + FORM_SEARCH_RESULT +
							" WHERE session_id = :session_id AND " +
							" form_data_id = :form_data_id AND " +
							" key = :key", params, new RowMapper<Object>() {
						@Override
						public Pair<Integer, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
							return (Pair<Integer, Integer>) new Pair(SqlUtils.getInteger(rs, "id"), SqlUtils.getInteger(rs, "rows_count"));
						}
					});
		} catch (IncorrectResultSizeDataAccessException ignored) {
		}

		if (pair != null && searchDataResultExists(sessionId)) {
			id = pair.getFirst();
			count = pair.getSecond();
			String dataQuery =
					"SELECT \"ORD\" idx, column_index, row_index, raw_value \n" +
					" FROM " + FORM_SEARCH_DATA_RESULT + " PARTITION(P" + sessionId + ") t" +
					" WHERE session_id = :session_id AND id = :id";
			if (count == null) {
				count = getNamedParameterJdbcTemplate().queryForObject("SELECT COUNT(*) FROM " + FORM_SEARCH_DATA_RESULT + " PARTITION(P" + sessionId + ")", params, Integer.class);
				params.put("count", count);
				getNamedParameterJdbcTemplate().update("UPDATE " + FORM_SEARCH_RESULT + " SET ROWS_COUNT = :count " +
						"WHERE session_id = :session_id AND form_data_id = :form_data_id AND key = :key", params);
			}
			params.clear();
			params.put("id", id);
			params.put("session_id", sessionId);
			params.put("from", range.getOffset());
			params.put("to", range.getOffset() + range.getCount() - 1);
			dataQuery += " AND \"ORD\" BETWEEN :from AND :to";
			List<FormDataSearchResult> dataRows = getNamedParameterJdbcTemplate().query(dataQuery, params, formDataSearchMapper);

			return new PagingResult<FormDataSearchResult>(dataRows, count);
		}
		return null;
	}

	private boolean searchDataResultExists(int sessionId) {
		int exists = getJdbcTemplate().queryForObject(
				"SELECT COUNT(*) FROM ALL_TAB_PARTITIONS" +
						" WHERE TABLE_NAME = '" + FORM_SEARCH_DATA_RESULT + "' AND PARTITION_NAME = 'P" + sessionId + "'", Integer.class);
		return exists != 0;
	}

	@Override
	public void prepareSearchDataResult(int sessionId) {
		try {
			if (!searchDataResultExists(sessionId)) {
				getJdbcTemplate().update("ALTER TABLE " + FORM_SEARCH_DATA_RESULT + " ADD PARTITION P" + sessionId + " VALUES(" + sessionId + ")");
			} else {
				getJdbcTemplate().update("ALTER TABLE " + FORM_SEARCH_DATA_RESULT + " TRUNCATE PARTITION P" + sessionId + " UPDATE GLOBAL INDEXES");
			}
		} catch (DataAccessException ignored) {}
	}

	@Override
	public  void deleteSearchDataResult(Integer sessionId) {
		try {
			if (searchDataResultExists(sessionId)) {
				getJdbcTemplate().update("ALTER TABLE " + FORM_SEARCH_DATA_RESULT + " DROP PARTITION P" + sessionId + " UPDATE GLOBAL INDEXES");
			}
		} catch (DataAccessException ignored) {}
	}

	@Override
	public  void deleteSearchDataResultByFormDataId(Long formDataId) {
		List<Integer> sessionIds = getJdbcTemplate().queryForList(
				"SELECT session_id FROM " + FORM_SEARCH_RESULT +
						" WHERE form_data_id = " + formDataId, Integer.class);
		for (Integer session_id : sessionIds) {
			deleteSearchDataResult(session_id);
		}
	}

	@Override
	public void deleteSearchResults(Integer sessionId, Long formDataId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("session_id", sessionId);
		params.put("form_data_id", formDataId);
		getNamedParameterJdbcTemplate().update(
				"DELETE FROM " + FORM_SEARCH_RESULT +
						" WHERE 1=1 " +
						(sessionId != null ? " AND session_id = :session_id " : "") +
						(formDataId != null ? " AND form_data_id = :form_data_id" : ""), params);
	}

	@Override
	public void clearSearchDataResult() {
		List<Integer> sessionIds = getJdbcTemplate().queryForList(
				"SELECT DISTINCT session_id FROM " + FORM_SEARCH_RESULT +
						" WHERE \"DATE\" + 1 < SYSDATE ", Integer.class);
		sessionIds.addAll(getJdbcTemplate().queryForList(
				"SELECT session_id from (" +
						"SELECT substr(PARTITION_NAME, 2) AS session_id FROM ALL_TAB_PARTITIONS" +
						" WHERE TABLE_NAME = '" + FORM_SEARCH_DATA_RESULT + "' AND PARTITION_NAME <> 'P0') t " +
				" WHERE NOT EXISTS(SELECT * FROM " + FORM_SEARCH_RESULT + " WHERE session_id = t.session_id) ", Integer.class));
		for (Integer session_id : sessionIds) {
			deleteSearchDataResult(session_id);
		}
	}

	@Override
	public void clearSearchResult() {
		getJdbcTemplate().update(
				"DELETE FROM " + FORM_SEARCH_RESULT +
						" WHERE \"DATE\" + 1 < SYSDATE ");
	}

	private int addSearchResult(int sessionId, long formDataId, String key) {
		Integer id = generateId("SEQ_SEARCH_FORM", Integer.class);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("session_id", sessionId);
		params.put("form_data_id", formDataId);
		params.put("id", id);
		params.put("key", key);
		getNamedParameterJdbcTemplate().update(
				"INSERT INTO " + FORM_SEARCH_RESULT + "(id, session_id, form_data_id, \"KEY\", \"DATE\")" +
						" VALUES(:id, :session_id, :form_data_id, :key, SYSDATE)",
				params);
		return id;
	}

	public int saveSearchResult(int sessionId, long formDataId, String key) {
		deleteSearchResults(sessionId, formDataId);
		return addSearchResult(sessionId, formDataId, key);
	}

	@Override
	public void saveSearchDataResult(final int id, final List<FormDataSearchResult> resultList) {
		getJdbcTemplate().batchUpdate("INSERT INTO " + FORM_SEARCH_DATA_RESULT_TMP + "(row_index, column_index, raw_value) VALUES(?, ?, ?)", new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, resultList.get(i).getRowIndex().intValue());
				ps.setInt(2, resultList.get(i).getColumnIndex().intValue());
				ps.setString(3, resultList.get(i).getStringFound());
			}

			@Override
			public int getBatchSize() {
				return resultList.size();
			}
		});
	}

	@Override
	public void saveSearchDataResult(int id, int sessionId, String query, Map<String, Object> params) {
		params.put("id", id);
		params.put("sessionId", sessionId);
		getNamedParameterJdbcTemplate().update(
				"INSERT INTO " + FORM_SEARCH_DATA_RESULT + " PARTITION(P" + sessionId + ") (id, session_id, row_index, column_index, raw_value, \"ORD\") " +
				"SELECT :id, :sessionId, row_index, column_index, raw_value, row_number() over(ORDER BY row_index, column_index) ord FROM (" +
				"SELECT to_number(row_index) row_index, to_number(column_index) column_index, raw_value FROM (" + query + ")" +
				" UNION ALL " +
				"SELECT row_index, column_index, raw_value FROM " + FORM_SEARCH_DATA_RESULT_TMP + ")", params);
	}

	@Override
	public void copyRows(long formDataSourceId, long formDataDestinationId) {
		FormData formDataSource = formDataDao.get(formDataSourceId, false);
		FormData formData = formDataDao.get(formDataDestinationId, false);
		// Проверяем, что макеты одинаковые
		if (formData.getFormTemplateId() != formDataSource.getFormTemplateId()) {
			throw new IllegalArgumentException("Макеты НФ должны совпадать");
		}
		// Очистка постоянного среза НФ-приемника
		removeRows(formData);
		// Копирование данных из постоянного среза источника в постоянный срез приемника
		Map<Integer, String[]> columnNames = DataRowMapper.getColumnNames(formData);
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		for (Column column : formData.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name);
			}
		}
		sql.append(")\nSELECT seq_form_data_nnn.nextval, :form_data_id, :temporary, manual, ord, alias");
		for (Column column : formData.getFormColumns()) {
			sql.append('\n');
			for (String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name);
			}
		}
		sql.append("\nFROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append("\nWHERE form_data_id = :form_data_source_id AND temporary = :temporary_source AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("form_data_source_id", formDataSource.getId());
		params.put("temporary_source", DataRowType.SAVED.getCode());
		params.put("manual", DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public void reorderRows(FormData formData, final List<DataRow<Cell>> rows) {
		if (rows == null || rows.isEmpty()) {
			return;
		}
		// сдвигаем все строки, чтобы обойти ограничение уникального индекса при сортировке
		shiftRows(formData, new DataRowRange(1, rows.size()), DataRowType.SAVED);
		// обновляем данные в бд
		StringBuilder sql = new StringBuilder("UPDATE form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" SET ord = :ord WHERE id = :id");

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
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
	}

	@Override
	public boolean isDataRowsCountChanged(FormData formData) {
		// сравниваем кол-во реальных строк с числом, хранящимся в form_data.number_current_row
		StringBuilder sql = new StringBuilder("SELECT (SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
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

	private void insertRows(FormData formData, int index, List<DataRow<Cell>> rows, DataRowType dataRowType) {
		int size = getRowCount(formData);
		if (index < 1 || index > size + 1) {
			throw new IllegalArgumentException(String.format("Вставка записей допустима только в диапазоне индексов [1; %s]. index = %s", size + 1, index));
		}
		if (rows == null) {
			throw new IllegalArgumentException("Аргумент \"rows\" должен быть задан");
		}
		// сдвигаем строки
		shiftRows(formData, new DataRowRange(index, rows.size()), dataRowType);
		// вставляем новые в образовавшийся промежуток
		Map<Integer, String[]> columnNames = DataRowMapper.getColumnNames(formData);

		StringBuilder sql = new StringBuilder("INSERT INTO form_data_");
		sql.append(formData.getFormTemplateId());
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
			values.put("temporary", dataRowType.getCode());
			values.put("manual", manual);
			values.put("ord", row.getIndex());
			values.put("alias", row.getAlias());
			for (Column column : formData.getFormColumns()) {
				String[] names = columnNames.get(column.getId());
				Cell cell = row.getCell(column.getAlias());
				values.put(names[COL_VALUE_IDX], cell.getValue());
				values.put(names[COL_STYLE_IDX], cell.getStyle() == null ? null : cell.getStyle().getId());
				values.put(names[COL_EDIT_IDX], cell.isEditable() ? 1 : 0);
				values.put(names[COL_COLSPAN_IDX], cell.getColSpan());
				values.put(names[COL_ROWSPAN_IDX], cell.getRowSpan());
			}
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
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
	public void updateRows(FormData formData, Collection<DataRow<Cell>> rows) {
		StringBuilder sql = new StringBuilder("UPDATE form_data_");
		sql.append(formData.getFormTemplateId());
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
				values.put(names[COL_VALUE_IDX], cell.getValue());
				values.put(names[COL_STYLE_IDX], cell.getStyle() == null ? null : cell.getStyle().getId());
				values.put(names[COL_EDIT_IDX], cell.isEditable() ? 1 : 0);
				values.put(names[COL_COLSPAN_IDX], cell.getColSpan());
				values.put(names[COL_ROWSPAN_IDX], cell.getRowSpan());
			}
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
	}

	@Override
	public void removeRows(FormData formData, final List<DataRow<Cell>> rows) {
		// примечание: строки надо удалять так, чтобы не нарушалась последовательность ORD = 1, 2, 3, ...
		StringBuilder sql = new StringBuilder("DELETE FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE id = :id");

		// формируем список параметров для батча
		List<Map<String, Long>> params = new ArrayList<Map<String, Long>>(rows.size());
		for (DataRow<Cell> row : rows) {
			Map<String, Long> values = new HashMap<String, Long>();
			values.put("id", row.getId());
			params.add(values);
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
		reorderRows(formData);
	}

	/**
	 * Переупорядочивает строки, восстанавливает последовательность ORD = 1, 2, 3, ...
	 *
	 * @param formData
	 */
	private void reorderRows(FormData formData) {
		StringBuilder sql = new StringBuilder("MERGE INTO form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" t USING\n(SELECT id, ROW_NUMBER() ");
		if (isSupportOver()) {
			sql.append("OVER (ORDER BY ord)");
		} else {
			sql.append("OVER ()");
		}
		sql.append(" AS neword FROM form_data_");
		sql.append(formData.getFormTemplateId());
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

	@Override
	public void removeCheckPoint(FormData formData) {
		// удаляем точку восстановления (временный срез)
		removeRowsInternal(formData, DataRowType.TEMP);
	}

	@Override
	public void restoreCheckPoint(FormData formData) {
		// удаляем постоянный срез
		removeRowsInternal(formData, DataRowType.SAVED);
		// переносим данные из временного среза - восстановление контрольной точки
		StringBuilder sql = new StringBuilder("UPDATE form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" SET temporary = :temporary WHERE form_data_id = :form_data_id AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
		refreshRefBookLinks(formData);
	}

	@Override
	public void createManual(FormData formData) {
		if (!formData.isManual()) {
			throw new IllegalArgumentException("Форма должна иметь признак ручного ввода");
		}
		// удаляет данные ручного ввода, formData.isManual() == true
		removeRowsInternal(formData, DataRowType.SAVED);
		// формируем запрос на копирование среза
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append(") \nSELECT seq_form_data_nnn.nextval, form_data_id, :temporary AS temporary, :manual AS manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append("\nFROM form_data_");
		sql.append(formData.getFormTemplateId());
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
	}

	@Override
	public void createCheckPoint(FormData formData) {
		removeRowsInternal(formData, DataRowType.TEMP);
		// формируем запрос на копирование среза
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append(") \nSELECT seq_form_data_nnn.nextval, form_data_id, :temporary AS temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append("\nFROM form_data_");
		sql.append(formData.getFormTemplateId());
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
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowRange range) {
		return getRowsInternal(fd, range, DataRowType.SAVED, true);
	}

	@Override
	public List<DataRow<Cell>> getRowsRefColumnsOnly(FormData fd, DataRowRange range, boolean correctionDiff) {
		return getRowsInternal(fd, range, correctionDiff ? DataRowType.TEMP : DataRowType.SAVED, false );
	}

	@Override
	public int getRowCount(FormData formData) {
		return getSizeInternal(formData, DataRowType.SAVED);
	}

	@Override
	public List<DataRow<Cell>> getTempRows(FormData fd, DataRowRange range) {
		return getRowsInternal(fd, range, DataRowType.TEMP, true);
	}

	@Override
	public int getTempRowCount(FormData formData) {
		return getSizeInternal(formData, DataRowType.TEMP);
	}

	@Override
	public int getAutoNumerationRowCount(FormData formData) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
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
	private List<DataRow<Cell>> getRowsInternal(FormData formData, DataRowRange range, DataRowType dataRowType, boolean isAllColumnsNeeded) {
		if (dataRowType != DataRowType.SAVED && dataRowType != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong type of 'isTemporary' argument");
		}
		DataRowMapper dataRowMapper = new DataRowMapper(formData);
		dataRowMapper.setAllColumnsNeeded(isAllColumnsNeeded);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql(range, dataRowType);
		if (!isSupportOver()) {
			sql.setFirst(sql.getFirst().replaceAll("OVER \\(PARTITION BY.{0,}ORDER BY ord\\)", "OVER ()"));
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(sql.getSecond());
			LOG.trace(sql.getFirst());
		}
		return getNamedParameterJdbcTemplate().query(sql.getFirst(), sql.getSecond(), dataRowMapper);
	}

	/**
	 * Возвращает количество строк в налоговой форме, включая итоговые (alias != null)
	 *
	 * @param formData
	 * @return
	 */
	private int getSizeInternal(FormData formData, DataRowType dataRowType) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
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

	@Override
	public void removeAllManualRows(FormData formData) {
		StringBuilder sql = new StringBuilder("DELETE FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("manual", DataRowType.MANUAL.getCode());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	/**
	 * Удаляет все строки из временного\постоянного срезов. Для временного среза удаляет строки как в версии ручного
	 * ввода, так и в автоматической версии. Признак formData.isManual учитывается только при удалении из
	 * постоянного среза
	 *
	 * @param formData          НФ
	 * @param dataRowType признак из какого среза удалить строки NO NULL
	 */
	private void removeRowsInternal(FormData formData, DataRowType dataRowType) {
		if (dataRowType != DataRowType.TEMP && dataRowType != DataRowType.SAVED) {
			throw new IllegalArgumentException("Value of argument 'isTemporary' is incorrect");
		}

		StringBuilder sql = new StringBuilder("DELETE FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary");
		if (dataRowType == DataRowType.SAVED) {
			sql.append(" AND manual = :manual");
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", dataRowType.getCode());
		if (dataRowType == DataRowType.SAVED) {
			params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public void removeRows(FormData formData, DataRowRange range) {
		StringBuilder sql = new StringBuilder("DELETE FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord BETWEEN :indexFrom AND :indexTo");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		params.put("indexFrom", range.getOffset());
		params.put("indexTo", range.getOffset() + range.getCount() - 1);

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
		shiftRows(formData, new DataRowRange(range.getOffset() + range.getCount(), -range.getCount()), DataRowType.SAVED);
	}

	/**
	 * Сдвигает строки на "range.count" позиций начиная с позиции "range.offset"
	 *
	 * @param formData экземпляр НФ для строк которых осуществляется сдвиг
	 * @param range диапазон для указания с какого индекса и на сколько осуществляем сдвиг
	 * @param dataRowType тип среза в котором осуществляется сдвиг строк
	 * @return количество смещенных строк
	 */
	private int shiftRows(FormData formData, DataRowRange range, DataRowType dataRowType) {
		StringBuilder sql = new StringBuilder("UPDATE form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" SET ord = ord + :shift WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord >= :offset");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", dataRowType.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		params.put("offset", range.getOffset());
		params.put("shift", range.getCount());

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(sql.toString());
		}
		return getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
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
				names.add("c" + column.getId());
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
		withSql.append(" FROM form_data_").append(formData.getFormTemplateId());
		withSql.append(" WHERE temporary = :temporary AND form_data_id = :form_data_id), DATA AS(\n");
		withSql.append("SELECT DISTINCT '").append(names.get(0)).append("' AS column_name, ").append(names.get(0)).append(" AS value FROM TAB");
		for (int i = 1; i < names.size(); i++) {
			withSql.append(" UNION \n SELECT DISTINCT '").append(names.get(i)).append("' AS column_name, ").append(names.get(i)).append(" AS value FROM TAB");
		}
		withSql.append(")\nSELECT DISTINCT rba.ref_book_id, data.value AS record_id FROM data\n");
		withSql.append("JOIN form_column fc ON('c'||fc.id = data.column_name AND fc.parent_column_id IS NULL)\n");
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

		if (LOG.isTraceEnabled()) {
			LOG.trace(params);
			LOG.trace(deleteSql.toString());
			LOG.trace(insertSql.toString());
		}
		getNamedParameterJdbcTemplate().update(deleteSql.toString(), params);
		getNamedParameterJdbcTemplate().update(insertSql.toString(), params);
	}
}