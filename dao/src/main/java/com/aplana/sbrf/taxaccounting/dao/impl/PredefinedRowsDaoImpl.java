package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.PredefinedRowsDao;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.taxaccounting.util.OrderUtils;

@Repository
public class PredefinedRowsDaoImpl extends AbstractDao implements PredefinedRowsDao {

	@Override
	@Transactional(readOnly=true)
	public List<DataRow> getPredefinedRows(final Form form) {
		return getJdbcTemplate().query(
			"select * from form_row where form_id = ? order by order",
			new Object[] { form.getId() },
			new int[] { Types.NUMERIC },
			new RowMapper<DataRow>() {
				@Override
				public DataRow mapRow(ResultSet rs, int index) throws SQLException {
					DataRow dataRow = new DataRow(rs.getString("alias"), form);
					dataRow.setOrder(rs.getInt("order"));
					String data = rs.getString("data");
					// TODO: парсинг строки и заполнение ячеек
					return dataRow;
				}
			}
		);
	}

	@Override
	@Transactional
	public void savePredefinedRows(Form form, final List<DataRow> predefinedRows) {
		OrderUtils.reorder(predefinedRows);
		getJdbcTemplate().batchUpdate(
			"insert into form_row (form_id, alias, order, data) values (" + form.getId() + ", ?, ?, ?)",
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int index) throws SQLException {
					DataRow dataRow = predefinedRows.get(index);
					ps.setString(1, dataRow.getAlias());
					ps.setInt(2, dataRow.getOrder());
					// TODO: сериализация данных строки в CLOB
					ps.setString(3, null);
				}
				
				@Override
				public int getBatchSize() {
					return predefinedRows.size();
				}
			}
		);
	}
}
