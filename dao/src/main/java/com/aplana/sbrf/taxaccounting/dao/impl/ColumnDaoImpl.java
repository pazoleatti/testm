package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BDUtils;
import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.OrderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository
@Transactional(readOnly=true)
public class ColumnDaoImpl extends AbstractDao implements ColumnDao {

    @Autowired
    private BDUtils bdUtils;

    @Override
    public void setDbUtils(BDUtils bdUtils) {
        this.bdUtils = bdUtils;
    }

	private final static class ColumnMapper implements RowMapper<Column> {
		public Column mapRow(ResultSet rs, int index) throws SQLException {
			final Column result;
			String type = rs.getString("type");
			if ("N".equals(type)) {
				result = new NumericColumn();
				((NumericColumn)result).setPrecision(rs.getInt("precision"));
				((NumericColumn) result).setMaxLength(rs.getInt("max_length"));
			} else if ("D".equals(type)) {
				result = new DateColumn();
				((DateColumn) result).setFormatId(rs.getInt("format"));
			} else if ("S".equals(type)) {
				result = new StringColumn();
				((StringColumn) result).setMaxLength(rs.getInt("max_length"));
			} else if ("R".equals(type)) {
                long attributeId = rs.getLong("attribute_id");
                int parentColumnId = rs.getInt("parent_column_id");
                String filter = rs.getString("filter");
                if (parentColumnId == 0) {
				    result = new RefBookColumn();
    				((RefBookColumn)result).setRefBookAttributeId(attributeId);
                    ((RefBookColumn)result).setFilter(filter);
                } else {
                    result = new ReferenceColumn();
                    ((ReferenceColumn)result).setRefBookAttributeId(attributeId);
                    ((ReferenceColumn)result).setParentId(parentColumnId);
                }
			} else {
				throw new IllegalArgumentException("Unknown column type: " + type);
			}
			result.setId(rs.getInt("id"));
			result.setAlias(rs.getString("alias"));
			result.setName(rs.getString("name"));
			result.setWidth(rs.getInt("width"));
			result.setOrder(rs.getInt("ord"));
			result.setChecking(rs.getBoolean("checking"));
			return result;
		}
	}
	
	public List<Column> getFormColumns(int formId) {
		return getJdbcTemplate().query(
			"select * from form_column where form_template_id = ? order by ord",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new ColumnMapper()
		);
	}

	@Transactional(readOnly = false)
	@Override
	public void saveFormColumns(final FormTemplate form) {
		final Logger log = new Logger();

		int formId = form.getId();

		JdbcTemplate jt = getJdbcTemplate();

		final Set<Integer> removedColumns = new HashSet<Integer>(jt.queryForList(
			"select id from form_column where form_template_id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			Integer.class
		));

		final List<Column> newColumns = new ArrayList<Column>();
		final List<Column> oldColumns = new ArrayList<Column>();

		List<Column> columns = form.getColumns();
		OrderUtils.reorder(columns);

		int order = 0;
		for (Column col: columns) {
			col.setOrder(++order);
			if (col.getId() == null || col.getId().intValue() < 0) {
				newColumns.add(col);
			} else {
				oldColumns.add(col);
				removedColumns.remove(col.getId());
			}
		}
		if(!removedColumns.isEmpty()){
			jt.batchUpdate(
					"delete from form_column where id = ?",
					new BatchPreparedStatementSetter() {

						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							ps.setInt(1, iterator.next());
						}

						@Override
						public int getBatchSize() {
							return removedColumns.size();
						}

						private Iterator<Integer> iterator = removedColumns.iterator();
					}
			);
		}

        // Сгенерированый ключ -> реальный ключ в БД
        Map<Integer, Integer> idsMapping = new HashMap<Integer, Integer>();

