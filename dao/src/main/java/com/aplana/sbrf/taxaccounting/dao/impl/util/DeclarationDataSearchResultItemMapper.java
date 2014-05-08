package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class DeclarationDataSearchResultItemMapper implements RowMapper<DeclarationDataSearchResultItem> {

	@Override
	public DeclarationDataSearchResultItem mapRow(ResultSet rs, int i) throws SQLException {
		DeclarationDataSearchResultItem result = new DeclarationDataSearchResultItem();

		result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
		result.setDepartmentName(rs.getString("department_name"));
		result.setDepartmentType(DepartmentType.fromCode(SqlUtils.getInteger(rs,"department_type")));
		result.setDeclarationDataId(SqlUtils.getLong(rs,"declaration_data_id"));
		result.setDeclarationTemplateId(SqlUtils.getInteger(rs,"declaration_template_id"));
		result.setReportPeriodId(SqlUtils.getInteger(rs,"report_period_id"));
		result.setReportPeriodName(rs.getString("report_period_name"));
		result.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
		result.setAccepted(rs.getBoolean("is_accepted"));
        result.setReportPeriodYear(SqlUtils.getInteger(rs,"year"));
		result.setDeclarationType(rs.getString("declaration_type_name"));

		return result;
	}
}
