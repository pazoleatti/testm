package com.aplana.sbrf.taxaccounting.dao.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;

@Repository
@Transactional(readOnly=true)
public class FormDataDaoImpl extends AbstractDao implements FormDataDao {
	@Autowired
	private FormDao formDao;
	
	private static class ValueRecord<T> {
		private T value;
		private String rowAlias;
		private int columnId;
		
		public ValueRecord(T value, String rowAlias, int columnId) {
			this.value = value;
			this.rowAlias = rowAlias;
			this.columnId = columnId;
		}
	}
	
	public FormData get(final long formDataId) {
		JdbcTemplate jt = getJdbcTemplate();
		final FormData formData = jt.queryForObject(
			"select * from form_data where id = ?",
			new Object[] { formDataId },
			new int[] { Types.NUMERIC },
			new RowMapper<FormData>() {
				public FormData mapRow(ResultSet rs, int index)	throws SQLException {
					int formId = rs.getInt("form_id");
					Form form = formDao.getForm(formId);
					return new FormData(formDataId, form);
				}
			}
		);
		
		final Map<Long, String> rowIdToAlias = new HashMap<Long, String>();
		
		jt.query(
			"select * from data_row where form_data_id = ? order by order",
			new Object[] { formDataId },
			new int[] { Types.NUMERIC },
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					Long rowId = rs.getLong("id");
					String alias = rs.getString("alias");
					rowIdToAlias.put(rowId, alias);
					formData.appendDataRow(alias);
				}
			}
		);
		
		readValues("numeric_value", rowIdToAlias, formData);
		readValues("string_value", rowIdToAlias, formData);
		readValues("date_value", rowIdToAlias, formData);
		return formData;
	}
	
	private void readValues(String tableName, final Map<Long, String> rowIdToAlias, final FormData formData) {
		getJdbcTemplate().query(
			"select * from " + tableName + " v where exists (select 1 from data_row r where r.id = v.row_id and r.form_data_id = ?)",
			new Object[] { formData.getId() },
			new int[] { Types.NUMERIC },
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					int columnId = rs.getInt("column_id");
					Long rowId = rs.getLong("row_id");
					Object value = rs.getObject("value");
					if (value != null) {
						String rowAlias = rowIdToAlias.get(rowId);
						String columnAlias = formData.getForm().getColumn(columnId).getAlias();
						formData.getDataRow(rowAlias).setColumnValue(columnAlias, value);
					}
				}
			}
		);
	}

	@Override
	@Transactional(readOnly=false)
	public long save(final FormData formData) {
		Long formDataId;
		JdbcTemplate jt = getJdbcTemplate();
		if (formData.getId() == null) {
			formDataId = generateId("seq_form_data", Long.class);
			jt.update(
				"insert into form_data (id, form_id) values (?, ?)",
				new Object[] { formDataId, formData.getForm().getId()}
			);
		} else {
			formDataId = formData.getId();
			jt.update(
				"delete from data_row where form_data_id = ?",
				new Object[] { formDataId },
				new int[] { Types.NUMERIC }
			);
		}
		final List<DataRow> dataRows = formData.getDataRows();
		final List<ValueRecord<BigDecimal>> numericValues = new ArrayList<ValueRecord<BigDecimal>>();
		final List<ValueRecord<String>> stringValues = new ArrayList<ValueRecord<String>>();
		final List<ValueRecord<Date>> dateValues = new ArrayList<ValueRecord<Date>>();
		
		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				DataRow dr = dataRows.get(index);
				String rowAlias = dr.getAlias();
				ps.setString(1, rowAlias);
				ps.setInt(2, index);
				
				for (Column<?> col: formData.getForm().getColumns()) {
					Object val = dr.getColumnValue(col.getAlias());
					if (val == null) {
						continue;
					} else if (val instanceof BigDecimal) {
						numericValues.add(new ValueRecord<BigDecimal>((BigDecimal)val, rowAlias, col.getId()));
					} else if (val instanceof String) {
						stringValues.add(new ValueRecord<String>((String)val, rowAlias, col.getId()));
					} else if (val instanceof Date) {
						dateValues.add(new ValueRecord<Date>((Date)val, rowAlias, col.getId()));
					}
				}
			}
			@Override
			public int getBatchSize() {
				return dataRows.size();
			}
		};
		
		jt.batchUpdate("insert into data_row (id, form_data_id, alias, order) values (nextval for seq_data_row, " + formDataId + ", ?, ?)", bpss);
		final Map<String, Long> rowsAliasToId = new HashMap<String, Long>(dataRows.size());
		jt.query(
			"select id, alias from data_row where form_data_id = ?",
			new Object[] { formDataId },
			new int[] { Types.NUMERIC },
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					rowsAliasToId.put(rs.getString("alias"), rs.getLong("id"));
				}
			}
		);
		insertValues("numeric_value", numericValues, rowsAliasToId);
		insertValues("string_value", stringValues, rowsAliasToId);
		insertValues("date_value", dateValues, rowsAliasToId);
		
		return formDataId;
	}
	
	private <T> void insertValues(String tableName, final List<ValueRecord<T>> values, final Map<String, Long> rowsAliasToId) {
		if (values.isEmpty()) {
			return;
		}
		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				ValueRecord<T> rec = values.get(index);
				ps.setLong(1, rowsAliasToId.get(rec.rowAlias));
				ps.setInt(2, rec.columnId);
				if (rec.value instanceof Date) {
					java.sql.Date sqlDate = new java.sql.Date(((Date)rec.value).getTime());
					ps.setDate(3, sqlDate);	
				} else if (rec.value instanceof BigDecimal) {
					ps.setBigDecimal(3, (BigDecimal)rec.value);
				} else if (rec.value instanceof String) {
					ps.setString(3, (String)rec.value);
				} else {
					assert false;
				}
			}
			public int getBatchSize() {
				return values.size();
			}
		};
		getJdbcTemplate().batchUpdate(
			"insert into " + tableName + " (row_id, column_id, value) values (?, ?, ?)",
			bpss
		);		
	}
}
