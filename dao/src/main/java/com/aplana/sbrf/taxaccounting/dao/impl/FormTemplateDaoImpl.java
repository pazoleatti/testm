package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;

@Repository
@Transactional(readOnly = true)
public class FormTemplateDaoImpl extends AbstractDao implements FormTemplateDao {
	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private ColumnDao columnDao;
	@Autowired
	private ScriptDao scriptDao;
	private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();

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
					form.getRows().addAll(xmlSerializationUtils.deserialize(stRowsData, form.getColumns()));
					logger.warn("Disabled due incompartibility with WAS");
				}
			}
			return form;
		}
	}

	//@Cacheable("FormTemplate")
	// TODO: пока не будет версии кеша, нормально работающей на кластере
	public FormTemplate get(int formId) {
		logger.info("Fetching FormTemplate with id = " + formId);
		JdbcTemplate jt = getJdbcTemplate();
		final FormTemplate form = jt.queryForObject(
				"select * from form where id = ?",
				new Object[]{formId},
				new int[]{Types.NUMERIC},
				new FormMapper(true)
		);
		return form;
	}

	@Transactional(readOnly = false)
	//@CacheEvict(value = "FormTemplate", key = "#form.id")
	// TODO: пока не будет версии кеша, нормально работающей на кластере	
	public int save(final FormTemplate form) {
		final int formTemplateId = form.getId().intValue();

		List<DataRow> rows = form.getRows();
		if (rows != null && !rows.isEmpty()) {
			String xml = xmlSerializationUtils.serialize(rows);
			// TODO: создание новых версий формы потребует инсертов в form
			getJdbcTemplate().update(
					"update form set data_rows = ? where id = ?",
					new Object[]{xml, formTemplateId},
					new int[]{Types.VARCHAR, Types.NUMERIC}
			);
		} else {
			getJdbcTemplate().update(
					"update form set data_rows = null where id = ?",
					new Object[]{formTemplateId},
					new int[]{Types.NUMERIC}
			);
		}

		columnDao.saveFormColumns(form);
		scriptDao.saveFormScripts(form);
		return formTemplateId;
	}

	public List<FormTemplate> listAll() {
		return getJdbcTemplate().query("select * from form", new FormMapper(false));
	}

	@Override
	public int getActiveFormTemplateId(int formTypeId) {
		JdbcTemplate jt = getJdbcTemplate();
		FormTemplate form;
		try {
			form =jt.queryForObject(
					"select * from form where type_id = ? and is_active = ?",
					new Object[]{formTypeId,1},
					new int[]{Types.NUMERIC,Types.NUMERIC}, 
					new FormMapper(false)
					);
			return form.getId();
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Для данного вида налоговой формы %d не найдено активного шаблона налоговой формы.",formTypeId);
		}catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Для даного вида налоговой формы %d найдено несколько активных шаблонов налоговой формы.",formTypeId);
		}
	}
}