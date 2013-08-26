package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.ReportPeriodMapper;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Реализация DAO для работы с {@link ReportPeriod отчётными периодами}
 * @author srybakov
*/
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {

	@Override
	public ReportPeriod get(int periodId) {
		try {
			return 0 == periodId? null :
                    getJdbcTemplate().queryForObject(
					"select * from report_period where id = ?",
					new Object[]{periodId},
					new int[]{Types.NUMERIC},
					new ReportPeriodMapper()
		);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не существует периода с id=" + periodId);
		}
	}

	@Override
	public ReportPeriod getCurrentPeriod(TaxType taxType) {
		try {
			return getJdbcTemplate().queryForObject(
					"select rp.* from report_period rp join tax_period tp on rp.tax_period_id = tp.id where" +
							" tp.tax_type = ? and rp.is_active = 1 and rp.is_balance_period = 0",
					new Object[]{taxType.getCode()},
					new int[]{Types.VARCHAR},
					new ReportPeriodMapper()
		);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Существует несколько открытых периодов по виду налога " + taxType);
		}
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return getJdbcTemplate().query(
				"select * from report_period where tax_period_id = ? order by ord",
				new Object[]{taxPeriodId},
				new int[]{Types.NUMERIC},
				new ReportPeriodMapper()
		);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriodAndDepartmentId(int taxPeriodId, long departmentId) {
		return getJdbcTemplate().query(
				"select * from report_period where tax_period_id = ? and department_id = ? order by ord",
				new Object[]{taxPeriodId, departmentId},
				new int[]{Types.NUMERIC, Types.NUMERIC},
				new ReportPeriodMapper()
		);
	}

	@Override
	public void changeActive(int reportPeriodId, boolean active) {
		getJdbcTemplate().update(
				"update report_period set is_active = ? where id = ?",
				new Object[]{active, reportPeriodId},
				new int[]{Types.NUMERIC, Types.NUMERIC}
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

		Integer order = reportPeriod.getOrder();
		if (order == 0) {
			order = jt.queryForInt(
					"select max(ord) from report_period where tax_period_id = ?",
					new Object[]{reportPeriod.getTaxPeriodId()},
					new int[]{Types.NUMERIC}
			);
		}
		jt.update(
				"insert into report_period (id, name, is_active, months, tax_period_id, ord, " +
						"is_balance_period, department_id, dict_tax_period_id)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				reportPeriod.getName(),
				reportPeriod.isActive(),
				reportPeriod.getMonths(),
				reportPeriod.getTaxPeriodId(),
				order,
				reportPeriod.isBalancePeriod(),
				reportPeriod.getDepartmentId(),
				reportPeriod.getDictTaxPeriodId()
		);
		return id;
	}

    @Override
    public ReportPeriod getLastReportPeriod(TaxType taxType, long departmentId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select * from ( " +
                            "select rp.* " +
                            "from TAX_PERIOD tp, REPORT_PERIOD rp " +
                            "where TP.id = RP.TAX_PERIOD_ID " +
                            "and TP.TAX_TYPE = ? " +
                            "and RP.DEPARTMENT_ID = ? " +
                            "order by tp.end_date desc, rp.ord desc) " +
                            "where rownum = 1",
                    new Object[]{taxType.getCode(), departmentId},
                    new int[]{Types.VARCHAR, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
