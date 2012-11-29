package com.aplana.sbrf.taxaccounting.dao.impl.util;


import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FormDataRowMapper implements RowMapper<FormData> {

	private FormTemplateDao formTemplateDao;

	public FormDataRowMapper(FormTemplateDao formTemplateDao){
		this.formTemplateDao = formTemplateDao;
	}

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
		fd.setReportPeriodId(rs.getInt("report_period_id"));
		return fd;
	}

}
