package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Реализация DAO для работы с {@link ReportPeriod отчётными периодами}
 * @author srybakov
*/
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {

	private final static RowMapper<ReportPeriod> REPORT_PERIOD_MAPPER = new RowMapper<ReportPeriod>() {
		@Override
		public ReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
			ReportPeriod reportPeriod = new ReportPeriod();
			reportPeriod.setId(rs.getInt("id"));
			reportPeriod.setName(rs.getString("name"));
			reportPeriod.setMonths(rs.getInt("months"));
			reportPeriod.setTaxPeriodId(rs.getInt("tax_period_id"));
			reportPeriod.setOrder(rs.getInt("ord"));
			reportPeriod.setDictTaxPeriodId(rs.getInt("dict_tax_period_id"));
			return reportPeriod;
		}
	};

	@Override
	public ReportPeriod get(int id) {
		try {
			return 0 == id? null :
                    getJdbcTemplate().queryForObject(
					"select * from report_period where id = ?",
					new Object[]{id},
					new int[]{Types.NUMERIC},
					REPORT_PERIOD_MAPPER
		);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не существует периода с id=" + id);
		}
	}



	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return getJdbcTemplate().query(
				"select * from report_period where tax_period_id = ? order by ord",
				new Object[]{taxPeriodId},
				new int[]{Types.NUMERIC},
				REPORT_PERIOD_MAPPER
		);
	}


	@Override
	@Transactional(readOnly = false)
	public int add(ReportPeriod reportPeriod) {
		JdbcTemplate jt = getJdbcTemplate();

		Integer id = reportPeriod.getId();
		if (id == null) {
			id = generateId("seq_report_period", Integer.class);
		}

		jt.update(
				"insert into report_period (id, name, months, tax_period_id, ord," +
						" dict_tax_period_id)" +
						" values (?, ?, ?, ?, ?, ?)",
				id,
				reportPeriod.getName(),
				reportPeriod.getMonths(),
				reportPeriod.getTaxPeriodId(),
				reportPeriod.getOrder(),
				reportPeriod.getDictTaxPeriodId()
		);
		reportPeriod.setId(id);
		return id;
	}

    @Override
    public ReportPeriod getLastReportPeriod(TaxType taxType, long departmentId) {
    	throw new DaoException("Ошибок не заводить. В разработке");
    }
    
	@Override
	public ReportPeriod getCurrentPeriod(TaxType taxType) {
		throw new DaoException("Ошибок не заводить. В разработке"); 
	}
	

	@Override
	public void changeActive(int reportPeriodId, boolean active) {
		throw new DaoException("Ошибок не заводить. В разработке");
	}
	
	@Override
	public List<ReportPeriod> listByTaxPeriodAndDepartmentId(int taxPeriodId, long departmentId) {
		throw new DaoException("Ошибок не заводить. В разработке");
	}
}
