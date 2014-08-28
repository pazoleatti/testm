package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FormDataSearchResultItemMapper implements RowMapper<FormDataSearchResultItem> {

	@Override
	public FormDataSearchResultItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		FormDataSearchResultItem result = new FormDataSearchResultItem();

		result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
		result.setDepartmentName(rs.getString("department_name"));
		result.setDepartmentType(DepartmentType.fromCode(SqlUtils.getInteger(rs,"department_type")));
		result.setFormDataId(SqlUtils.getLong(rs,"form_data_id"));
		result.setFormDataKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"form_data_kind_id")));
		result.setFormTemplateId(SqlUtils.getInteger(rs,"form_template_id"));
		result.setFormTypeId(SqlUtils.getInteger(rs,"form_type_id"));
		result.setFormTypeName(rs.getString("form_type_name"));
		result.setReportPeriodId(SqlUtils.getInteger(rs,"report_period_id"));
		result.setReportPeriodName(rs.getString("report_period_name"));
		result.setState(WorkflowState.fromId(SqlUtils.getInteger(rs,"state")));
		result.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
        result.setReportPeriodYear(SqlUtils.getInteger(rs,"year"));
        Integer reportPeriodMonth = SqlUtils.getInteger(rs,"period_order");
        result.setReportPeriodMonth(rs.wasNull() ? null : reportPeriodMonth);
        Integer returnSign = SqlUtils.getInteger(rs,"return_sign");
        result.setReturnSign(rs.wasNull() ? null : 1 == returnSign);

		return result;
	}
}
