package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

@Repository
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

	@Autowired
	private FormTemplateDao formTemplateDao;

	private class FormDataRowMapper implements RowMapper<FormData> {
		public FormData mapRow(ResultSet rs, int index) throws SQLException {
			long formDataId = rs.getLong("id");
			int formId = rs.getInt("form_id");
			FormTemplate form = formTemplateDao.get(formId);
			FormData fd = new FormData();
			fd.initFormTemplateParams(form);
			fd.setId(formDataId);
			fd.setDepartmentId(rs.getInt("department_id"));
			fd.setState(WorkflowState.fromId(rs.getInt("state")));
			fd.setKind(FormDataKind.fromId(rs.getInt("kind")));
			return fd;
		}
	}
	@Override
	public List<FormData> findByFilter(FormDataDaoFilter dataFilter){
		String query = "select * from FORM_DATA inner join form ON FORM_DATA.FORM_ID = FORM.ID where " +
				"FORM_DATA.DEPARTMENT_ID in " + transformToSqlInStatement(dataFilter.getDepartment())+
				" and FORM.TYPE_ID in " + transformToSqlInStatement(dataFilter.getKind());

		return getJdbcTemplate().query(query, new FormDataRowMapper());
	}

}
