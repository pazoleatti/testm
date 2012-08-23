package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;

@Repository
@Transactional(readOnly=true)
public class ColumnDaoImpl extends AbstractDao implements ColumnDao {

	private final static class ColumnMapper implements RowMapper<Column<?>> {
		public Column<?> mapRow(ResultSet rs, int index) throws SQLException {
			final Column<?> result;
			String type = rs.getString("type");
			if ("N".equals(type)) {
				result = new NumericColumn();
			} else if ("D".equals(type)) {
				result = new DateColumn();
			} else if ("S".equals(type)) {
				result = new StringColumn();
			} else {
				throw new IllegalArgumentException("Unknown column type: " + type);
			}
			result.setId(rs.getInt("id"));
			result.setAlias(rs.getString("alias"));
			result.setFormId(rs.getInt("form_id"));
			result.setName(rs.getString("name"));
			result.setOrder(rs.getInt("order"));
			return result;
		}
	}
	
	public List<Column<?>> getFormColumns(int formId) {
		return getJdbcTemplate().query(
			"select * from form_column where form_id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new ColumnMapper()
		);
	}
	
}
