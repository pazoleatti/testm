package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeclarationDataSearchResultItemMapper implements RowMapper<DeclarationDataSearchResultItem> {
	@Override
	public DeclarationDataSearchResultItem mapRow(ResultSet rs, int i) throws SQLException {
		DeclarationDataSearchResultItem result = new DeclarationDataSearchResultItem();

		result.setDepartmentId(rs.getInt("department_id"));
		result.setDepartmentName(rs.getString("department_name"));
		result.setDepartmentType(DepartmentType.fromCode(rs.getInt("department_type")));
		result.setDeclarationDataId(rs.getLong("declaration_data_id"));
		result.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
		result.setReportPeriodId(rs.getInt("report_period_id"));
		result.setReportPeriodName(rs.getString("report_period_name"));
		result.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
		result.setAccepted(rs.getBoolean("is_accepted"));
		result.setDeclarationType(rs.getString("declaration_type_name"));

		return result;
	}
}
