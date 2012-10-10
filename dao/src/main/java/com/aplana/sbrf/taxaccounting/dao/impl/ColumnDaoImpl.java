package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.util.OrderUtils;

@Repository
@Transactional(readOnly=true)
public class ColumnDaoImpl extends AbstractDao implements ColumnDao {

	private final static class ColumnMapper implements RowMapper<Column> {
		public Column mapRow(ResultSet rs, int index) throws SQLException {
			final Column result;
			String type = rs.getString("type");
			if ("N".equals(type)) {
				result = new NumericColumn();
				((NumericColumn)result).setPrecision(rs.getInt("precision"));
			} else if ("D".equals(type)) {
				result = new DateColumn();
			} else if ("S".equals(type)) {
				result = new StringColumn();
				((StringColumn)result).setDictionaryCode(rs.getString("dictionary_code"));
			} else {
				throw new IllegalArgumentException("Unknown column type: " + type);
			}
			result.setId(rs.getInt("id"));
			result.setAlias(rs.getString("alias"));
			result.setName(rs.getString("name"));
			result.setWidth(rs.getInt("width"));
			result.setEditable(rs.getBoolean("editable"));
			result.setMandatory(rs.getBoolean("mandatory"));
			result.setOrder(rs.getInt("order"));
			result.setGroupName(rs.getString("group_name"));
			return result;
		}
	}
	
	public List<Column> getFormColumns(int formId) {
		return getJdbcTemplate().query(
			"select * from form_column where form_id = ? order by order",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new ColumnMapper()
		);
	}

	@Override
	public void saveFormColumns(final Form form) {
		int formId = form.getId();
		
		JdbcTemplate jt = getJdbcTemplate();
		
		final Set<Integer> removedColumns = new HashSet<Integer>(jt.queryForList(
			"select id from form_column where form_id = ?",
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
			if (col.getId() < 0) {
				newColumns.add(col);
			} else {
				oldColumns.add(col);
			}
			removedColumns.remove(col.getId());
		}
		
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
		
		jt.batchUpdate(
			"insert into form_column (id, name, form_id, alias, type, editable, mandatory, width, precision, dictionary_code, order, group_name) " +
			"values (nextval for seq_form_column, ?, " + formId + ", ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int index) throws SQLException {					 
					Column col = newColumns.get(index);
					ps.setString(1, col.getName());
					ps.setString(2, col.getAlias());
					ps.setString(3, getTypeFromCode(col));
					ps.setInt(4, col.isEditable() ? 1 : 0);
					ps.setInt(5, col.isMandatory() ? 1 : 0);
					ps.setInt(6, col.getWidth());
					
					if (col instanceof NumericColumn) {
						ps.setInt(7, ((NumericColumn)col).getPrecision());
					} else {
						ps.setNull(7, Types.NUMERIC);
					}
					
					if (col instanceof StringColumn) {
						ps.setString(8, ((StringColumn)col).getDictionaryCode());
					} else {
						ps.setNull(8, Types.VARCHAR);
					}
					
					ps.setInt(9, col.getOrder());
					ps.setString(10, col.getGroupName());
				}
				
				@Override
				public int getBatchSize() {
					return newColumns.size();
				}
			}
		);
		
		
		jt.batchUpdate(
			"update form_column set name = ?, alias = ?, type = ?, editable = ?, mandatory = ?, width = ?, precision = ?, dictionary_code = ?, order = ?, group_name = ? " +
			"where id = ?",
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int index) throws SQLException {
					Column col = oldColumns.get(index);
					ps.setString(1, col.getName());
					ps.setString(2, col.getAlias());
					ps.setString(3, getTypeFromCode(col));
					ps.setInt(4, col.isEditable() ? 1 : 0);
					ps.setInt(5, col.isMandatory() ? 1 : 0);
					ps.setInt(6, col.getWidth());
					if (col instanceof NumericColumn) {
						ps.setInt(7, ((NumericColumn)col).getPrecision());
					} else {
						ps.setNull(7, Types.NUMERIC);
					}
					if (col instanceof StringColumn) {
						ps.setString(8, ((StringColumn)col).getDictionaryCode());
					} else {
						ps.setNull(8, Types.VARCHAR);
					}
					ps.setInt(9, col.getOrder());
					ps.setString(10, col.getGroupName());
					ps.setInt(11, col.getId());
				}
				
				@Override
				public int getBatchSize() {
					return oldColumns.size();
				}
			}
		);
		
		jt.query(
			"select id, alias from form_column where form_id = " + formId,
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
		} else {
			throw new IllegalArgumentException("Unknown column type: " + col.getClass().getName());
		}
	}
}
