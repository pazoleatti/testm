package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

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
            Integer id = SqlUtils.getInteger(rs, "id");
            reportPeriod.setId(id);
            reportPeriod.setName(rs.getString("name"));
            reportPeriod.setTaxPeriod(taxPeriodDao.get(SqlUtils.getInteger(rs, "tax_period_id")));
            reportPeriod.setStartDate(rs.getDate("start_date"));
            reportPeriod.setEndDate(rs.getDate("end_date"));
            reportPeriod.setDictTaxPeriodId(SqlUtils.getInteger(rs, "dict_tax_period_id"));
            Date calendarStartDate = rs.getDate("calendar_start_date");
			reportPeriod.setCalendarStartDate(calendarStartDate);
            reportPeriod.setOrder(getReportOrder(calendarStartDate, id));
            return reportPeriod;
        }

        /**
         * Получить порядковый номер отчётного периода в налоговом, основываясь на календарную дату начала ОП:
         * 1 января	 - 1
         * 1 апреля	 - 2
         * 1 июля	 - 3
         * 1 октября - 4
         * @return порядковый номер отчётного периода в налоговом
         */
        private int getReportOrder(Date date, Integer id) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int monthNumber = calendar.get(Calendar.MONTH);
            List<Integer> correctMonths = Arrays.asList(0, 3, 6, 9);
            // день всегда первое число месяца; месяцы только янв, апр, июл и окт
            if (calendar.get(Calendar.DAY_OF_MONTH) != 1 || (!correctMonths.contains(monthNumber))) {
                throw new DaoException("Неверная календарная дата начала отчетного периода с id=" + id);
            }
            return (monthNumber + 3) / 3;
        }
    }

	@Override
	public ReportPeriod get(int id) {
		try {
			return 0 == id? null :
                    getJdbcTemplate().queryForObject(
					"select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date  " +
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
				"select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date " +
						"from report_period where " + SqlUtils.transformToSqlInStatement("id", ids),
				new ReportPeriodMapper()
		);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return getJdbcTemplate().query(
				"select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date " +
						"from report_period where tax_period_id = ?",
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
				"insert into report_period (id, name, tax_period_id, " +
						" dict_tax_period_id, start_date, end_date, calendar_start_date)" +
						" values (?, ?, ?, ?, ?, ?, ?)",
				id,
				reportPeriod.getName(),
				reportPeriod.getTaxPeriod().getId(),
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
                "select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
						"rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
                        "(select distinct report_period_id from department_report_period " +
                        "where correction_date is null and "+ SqlUtils.transformToSqlInStatement("department_id", departmentList)+") " +
                        "and rp.tax_period_id = tp.id " +
                        "and tp.tax_type = \'" + String.valueOf(taxType.getCode()) + "\' " +
                        "order by tp.year desc, rp.calendar_start_date",
                new ReportPeriodMapper());
    }

    @Override
    public List<Long> getPeriodsByTaxTypesAndDepartments(List<TaxType> taxTypes, List<Integer> departmentList) {
        Object[] params = new Object[departmentList.size()];
        int cnt = 0;
        for (Integer departmentId : departmentList) {
            params[cnt++] = departmentId;
        }

        try {
            return getJdbcTemplate().queryForList(
                    "select rp.id from report_period rp, tax_period tp where rp.id in " +
                            "(select distinct report_period_id from department_report_period " +
                            "where correction_date is null and department_id in("+ SqlUtils.preparePlaceHolders(departmentList.size())+")) " +
                            "and rp.tax_period_id = tp.id " +
                            "and tp.tax_type in " + SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) +
                            "order by tp.year desc, rp.calendar_start_date", Long.class, params);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new  DaoException("", e);
        }
    }

    @Override
	public List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList,
                                                                    boolean withoutBalance, boolean withoutCorrect) {

		return getJdbcTemplate().query(
				"select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
						"rp.calendar_start_date from report_period rp, tax_period tp where rp.id in " +
						"(select distinct report_period_id from department_report_period " +
						"where "+ SqlUtils.transformToSqlInStatement("department_id", departmentList)+" and is_active=1 " +
						(withoutBalance ? " and is_balance_period=0 " : "") + (withoutCorrect ? "and correction_date is null" : "") + " ) " +
						"and rp.tax_period_id = tp.id " +
						"and tp.tax_type = \'" + String.valueOf(taxType.getCode()) +"\' " +
						"order by tp.year desc, rp.calendar_start_date",
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
                            "and drp.IS_BALANCE_PERIOD=0 and drp.IS_ACTIVE=0 and CORRECTION_DATE is null " +
                            "order by year",
                    new Object[]{String.valueOf(taxType.getCode()), departmentId},
                    new int[] { Types.VARCHAR, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ReportPeriod> getClosedPeriodsForFormTemplate(Integer formTemplateId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT DISTINCT " +
                            "rp.id, rp.name, rp.tax_period_id, rp.dict_tax_period_id, rp.start_date, rp.end_date, " +
                            "rp.calendar_start_date " +
                            "FROM report_period rp " +
                            "LEFT JOIN form_data fd ON fd.report_period_id = rp.id " +
                            "LEFT JOIN department_report_period drp ON fd.report_period_id = drp.report_period_id " +
                            "AND drp.department_id = fd.department_id " +
                            "AND drp.correction_date IS NULL " +
                            "WHERE drp.is_active = 0 AND fd.form_template_id = ?",
                    new Object[]{formTemplateId},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существуют закрытые периоды для версии макета с id = " + formTemplateId);
        }
    }

    @Override
    public ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date " +
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
					"select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
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
            logger.error("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", e);
			throw new DaoException(String.format("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", taxType.getCode(), date));
		}
	}

	@Override
	public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate) {
		try {
			return getJdbcTemplate().query(
					"select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
							"rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
							"where tp.tax_type = ? and rp.end_date>=? and rp.calendar_start_date<=?",
					new Object[]{new Object[]{String.valueOf(taxType.getCode())}, endDate, startDate},
					new int[] { Types.VARCHAR, Types.DATE, Types.DATE },
					new ReportPeriodMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format("Не найдены отчетные периоды с типом = \"%s\" за период (%s; %s)", taxType.getCode(), startDate, endDate));
		}
	}

    @Override
    public ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, tax_period_id, start_date, end_date, dict_tax_period_id, calendar_start_date  " +
                            "from report_period where dict_tax_period_id = (select record_id from ref_book_value " +
                            "where attribute_id = 25 and string_value = ?) and tax_period_id in (select id from " +
                            "tax_period where year = ? and tax_type = ?)",
                    new Object[]{code, year, String.valueOf(taxType.getCode())},
                    new int[]{Types.VARCHAR, Types.NUMERIC, Types.CHAR},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}

