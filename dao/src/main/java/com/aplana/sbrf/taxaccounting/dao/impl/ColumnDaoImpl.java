package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.util.OrderUtils;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository
public class ColumnDaoImpl extends AbstractDao implements ColumnDao {

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
                    ((RefBookColumn) result).setRefBookAttributeId(attributeId);
                    ((RefBookColumn) result).setRefBookAttributeId2(attributeId2);
                    ((RefBookColumn) result).setFilter(filter);
                    RefBook refBook = refBookDao.getByAttribute(attributeId);
                    ((RefBookColumn) result).setHierarchical(refBook.getType() == 1);
                    RefBookAttribute refBookAttribute;
                    try {
                        refBookAttribute = refBook.getAttribute("NAME");
                    } catch (IllegalArgumentException e) {
                        refBookAttribute = null;
                    }
                    ((RefBookColumn) result).setNameAttributeId(refBookAttribute != null ? refBook.getAttribute("NAME").getId() : attributeId);
                } else {
                    result = new ReferenceColumn();
                    ((ReferenceColumn) result).setRefBookAttributeId(attributeId);
                    ((ReferenceColumn) result).setRefBookAttributeId2(attributeId2);
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
			result.setChecking(rs.getBoolean("checking"));
			return result;
		}
	}
	
	@Override
    public List<Column> getFormColumns(int formId) {
		return getJdbcTemplate().query(
				"SELECT " +
				"  id, name, form_template_id, alias, type, width, precision, ord, max_length, " +
				"  checking, format, attribute_id, filter, parent_column_id, attribute_id2, numeration_row " +
				"FROM form_column " +
				"WHERE form_template_id = ? " +
				"ORDER BY ord",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new ColumnMapper()
		);
	}

	@Override
	public void saveFormColumns(final FormTemplate formTemplate) {
		final Logger log = new Logger();

		final int formTemplateId = formTemplate.getId();

		JdbcTemplate jt = getJdbcTemplate();

		final Set<String> removedColumns = new HashSet<String>(jt.queryForList(
			"SELECT alias FROM form_column WHERE form_template_id = ?",
			new Object[] { formTemplateId },
			new int[] { Types.NUMERIC },
                String.class
		));

		final List<Column> newColumns =  new ArrayList<Column>();
		final List<Column> oldColumns = new ArrayList<Column>();

        List<Column> columns = formTemplate.getColumns();

		OrderUtils.reorder(newColumns);

		int order = 0;
		for (Column col: columns) {
			col.setOrder(++order);
			if (!removedColumns.contains(col.getAlias())) {
				newColumns.add(col);
			} else {
				oldColumns.add(col);
				removedColumns.remove(col.getAlias());
			}
		}

		if(!removedColumns.isEmpty()){
            final String[] alias = new String[1];
			try {
                jt.batchUpdate(
                        "DELETE FROM form_column WHERE alias = ? and form_template_id = ?",
                        new BatchPreparedStatementSetter() {

                            @Override
                            public void setValues(PreparedStatement ps, int index) throws SQLException {
                                alias[0] = iterator.next();
                                ps.setString(1, alias[0]);
                                ps.setInt(2, formTemplateId);
                            }

                            @Override
                            public int getBatchSize() {
                                return removedColumns.size();
                            }

                            private Iterator<String> iterator = removedColumns.iterator();
                        }
                );
            } catch (DataIntegrityViolationException e){
                logger.error("", e);
                throw new DaoException("Обнаружено использование колонки", e);
            }
		}

        // Сгенерированый ключ -> реальный ключ в БД
        Map<Integer, Integer> idsMapping = new HashMap<Integer, Integer>();

		if (!newColumns.isEmpty()) {
            List<Long> genKeys = bdUtils.getNextIds(BDUtils.Sequence.FORM_COLUMN, (long) newColumns.size());
            int counter = 0;
            for (Column column : newColumns) {
                if (column.getId() == null || column.getId() < 0) {
                    if (column.getId() != null) {
                        idsMapping.put(column.getId(), genKeys.get(counter).intValue());
                    }
                    column.setId(genKeys.get(counter).intValue());
                }
                counter++;
            }
            for (Column column : oldColumns){
                column.setId(getColumnIdByAlias(formTemplateId, column.getAlias()));
            }
            // Подмена ссылок
            for (Column column : columns) {
                if (ColumnType.REFERENCE.equals(column.getColumnType())) {
                    ReferenceColumn referenceColumn = (ReferenceColumn)column;
                    // При экспорте parentId не сериализуется, а прописывается алиас для parentId, здесь в случии импорта подставляем нужный id
                    if(referenceColumn.getParentId()==0 && referenceColumn.getParentAlias()!=null){
                        referenceColumn.setParentId(
                                formTemplate.getColumn(
                                        referenceColumn.getParentAlias()).getId());
                    }
                    else if(referenceColumn.getParentId() < 0) {
                        referenceColumn.setParentId(idsMapping.get(referenceColumn.getParentId()));
                    }
                }
            }

			jt.batchUpdate(
				"INSERT INTO form_column (id, name, form_template_id, alias, type, width, precision, ord, max_length, " +
                "checking, format, attribute_id, filter, parent_column_id, attribute_id2, numeration_row) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						Column col = newColumns.get(index);

                        ps.setInt(1, col.getId());
						ps.setString(2, col.getName());
						ps.setInt(3, formTemplateId);
						ps.setString(4, col.getAlias());
						ps.setString(5, String.valueOf(col.getColumnType().getCode()));
						ps.setInt(6, col.getWidth());

						if (ColumnType.NUMBER.equals(col.getColumnType())) {
							if (((NumericColumn) col).getPrecision() > NumericColumn.MAX_PRECISION){
								log.warn("Превышена максимально допустимая точность числа в графе " + col.getName() +
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
								log.warn("Превышена максимально допустимая длина строки в графе \"" + col.getName() +
									"\". Будет установлено максимальное значение: " + StringColumn.MAX_LENGTH);
							}
							ps.setInt(9, Math.min(((StringColumn) col).getMaxLength(), StringColumn.MAX_LENGTH));
							ps.setNull(11, Types.INTEGER);
						} else if (ColumnType.NUMBER.equals(col.getColumnType())) {
							if (((NumericColumn) col).getMaxLength() > NumericColumn.MAX_LENGTH){
								log.warn("Превышена максимально допустимая длина числа в графе " + col.getName() +
										"\". Будет установлено максимальное значение: " + NumericColumn.MAX_LENGTH);
							}
							ps.setInt(9, Math.min(((NumericColumn) col).getMaxLength(), NumericColumn.MAX_LENGTH));
							ps.setNull(11, Types.INTEGER);
						} else if (ColumnType.DATE.equals(col.getColumnType())) {
							ps.setInt(11, ((DateColumn)col).getFormatId());
							ps.setNull(9, Types.INTEGER);
						} else {
							ps.setNull(9, Types.INTEGER);
							ps.setNull(11, Types.INTEGER);
						}

                        if (ColumnType.REFBOOK.equals(col.getColumnType())) {
                            ps.setLong(12, ((RefBookColumn) col).getRefBookAttributeId());
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
                    }

					@Override
					public int getBatchSize() {
						return newColumns.size();
					}
				}
			);
		}

		if(!oldColumns.isEmpty()){
			jt.batchUpdate(
					"UPDATE form_column SET name = ?, alias = ?, type = ?, width = ?, precision = ?, ord = ?, " +
                            "max_length = ?, checking = ?, format = ?, attribute_id = ?, filter = ?, " +
                            "parent_column_id = ?, attribute_id2 = ?, numeration_row = ? " +
                            "WHERE alias = ? and form_template_id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							Column col = oldColumns.get(index);
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
                                ps.setNull(11, Types.CHAR);
                                ps.setNull(12, Types.NUMERIC);
                                ps.setNull(13, Types.NUMERIC);
                                ps.setNull(14, Types.NUMERIC);
							} else if(ColumnType.NUMBER.equals(col.getColumnType())){
								ps.setInt(7, ((NumericColumn) col).getMaxLength());
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
                                    ps.setLong(12, getColumnIdByAlias(formTemplateId, ((ReferenceColumn) col).getParentAlias()));
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
							ps.setString(15, col.getAlias());
                            ps.setInt(16, formTemplateId);
						}

						@Override
						public int getBatchSize() {
							return oldColumns.size();
						}
					}
			);
		}
	}

	private static final String getAttributeId2Query = "SELECT DISTINCT attribute_id, attribute_id2 " +
			" FROM form_column WHERE %s AND attribute_id2 IS NOT NULL AND attribute_id2 <> 0";

    @Override
	public Map<Long, List<Long>> getAttributeId2(List<RefBookAttribute> attributes) {
		final Map<Long, List<Long>> result = new HashMap<Long, List<Long>>();
		if (attributes.size() == 0) {
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
			String.format(getAttributeId2Query, SqlUtils.transformToSqlInStatement("attribute_id", attributeIds)),
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

    @Override
    public int getColumnIdByAlias(int formTemplateId, String columnAlias){
        try {
            return getJdbcTemplate().queryForInt(
                    "SELECT id FROM form_column WHERE form_template_id = ? and alias = ?",
                    formTemplateId, columnAlias);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }
}
