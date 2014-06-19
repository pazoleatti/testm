package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
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
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return physicalGetRows(fd,
				new TypeFlag[] {TypeFlag.DEL, TypeFlag.SAME}, filter, range);
	}

	@Override
	public int getSavedSize(FormData fd, DataRowFilter filter) {
		return physicalGetSize(fd,
                new TypeFlag[]{TypeFlag.DEL, TypeFlag.SAME}, filter);
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return physicalGetRows(fd,
				new TypeFlag[] {TypeFlag.ADD, TypeFlag.SAME}, filter, range);
	}

	@Override
	public int getSize(FormData fd, DataRowFilter filter) {
		return physicalGetSize(fd,
                new TypeFlag[]{TypeFlag.ADD, TypeFlag.SAME}, filter);
	}

    @Override
    public int getSizeWithoutTotal(FormData formData, DataRowFilter filter) {
        return physicalGetSizeWithoutTotal(formData,
                new TypeFlag[]{TypeFlag.ADD, TypeFlag.SAME}, filter);
    }

    @Override
	public void updateRows(FormData fd, Collection<DataRow<Cell>> rows) {
		// Если строка помечена как ADD, необходимо обновление
		// Если строка помечена как SAME, то помечаем её как DEL создаем новую с
		// тем же значением ORD
		List<DataRow<Cell>> forUpdate = new ArrayList<DataRow<Cell>>();
		List<DataRow<Cell>> forCreate = new ArrayList<DataRow<Cell>>();
		List<Long> forCreateOrder = new ArrayList<Long>();
        //TODO Заменить цикл на вызов ДАО
		for (DataRow<Cell> dataRow : rows) {
			Long id = dataRow.getId();
			Pair<Integer, Long> typeAndOrd = getTypeAndOrdById(fd.getId(), id);
			if (TypeFlag.ADD.getKey() == typeAndOrd.getFirst()) {
				forUpdate.add(dataRow);
			} else {
				forCreate.add(dataRow);
				forCreateOrder.add(typeAndOrd.getSecond());
			}
		}

		batchRemoveCells(forUpdate);
		batchInsertCells(forUpdate);

		physicalUpdateRowsType(fd, forCreate, TypeFlag.DEL);
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
				"delete from DATA_ROW where FORM_DATA_ID=? and TYPE=? and MANUAL = ?",
				new Object[] { formData.getId(), TypeFlag.ADD.getKey(), formData.isManual() ? 1 : 0 });
		getJdbcTemplate().update(
                "update DATA_ROW set TYPE=? where FORM_DATA_ID=? and TYPE=? and MANUAL = ?",
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
		Pair<Long, Integer> ordAndIndex = getOrdAndIndex(formData.getId(),
				afterRow.getId());
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

	private void insertRows(FormData formData, int index, long ordBegin,
			List<DataRow<Cell>> rows) {
		Long ordEnd = getOrd(formData.getId(), index + 1);
		long ordStep = ordEnd == null ? DataRowDaoImplUtils.DEFAULT_ORDER_STEP
				: DataRowDaoImplUtils
						.calcOrdStep(ordBegin, ordEnd, rows.size());
		if (ordStep == 0) {
			/*Реализовация перепаковки поля ORD. Слишком маленькие значения ORD. В промежуток нельзя вставить
			такое количество строк*/
            long diff = 5 * rows.size(); //minimal diff between rows
            int endIndex = physicalGetSize(formData,
                    new TypeFlag[]{TypeFlag.DEL, TypeFlag.ADD, TypeFlag.SAME}, null);

            /* Делаем так чтобы пересортировать колонки в один запрос. Для этого сначала выбираем временную таблицу с индексами (RR)
             *  затем выбираем индексы начиная с того после которого надо вставить и до самого конца.
             *  Прибавляем ровно ту разницу, котрая необходима для вставки строк.
             */
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("diff", diff);
            map.put("types", Arrays.asList(TypeFlag.DEL.getKey(), TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
            map.put("formDataId", formData.getId());
            map.put("dataStartRowIndex", (long) (index + 1));
            map.put("dataEndRowIndex", (long) endIndex);
            String sql = "update DATA_ROW set ORD = ORD + :diff where ID in" +
                    "(select RR.ID from " +
                    "(select row_number() over (order by DR.ORD) as IDX, DR.ID, DR.ORD from DATA_ROW DR where DR.TYPE in (:types) and FORM_DATA_ID=:formDataId) " +
                    "RR where RR.IDX between (:dataStartRowIndex) and (:dataEndRowIndex))";
            if (!isSupportOver()){ // для юнит-тестов (hsql db)
                sql = sql.replaceFirst("over \\(order by DR.ORD\\)", "over ()");
            }
            getNamedParameterJdbcTemplate().update(
                    sql, map
            );
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

	private void physicalUpdateRowsType(FormData formData,
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

	private List<DataRow<Cell>> physicalGetRows(FormData formData, TypeFlag[] types,
												DataRowFilter filter, DataRowRange range) {
		DataRowMapper dataRowMapper = new DataRowMapper(formData, types, filter,
				range);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql();

        if(!isSupportOver()){
            sql.first = sql.getFirst().replaceFirst("over \\(order by sub.ORD\\)", "over ()");
        }

		List<DataRow<Cell>> dataRows = getNamedParameterJdbcTemplate().query(
				sql.getFirst(), sql.getSecond(), dataRowMapper);
		// SBRFACCTAX-2082
		// FormDataUtils.setValueOners(dataRows);
		return dataRows;
	}

	private int physicalGetSize(FormData formData, TypeFlag[] types,
                                DataRowFilter filter) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formData.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
        params.put("manual", formData.isManual() ? 1 : 0);
		return getNamedParameterJdbcTemplate()
				.queryForInt(
						"select count(ID) from DATA_ROW where FORM_DATA_ID = :formDataId and TYPE in (:types) and manual = :manual",
						params);
	}

    /**
     * Получить количество строк без учета итоговых
     * @param formData
     * @param types
     * @param filter
     * @return
     */
    private int physicalGetSizeWithoutTotal(FormData formData, TypeFlag[] types,
                                DataRowFilter filter) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("formDataId", formData.getId());
        params.put("types", TypeFlag.rtsToKeys(types));
        params.put("manual", formData.isManual() ? 1 : 0);
        return getNamedParameterJdbcTemplate()
                .queryForInt(
                        "select count(ID) from DATA_ROW where FORM_DATA_ID = :formDataId and TYPE in (:types) and manual = :manual AND alias IS NULL ",
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
                "insert into DATA_ROW (ID, FORM_DATA_ID, ALIAS, ORD, TYPE, MANUAL) values (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, ids.get(i));

                        ps.setLong(2, formData.getId());

                        DataRow<Cell> drow = dataRows.get(i);
                        String rowAlias = drow.getAlias();
                        ps.setString(3, rowAlias);

                        Long order = null;
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
		List<Long> list = getNamedParameterJdbcTemplate().queryForList(sql,
				params, Long.class);
		return list.isEmpty() ? null : DataAccessUtils
				.requiredSingleResult(list);
	}

	/**
	 * Метод получает пару: ORD и INDEX. Метод работает со временным срезом формы
	 * 
	 * @param formDataId
	 * @param dataRowId
	 * @return
	 */
	private Pair<Long, Integer> getOrdAndIndex(long formDataId, Long dataRowId) {
		String sql = "select ORD, IDX from (select row_number() over (order by ORD) as IDX, ORD, ID from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId) RR where ID = :dataRowId";
        if (!isSupportOver()){
            sql = sql.replaceFirst("over \\(order by ORD\\)", "over ()");
        }
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formDataId);
		params.put("types",
				Arrays.asList(TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
		params.put("dataRowId", dataRowId);
		try {
			return DataAccessUtils
					.requiredSingleResult(getNamedParameterJdbcTemplate()
							.query(sql, params,
									new RowMapper<Pair<Long, Integer>>() {
										@Override
										public Pair<Long, Integer> mapRow(
												ResultSet rs, int rowNum)
												throws SQLException {
											return new Pair<Long, Integer>(SqlUtils
                                                    .getLong(rs,"ORD"), SqlUtils
                                                    .getInteger(rs, "IDX"));
										}
									}));
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(ERROR_MSG_NO_ROWID, dataRowId, formDataId);
		}
	}

	/**
	 * Метод получает пару: TYPE и ORD. Метод работает со временным срезом формы
	 * 
	 * @param formDataId
	 * @param dataRowId
	 * @return
	 */
	private Pair<Integer, Long> getTypeAndOrdById(long formDataId,
			Long dataRowId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formDataId);
		params.put("types",
				Arrays.asList(TypeFlag.ADD.getKey(), TypeFlag.SAME.getKey()));
		params.put("dataRowId", dataRowId);

		try {
			return DataAccessUtils
					.requiredSingleResult(getNamedParameterJdbcTemplate()
							.query("select TYPE, ORD, ID from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId and ID = :dataRowId",
									params,
									new RowMapper<Pair<Integer, Long>>() {

										@Override
										public Pair<Integer, Long> mapRow(
												ResultSet rs, int rowNum)
												throws SQLException {
											return new Pair<Integer, Long>(SqlUtils
                                                    .getInteger(rs,"TYPE"), SqlUtils
                                                    .getLong(rs,"ORD"));
										}
									}));
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(ERROR_MSG_NO_ROWID, dataRowId, formDataId);
		}
	}

	private void checkIndexesRange(FormData formData, boolean saved, boolean forNew,
			int... indexes) {
		int size = saved ? getSavedSize(formData, null) : getSize(formData, null);
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

    @Override
    public PagingResult<FormDataSearchResult> searchByKey(Long formDataId, Integer formTemplateId, DataRowRange range, String key, boolean isCaseSensitive) {
        Pair<String, Map<String, Object>> sql = getSearchQuery(formDataId, formTemplateId, key, isCaseSensitive);
        // get query and params
        String query = sql.getFirst();
        Map<String, Object> params = sql.getSecond();

        // calculate count
        String countQuery = "select count(*) from ("+query+")";
        int count = getNamedParameterJdbcTemplate().queryForInt(countQuery, params);

        String dataQuery = "select * from ("+query+") WHERE IDX between :from and :to";
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

    /**
     * Метод возвращает пару - строку запроса и параметры
     * @return
     */
    private Pair<String, Map<String, Object>> getSearchQuery(Long formDataId, Integer formTemplateId, String key, boolean isCaseSensitive){

        StringBuffer sql = new StringBuffer();
        sql.append(
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
                "   from(   SELECT row_id, column_id, TO_CHAR(value) as val \n"+
                "           FROM DATE_VALUE WHERE row_id in (select id from data_row where form_data_id=:fdId) \n"+
                "           UNION all SELECT row_id, column_id, TO_CHAR(value) as val FROM STRING_VALUE WHERE row_id in (select id from data_row where form_data_id=:fdId) \n"+
                "           UNION all SELECT nv.row_id, nv.column_id, \n" +
                "                case when fc.precision is null or fc.precision = 0 then \n" +
                "                    TO_CHAR(nv.value) \n" +
                "                else \n" +
                "                    ltrim(TO_CHAR(nv.value,substr('99999999999999999D0000000000',1,18+fc.precision))) \n" +
                "                end as val \n" +
                "            FROM NUMERIC_VALUE nv join form_column fc on fc.id = nv.column_id WHERE row_id in (select id from data_row where form_data_id=:fdId) \n" +
                "           UNION all SELECT row_id, rfc.id as column_id, rsq.val \n" +
                "           from (  SELECT row_id, column_id, TO_CHAR(value) as val \n" +
                "                   FROM NUMERIC_VALUE WHERE row_id in (select id from data_row where form_data_id=:fdId) ) rsq \n" +
                "                       join FORM_COLUMN rfc on rsq.column_id = rfc.parent_column_id \n" +
                "                   where rfc.form_template_id = :ftId and rfc.type = 'R' and rfc.parent_column_id is not null) d \n" +
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

                    "            ORDER BY row_index, column_index "
        );

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fdId", formDataId);
        params.put("ftId", formTemplateId);
        params.put("key", "%"+key+"%");

        return new Pair<String, Map<String, Object>>(sql.toString(), params);
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
