package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.Form;

@Repository
@Transactional(readOnly=true)
public class FormDaoImpl extends AbstractDao implements FormDao {
	@Autowired
	private FormTypeDao formTypeDao;
	
	private class FormMapper implements RowMapper<Form> {
		@Override
		public Form mapRow(ResultSet rs, int index) throws SQLException {
			Form form = new Form();
			form.setId(rs.getInt("id"));
			// TODO: очень неоптимально, нужно переделать
			form.setType(formTypeDao.getType(rs.getInt("type_id")));
			return form;
		}
	}
	
	@Override
	public Form getForm(int formId) {
		return getJdbcTemplate().queryForObject(
			"select * from form where id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			new FormMapper()
		);
	}

	@Override
	@Transactional(readOnly=false)
	public int saveForm(Form form) {
		return 0;
	}

	@Override
	public List<Form> listForms() {
		return getJdbcTemplate().query("select * from form", new FormMapper());
	}
}