package com.aplana.sbrf.taxaccounting.dao.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.PredefinedRowsDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.util.OrderUtils;
import com.aplana.sbrf.taxaccounting.util.json.DataRowDeserializer;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;

@Repository
public class PredefinedRowsDaoImpl extends AbstractDao implements PredefinedRowsDao, InitializingBean {

	@Override
	@Transactional(readOnly=true)
	public List<DataRow> getPredefinedRows(final Form form) {
		
		final ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting-dao-read", new Version(1, 0, 0, null));
		module.addDeserializer(DataRow.class, new DataRowDeserializer(form, FormatUtils.getShortDateFormat(), true));
		objectMapper.registerModule(module);
		
		
		return getJdbcTemplate().query(
			"select * from form_row where form_id = ? order by order",
			new Object[] { form.getId() },
			new int[] { Types.NUMERIC },
			new RowMapper<DataRow>() {
				@Override
				public DataRow mapRow(ResultSet rs, int index) throws SQLException {
					String data = rs.getString("data");
					try {
						return objectMapper.readValue(data, DataRow.class);
					} catch (IOException e) {
						logger.error("Failed to read json", e);
						throw new DaoException("Не удалось прочитать данные в формате json: " + e.getMessage());
					}
				}
			}
		);
	}

	@Override
	@Transactional
	public void savePredefinedRows(Form form, final List<DataRow> predefinedRows) {
		OrderUtils.reorder(predefinedRows);
		
		final ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting-dao-write", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer(FormatUtils.getShortDateFormat()));
		objectMapper.registerModule(module);
		
		getJdbcTemplate().batchUpdate(
			"insert into form_row (form_id, alias, order, data) values (" + form.getId() + ", ?, ?, ?)",
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int index) throws SQLException {
					DataRow dataRow = predefinedRows.get(index);
					ps.setString(1, dataRow.getAlias());
					ps.setInt(2, dataRow.getOrder());
					try {
						String rowJson = objectMapper.writeValueAsString(dataRow);
						ps.setString(3, rowJson);
					} catch (Exception e) {
						logger.error("Json generation error", e);
						throw new DaoException("Не удалось сериализовать значение строки данных в JSON: " + e.getMessage());
					}
				}
				@Override
				public int getBatchSize() {
					return predefinedRows.size();
				}
			}
		);
	}
}
