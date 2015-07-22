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
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	private static final Log log = LogFactory.getLog(DataRowDaoImpl.class);

	@Autowired
	private BDUtils bdUtils;

	@Autowired
	private FormDataDao formDataDao;

	@Override
	public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive, boolean manual) {
		Pair<String, Map<String, Object>> sql = getSearchQuery(formDataId, formTemplateId, key, isCaseSensitive, manual);
		// get query and params
		String query = sql.getFirst();
		Map<String, Object> params = sql.getSecond();

		// calculate count
		String countQuery = "SELECT COUNT(*) FROM (" + query + ")";
		int count = getNamedParameterJdbcTemplate().queryForObject(countQuery, params, Integer.class);

		List<FormDataSearchResult> dataRows;

		if (count != 0) {
			String dataQuery = "SELECT idx, column_index, row_index, true_val FROM (" + query + ") WHERE idx BETWEEN :from AND :to";
			params.put("from", range.getOffset());
			params.put("to", range.getCount() * 2 - 1);

			dataRows = getNamedParameterJdbcTemplate().query(dataQuery, params, new RowMapper<FormDataSearchResult>() {
				@Override
				public FormDataSearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
					FormDataSearchResult result = new FormDataSearchResult();
					result.setIndex(SqlUtils.getLong(rs, "idx"));
					result.setColumnIndex(SqlUtils.getLong(rs, "column_index"));
					result.setRowIndex(SqlUtils.getLong(rs, "row_index"));
					result.setStringFound(rs.getString("true_val"));
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
	private Pair<String, Map<String, Object>> getSearchQuery(Long formDataId, Integer formTemplateId, String key, boolean isCaseSensitive, boolean manual) {

		String generateSubSqlQuery = "select listagg(row_query, ' ' ) WITHIN GROUP (ORDER BY pos) as query from \n" +
				"(\n" +
				"with fc as (select row_number() over (order by ord) as pos, form_template_id, id, ord, type, numeration_row, precision, parent_column_id from form_column fc where form_template_id = :ftId order by ord)\n" +
				"select fc.pos, fc.form_template_id, case when fc.pos = 1 then '' else ' union all ' end \n" +
				"       || 'select ord as row_index, '||fc.id ||' as column_id, '\n" +
				"       || case when type = 'S' then 'to_char(c'||fc.id||') as raw_value, '\n" +
				"               when type = 'N' then case when fc.precision is null or fc.precision = 0 then 'to_char(c'||fc.id||') as raw_value, '\n" +
				"                 else 'ltrim(to_char(c'||fc.id||',substr(''99999999999999990.0000000000'',1,18+'||fc.precision||'))) as raw_value, ' end\n" +
				"               when type = 'A' then 'to_char((row_number() over(order by ord)) + ' || case when fc.NUMERATION_ROW=0 then 0 else (select number_previous_row from form_data where id = :fdId) end ||') as raw_value,'  \n" +
				"               else ' null as raw_value, ' end\n" +
				"				|| case when (type = 'R' and parent_column_id is null) then 'c'||fc.id||' as reference_id ' \n" +
				"               	when (type = 'R' and parent_column_id is not null) then 'c'||fc.parent_column_id||' as reference_id '\n" +
				"               	else ' null as reference_id ' end " +
				"       || ' from t ' as row_query\n" +
				"from fc\n" +
				")";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fdId", formDataId);
		params.put("ftId", formTemplateId);
		params.put("key", "%" + key + "%");
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", manual);

		String subSql = getNamedParameterJdbcTemplate().queryForObject(generateSubSqlQuery, params, String.class);

		String sql =
				"SELECT row_number() OVER (ORDER BY a.row_index, a.column_index) AS idx, row_index, column_index, true_val\n" +
						"FROM (\n" +
						"  SELECT row_index, fc.ord AS column_index, coalesce(raw_value, d.string_value, o.string_value, z.string_value) AS true_val\n" +
						"  FROM (\n" +
						"    WITH t AS (\n" +
						"        SELECT * FROM form_data_" + formTemplateId + " fd WHERE fd.form_data_id = :fdId and fd.temporary = :temporary and fd.manual = :manual)\n" +
						subSql +
						"        ) hell\n" +
						"  INNER JOIN form_column fc ON fc.id = hell.column_id\n" +
						"  LEFT JOIN ref_book_attribute rba ON rba.id = fc.attribute_id\n" +
						"  LEFT JOIN (\n" +
						"      SELECT dep.id AS record_id, rba.id AS attribute_id, dep.string_value AS string_value\n" +
						"      FROM (\n" +
						"        SELECT id, to_char(NAME) AS NAME, to_char(type) AS type, to_char(shortname) AS shortname, to_char(tb_index) AS tb_index, to_char(sbrf_code) AS sbrf_code, to_char(code) code\n" +
						"        FROM department\n" +
						"        )\n" +
						"      unpivot(string_value FOR alias IN (NAME, type, shortname, tb_index, sbrf_code, code)) dep\n" +
						"      INNER JOIN ref_book_attribute rba ON rba.ref_book_id = 30 AND dep.alias = rba.alias\n" +
						"      ) d ON fc.type = 'R' AND rba.ref_book_id = 30 AND d.record_id = hell.reference_id AND coalesce(fc.attribute_id, fc.attribute_id2) = d.attribute_id\n" +
						"   LEFT JOIN (\n" +
						"      SELECT oktmo.id AS record_id, rba.id AS attribute_id, oktmo.string_value\n" +
						"      FROM (\n" +
						"        SELECT id, to_char(code) AS code, to_char(NAME) AS NAME\n" +
						"        FROM ref_book_oktmo rbo\n" +
						"        )\n" +
						"      unpivot(string_value FOR alias IN (code, NAME)) oktmo\n" +
						"      INNER JOIN ref_book_attribute rba ON rba.ref_book_id = 96 AND oktmo.alias = rba.alias\n" +
						"    ) o ON fc.type = 'R' AND rba.ref_book_id = 96 AND o.record_id = hell.reference_id AND coalesce(fc.attribute_id, fc.attribute_id2) = o.attribute_id\n" +
						"  LEFT JOIN (\n" +
						"      SELECT record_id, attribute_id, coalesce(string_value, TO_CHAR(number_value), TO_CHAR(date_value)) AS string_value\n" +
						"      FROM ref_book_value\n" +
						"      ) z ON fc.type = 'R' AND rba.ref_book_id NOT IN (30, 96) AND z.record_id = hell.reference_id AND coalesce(fc.attribute_id, fc.attribute_id2) = z.attribute_id\n" +
						"  ) a\n" +

						// check case sensitive
						(
								isCaseSensitive ?
										"            WHERE true_val like :key \n" :
										"            WHERE LOWER(true_val) like LOWER(:key) \n"
						);

		return new Pair<String, Map<String, Object>>(sql, params);
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
        }
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public void reorderRows(FormData formData, final List<DataRow<Cell>> rows) {
		if (rows == null || rows.isEmpty()) {
			return;
		}
		// сдвигаем все строки, чтобы обойти ограничение уникального индекса при сортировке
		shiftRows(formData, new DataRowRange(1, rows.size()));
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
		if (log.isTraceEnabled()) {
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().batchUpdate(sql.toString().intern(), params.toArray(new Map[0]));
	}

	@Override
	public boolean isDataRowsCountChanged(FormData formData) {
		// сравниваем кол-во реальных строк с числом, хранящимся в form_data.number_current_row
		StringBuilder sql = new StringBuilder("SELECT (SELECT COUNT(*) FROM form_data_\n");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND alias IS NULL OR alias LIKE '%)\n");
		sql.append(DataRowMapper.ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%')");
		sql.append(" - (SELECT number_current_row FROM form_data WHERE id = :form_data_id) FROM DUAL");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		int difference = getNamedParameterJdbcTemplate().queryForObject(sql.toString().intern(), params, Integer.class);
		return difference != 0;
	}

	@Override
	public void cleanValue(final Collection<Integer> columnIdList) {
		//TODO SBRFACCTAX-11384 написать новый запрос
		/*if (columnIdList == null || columnIdList.isEmpty()) {
            return;
        }
        getJdbcTemplate().update("DELETE FROM data_cell WHERE " + SqlUtils.transformToSqlInStatement("column_id", columnIdList));*/
	}

	@Override
	public void insertRows(FormData formData, int index, List<DataRow<Cell>> rows) {
		int size = getSavedSize(formData);
		if (index < 1 || index > size + 1) {
			throw new IllegalArgumentException(String.format("Вставка записей допустима только в диапазоне индексов [1; %s]. index = %s", size + 1, index));
		}
		// сдвигаем строки
		shiftRows(formData, new DataRowRange(index, rows.size()));
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
		List<Long> ids = bdUtils.getNextDataRowIds(Long.valueOf(rows.size())); // нужно получить отдельным запросом, чтобы актуализировать rows
		int manual = formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode();
		List<Map<String, Object>> params = new ArrayList<Map<String, Object>>(rows.size());
		int i = 0;
		for (DataRow<Cell> row : rows) {
			row.setId(ids.get(i));
			row.setIndex(index + i++);

			Map<String, Object> values = new HashMap<String, Object>();
			values.put("id", row.getId());
			values.put("form_data_id", formData.getId());
			values.put("temporary", DataRowType.SAVED.getCode());
			values.put("manual", manual);
			values.put("ord", row.getIndex());
			values.put("alias", row.getAlias());
			for (Column column : formData.getFormColumns()) {
				String[] names = columnNames.get(column.getId());
				Cell cell = row.getCell(column.getAlias());
				values.put(names[0], cell.getValue());
				values.put(names[1], cell.getStyle() == null ? null : cell.getStyle().getId());
				values.put(names[2], cell.isEditable() ? 1 : 0);
				values.put(names[3], cell.getColSpan());
				values.put(names[4], cell.getRowSpan());
			}
			params.add(values);
		}
		if (log.isTraceEnabled()) {
			log.trace(sql.toString());
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
		if (log.isTraceEnabled()) {
			log.trace("updateRows: " + sql.toString().intern());
		}
		// формируем список параметров для батча
		int manual = formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode();
		List<Map<String, Object>> params = new ArrayList<Map<String, Object>>(rows.size());
		for (DataRow<Cell> row : rows) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("id", row.getId());
			values.put("alias", row.getAlias());
			for (Column column : formData.getFormColumns()) {
				String[] names = columnNames.get(column.getId());
				Cell cell = row.getCell(column.getAlias());
				values.put(names[0], cell.getValue());
				values.put(names[1], cell.getStyle() == null ? null : cell.getStyle().getId());
				values.put(names[2], cell.isEditable() ? 1 : 0);
				values.put(names[3], cell.getColSpan());
				values.put(names[4], cell.getRowSpan());
			}
			params.add(values);
		}
		if (log.isTraceEnabled()) {
			log.trace(sql.toString());
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
		if (log.isTraceEnabled()) {
			log.trace(sql.toString());
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public void removeCheckPoint(FormData formData) {
		// удаляем точку восстановления (временный срез)
		removeRowsInternal(formData, DataRowType.TEMP);
		// актуализируем ссылки на справочники
		refreshRefBookLinks(formData);
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public void createManual(FormData formData) {
		if (!formData.isManual()) {
			throw new IllegalArgumentException("Форма должна иметь признак ручного ввода");
		}
		// удаляет данные
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
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
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowRange range) {
		return getRowsInternal(fd, range);
	}

	@Override
	public int getSavedSize(FormData formData) {
		return getSizeInternal(formData);
	}

	@Override
	public int getSizeWithoutTotal(FormData formData) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND alias IS NULL");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		return getNamedParameterJdbcTemplate().queryForObject(sql.toString().intern(), params, Integer.class);
	}

	/**
	 * @param formData    НФ для которой требуется получить строки
	 * @param range       параметры пейджинга, может быть null
	 * @return список строк, != null
	 */
	private List<DataRow<Cell>> getRowsInternal(FormData formData, DataRowRange range) {
		DataRowMapper dataRowMapper = new DataRowMapper(formData);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql(range);
		if (!isSupportOver()) {
			sql.setFirst(sql.getFirst().replaceAll("OVER \\(PARTITION BY.{0,}ORDER BY ord\\)", "OVER ()"));
		}
		if (log.isTraceEnabled()) {
			log.trace(sql.getSecond());
			log.trace(sql.getFirst());
		}
		return getNamedParameterJdbcTemplate().query(sql.getFirst(), sql.getSecond(), dataRowMapper);
	}

	/**
	 * Возвращает количество строк в налоговой форме, включая итоговые (alias != null)
	 *
	 * @param formData
	 * @return
	 */
	private int getSizeInternal(FormData formData) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
		shiftRows(formData, new DataRowRange(range.getOffset() + range.getCount(), -range.getCount()));
	}

	/**
	 * Сдвигает строки на "range.count" позиций начиная с позиции "range.offset"
	 *
	 * @param formData экземпляр НФ для строк которых осуществляется сдвиг
	 * @param range    диапазон для указания с какого индекса и на сколько осуществляем сдвиг
	 * @return количество смещенных строк
	 */
	private int shiftRows(FormData formData, DataRowRange range) {
		StringBuilder sql = new StringBuilder("UPDATE form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" SET ord = ord + :shift WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND ord >= :offset");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		params.put("offset", range.getOffset());
		params.put("shift", range.getCount());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
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
			withSql.append(" UNION \n SELECT DISTINCT '").append(names.get(0)).append("' AS column_name, ").append(names.get(0)).append(" AS value FROM TAB");
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

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(deleteSql.toString());
			log.trace(insertSql.toString());
		}
		getNamedParameterJdbcTemplate().update(deleteSql.toString(), params);
		getNamedParameterJdbcTemplate().update(insertSql.toString(), params);
	}
}