package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Реализация DAO для работы с {@link com.aplana.sbrf.taxaccounting.model.TaxPeriod налоговыми периодами}
 */
@Repository
@Transactional(readOnly = true)
public class TaxPeriodDaoImpl extends AbstractDao implements TaxPeriodDao {

	private final class TaxPeriodRowMapper implements RowMapper<TaxPeriod> {
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

	@Override
	public List<TaxPeriod> listByTaxTypeAndDate(TaxType taxType, Date from, Date to) {
		try {
			return getJdbcTemplate().query(
					"select * from tax_period where tax_type = ? and end_date>=? and start_date<=?",
					new Object[]{taxType.getCode(), from, to},
					new int[] { Types.VARCHAR, Types.DATE, Types.DATE },
					new TaxPeriodRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось получить список налоговых периодов с типом = " + taxType.getCode());
		}
	}

	@Override
	public int add(TaxPeriod taxPeriod) {
		JdbcTemplate jt = getJdbcTemplate();

		Integer id = taxPeriod.getId();
		if (id == null) {
			id = generateId("seq_tax_period", Integer.class);
		}
		jt.update(
				"insert into tax_period (id, tax_type, start_date, end_date)" +
						" values (?, ?, ?, ?)",
				new Object[]{
						id,
						taxPeriod.getTaxType().getCode(),
						taxPeriod.getStartDate(),
						taxPeriod.getEndDate()
				},
				new int[]{Types.NUMERIC, Types.VARCHAR, Types.DATE, Types.DATE}

		);
		taxPeriod.setId(id);
		return id;
	}

	@Override
	public TaxPeriod getLast(TaxType taxType) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from tax_period where tax_type=? and start_date = (select max(start_date) from tax_period)",
					new Object[]{taxType.getCode()},
					new int[] { Types.VARCHAR},
					new TaxPeriodRowMapper()
			);

		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
}