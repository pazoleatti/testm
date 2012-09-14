package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

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
import com.aplana.sbrf.taxaccounting.model.Form;

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
		public Form mapRow(ResultSet rs, int index) throws SQLException {
			Form form = new Form();
			form.setId(rs.getInt("id"));
			form.setType(formTypeDao.getType(rs.getInt("type_id")));
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
			new FormMapper()
		);
		
		form.getColumns().addAll(columnDao.getFormColumns(formId));
		scriptDao.fillFormScripts(form);
		return form;
	}

	@Transactional(readOnly=false)
	@CacheEvict(value="Form", key="#form.id")
	public int saveForm(final Form form) {
		// TODO: обновление/вставка записи в form
		columnDao.saveFormColumns(form);
		scriptDao.saveFormScripts(form);
		
		int formId = form.getId();
		getJdbcTemplate().update("delete from form_row where form_id = ?", new Object[] { formId }, new int[] { Types.INTEGER });
		return form.getId().intValue();
	}

	public List<Form> listForms() {
		return getJdbcTemplate().query("select * from form", new FormMapper());
	}
}