		if (!newColumns.isEmpty()) {
            List<Long> genKeys = bdUtils.getNextIds(BDUtils.Sequence.FORM_COLUMN, (long) newColumns.size());
            int counter = 0;
            for (Column column : newColumns) {
                if (column.getId() == null || column.getId().intValue() < 0) {
                    if (column.getId() != null) {
                        idsMapping.put(column.getId(), genKeys.get(counter).intValue());
                    }
                    column.setId(genKeys.get(counter).intValue());
                }
                counter++;
            }
            // Подмена ссылок
            for (Column column : columns) {
                if (column instanceof ReferenceColumn) {
                    ReferenceColumn referenceColumn = ((ReferenceColumn)column);
                    if (referenceColumn.getParentId() < 0) {
                        referenceColumn.setParentId(idsMapping.get(referenceColumn.getParentId()));
                    }
                }
            }

			jt.batchUpdate(
				"insert into form_column (id, name, form_template_id, alias, type, width, precision, ord, max_length, " +
                "checking, format, attribute_id, filter, parent_column_id) " +
				"values (?, ?, " + formId + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						Column col = newColumns.get(index);

                        ps.setInt(1, col.getId());
						ps.setString(2, col.getName());
						ps.setString(3, col.getAlias());
						ps.setString(4, getTypeFromCode(col));
						ps.setInt(5, col.getWidth());

						if (col instanceof NumericColumn) {
							ps.setInt(6, ((NumericColumn)col).getPrecision());
						} else {
							ps.setNull(6, Types.NUMERIC);
						}

                        ps.setInt(7, col.getOrder());
                        ps.setBoolean(9, col.isChecking());

						if (col instanceof StringColumn) {
							//TODO: Продумать данный момент. Сейчас, если максимальное значение превышается, то мы
							// "пропускаем" значение в базу и просто выводим предупреждение.
							if (((StringColumn) col).getMaxLength() > StringColumn.MAX_LENGTH){
								log.warn("Превышена максимально допустимая длина строки в столбце " + col.getName());
							}
							ps.setInt(8, ((StringColumn) col).getMaxLength());
							ps.setNull(10, Types.INTEGER);
						} else if (col instanceof NumericColumn) {
							if (((NumericColumn) col).getMaxLength() > NumericColumn.MAX_LENGTH){
								log.warn("Превышена максимально допустимая длина строки в столбце " + col.getName());
							}
							ps.setInt(8, ((NumericColumn) col).getMaxLength());
							ps.setNull(10, Types.INTEGER);
						} else if (col instanceof DateColumn) {
							ps.setInt(10, ((DateColumn)col).getFormatId());
							ps.setNull(8, Types.INTEGER);
						} else {
							ps.setNull(8, Types.INTEGER);
							ps.setNull(10, Types.INTEGER);
						}

                        if (col instanceof RefBookColumn) {
                            ps.setLong(11, ((RefBookColumn) col).getRefBookAttributeId());
                            ps.setString(12, ((RefBookColumn) col).getFilter());
                            ps.setNull(13, Types.NUMERIC);
                        } else if (col instanceof ReferenceColumn) {
                            ps.setLong(11, ((ReferenceColumn) col).getRefBookAttributeId());
                            ps.setNull(12, Types.CHAR);
                            ps.setLong(13, ((ReferenceColumn) col).getParentId());
                        } else {
                            ps.setNull(11, Types.NUMERIC);
                            ps.setNull(12, Types.CHAR);
                            ps.setNull(13, Types.NUMERIC);
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
					"update form_column set name = ?, alias = ?, type = ?, width = ?, precision = ?, ord = ?, " +
                            "max_length = ?, checking = ?, format = ?, attribute_id = ?, filter = ?, " +
                            "parent_column_id = ? where id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							Column col = oldColumns.get(index);
							ps.setString(1, col.getName());
							ps.setString(2, col.getAlias());
							ps.setString(3, getTypeFromCode(col));
							ps.setInt(4, col.getWidth());

							if (col instanceof NumericColumn) {
								ps.setInt(5, ((NumericColumn)col).getPrecision());
							} else {
								ps.setNull(5, Types.NUMERIC);
							}

                            ps.setInt(6, col.getOrder());
                            ps.setBoolean(8, col.isChecking());

							if (col instanceof StringColumn) {
								ps.setInt(7, ((StringColumn) col).getMaxLength());
								ps.setNull(9, Types.INTEGER);
								ps.setNull(10, Types.NUMERIC);
                                ps.setNull(11, Types.CHAR);
                                ps.setNull(12, Types.NUMERIC);
							} else if(col instanceof NumericColumn){
								ps.setInt(7, ((NumericColumn) col).getMaxLength());
								ps.setNull(9, Types.INTEGER);
								ps.setNull(10, Types.NUMERIC);
                                ps.setNull(11, Types.CHAR);
                                ps.setNull(12, Types.NUMERIC);
							} else if (col instanceof DateColumn) {
								ps.setInt(9, ((DateColumn)col).getFormatId());
								ps.setNull(7, Types.INTEGER);
								ps.setNull(10, Types.NUMERIC);
                                ps.setNull(11, Types.CHAR);
                                ps.setNull(12, Types.NUMERIC);
							} else if (col instanceof RefBookColumn) {
								ps.setNull(7, Types.INTEGER);
								ps.setNull(9, Types.INTEGER);
								ps.setLong(10, ((RefBookColumn) col).getRefBookAttributeId());
                                ps.setString(11, ((RefBookColumn) col).getFilter());
                                ps.setNull(12, Types.NUMERIC);
							} else if (col instanceof ReferenceColumn) {
                                ps.setNull(7, Types.INTEGER);
                                ps.setNull(9, Types.INTEGER);
                                ps.setLong(10, ((ReferenceColumn) col).getRefBookAttributeId());
                                ps.setNull(11, Types.CHAR);
                                System.out.println("parent_id = "+((ReferenceColumn) col).getParentId());
                                ps.setLong(12, ((ReferenceColumn) col).getParentId());
                            } else {
								ps.setNull(7, Types.INTEGER);
								ps.setNull(9, Types.INTEGER);
								ps.setNull(10, Types.NUMERIC);
                                ps.setNull(11, Types.CHAR);
                                ps.setNull(12, Types.NUMERIC);
							}
							ps.setInt(13, col.getId());
						}

						@Override
						public int getBatchSize() {
							return oldColumns.size();
						}
					}
			);
		}

		jt.query(
			"select id, alias from form_column where form_template_id = " + formId,
			new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					String alias = rs.getString("alias");
					int columnId = rs.getInt("id");
					form.getColumn(alias).setId(columnId);
				}
			}
		);
	}

	private String getTypeFromCode(Column col) {
        if (col instanceof NumericColumn) {
            return "N";
        } else if (col instanceof StringColumn) {
            return "S";
        } else if (col instanceof DateColumn) {
            return "D";
        } else if (col instanceof RefBookColumn || col instanceof ReferenceColumn) {
            return "R";
        } else {
            throw new IllegalArgumentException("Unknown column type: " + col.getClass().getName());
        }
    }
}
