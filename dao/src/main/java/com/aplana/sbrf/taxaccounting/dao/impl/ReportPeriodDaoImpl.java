package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Реализация DAO для работы с {@link ReportPeriod отчётными периодами}
 * @author srybakov
*/
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {
	
	@Autowired
	private TaxPeriodDao taxPeriodDao;
	
    private class ReportPeriodMapper implements RowMapper<ReportPeriod> {
        @Override
        public ReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            ReportPeriod reportPeriod = new ReportPeriod();
            reportPeriod.setId(rs.getInt("id"));
            reportPeriod.setName(rs.getString("name"));
            reportPeriod.setTaxPeriod(taxPeriodDao.get(rs.getInt("tax_period_id")));
            reportPeriod.setOrder(rs.getInt("ord"));
            reportPeriod.setStartDate(rs.getDate("start_date"));
            reportPeriod.setEndDate(rs.getDate("end_date"));
            reportPeriod.setDictTaxPeriodId(rs.getInt("dict_tax_period_id"));
			reportPeriod.setCalendarStartDate(rs.getDate("calendar_start_date"));
            return reportPeriod;
        }
    }

	@Override
	public ReportPeriod get(int id) {
		try {
			return 0 == id? null :
                    getJdbcTemplate().queryForObject(
					"select id, name, tax_period_id, ord, start_date, end_date, dict_tax_period_id, calendar_start_date  " +
							"from report_period where id = ?",
					new Object[]{id},
					new int[]{Types.NUMERIC},
					new ReportPeriodMapper()
		);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не существует периода с id=" + id);
		}
	}

	@Override
	public List<ReportPeriod> get(List<Integer> ids) {
		return getJdbcTemplate().query(
				"select id, name, tax_period_id, ord, start_date, end_date, dict_tax_period_id, calendar_start_date " +
						"from report_period where " + SqlUtils.transformToSqlInStatement("id", ids),
				new ReportPeriodMapper()
		);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return getJdbcTemplate().query(
				"select id, name, tax_period_id, ord, start_date, end_date, dict_tax_period_id, calendar_start_date " +
						"from report_period where tax_period_id = ? order by ord",
				new Object[]{taxPeriodId},
				new int[]{Types.NUMERIC},
				new ReportPeriodMapper()
		);
	}

	@Override
	@Transactional(readOnly = false)
	public int save(ReportPeriod reportPeriod) {
		JdbcTemplate jt = getJdbcTemplate();

		Integer id = reportPeriod.getId();
		if (id == null) {
			id = generateId("seq_report_period", Integer.class);
		}

		jt.update(
				"insert into report_period (id, name, tax_period_id, ord," +
						" dict_tax_period_id, start_date, end_date, calendar_start_date)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				reportPeriod.getName(),
				reportPeriod.getTaxPeriod().getId(),
				reportPeriod.getOrder(),
				reportPeriod.getDictTaxPeriodId(),
				reportPeriod.getStartDate(),
				reportPeriod.getEndDate(),
				reportPeriod.getCalendarStartDate()
		);
		reportPeriod.setId(id);
		return id;
	}

	@Override
	public void remove(int reportPeriodId) {
		getJdbcTemplate().update(
				"delete from report_period where id = ?",
				new Object[] {reportPeriodId},
				new int[] {Types.NUMERIC}
		);
	}

	@Override
    public List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList) {

        return getJdbcTemplate().query(
                "select rp.id, rp.name, rp.tax_period_id, rp.ord, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
						"rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
                        "(select distinct report_period_id from department_report_period " +
                        "where correction_date is null and "+ SqlUtils.transformToSqlInStatement("department_id", departmentList)+") " +
                        "and rp.tax_period_id = tp.id " +
                        "and tp.tax_type = \'" + String.valueOf(taxType.getCode()) + "\' " +
                        "order by tp.year desc, rp.ord",
                new ReportPeriodMapper());
    }

	@Override
	public List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList,
                                                                    boolean withoutBalance, boolean withoutCorrect) {

		return getJdbcTemplate().query(
				"select rp.id, rp.name, rp.tax_period_id, rp.ord, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
						"rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
						"(select distinct report_period_id from department_report_period " +
						"where "+ SqlUtils.transformToSqlInStatement("department_id", departmentList)+" and is_active=1 " +
						(withoutBalance ? " and is_balance_period=0 " : "") + (withoutCorrect ? "and correction_date is null" : "") + " ) " +
						"and rp.tax_period_id = tp.id " +
						"and tp.tax_type = \'" + String.valueOf(taxType.getCode()) +"\' " +
						"order by tp.year desc, rp.ord",
				new ReportPeriodMapper());
	}

    @Override
    public List<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId) {
        try {
            return getJdbcTemplate().query(
                    "select * from REPORT_PERIOD rp " +
                            "left join TAX_PERIOD tp on rp.TAX_PERIOD_ID=tp.ID " +
                            "left join DEPARTMENT_REPORT_PERIOD drp on rp.ID=drp.REPORT_PERIOD_ID  " +
                            "where tp.TAX_TYPE = ? and drp.DEPARTMENT_ID= ? " +
                            "and drp.IS_BALANCE_PERIOD=0 and drp.IS_ACTIVE=0 and CORRECTION_DATE is null",
                    new Object[]{String.valueOf(taxType.getCode()), departmentId},
                    new int[] { Types.VARCHAR, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, tax_period_id, ord, start_date, end_date, dict_tax_period_id, calendar_start_date " +
							"from report_period where tax_period_id = ? and dict_tax_period_id = ?",
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с tax_period_id=" + taxPeriodId + " и dict_tax_period_id = " + dictTaxPeriodId);
        }
    }

	@Override
	public ReportPeriod getReportPeriodByDate(TaxType taxType, Date date) {
		try {
			List<ReportPeriod> result = getJdbcTemplate().query(
					"select rp.id, rp.name, rp.tax_period_id, rp.ord, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
							"rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
							"where tp.tax_type = ? and rp.end_date=?",
					new Object[]{taxType.getCode(), date},
					new int[] { Types.VARCHAR, Types.DATE },
					new ReportPeriodMapper()
			);
			if (result.isEmpty()) {
				throw new DaoException(String.format("Не найден отчетный период с типом = \"%s\" на дату \"%tD\"", taxType.getCode(), date));
			}
			return result.get(0);
		} catch (Exception e) {
			// изменить форматирование для даты на "%td %<tm,%<tY" (31.12.2013) вместо "%tD" (12/31/13)(Marat Fayzullin 02.18.2014)
			throw new DaoException(String.format("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", taxType.getCode(), date));
		}
	}

	@Override
	public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate) {
		try {
			return getJdbcTemplate().query(
					"select rp.id, rp.name, rp.tax_period_id, rp.ord, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
							"rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
							"where tp.tax_type = ? and rp.end_date>=? and rp.calendar_start_date<=?",
					new Object[]{taxType.getCode(), startDate, endDate},
					new int[] { Types.VARCHAR, Types.DATE, Types.DATE },
					new ReportPeriodMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format("Не найдены отчетные периоды с типом = \"%s\" за период (%s; %s)", taxType.getCode(), startDate, endDate));
		}
	}

}

