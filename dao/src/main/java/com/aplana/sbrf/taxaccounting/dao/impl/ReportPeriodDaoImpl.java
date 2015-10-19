package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	private static final Log LOG = LogFactory.getLog(ReportPeriodDaoImpl.class);

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
            reportPeriod.setAccName(getAccName(reportPeriod.getName(), reportPeriod.getCalendarStartDate()));
            return reportPeriod;
        }

        /**
         * Возвращает полное имя периода для нф с нарастающим итогом
         * @return
         */
        private String getAccName(String name, Date calendarStartDate) {
            Calendar sDate = Calendar.getInstance();
            sDate.setTime(calendarStartDate);
            int day = sDate.get(Calendar.DAY_OF_MONTH);
            int month = sDate.get(Calendar.MONTH) + 1;
            if (day == 1 && month == 4) {
                //2 квартал: 2 квартал (полугодие)
                return name + " (полугодие)";
            } else if (day == 1 && month == 7) {
                //3 квартал: 3 квартал (9 месяцев)
                return name + " (9 месяцев)";
            } else if (day == 1 && month == 10) {
                //4 квартал: 4 квартал (год)
                return name + " (год)";
            } else {
                return name;
            }
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
                        "and tp.tax_type = \'" + taxType.getCode() + "\' " +
                        "order by tp.year desc, rp.calendar_start_date",
                new ReportPeriodMapper());
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
						"and tp.tax_type = \'" + taxType.getCode() +"\' " +
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
    public List<ReportPeriod> getComparativPeriods(TaxType taxType, int departmentId) {
        try {
            return getJdbcTemplate().query(
                    "select * from REPORT_PERIOD rp " +
                            "left join TAX_PERIOD tp on rp.TAX_PERIOD_ID=tp.ID " +
                            "left join DEPARTMENT_REPORT_PERIOD drp on rp.ID=drp.REPORT_PERIOD_ID  " +
                            "where tp.TAX_TYPE = ? and drp.DEPARTMENT_ID= ? " +
                            "and CORRECTION_DATE is null " +
                            "order by tp.year desc, rp.calendar_start_date",
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
			LOG.error("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", e);
			throw new DaoException(String.format("Возникли ошибки во время поиска отчетного периода с типом = \"%s\" на дату \"%tD\"", taxType.getCode(), date));
		}
	}

	@Override
	public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate) {
		try {
			return getJdbcTemplate().query(
					"select rp.id, rp.name, rp.tax_period_id, rp.start_date, rp.end_date, rp.dict_tax_period_id, " +
							"rp.calendar_start_date from report_period rp join tax_period tp on rp.tax_period_id = tp.id " +
							"where tp.tax_type = ? and rp.end_date>=? and (rp.calendar_start_date<=? or ? is null)",
					new Object[]{String.valueOf(taxType.getCode()), startDate, endDate, endDate},
					new int[] { Types.VARCHAR, Types.DATE, Types.DATE, Types.DATE },
					new ReportPeriodMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format("Не найдены отчетные периоды с типом = \"%s\" за период (%s; %s)", taxType.getCode(), startDate, endDate));
		}
	}

    private static final String RP_BY_DATE_AND_DEPARTMENT =
            "SELECT\n" +
                    "  rp.id,\n" +
                    "  rp.name,\n" +
                    "  rp.tax_period_id,\n" +
                    "  rp.start_date,\n" +
                    "  rp.end_date,\n" +
                    "  rp.dict_tax_period_id,\n" +
                    "  rp.calendar_start_date\n" +
                    "FROM report_period rp\n" +
                    "  JOIN tax_period tp ON rp.tax_period_id = tp.id\n" +
                    "  JOIN DEPARTMENT_REPORT_PERIOD drp ON drp.REPORT_PERIOD_ID = rp.id\n" +
                    "WHERE tp.tax_type = ? AND rp.end_date >= ? AND (? IS NULL OR rp.calendar_start_date <= ?)\n" +
                    "      AND drp.DEPARTMENT_ID = ?\n" +
                    "ORDER BY rp.end_date";
    @Override
    public List<ReportPeriod> getReportPeriodsByDateAndDepartment(TaxType taxType, int depId, Date startDate, Date endDate) {
        return getJdbcTemplate().query(
                RP_BY_DATE_AND_DEPARTMENT,
                new Object[]{String.valueOf(taxType.getCode()), startDate, endDate, endDate, depId},
                new ReportPeriodMapper()
        );
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

