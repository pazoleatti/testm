package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

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
        result.setState(State.fromId(rs.getInt("state")));
        result.setReportPeriodYear(SqlUtils.getInteger(rs,"year"));
		result.setDeclarationType(rs.getString("declaration_type_name"));
        result.setCorrectionDate(rs.getDate("correction_date"));
        result.setTaxOrganCode(rs.getString("tax_organ_code"));
        result.setTaxOrganKpp(rs.getString("kpp"));
        result.setOktmo(rs.getString("oktmo"));
        result.setAsnuId(SqlUtils.getLong(rs,"asnu_id"));
        result.setFileName(rs.getString("file_name"));
        result.setDeclarationFormKind(DeclarationFormKind.fromId(SqlUtils.getLong(rs,"form_kind")));
        result.setNote(rs.getString("note"));
        result.setDocStateId(SqlUtils.getLong(rs,"doc_state_id"));
        result.setDocState(rs.getString("doc_state"));
        result.setCreateDate(rs.getDate("creation_date"));
        Timestamp decl_data_creation_date = rs.getTimestamp("decl_data_creation_date");
        if (decl_data_creation_date != null) {
            result.setDeclarationDataCreationDate(new Date(decl_data_creation_date.getTime()));
        }
        result.setImportDeclarationDataUserName(rs.getString("import_decl_data_user_name"));
        return result;
	}
}
