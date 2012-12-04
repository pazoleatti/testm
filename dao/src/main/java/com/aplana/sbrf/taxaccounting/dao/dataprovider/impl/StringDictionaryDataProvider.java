package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.StringFilterDictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class StringDictionaryDataProvider extends JdbcDictionaryDataProvider<String> implements StringFilterDictionaryDataProvider {
	@Override
	public String getValue(ResultSet rs) throws SQLException {
		return rs.getString("value");
	}

	public List<DictionaryItem<String>> getValues(String valuePattern) {
		return getJdbcTemplate().query(
			"select * from (" + getSqlQuery() + ") where value like ?",
			new Object[] { valuePattern },
			new int[] { Types.VARCHAR },
			new ItemRowMapper()
		);
	}
}


