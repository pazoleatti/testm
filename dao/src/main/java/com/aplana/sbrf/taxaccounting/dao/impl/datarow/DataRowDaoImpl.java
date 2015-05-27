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
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
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

	//TODO SBRFACCTAX-11205 поменять на новую структуру хранения
	@Override
	public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive) {
		Pair<String, Map<String, Object>> sql = getSearchQuery(formDataId, formTemplateId, key, isCaseSensitive);
		// get query and params
		String query = sql.getFirst();
		Map<String, Object> params = sql.getSecond();

		// calculate count
		String countQuery = "select count(*) from (" + query + ")";
		int count = getNamedParameterJdbcTemplate().queryForInt(countQuery, params);

		List<FormDataSearchResult> dataRows;

		if (count != 0) {
			String dataQuery = "SELECT * FROM (" + query + ") WHERE IDX BETWEEN :from AND :to";
			params.put("from", range.getOffset());
			params.put("to", range.getCount() * 2 - 1);

			dataRows = getNamedParameterJdbcTemplate().query(dataQuery, params, new RowMapper<FormDataSearchResult>() {
				@Override
				public FormDataSearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
					FormDataSearchResult result = new FormDataSearchResult();
					result.setIndex(SqlUtils.getLong(rs, "IDX"));
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

	//TODO SBRFACCTAX-11205 поменять на новую структуру хранения
	/**
	 * Метод возвращает пару - строку запроса и параметры
	 *
	 * @return
	 */
	private Pair<String, Map<String, Object>> getSearchQuery(Long formDataId, Integer formTemplateId, String key, boolean isCaseSensitive) {

		String sql =
				"WITH dcell_temp AS (SELECT row_id, column_id, nvalue, svalue, dvalue FROM data_cell WHERE row_id IN (SELECT id FROM data_row WHERE form_data_id=:fdId)), \n" +
						"dcell AS (SELECT row_id, column_id, svalue, dvalue, CASE WHEN fc.type = 'R' and fc.parent_column_id is not NULL THEN \n" +
						"  (SELECT reference_value FROM ref_book_value WHERE attribute_id = fc.attribute_id AND record_id = \n" +
						"   (SELECT nvalue FROM dcell_temp WHERE dcell_temp.column_id = fc.parent_column_id AND dcell_temp.row_id = dc.row_id)) \n" +
						"  ELSE nvalue END AS nvalue \n" +
						"FROM dcell_temp dc JOIN FORM_COLUMN fc ON fc.id=dc.column_id) \n" +
						"SELECT row_number() over (order by row_index, column_index) as IDX, row_index, column_index, true_val \n" +
						"from (SELECT dense_rank()over(order by dr.ord) row_index, dc.ord as column_index, dc.type, val, dc.attribute_id, dc.attribute_id2, \n" +
						"       case when dc.type='R' and (select ref_book_id from ref_book_attribute where id=dc.attribute_id)=30 then \n" +
						"           (case when (select alias from ref_book_attribute where id=dc.attribute_id)='NAME' then (select name from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='PARENT_ID' then (select to_char(parent_id) from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='REGION_ID' then (select to_char(region_id) from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='SBRF_CODE' then (select to_char(sbrf_code) from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='SHORTNAME' then (select to_char(shortname) from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='TB_INDEX' then (select to_char(tb_index) from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='ID' then (select to_char(id) from department where id=val) \n" +
						"               when (select alias from ref_book_attribute where id=dc.attribute_id)='TYPE' then (select to_char(type) from department where id=val) \n" +
						"           else null end ) \n" +
						"       when dc.type='R' then (select coalesce(string_value, TO_CHAR(number_value), TO_CHAR(date_value)) from ref_book_value where record_id=val and attribute_id=coalesce(dc.attribute_id2, dc.attribute_id)) else val end true_val \n" +
						"   from(   SELECT row_id, column_id, TO_CHAR(dvalue) as val FROM dcell \n" +
						"           UNION ALL SELECT row_id, column_id, svalue FROM dcell \n" +
						"           UNION ALL SELECT dcell.row_id, dcell.column_id, \n" +
						"                case when fc.precision is null or fc.precision = 0 then \n" +
						"                    TO_CHAR(dcell.nvalue) \n" +
						"                else \n" +
						"                    ltrim(TO_CHAR(dcell.nvalue,substr('99999999999999999D0000000000',1,18+fc.precision))) \n" +
						"                end as val \n" +
						"            FROM dcell JOIN form_column fc ON fc.id = dcell.column_id \n" +
						"           UNION ALL SELECT row_id, rfc.id as column_id, rsq.val \n" +
						"           FROM (SELECT row_id, column_id, TO_CHAR(nvalue) AS val FROM dcell ) rsq \n" +
						"                 JOIN form_column rfc ON rsq.column_id = rfc.parent_column_id \n" +
						"                   WHERE rfc.form_template_id = :ftId and rfc.type = 'R' and rfc.parent_column_id is not null \n" +
						"           UNION all SELECT data_row.id as row_id, rfc.id as column_id, TO_CHAR( (row_number() over(order by data_row.ord)) + \n" +
						"              case when rfc.NUMERATION_ROW=0 then \n" +
						"                0 \n" +
						"              else \n" +
						"                (select number_previous_row from form_data where form_data.id = :fdId) \n" +
						"              end) as val \n" +
						"            FROM data_row \n" +
						"            JOIN FORM_COLUMN rfc ON rfc.form_template_id = :ftId \n" +
						"            WHERE rfc.form_template_id = :ftId and rfc.type = 'A' and form_data_id= :fdId and data_row.alias is null) d \n" +
						"    RIGHT JOIN ( select id, ord from DATA_ROW where form_data_id=:fdId) dr ON dr.id = d.row_id \n" +
						"    LEFT JOIN FORM_COLUMN dc ON dc.id = d.column_id \n" +
						"    ORDER BY dr.ord \n" +
						") \n" +

						// check case sensitive
						(
								isCaseSensitive ?
										"            WHERE true_val like :key \n" :
										"            WHERE LOWER(true_val) like LOWER(:key) \n"
						) +
						"            ORDER BY row_index, column_index ";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fdId", formDataId);
		params.put("ftId", formTemplateId);
		params.put("key", "%" + key + "%");

		return new Pair<String, Map<String, Object>>(sql.toString(), params);
	}

	@Override
	public void copyRows(long formDataSourceId, long formDataDestinationId) {
		FormData formDataSource = formDataDao.get(formDataSourceId, false);
		FormData formData = formDataDao.get(formDataDestinationId, false);
		// Проверяем, что макеты одинаковые
		if (formData.getFormTemplateId() != formDataSource.getFormTemplateId()) {
			throw new IllegalArgumentException("Макеты НФ должны совпадать");
		}
		// Очистка временного среза НФ-приемника
		removeRows(formData);
		// Копирование данных из постоянного среза источника во временный срез приемника
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
		params.put("temporary", DataRowType.TEMP.getCode());
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
		// считаем кол-во строк в постоянном срезе - кол-во строк во временном срезе.
		StringBuilder sql = new StringBuilder("SELECT\n");
		sql.append("(SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND manual = 0 AND temporary = 1)\n");
		sql.append("-(SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND manual = 0 AND temporary = 0) \nFROM DUAL");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		int difference = getNamedParameterJdbcTemplate().queryForInt(sql.toString().intern(), params);
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
		int tempSize = getTempSize(formData);
		if (index < 1 || index > tempSize + 1) {
			throw new IllegalArgumentException(String.format("Вставка записей допустима только в диапазоне индексов [1; %s]. index = %s", tempSize + 1, index));
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
			values.put("temporary", DataRowType.TEMP.getCode());
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
		// удаляем полностью строки из временного среза
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
		params.put("temporary", DataRowType.TEMP.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public void commit(FormData formData) {
		// удаляем постоянный срез
		removeRowsInternal(formData, DataRowType.SAVED);
		// переносим данные из временного среза
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

		refreshRefBookLinks(formData);
	}

	@Override
	public void rollback(FormData formData) {
		// удаляем временный срез
		removeRows(formData);
	}

	@Override
	public void createTemporary(FormData formData) {
		// удаляет данные временного среза
		removeRows(formData);
		// формируем запрос на копирование среза
		StringBuilder sql = new StringBuilder("INSERT INTO form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" (id, form_data_id, temporary, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append(") \nSELECT seq_form_data_nnn.nextval, form_data_id, 1, manual, ord, alias");
		DataRowMapper.getColumnNamesString(formData, sql);
		sql.append("\nFROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append("\nWHERE form_data_id = :form_data_id AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		getNamedParameterJdbcTemplate().update(sql.toString().intern(), params);
	}

	@Override
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowRange range) {
		return getRowsInternal(fd, DataRowType.SAVED, range);
	}

	@Override
	public int getSavedSize(FormData formData) {
		return getSizeInternal(formData, DataRowType.SAVED);
	}

	@Override
	public List<DataRow<Cell>> getTempRows(FormData fd, DataRowRange range) {
		return getRowsInternal(fd, DataRowType.TEMP, range);
	}

	@Override
	public int getTempSize(FormData formData) {
		return getSizeInternal(formData, DataRowType.TEMP);
	}

	@Override
	public int getTempSizeWithoutTotal(FormData formData) {
		return getSizeWithoutTotalInternal(formData, DataRowType.TEMP);
	}

	/**
	 * @param formData    НФ для которой требуется получить строки
	 * @param isTemporary признак временного среза или постоянного (DataRowType.SAVED - постоянный, DataRowType.TEMP - временный)
	 * @param range       параметры пейджинга, может быть null
	 * @return список строк, != null
	 */
	private List<DataRow<Cell>> getRowsInternal(FormData formData, DataRowType isTemporary, DataRowRange range) {
		if (isTemporary != DataRowType.SAVED && isTemporary != DataRowType.TEMP) {
			throw new IllegalArgumentException("Wrong type of 'isTemporary' argument");
		}
		DataRowMapper dataRowMapper = new DataRowMapper(formData);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql(range, isTemporary);
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
	 * @param isTemporary признак временного среза или постоянного (DataRowType.SAVED - постоянный, DataRowType.TEMP - временный)
	 * @return
	 */
	private int getSizeInternal(FormData formData, DataRowType isTemporary) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", isTemporary.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		return getNamedParameterJdbcTemplate().queryForInt(sql.toString().intern(), params);
	}

	/**
	 * Получить количество строк без учета итоговых (alias != null)
	 *
	 * @param formData
	 * @param isTemporary признак временного среза или постоянного (DataRowType.SAVED - постоянный, DataRowType.TEMP - временный)
	 * @return
	 */
	private int getSizeWithoutTotalInternal(FormData formData, DataRowType isTemporary) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual AND alias IS NULL");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", isTemporary.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		if (log.isTraceEnabled()) {
			log.trace(params);
			log.trace(sql.toString());
		}
		return getNamedParameterJdbcTemplate().queryForInt(sql.toString().intern(), params);
	}

	@Override
	public void removeRows(FormData formData) {
		removeRowsInternal(formData, DataRowType.TEMP);
	}

	/**
	 * Удаляет все строки из временноо\постоянного срезов
	 *
	 * @param formData          НФ
	 * @param stableOrTemporary признак из какого среза удалить строки
	 */
	private void removeRowsInternal(FormData formData, DataRowType stableOrTemporary) {
		if (stableOrTemporary != DataRowType.TEMP && stableOrTemporary != DataRowType.SAVED) {
			throw new IllegalArgumentException("Value of argument 'isTemporary' is incorrect");
		}

		StringBuilder sql = new StringBuilder("DELETE FROM form_data_");
		sql.append(formData.getFormTemplateId());
		sql.append(" WHERE form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", stableOrTemporary.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

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
		params.put("temporary", DataRowType.TEMP.getCode());
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
		params.put("temporary", DataRowType.TEMP.getCode());
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
	private void refreshRefBookLinks(FormData formData) {
		// составляем список справочных граф
		List<String> names = new ArrayList<String>();
		for (Column column : formData.getFormColumns()) {
			if (column.getColumnType() == ColumnType.REFERENCE && ((ReferenceColumn) column).getParentId() == 0) {
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