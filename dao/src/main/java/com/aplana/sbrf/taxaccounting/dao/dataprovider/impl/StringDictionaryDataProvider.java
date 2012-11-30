package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringDictionaryDataProvider extends JdbcDictionaryDataProvider<String> {
	@Override
	public String getValue(ResultSet rs) throws SQLException {
		return rs.getString("value");
	}
}


