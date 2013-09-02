package com.aplana.sbrf.taxaccounting.dao.impl.util;


import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FormDataSearchResultItemMapper implements RowMapper<FormDataSearchResultItem> {
	@Override
	public FormDataSearchResultItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		FormDataSearchResultItem result = new FormDataSearchResultItem();

		result.setDepartmentId(rs.getInt("department_id"));
		result.setDepartmentName(rs.getString("department_name"));
		result.setDepartmentType(DepartmentType.fromCode(rs.getInt("department_type")));
		result.setFormDataId(rs.getLong("form_data_id"));
		result.setFormDataKind(FormDataKind.fromId(rs.getInt("form_data_kind_id")));
		result.setFormTemplateId(rs.getInt("form_template_id"));
		result.setFormTypeId(rs.getInt("form_type_id"));
		result.setFormTypeName(rs.getString("form_type_name"));
		result.setReportPeriodId(rs.getInt("report_period_id"));
		result.setReportPeriodName(rs.getString("report_period_name"));
		result.setState(WorkflowState.fromId(rs.getInt("state")));
		result.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
        Integer periodOrder = rs.getInt("period_order");
        result.setPeriodOrder(rs.wasNull() ? null : periodOrder);

		return result;
	}
}
