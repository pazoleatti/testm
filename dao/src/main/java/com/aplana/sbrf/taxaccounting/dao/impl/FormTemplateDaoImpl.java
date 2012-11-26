package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;

@Repository
@Transactional(readOnly=true)
public class FormTemplateDaoImpl extends AbstractDao implements FormTemplateDao {
	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private ColumnDao columnDao;
	@Autowired
	private ScriptDao scriptDao;
	
	private class FormMapper implements RowMapper<FormTemplate> {
		private boolean deepFetch;
		public FormMapper(boolean deepFetch) {
			this.deepFetch = deepFetch; 
		}
		public FormTemplate mapRow(ResultSet rs, int index) throws SQLException {
			FormTemplate form = new FormTemplate();
			form.setId(rs.getInt("id"));
			form.setType(formTypeDao.getType(rs.getInt("type_id")));
			
			if (deepFetch) {
				form.getColumns().addAll(columnDao.getFormColumns(form.getId()));
				scriptDao.fillFormScripts(form);				
				String stRowsData = rs.getString("data_rows");
				if (stRowsData != null) { 
					// TODO: Механизм сериализации/десериализации предопределённых строк требует доработки,
					// так как оригинальная реализация не работала на WAS из-за другой версии Jackson.
					// Пока просто отключаем эту функцию
					/*
					final ObjectMapper objectMapper = new ObjectMapper();
					CustomDeserializerFactory sf = new CustomDeserializerFactory();
					objectMapper.setDeserializerProvider(new StdDeserializerProvider(sf));
					sf.addSpecificMapping(DataRow.class, new DataRowDeserializer(form, FormatUtils.getShortDateFormat(), true));
					List<DataRow> rows;
					try {
						rows = objectMapper.readValue(stRowsData, TypeFactory.collectionType(List.class, DataRow.class));
					} catch (IOException e) {
						logger.error("Failed to read json", e);
						throw new DaoException("Не удалось прочитать данные в формате json: " + e.getMessage());
					}
					form.getRows().addAll(rows); */
					
					logger.warn("Disabled due incompartibility with WAS");
				}
			}
			return form;
		}
	}

	@Cacheable("Form")
	public FormTemplate get(int formId) {
		logger.info("Fetching FormTemplate with id = " + formId);
		JdbcTemplate jt = getJdbcTemplate();
		final FormTemplate form = jt.queryForObject(
			"select * from form where id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new FormMapper(true)
		);
		return form;
	}

	@Transactional(readOnly=false)
	@CacheEvict(value="Form", key="#form.id")
	public int save(final FormTemplate form) {
		final ObjectMapper objectMapper = new ObjectMapper();
		CustomSerializerFactory sf = new CustomSerializerFactory();
		sf.addSpecificMapping(DataRow.class, new DataRowSerializer(FormatUtils.getShortDateFormat()));
		objectMapper.setSerializerFactory(sf);

		final int formTemplateId = form.getId().intValue();
		// TODO: Механизм сериализации/десериализации предопределённых строк требует доработки,
		// так как оригинальная реализация не работала на WAS из-за другой версии Jackson.
		// Пока просто отключаем эту функцию
		/*
		String rowsJson;
		OrderUtils.reorder(form.getRows());
		try {
			rowsJson = objectMapper.writeValueAsString(form.getRows());
		} catch (Exception e) {
			logger.error("Json generation error", e);
			throw new DaoException("Не удалось сериализовать значение строки данных в JSON: " + e.getMessage());
		}
		
		
		// TODO: создание новых версий формы потребует инсертов в form
		getJdbcTemplate().update(
			"update form set data_rows = ? where id = ?",
			new Object[] { rowsJson, formId },
			new int[] { Types.VARCHAR, Types.NUMERIC }
		);
		
		*/

		columnDao.saveFormColumns(form);
		scriptDao.saveFormScripts(form);
		return formTemplateId;
	}

	public List<FormTemplate> listAll() {
		return getJdbcTemplate().query("select * from form", new FormMapper(false));
	}
}