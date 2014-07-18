package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BookerStatementsSearchResultItemMapper implements RowMapper<BookerStatementsSearchResultItem> {

	@Override
	public BookerStatementsSearchResultItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        BookerStatementsSearchResultItem result = new BookerStatementsSearchResultItem();

		result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
		result.setBookerStatementsTypeId(SqlUtils.getInteger(rs, "type"));
		result.setAccountPeriodId(SqlUtils.getInteger(rs, "account_period_id"));
		result.setAccountPeriodName(rs.getString("account_period_name"));
        result.setAccountPeriodYear(SqlUtils.getInteger(rs, "year"));

		return result;
	}
}
