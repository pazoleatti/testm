package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class StringDictionaryDataProvider extends JdbcDictionaryDataProvider<String> {
	@Override
	public String getValue(ResultSet rs) throws SQLException {
		return rs.getString("value");
	}

	public List<DictionaryItem<String>> getValues(String pattern) {
		String preparedPattern = preparePattern(pattern);
		return getJdbcTemplate().query(
				"select * from (" + getSqlQuery() + ") where lower(value) like ? escape '\\' or lower(name) like ? escape '\\'",
				new Object[]{preparedPattern, preparedPattern},
				new int[]{Types.VARCHAR, Types.VARCHAR},
				new ItemRowMapper()
		);
	}
}


