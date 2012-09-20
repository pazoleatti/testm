package com.aplana.sbrf.taxaccounting.dao.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.util.OrderUtils;
import com.aplana.sbrf.taxaccounting.util.json.DataRowDeserializer;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;

@Repository
@Transactional(readOnly=true)
public class FormDaoImpl extends AbstractDao implements FormDao {
	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private ColumnDao columnDao;
	@Autowired
	private ScriptDao scriptDao;
	
	private class FormMapper implements RowMapper<Form> {
		private boolean deepFetch;
		public FormMapper(boolean deepFetch) {
			this.deepFetch = deepFetch; 
		}
		public Form mapRow(ResultSet rs, int index) throws SQLException {
			Form form = new Form();
			form.setId(rs.getInt("id"));
			form.setType(formTypeDao.getType(rs.getInt("type_id")));
			
			if (deepFetch) {
				form.getColumns().addAll(columnDao.getFormColumns(form.getId()));
				scriptDao.fillFormScripts(form);				
				String stRowsData = rs.getString("rows");
				if (stRowsData != null) {
					final ObjectMapper objectMapper = new ObjectMapper();
					SimpleModule module = new SimpleModule("taxaccounting-dao-read", new Version(1, 0, 0, null));
					module.addDeserializer(DataRow.class, new DataRowDeserializer(form, FormatUtils.getShortDateFormat(), true));
					objectMapper.registerModule(module);
					List<DataRow> rows;
					try {
						rows = objectMapper.readValue(stRowsData, new TypeReference<List<DataRow>>() {});
					} catch (IOException e) {
						logger.error("Failed to read json", e);
						throw new DaoException("Не удалось прочитать данные в формате json: " + e.getMessage());
					}
					form.getRows().addAll(rows);
				}
			}
			return form;
		}
	}

	@Cacheable("Form")
	public Form getForm(int formId) {
		logger.info("Fetching Form with id = " + formId);
		JdbcTemplate jt = getJdbcTemplate();
		final Form form = jt.queryForObject(
			"select * from form where id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new FormMapper(true)
		);
		return form;
	}

	@Transactional(readOnly=false)
	@CacheEvict(value="Form", key="#form.id")
	public int saveForm(final Form form) {
		final ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting-dao-write", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer(FormatUtils.getShortDateFormat()));
		objectMapper.registerModule(module);
		
		String rowsJson;
		OrderUtils.reorder(form.getRows());
		try {
			rowsJson = objectMapper.writeValueAsString(form.getRows());
		} catch (Exception e) {
			logger.error("Json generation error", e);
			throw new DaoException("Не удалось сериализовать значение строки данных в JSON: " + e.getMessage());
		}
		
		final int formId = form.getId();
		// TODO: создание новых версий формы потребует инсертов в form
		getJdbcTemplate().update(
			"update form set rows = ? where id = ?",
			new Object[] { rowsJson, formId },
			new int[] { Types.VARCHAR, Types.NUMERIC }
		);

		columnDao.saveFormColumns(form);
		scriptDao.saveFormScripts(form);
		return form.getId().intValue();
	}

	public List<Form> listForms() {
		return getJdbcTemplate().query("select * from form", new FormMapper(false));
	}
}