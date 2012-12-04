package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class NumericDictionaryDataProvider extends JdbcDictionaryDataProvider<BigDecimal> {

	@Override
	public BigDecimal getValue(ResultSet rs) throws SQLException {
		return rs.getBigDecimal("value");
	}

	@Override
	public List<DictionaryItem<BigDecimal>> getValues(String pattern) {
		String preparedPattern = preparePattern(pattern);
		return getJdbcTemplate().query(
				"select * from (" + getSqlQuery() + ") where to_nchar(value) like ? escape '\\' or lower(name) like ? escape '\\'",
				new Object[]{preparedPattern, preparedPattern},
				new int[]{Types.VARCHAR, Types.VARCHAR},
				new ItemRowMapper()
		);
	}
}
