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

@Repository
@Transactional(readOnly=true)
public class ColumnDaoImpl extends AbstractDao implements ColumnDao {

	private final static class ColumnMapper implements RowMapper<Column> {
		@Override
		public Column mapRow(ResultSet rs, int index) throws SQLException {
			Column result = new Column();
			result.setId(rs.getInt("id"));
			result.setFormId(rs.getInt("form_id"));
			result.setName(rs.getString("name"));
			result.setOrder(rs.getInt("order"));
			result.setType(Column.Types.getType(rs.getString("type").charAt(0)));
			return result;
		}
	}
	
	@Override
	public List<Column> getFormColumns(int formId) {
		return getJdbcTemplate().query(
			"select * from form_column where form_id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new ColumnMapper()
		);
	}
	
}
