package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DictionaryTaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;


@Repository
@Transactional
public class DictionaryTaxPeriodDaoImpl  extends AbstractDao implements DictionaryTaxPeriodDao {

	private final static class DictionaryTaxPeriodMapper implements RowMapper<DictionaryTaxPeriod> {
		public DictionaryTaxPeriod mapRow(ResultSet rs, int index) throws SQLException {
			final DictionaryTaxPeriod result = new DictionaryTaxPeriod();
			result.setCode(rs.getInt("code"));
			result.setName(rs.getString("name"));
			return result;
		}
	}
	@Override
	public List<DictionaryTaxPeriod> getByTaxType(TaxType taxType) {
		return getJdbcTemplate().query(
				"select * from dict_tax_period where " + taxType.getCode() +" = 1",
				new Object[]{},
				new DictionaryTaxPeriodMapper()
		);

	}

	@Override
	public DictionaryTaxPeriod get(int code) {
		return getJdbcTemplate().queryForObject(
				"select * from dict_tax_period where code = ?",
				new Object[]{code},
				new int[]{Types.NUMERIC},
				new DictionaryTaxPeriodMapper()
		);
	}
}
