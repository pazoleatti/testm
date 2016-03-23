package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.AutoNumerationColumn;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.ColumnType;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.NumerationType;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class ColumnDaoImpl extends AbstractDao implements ColumnDao {

	private static final Log LOG = LogFactory.getLog(ColumnDaoImpl.class);

    @Autowired
    private BDUtils bdUtils;
    @Autowired
    private RefBookDao refBookDao;

	private class ColumnMapper implements RowMapper<Column> {
		@Override
        public Column mapRow(ResultSet rs, int index) throws SQLException {
			final Column result;
			String type = rs.getString("type");
			if ("N".equals(type)) {
				result = new NumericColumn();
				((NumericColumn)result).setPrecision(SqlUtils.getInteger(rs, "precision"));
				((NumericColumn) result).setMaxLength(SqlUtils.getInteger(rs, "max_length"));
			} else if ("D".equals(type)) {
				result = new DateColumn();
				((DateColumn) result).setFormatId(SqlUtils.getInteger(rs, "format"));
			} else if ("S".equals(type)) {
				result = new StringColumn();
				((StringColumn) result).setMaxLength(SqlUtils.getInteger(rs, "max_length"));
                ((StringColumn) result).setFilter(rs.getString("filter"));
			} else if ("R".equals(type)) {
                Long attributeId = SqlUtils.getLong(rs, "attribute_id");
                Long attributeId2 = SqlUtils.getLong(rs, "attribute_id2");
                if (rs.wasNull()) {
                    attributeId2 = null;
                }
                Integer parentColumnId = SqlUtils.getInteger(rs, "parent_column_id");
                String filter = rs.getString("filter");
                if (parentColumnId == null) {
                    result = new RefBookColumn();
                    ((RefBookColumn) result).setRefBookAttributeId1(attributeId);
                    ((RefBookColumn) result).setRefBookAttributeId2(attributeId2);
                    if (attributeId2 != null) {
                        ((RefBookColumn) result).setRefBookAttribute(refBookDao.getByAttribute(attributeId2).getAttribute(attributeId2));
                    } else {
                        ((RefBookColumn) result).setRefBookAttribute(refBookDao.getByAttribute(attributeId).getAttribute(attributeId));
                    }
                    ((RefBookColumn) result).setFilter(filter);
                    RefBook refBook = refBookDao.getByAttribute(attributeId);
                    ((RefBookColumn) result).setHierarchical(refBook.getType() == 1);
                    RefBookAttribute refBookAttribute;
                    try {
                        refBookAttribute = refBook.getAttribute("NAME");
                    } catch (IllegalArgumentException e) {
                        refBookAttribute = null;
                    }
                    ((RefBookColumn) result).setNameAttributeId(refBookAttribute != null ? refBookAttribute.getId() : attributeId);
                } else {
                    result = new ReferenceColumn();
                    ((ReferenceColumn) result).setRefBookAttributeId(attributeId);
                    ((ReferenceColumn) result).setRefBookAttributeId2(attributeId2);
                    if (attributeId2 != null) {
                        ((ReferenceColumn) result).setRefBookAttribute(refBookDao.getByAttribute(attributeId2).getAttribute(attributeId2));
                    } else {
                        ((ReferenceColumn) result).setRefBookAttribute(refBookDao.getByAttribute(attributeId).getAttribute(attributeId));
                    }
                    ((ReferenceColumn) result).setParentId(parentColumnId);
                }
            } else if ("A".equals(type)) {
                result = new AutoNumerationColumn();
                ((AutoNumerationColumn) result).setNumerationType(NumerationType.getById(SqlUtils.getInteger(rs, "numeration_row")));
			} else {
				throw new IllegalArgumentException("Unknown column type: " + type);
			}
			result.setId(SqlUtils.getInteger(rs, "id"));
			result.setAlias(rs.getString("alias"));
			result.setName(rs.getString("name"));
			result.setWidth(SqlUtils.getInteger(rs, "width"));
			result.setOrder(SqlUtils.getInteger(rs, "ord"));
			result.setDataOrder(SqlUtils.getInteger(rs, "data_ord"));
			result.setChecking(rs.getBoolean("checking"));
			return result;
		}
	}

	@Override
    public List<Column> getFormColumns(int formId) {
		return getJdbcTemplate().query(
				"SELECT " +
				"  id, name, form_template_id, alias, type, width, precision, ord, max_length, " +
				"  checking, format, attribute_id, filter, parent_column_id, attribute_id2, numeration_row, data_ord " +
				"FROM form_column " +
				"WHERE form_template_id = ? " +
				"ORDER BY ord",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new ColumnMapper()
		);
	}

	@Override
	public void updateFormColumns(final FormTemplate formTemplate) {
		// получаем "старые" столбцы до обновления макета, потом в этом списке останутся столбцы на удаление
		final Map<String, Character> prevColumns = new HashMap<String, Character>();
		getNamedParameterJdbcTemplate().query(
				"SELECT alias, type FROM form_column WHERE form_template_id = :form_template_id ORDER BY ord",
				new HashMap<String, Integer>() {{
					put("form_template_id", formTemplate.getId());
				}},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						prevColumns.put(rs.getString("alias"), rs.getString("type").charAt(0));
					}
				}
		);

		// определяем какие столбцы надо добавить, удалить или обновить
		List<Column> columns = formTemplate.getColumns();
		List<Column> insertColumns =  new ArrayList<Column>();
		List<Column> updateColumns = new ArrayList<Column>();
		int order = 0;
		for (Column column: columns) {
			column.setOrder(++order);
			if (!prevColumns.containsKey(column.getAlias())) {
				insertColumns.add(column);
			} else {
				updateColumns.add(column);
				prevColumns.remove(column.getAlias());
			}
		}
		// выставляем актуальные значения для поля dataOrder
		calculateNewDataOrder(insertColumns, updateColumns);
		// удаляем столбцы в БД
        deleteFormColumns(prevColumns.keySet(), formTemplate);
		// вставляем новые столбцы
		createFormColumns(insertColumns, formTemplate);
		// обновляем "старые"
        updateFormColumns(updateColumns, formTemplate);
	}

	/**
	 * Вычисляет для новых столбцов значения для dataOrder так, чтобы они не пересекались с текущими столбцами
	 * и по максимуму заполнили пропуски в последовательности
	 * @param newColumns столбцы, для которых вычисляется dataOrder
	 * @param oldColumns столбцы, с dataOrder которых не должно быть пересечений
	 */
	static void calculateNewDataOrder(List<Column> newColumns, List<Column> oldColumns) {
		// Получаем текущую последовательность dataOrder
		List<Integer> dataOrders = new ArrayList<Integer>();
		for (Column column : oldColumns) {
			dataOrders.add(column.getDataOrder());
		}
		int dataOrder = 0;
		for (Column column : newColumns) {
			while(true) {
				if (!dataOrders.contains(dataOrder)) {
					column.setDataOrder(dataOrder++);
					break;
				}
				dataOrder++;
			}
		}
	}

    void createFormColumns(final List<Column> newColumns, final FormTemplate formTemplate) {
		if (newColumns.isEmpty()) {
			return;
		}
        List<Long> newIds = bdUtils.getNextIds(BDUtils.Sequence.FORM_COLUMN, (long) newColumns.size());
		List<Integer> dataOrders = new ArrayList<Integer>();
		// выставляем id граф
		for (int i = 0; i < newColumns.size(); i++) {
			Column column = newColumns.get(i);
			column.setId(newIds.get(i).intValue());
			dataOrders.add(i, column.getDataOrder());
		}
		setReferenceParentId(formTemplate, newColumns);

        getJdbcTemplate().batchUpdate(
                "INSERT INTO form_column (id, name, form_template_id, alias, type, width, precision, ord, max_length, " +
                        "checking, format, attribute_id, filter, parent_column_id, attribute_id2, numeration_row, data_ord) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        Column col = newColumns.get(index);

                        ps.setInt(1, col.getId());
                        ps.setString(2, col.getName());
                        ps.setInt(3, formTemplate.getId());
                        ps.setString(4, col.getAlias());
                        ps.setString(5, String.valueOf(col.getColumnType().getCode()));
                        ps.setInt(6, col.getWidth());

                        if (ColumnType.NUMBER.equals(col.getColumnType())) {
                            if (((NumericColumn) col).getPrecision() > NumericColumn.MAX_PRECISION){
								LOG.warn("Превышена максимально допустимая точность числа в графе " + col.getName() +
                                        "\". Будет установлено максимальное значение: " + NumericColumn.MAX_PRECISION);
                            }
                            ps.setInt(7, ((NumericColumn)col).getPrecision());
                        } else {
                            ps.setNull(7, Types.NUMERIC);
                        }

                        ps.setInt(8, col.getOrder());
                        ps.setBoolean(10, col.isChecking());

                        if (ColumnType.STRING.equals(col.getColumnType())) {
                            if (((StringColumn) col).getMaxLength() > StringColumn.MAX_LENGTH){
								LOG.warn("Превышена максимально допустимая длина строки в графе \"" + col.getName() +
                                        "\". Будет установлено максимальное значение: " + StringColumn.MAX_LENGTH);
                            }
                            ps.setInt(9, Math.min(((StringColumn) col).getMaxLength(), StringColumn.MAX_LENGTH));
                            ps.setNull(11, Types.INTEGER);
                        } else if (ColumnType.NUMBER.equals(col.getColumnType())) {
                            if (((NumericColumn) col).getMaxLength() > NumericColumn.MAX_LENGTH ||
                                    ((NumericColumn) col).getMaxLength() > NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION) {
                                LOG.warn("Превышена максимально допустимая длина числа в графе " + col.getName() +
                                        "\". Будет установлено максимальное значение: " + (NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION));
                            }
                            ps.setInt(9, Math.min(((NumericColumn) col).getMaxLength(), NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION));
                            ps.setNull(11, Types.INTEGER);
                        } else if (ColumnType.DATE.equals(col.getColumnType())) {
                            ps.setInt(11, ((DateColumn)col).getFormatId());
                            ps.setNull(9, Types.INTEGER);
                        } else {
                            ps.setNull(9, Types.INTEGER);
                            ps.setNull(11, Types.INTEGER);
                        }

                        if (ColumnType.REFBOOK.equals(col.getColumnType())) {
                            if (((RefBookColumn) col).getRefBookAttributeId() != null)
                                ps.setLong(12, ((RefBookColumn) col).getRefBookAttributeId());
                            else
                                ps.setNull(12, Types.NUMERIC);
                            ps.setString(13, ((RefBookColumn) col).getFilter());
                            ps.setNull(14, Types.NUMERIC);
                            if (((RefBookColumn) col).getRefBookAttributeId2()== null){
                                ps.setNull(15, Types.NUMERIC);
                            }
                            else {
                                ps.setLong(15, ((RefBookColumn) col).getRefBookAttributeId2());
                            }
                            ps.setNull(16, Types.NUMERIC);
                        } else if (ColumnType.REFERENCE.equals(col.getColumnType())) {
                            ps.setLong(12, ((ReferenceColumn) col).getRefBookAttributeId());
                            ps.setNull(13, Types.CHAR);
                            ps.setLong(14, ((ReferenceColumn) col).getParentId());
                            if (((ReferenceColumn) col).getRefBookAttributeId2()== null){
                                ps.setNull(15, Types.NUMERIC);
                            }
                            else {
                                ps.setLong(15, ((ReferenceColumn) col).getRefBookAttributeId2());
                            }
                            ps.setNull(16, Types.NUMERIC);
                        } else if (ColumnType.AUTO.equals(col.getColumnType())) {
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.CHAR);
                            ps.setNull(14, Types.NUMERIC);
                            ps.setNull(15, Types.NUMERIC);
                            ps.setInt(16, ((AutoNumerationColumn) col).getNumerationType().getId());
                        } else {
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.CHAR);
                            ps.setNull(14, Types.NUMERIC);
                            ps.setNull(15, Types.NUMERIC);
                            ps.setNull(16, Types.NUMERIC);
                        }
						ps.setInt(17, col.getDataOrder());
                    }

                    @Override
                    public int getBatchSize() {
                        return newColumns.size();
                    }
                }
        );
		// очистка столбцов перед их использованием
		deleteColumnData(formTemplate, dataOrders);
    }

	/**
	 * Выставляет parentId для новых зависимых граф
	 * @param formTemplate
	 * @param columns
	 */
    void setReferenceParentId(FormTemplate formTemplate, List<Column> columns){
        for (Column column : columns) {
            if (ColumnType.REFERENCE.equals(column.getColumnType())) {
                ReferenceColumn referenceColumn = (ReferenceColumn)column;
                // При экспорте parentId не сериализуется, а прописывается алиас для parentId, здесь в случии импорта подставляем нужный id
                if(referenceColumn.getParentAlias() != null){
                    referenceColumn.setParentId(formTemplate.getColumn(referenceColumn.getParentAlias()).getId());
                }
            }
        }
    }

    /**
     * Удаляем столбцы
     */
    void deleteFormColumns(final Collection<String> removeColumns, FormTemplate formTemplate) {
		if (removeColumns.isEmpty()) {
			return;
		}
        try {
			String aliasClause = SqlUtils.transformToSqlInStatementForString("alias", removeColumns);
			// получаем сведения о том, какие столбцы необходимо почистить в FORM_DATA_ROW
			StringBuilder sb = new StringBuilder("SELECT data_ord FROM form_column WHERE form_template_id = ");
			sb.append(formTemplate.getId());
			sb.append(" AND ");
			sb.append(aliasClause);
			List<Integer> dataOrders = getJdbcTemplate().queryForList(sb.toString(), Integer.class);
			// удаление из таблицы FORM_COLUMN
			sb = new StringBuilder("DELETE FROM form_column WHERE form_template_id = ");
			sb.append(formTemplate.getId());
			sb.append(" AND ");
			sb.append(aliasClause);
			int count = getJdbcTemplate().update(sb.toString());
			if (count != removeColumns.size()) {
				throw new IllegalArgumentException("Column aliases are missing in the specified formtemplate");
			}
			// удаление данных из таблицы FORM_DATA_ROW
			deleteColumnData(formTemplate, dataOrders);
        } catch (DataIntegrityViolationException e){
			LOG.error("Невозможно удалить графы", e);
            throw new DaoException("Невозможно удалить графы", e);
        }
    }

	/**
	 * Удаление данных для указанных граф. Используется при удалении граф, чтобы почистить за собой и при создании
	 * новых, чтобы мусор не мешал работать.
	 */
	void deleteColumnData(FormTemplate formTemplate, List<Integer> dataOrders) {
		if (dataOrders.isEmpty()) {
			return;
		}
		for (int i = 0; i < dataOrders.size(); i++) {
			if (dataOrders.get(i) == null) {
				throw new IllegalArgumentException("Argument \"dataOrders\" does not must contain \"null\" value");
			}
		}
		// удаление данных из таблицы FORM_DATA_ROW
		StringBuilder sb = new StringBuilder("UPDATE form_data_row SET ");
		for (int dataOrder : dataOrders) {
			if (dataOrder != dataOrders.get(0)) {
				sb.append(",");
			}
			sb.append(" c").append(dataOrder);
			sb.append(" = NULL, c").append(dataOrder).append("_style = NULL");
		}
		sb.append(" WHERE form_data_id IN (SELECT id FROM form_data WHERE form_template_id = ");
		sb.append(formTemplate.getId()).append(')');
		getJdbcTemplate().update(sb.toString());
	}

    void updateFormColumns(final List<Column> oldColumns, final FormTemplate formTemplate){
		if (oldColumns.isEmpty()) {
			return;
		}
		clearTypeChangedColumns(formTemplate);

		setReferenceParentId(formTemplate, oldColumns);
		final HashSet<Long> ids = new HashSet<Long>();
        getJdbcTemplate().batchUpdate(
                "UPDATE form_column SET name = ?, alias = ?, type = ?, width = ?, precision = ?, ord = ?, " +
                        "max_length = ?, checking = ?, format = ?, attribute_id = ?, filter = ?, " +
                        "parent_column_id = ?, attribute_id2 = ?, numeration_row = ?, data_ord = ? " +
                        "WHERE alias = ? and form_template_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        Column col = oldColumns.get(index);
                        ids.add(Long.valueOf(col.getId()));
                        ps.setString(1, col.getName());
                        ps.setString(2, col.getAlias());
                        ps.setString(3, String.valueOf(col.getColumnType().getCode()));
                        ps.setInt(4, col.getWidth());

                        if (ColumnType.NUMBER.equals(col.getColumnType())) {
                            ps.setInt(5, ((NumericColumn)col).getPrecision());
                        } else {
                            ps.setNull(5, Types.NUMERIC);
                        }

                        ps.setInt(6, col.getOrder());
                        ps.setBoolean(8, col.isChecking());

                        if (ColumnType.STRING.equals(col.getColumnType())) {
                            ps.setInt(7, ((StringColumn) col).getMaxLength());
                            ps.setNull(9, Types.INTEGER);
                            ps.setNull(10, Types.NUMERIC);
                            ps.setString(11, ((StringColumn) col).getFilter());
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.NUMERIC);
                            ps.setNull(14, Types.NUMERIC);
                        } else if(ColumnType.NUMBER.equals(col.getColumnType())){
                            if (((NumericColumn) col).getMaxLength() > NumericColumn.MAX_LENGTH ||
                                    ((NumericColumn) col).getMaxLength() > NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION) {
                                LOG.warn("Превышена максимально допустимая длина числа в графе " + col.getName() +
                                        "\". Будет установлено максимальное значение: " + (NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION));
                            }
                            ps.setInt(7, Math.min(((NumericColumn) col).getMaxLength(), NumericColumn.MAX_LENGTH - NumericColumn.MAX_PRECISION));
                            ps.setNull(9, Types.INTEGER);
                            ps.setNull(10, Types.NUMERIC);
                            ps.setNull(11, Types.CHAR);
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.NUMERIC);
                            ps.setNull(14, Types.NUMERIC);
                        } else if (ColumnType.DATE.equals(col.getColumnType())) {
                            if (((DateColumn)col).getFormatId() == null)
                                ps.setNull(9, Types.INTEGER);
                            else
                                ps.setInt(9, ((DateColumn)col).getFormatId());
                            ps.setNull(7, Types.INTEGER);
                            ps.setNull(10, Types.NUMERIC);
                            ps.setNull(11, Types.CHAR);
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.NUMERIC);
                            ps.setNull(14, Types.NUMERIC);
                        } else if (ColumnType.REFBOOK.equals(col.getColumnType())) {
                            ps.setNull(7, Types.INTEGER);
                            ps.setNull(9, Types.INTEGER);
                            ps.setLong(10, ((RefBookColumn) col).getRefBookAttributeId());
                            ps.setString(11, ((RefBookColumn) col).getFilter());
                            ps.setNull(12, Types.NUMERIC);
                            if (((RefBookColumn) col).getRefBookAttributeId2() != null) {
                                ps.setLong(13, ((RefBookColumn) col).getRefBookAttributeId2());
                            } else {
                                ps.setNull(13, Types.NULL);
                            }
                            ps.setNull(14, Types.NUMERIC);
                        } else if (ColumnType.REFERENCE.equals(col.getColumnType())) {
                            ps.setNull(7, Types.INTEGER);
                            ps.setNull(9, Types.INTEGER);
                            ps.setLong(10, ((ReferenceColumn) col).getRefBookAttributeId());
                            ps.setNull(11, Types.CHAR);
                            if (((ReferenceColumn) col).getParentId() != 0)
                                ps.setLong(12, ((ReferenceColumn) col).getParentId());
                            else
                                ps.setLong(12, formTemplate.getColumn(((ReferenceColumn) col).getParentAlias()).getId());
                            if (((ReferenceColumn) col).getRefBookAttributeId2() != null) {
                                ps.setLong(13, ((ReferenceColumn) col).getRefBookAttributeId2());
                            } else {
                                ps.setNull(13, Types.NULL);
                            }
                            ps.setNull(14, Types.NUMERIC);
                        } else if (ColumnType.AUTO.equals(col.getColumnType())) {
                            ps.setNull(7, Types.INTEGER);
                            ps.setNull(9, Types.INTEGER);
                            ps.setNull(10, Types.NUMERIC);
                            ps.setNull(11, Types.CHAR);
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.NUMERIC);
                            ps.setInt(14, ((AutoNumerationColumn) col).getNumerationType().getId());
                        } else {
                            ps.setNull(7, Types.INTEGER);
                            ps.setNull(9, Types.INTEGER);
                            ps.setNull(10, Types.NUMERIC);
                            ps.setNull(11, Types.CHAR);
                            ps.setNull(12, Types.NUMERIC);
                            ps.setNull(13, Types.NUMERIC);
                            ps.setNull(14, Types.NUMERIC);
                        }
						ps.setInt(15, col.getDataOrder());
                        ps.setString(16, col.getAlias());
                        ps.setInt(17, formTemplate.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return oldColumns.size();
                    }
                }
        );
    }

	/**
	 * Для изменивших свой тип граф чистим таблицу с данными
	 * @param formTemplate макет НФ
	 */
	void clearTypeChangedColumns(final FormTemplate formTemplate) {
		final List<Integer> dataOrders = new ArrayList<Integer>();
		getNamedParameterJdbcTemplate().query(
			"SELECT alias, type, data_ord FROM form_column WHERE form_template_id = :form_template_id ORDER BY ord",
			new HashMap<String, Integer>() {{
				put("form_template_id", formTemplate.getId());
			}},
			new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					String alias = rs.getString("alias");
					char type = rs.getString("type").charAt(0);
					Integer dataOrder = rs.getInt("data_ord");
					Column column = formTemplate.getColumn(alias);
					if (column.getColumnType().getCode() != type) {
						column.setDataOrder(dataOrder);
						dataOrders.add(dataOrder);
					}
				}
			}
		);
		deleteColumnData(formTemplate, dataOrders);
	}

    private static final String GET_ATTRIBUTE_ID_2_QUERY = "SELECT DISTINCT attribute_id, attribute_id2 " +
			" FROM form_column WHERE %s AND attribute_id2 IS NOT NULL AND attribute_id2 <> 0";

    @Override
	public Map<Long, List<Long>> getAttributeId2(List<RefBookAttribute> attributes) {
		final Map<Long, List<Long>> result = new HashMap<Long, List<Long>>();
		if (attributes.isEmpty()) {
			return result;
		}
		// создаем пустые списки
		Iterator<RefBookAttribute> iterator = attributes.iterator();
		List<Long> attributeIds = new ArrayList<Long>();
		while (iterator.hasNext()) {
			Long attributeId = iterator.next().getId();
			attributeIds.add(attributeId);
			result.put(attributeId, new ArrayList<Long>());
		}
        getJdbcTemplate().query(
			String.format(GET_ATTRIBUTE_ID_2_QUERY, SqlUtils.transformToSqlInStatement("attribute_id", attributeIds)),
			new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					 result.get(rs.getLong(1)).add(rs.getLong(2));
				}
			}
		);

		Iterator<Long> iterator2 = attributeIds.iterator();
		while (iterator2.hasNext()) {
			Long attributeId = iterator2.next();
			if (result.get(attributeId).isEmpty()) {
				result.remove(attributeId);
			}
		}
        return result;
    }
}