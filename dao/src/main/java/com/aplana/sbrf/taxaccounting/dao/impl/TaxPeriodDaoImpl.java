package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Реализация DAO для работы с {@link com.aplana.sbrf.taxaccounting.model.TaxPeriod налоговыми периодами}
 */
@Repository
@Transactional(readOnly = true)
public class TaxPeriodDaoImpl extends AbstractDao implements TaxPeriodDao {

	private static final class TaxPeriodRowMapper implements RowMapper<TaxPeriod> {
		@Override
		public TaxPeriod mapRow(ResultSet rs, int index) throws SQLException {
			TaxPeriod t = new TaxPeriod();
			t.setId(rs.getInt("id"));
			t.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
			t.setStartDate(rs.getDate("start_date"));
			t.setEndDate(rs.getDate("end_date"));
			return t;
		}
	}

	@Override
	public TaxPeriod get(int taxPeriodId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from tax_period where id = ?",
					new Object[] { taxPeriodId },
					new int[] { Types.NUMERIC },
					new TaxPeriodRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти налоговый период с id = " + taxPeriodId);
		}
	}

	@Override
	public List<TaxPeriod> listByTaxType(TaxType taxType) {
		try {
			return getJdbcTemplate().query(
					"select * from tax_period where tax_type = ?",
					new Object[]{taxType.getCode()},
					new int[] { Types.VARCHAR },
					new TaxPeriodRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось получить список налоговых периодов с типом = " + taxType.getCode());
		}
	}
}