package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleNumericDictionaryDataProvider extends JdbcDictionaryDataProvider<BigDecimal> {

	@Override
	public BigDecimal getValue(ResultSet rs) throws SQLException {
		return rs.getBigDecimal("value");
	}
}
