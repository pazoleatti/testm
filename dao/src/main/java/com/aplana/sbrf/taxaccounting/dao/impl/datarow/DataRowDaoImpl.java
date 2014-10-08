package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Реализация ДАО для работы со строками НФ
 * 
 * @author sgoryachkin
 * 
 */
@Repository
public class DataRowDaoImpl extends AbstractDao implements DataRowDao {

    @Autowired
	BDUtils dbUtils;

	public static final String ERROR_MSG_NO_ROWID = "Невозможно сохранить изменения, так как данные устарели. " +
                                                    "Изменения будут отменены. Переоткройте форму заново.";

	public static final String ERROR_MSG_INDEX = "Индекс %s не входит в допустимый диапазон 1..%s";

	@Override
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowRange range) {
		return physicalGetRows(fd, new TypeFlag[] {TypeFlag.DEL, TypeFlag.SAME}, range);
	}

	@Override
	public int getSavedSize(FormData fd) {
		return physicalGetSize(fd, new TypeFlag[]{TypeFlag.DEL, TypeFlag.SAME});
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowRange range) {
		return physicalGetRows(fd, new TypeFlag[] {TypeFlag.ADD, TypeFlag.SAME}, range);
	}

	@Override
	public int getSize(FormData fd) {
		return physicalGetSize(fd, new TypeFlag[]{TypeFlag.ADD, TypeFlag.SAME});
	}

    @Override
    public int getSizeWithoutTotal(FormData formData) {
        return physicalGetSizeWithoutTotal(formData,
                new TypeFlag[]{TypeFlag.ADD, TypeFlag.SAME});
    }

    @Override
	public void updateRows(FormData fd, Collection<DataRow<Cell>> rows) {
		// Если строка помечена как ADD, необходимо обновление
		// Если строка помечена как SAME, то помечаем её как DEL создаем новую с
		// тем же значением ORD
		List<DataRow<Cell>> forUpdate = new ArrayList<DataRow<Cell>>();
		List<DataRow<Cell>> forCreate = new ArrayList<DataRow<Cell>>();
		List<Long> forCreateOrder = new ArrayList<Long>();

		List<Long> dataRowIds = new ArrayList<Long>(rows.size());
		for (DataRow<Cell> dataRow : rows) {
			dataRowIds.add(dataRow.getId());
		}
		Map<Long, Pair<Integer, Long>> typeAndOrds = getTypeAndOrdById(fd.getId(), dataRowIds);

		for (DataRow<Cell> dataRow : rows) {
			Pair<Integer, Long> typeAndOrd = typeAndOrds.get(dataRow.getId());
			if (TypeFlag.ADD.getKey() == typeAndOrd.getFirst()) {
				forUpdate.add(dataRow);
			} else {
				forCreate.add(dataRow);
				forCreateOrder.add(typeAndOrd.getSecond());
			}
		}
		// удаляем старые ячейки
		batchRemoveCells(forUpdate);
		// создаем новые ячейки
		batchInsertCells(forUpdate);
		// помечаем старые строки на удаление, так как обновление = удаление(type=-1) + создание(type=1)
		physicalUpdateRowsType(forCreate, TypeFlag.DEL);
		// создает новые строки с указанием порядка forCreateOrder
		physicalInsertRows(fd, forCreate, null, null, forCreateOrder);

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
	public void removeRows(final FormData formData, final int idxFrom, final int idxTo) {
		checkIndexesRange(formData, false, false, idxFrom, idxTo);
		if (idxTo < idxFrom) {
			throw new IllegalArgumentException(
					"Индекс начального элемента меньше индекса конечного");
		}
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		String idsSQL = "select ID from (select row_number() over (order by ORD) as IDX, ID, TYPE from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId) RR where IDX between :from and :to";
        if (!isSupportOver()){
            idsSQL = idsSQL.replaceFirst("over \\(order by ORD\\)", "over ()");
        }

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("types", TypeFlag.rtsToKeys(new TypeFlag[] { TypeFlag.ADD,
				TypeFlag.SAME }));
		params.put("formDataId", formData.getId());
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
	public void removeRows(FormData formData) {
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		getJdbcTemplate().update(
				"DELETE FROM data_row WHERE form_data_id=? AND type=? AND manual = ?",
				new Object[] { formData.getId(), TypeFlag.ADD.getKey(), formData.isManual() ? 1 : 0 });
		getJdbcTemplate().update(
                "UPDATE data_row SET type=? WHERE form_data_id=? AND type=? AND manual = ?",
                new Object[]{TypeFlag.DEL.getKey(), formData.getId(),
                        TypeFlag.SAME.getKey(), formData.isManual() ? 1 : 0});
	}

	@Override
	public void saveRows(final FormData formData, final List<DataRow<Cell>> dataRows) {
		// Полностью чистим временный срез строк.
		removeRows(formData);
		// Вставляем строки
		physicalInsertRows(formData, dataRows,
                0l,
                DataRowDaoImplUtils.DEFAULT_ORDER_STEP, null);
	}

	@Override
	public void insertRows(FormData formData, int index, List<DataRow<Cell>> rows) {
		checkIndexesRange(formData, false, true, index);
		index--;
		Long ordBegin = getOrd(formData.getId(), index);
		if (ordBegin == null) {
			ordBegin = 0l;
		}
		insertRows(formData, index, ordBegin, rows);
	}

	@Override
	public void insertRows(FormData formData, DataRow<Cell> afterRow,
			List<DataRow<Cell>> rows) {
		Pair<Long, Integer> ordAndIndex = getOrdAndIndex(formData.getId(), afterRow.getId());
		insertRows(formData, ordAndIndex.getSecond(), ordAndIndex.getFirst(), rows);
	}

	@Override
	public void commit(long formDataId) {
		physicalRemoveRows(formDataId, TypeFlag.DEL);
		physicalUpdateRowsType(formDataId, TypeFlag.ADD, TypeFlag.SAME);
	}

	@Override
	public void rollback(long formDataId) {
		physicalRemoveRows(formDataId, TypeFlag.ADD);
		physicalUpdateRowsType(formDataId, TypeFlag.DEL, TypeFlag.SAME);
	}

	/**
	 * Создаем строки во временном срезе
	 * @param formData
	 * @param index
	 * @param ordBegin
	 * @param rows
	 */
	private void insertRows(FormData formData, int index, long ordBegin, List<DataRow<Cell>> rows) {
		Long ordEnd = getOrd(formData.getId(), index + 1);
		long ordStep = ordEnd == null ?
			DataRowDaoImplUtils.DEFAULT_ORDER_STEP :
			DataRowDaoImplUtils.calcOrdStep(ordBegin, ordEnd, rows.size());
		if (ordStep == 0) {
			/*Реализация перепаковки поля ORD. Слишком маленькие значения ORD. В промежуток нельзя вставить
			такое количество строк*/
            long diff = 5 * rows.size(); //minimal diff between rows
            int endIndex = physicalGetSize(formData, new TypeFlag[]{TypeFlag.DEL, TypeFlag.ADD, TypeFlag.SAME});
            /* Делаем так, чтобы пересортировать строки в один запрос. Для этого сначала выбираем временную таблицу с индексами (RR)
             *  затем выбираем индексы начиная с того после которого надо вставить и до самого конца.
             *  Прибавляем ровно ту разницу, которая необходима для вставки строк.
             */
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("diff", diff);
            map.put("types", Arrays.asList(TypeFlag.DEL.getKey(), TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
            map.put("formDataId", formData.getId());
            map.put("dataStartRowIndex", (long) (index + 1));
            map.put("dataEndRowIndex", (long) endIndex);
            String sql = "update DATA_ROW set ORD = ORD + :diff where ID in" +
                    "(select RR.ID from " +
                    "(select row_number() OVER (ORDER BY dr.ord) as IDX, DR.ID, DR.ORD from DATA_ROW DR where DR.TYPE in (:types) and FORM_DATA_ID=:formDataId) " +
                    "RR where RR.IDX between (:dataStartRowIndex) and (:dataEndRowIndex))";
            if (!isSupportOver()){ // для юнит-тестов (hsql db)
                sql = sql.replaceFirst("OVER \\(ORDER BY dr.ord\\)", "over ()");
            }
            getNamedParameterJdbcTemplate().update(sql, map);
            ordEnd = getOrd(formData.getId(), index + 1);
            ordStep = DataRowDaoImplUtils
                    .calcOrdStep(ordBegin, ordEnd, rows.size());
		}
		physicalInsertRows(formData, rows, ordBegin, ordStep, null);
	}

	private void physicalRemoveRows(long formDataId, TypeFlag type) {
		getJdbcTemplate().update(
                "delete from DATA_ROW where FORM_DATA_ID = ? and TYPE = ?",
                formDataId, type.getKey());
	}

	private void physicalUpdateRowsType(long formDataId, TypeFlag fromType,
                                        TypeFlag toType) {
		getJdbcTemplate()
				.update("update DATA_ROW set TYPE = ? where FORM_DATA_ID = ? and TYPE = ?",
						toType.getKey(), formDataId, fromType.getKey());
	}

	private void physicalUpdateRowsType(final List<DataRow<Cell>> dataRows, final TypeFlag toType) {

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

	private List<DataRow<Cell>> physicalGetRows(FormData formData, TypeFlag[] types, DataRowRange range) {
		DataRowMapper dataRowMapper = new DataRowMapper(formData, types, range);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql();

        if(!isSupportOver()){
            sql.first = sql.getFirst().replaceAll("OVER \\(ORDER BY sub.ord\\)", "OVER ()");
        }

		List<DataRow<Cell>> dataRows = getNamedParameterJdbcTemplate().query(
				sql.getFirst(), sql.getSecond(), dataRowMapper);

		return dataRows;
	}

	/**
	 * Возвращает количество строк в налоговой форме, включая итоговые (alias != null)
	 * @param formData
	 * @param types
	 * @return
	 */
	private int physicalGetSize(FormData formData, TypeFlag[] types) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formData.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
        params.put("manual", formData.isManual() ? 1 : 0);
		return getNamedParameterJdbcTemplate().queryForInt(
			"SELECT COUNT(id) FROM data_row WHERE form_data_id = :formDataId AND type IN (:types) AND manual = :manual",
			params);
	}

    /**
     * Получить количество строк без учета итоговых
     * @param formData
     * @param types
     * @return
     */
    private int physicalGetSizeWithoutTotal(FormData formData, TypeFlag[] types) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("formDataId", formData.getId());
        params.put("types", TypeFlag.rtsToKeys(types));
        params.put("manual", formData.isManual() ? 1 : 0);
        return getNamedParameterJdbcTemplate().queryForInt(
			"SELECT COUNT(id) FROM data_row WHERE form_data_id = :formDataId AND type IN (:types) AND manual = :manual AND alias IS NULL ",
			params);
    }

    private void physicalInsertRows(final FormData formData,
                                    final List<DataRow<Cell>> dataRows, final Long ordBegin,
                                    final Long ordStep, final List<Long> orders) {
        if (dataRows.isEmpty()) {
            return;
        }

        if (DataRowDaoImplUtils.hasDuplicates(dataRows)){
            throw new IllegalArgumentException("Дубликаты строк не допустимы в списке. Дубликаты - ссылки на один и тот же объект DataRow");
        }
        // получение id'шников для вставки строк батчем
        final List<Long> ids = dbUtils.getNextDataRowIds(Integer.valueOf(dataRows.size()).longValue());
        getJdbcTemplate().batchUpdate(
                "INSERT INTO data_row (id, form_data_id, alias, ord, type, manual) VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, ids.get(i));

                        ps.setLong(2, formData.getId());

                        DataRow<Cell> drow = dataRows.get(i);
                        String rowAlias = drow.getAlias();
                        ps.setString(3, rowAlias);

                        Long order;
                        if (orders != null) {
                            order = orders.get(i);
                        } else {
                            order = (i + 1) * ordStep + ordBegin;
                        }
                        ps.setLong(4, order);

                        ps.setInt(5, TypeFlag.ADD.getKey());
                        ps.setBoolean(6, formData.isManual());
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                });
        final Iterator<DataRow<Cell>> iterator = dataRows.iterator();
        for (Long id : ids) {
            iterator.next().setId(id);
        }
        batchInsertCells(dataRows);
    }

	/**
	 * Метод получает значение ORD для строки по индексу. Метод работает со временным срезом формы
	 * 
	 * @param formDataId
	 * @param dataRowIndex
	 * @return
	 */
	private Long getOrd(long formDataId, int dataRowIndex) {
		String sql = "select ORD from (select row_number() over (order by ORD) as IDX, ORD from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId) RR where IDX = :dataRowIndex";
        if (!isSupportOver()){
            sql = sql.replaceFirst("over \\(order by ORD\\)", "over ()");
        }
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formDataId);
		params.put("types", Arrays.asList(TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
		params.put("dataRowIndex", dataRowIndex);
		List<Long> list = getNamedParameterJdbcTemplate().queryForList(sql, params, Long.class);
		return list.isEmpty() ? null : DataAccessUtils.requiredSingleResult(list);
	}

	/**
	 * Метод получает пару: ORD и INDEX. Метод работает со временным срезом формы
	 * 
	 * @param formDataId
	 * @param dataRowId
	 * @return
	 */
	private Pair<Long, Integer> getOrdAndIndex(long formDataId, Long dataRowId) {
		String sql = "SELECT ord, idx FROM (SELECT ROW_NUMBER() OVER (ORDER BY ord) AS idx, ord, id FROM data_row WHERE type IN (:types) AND form_data_id=:formDataId) rr WHERE id = :dataRowId";
        if (!isSupportOver()){
            sql = sql.replaceFirst("OVER \\(ORDER BY ord\\)", "OVER ()");
        }
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formDataId);
		params.put("types",
				Arrays.asList(TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
		params.put("dataRowId", dataRowId);
		try {
			return DataAccessUtils.requiredSingleResult(getNamedParameterJdbcTemplate()
							.query(sql, params,
									new RowMapper<Pair<Long, Integer>>() {
										@Override
										public Pair<Long, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
											return new Pair<Long, Integer>(
													SqlUtils.getLong(rs,"ord"),
													SqlUtils.getInteger(rs, "idx"));
										}
									}));
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(ERROR_MSG_NO_ROWID, dataRowId, formDataId);
		}
	}

	/**
	 * Метод получает пару: TYPE и ORD. Метод работает со временным срезом формы
	 * 
	 */
	private Map<Long, Pair<Integer, Long>> getTypeAndOrdById(long formDataId, List<Long> dataRowIds) {
        String sql = "SELECT type, ord, id FROM data_row WHERE TYPE IN (:types) AND form_data_id = :formDataId AND ";
		final Map<Long, Pair<Integer, Long>> result = new HashMap<Long, Pair<Integer, Long>>();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("formDataId", formDataId);
        params.addValue("types", Arrays.asList(TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
        if (dataRowIds.size() < 1000) {
            params.addValue("dataRowIds", dataRowIds);
            sql += "id IN (:dataRowIds)";
        } else {
            sql += SqlUtils.transformToSqlInStatement("id", dataRowIds);
        }

        getNamedParameterJdbcTemplate()
			.query(sql,
					params,
					new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs) throws SQLException {
							Integer type = SqlUtils.getInteger(rs, "type");
							Long ord = SqlUtils.getLong(rs, "ord");
							Long id = SqlUtils.getLong(rs, "id");
							result.put(id, new Pair<Integer, Long>(type, ord));
						}
					}
			);
		if (result.size() != dataRowIds.size()) {
			throw new DaoException(ERROR_MSG_NO_ROWID);
		}
		return result;
	}

	/**
	 * Проверяем диапазон значений для индексов. Причем для индексов в режиме добавления новых записей диапазон
	 * на единицу больше, так как может потребоваться вставка после последней строки.
	 * @param formData экземпляр налоговой формы
	 * @param saved выбор срезов. true - обычный, false - временный
	 * @param forNew режим проверки
	 * @param indexes проверяемые индексы
	 */
	private void checkIndexesRange(FormData formData, boolean saved, boolean forNew,
			int... indexes) {
		int size = saved ? getSavedSize(formData) : getSize(formData);
		int lastIndex = forNew ? size + 1 : size;
		for (int index : indexes) {
			if (index < 1 || index > lastIndex) {
				throw new IllegalArgumentException(String.format(
						ERROR_MSG_INDEX, index, lastIndex));
			}
		}
	}

	/**
	 * Метод сохраняет параметры Cell
	 * 
	 * @param dataRows
	 */
	private void batchInsertCells(List<DataRow<Cell>> dataRows) {
		List<Object[]> batchList = new ArrayList<Object[]>();
		for (DataRow<Cell> dataRow : dataRows) {
			for (String alias : dataRow.keySet()) {
				Cell cell = dataRow.getCell(alias);
				Column column = cell.getColumn();

				Object svalue = null, dvalue = null, nvalue = null;
				Object val = cell.getValue();
				if (val != null && column.getColumnType() != null) {
					switch(column.getColumnType()) {
						case STRING:
							svalue = val;
							break;
						case DATE:
							dvalue = val;
							break;
						default:
							nvalue = val;
					}
				}
				FormStyle style = cell.getStyle();
				batchList.add(new Object[] {dataRow.getId(), column.getId(), svalue, nvalue, dvalue,
						style == null ? null : style.getId(),
						cell.isEditable() ? 1 : null,
						cell.getColSpan() == 1 ? null : cell.getColSpan(),
						cell.getRowSpan() == 1 ? null : cell.getRowSpan()});
			}
		}
		if (!batchList.isEmpty()) {
			getJdbcTemplate().batchUpdate(
				" INSERT INTO data_cell (row_id, column_id, svalue, nvalue, dvalue, style_id, editable, colspan, rowspan) " +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", batchList);
		}
	}

    @Override
    public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive) {
        Pair<String, Map<String, Object>> sql = getSearchQuery(formDataId, formTemplateId, key, isCaseSensitive);
        // get query and params
        String query = sql.getFirst();
        Map<String, Object> params = sql.getSecond();

        // calculate count
        String countQuery = "select count(*) from ("+query+")";
        int count = getNamedParameterJdbcTemplate().queryForInt(countQuery, params);

        String dataQuery = "SELECT * FROM ("+query+") WHERE IDX BETWEEN :from AND :to";
        params.put("from", range.getOffset());
        params.put("to", range.getLimit() + range.getLimit() - 1);

        List<FormDataSearchResult> dataRows = getNamedParameterJdbcTemplate().query(dataQuery, params, new RowMapper<FormDataSearchResult>() {
            @Override
            public FormDataSearchResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                FormDataSearchResult result = new FormDataSearchResult();
                result.setIndex(SqlUtils.getLong(rs,"IDX"));
                result.setColumnIndex(SqlUtils.getLong(rs,"column_index"));
                result.setRowIndex(SqlUtils.getLong(rs,"row_index"));
                result.setStringFound(rs.getString("true_val"));

                return result;
            }
        });

        PagingResult<FormDataSearchResult> pagingResult = new PagingResult<FormDataSearchResult>(dataRows, count);

        return pagingResult;
    }

    @Override
    public boolean isDataRowsCountChanged(long formId) {
        String sql = "select sum(type) from data_row where form_data_id = :formId";
        SqlParameterSource sqlParameters = new MapSqlParameterSource().addValue("formId", formId);
        int difference = getNamedParameterJdbcTemplate().queryForInt(sql, sqlParameters);
        return difference != 0;
    }

    /**
     * Метод возвращает пару - строку запроса и параметры
     * @return
     */
    private Pair<String, Map<String, Object>> getSearchQuery(Long formDataId, Integer formTemplateId, String key, boolean isCaseSensitive){

        String sql =
				"WITH dcell AS (SELECT row_id, column_id, nvalue, svalue, dvalue FROM data_cell WHERE row_id IN (SELECT id FROM data_row WHERE form_data_id=:fdId)) \n" +
                "SELECT row_number() over (order by row_index, column_index) as IDX, row_index, column_index, true_val \n" +
                "from (SELECT dense_rank()over(order by dr.ord) row_index, dc.ord as column_index, dc.type, val, dc.attribute_id, dc.attribute_id2, \n" +
                "       case when dc.type='R' and (select ref_book_id from ref_book_attribute where id=dc.attribute_id)=30 then \n"+
                "           (case when (select alias from ref_book_attribute where id=dc.attribute_id)='NAME' then (select name from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='PARENT_ID' then (select to_char(parent_id) from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='REGION_ID' then (select to_char(region_id) from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='SBRF_CODE' then (select to_char(sbrf_code) from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='SHORTNAME' then (select to_char(shortname) from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='TB_INDEX' then (select to_char(tb_index) from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='ID' then (select to_char(id) from department where id=val) \n"+
                "               when (select alias from ref_book_attribute where id=dc.attribute_id)='TYPE' then (select to_char(type) from department where id=val) \n"+
                "           else null end ) \n"+
                "       when dc.type='R' then (select coalesce(string_value, TO_CHAR(number_value), TO_CHAR(date_value)) from ref_book_value where record_id=val and attribute_id=coalesce(dc.attribute_id2, dc.attribute_id)) else val end true_val \n"+
                "   from(   SELECT row_id, column_id, TO_CHAR(dvalue) as val FROM dcell \n"+
                "           UNION ALL SELECT row_id, column_id, svalue FROM dcell \n"+
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
                "    RIGHT JOIN ( select id, ord from DATA_ROW where form_data_id=:fdId) dr ON dr.id = d.row_id \n"+
                "    LEFT JOIN FORM_COLUMN dc ON dc.id = d.column_id \n"+
                "    ORDER BY dr.ord \n" +
                ") \n" +

        // check case sensitive
                    (
                            isCaseSensitive ?
                    "            WHERE true_val like :key \n":
                    "            WHERE LOWER(true_val) like LOWER(:key) \n"
                    )+
                    "            ORDER BY row_index, column_index ";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fdId", formDataId);
        params.put("ftId", formTemplateId);
        params.put("key", "%"+key+"%");

        return new Pair<String, Map<String, Object>>(sql.toString(), params);
    }

    private void batchRemoveCells(final List<DataRow<Cell>> dataRows) {
		if (!dataRows.isEmpty()) {
			final List<Number> idList = new ArrayList<Number>(dataRows.size());
			for (DataRow row : dataRows) {
				idList.add(row.getId());
			}
            String sql = "DELETE FROM data_cell WHERE " + SqlUtils.transformToSqlInStatement("row_id", idList);
            getJdbcTemplate().update(sql);
		}
	}

    @Override
    public void cleanValue(final Collection<Integer> columnIdList) {
        if (columnIdList == null || columnIdList.isEmpty()) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<String, Object>() {{put("ids", columnIdList);}};
        getNamedParameterJdbcTemplate().update("DELETE FROM data_cell WHERE column_id IN (:ids)", paramMap);
    }
}
