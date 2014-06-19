package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

@Repository
@Transactional(readOnly=true)
public class DepartmentReportPeriodDaoImpl extends AbstractDao implements
		DepartmentReportPeriodDao {

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	private final RowMapper<DepartmentReportPeriod> mapper = new RowMapper<DepartmentReportPeriod>() {
		@Override
		public DepartmentReportPeriod mapRow(ResultSet rs, int index)
				throws SQLException {
			DepartmentReportPeriod reportPeriod = new DepartmentReportPeriod();
            reportPeriod.setId(SqlUtils.getLong(rs,"ID"));
			reportPeriod.setDepartmentId(SqlUtils.getLong(rs,"DEPARTMENT_ID"));
			reportPeriod.setReportPeriod(reportPeriodDao.get(SqlUtils
                    .getInteger(rs,"REPORT_PERIOD_ID")));
			reportPeriod.setActive(SqlUtils.getInteger(rs, "IS_ACTIVE") != 0);
			reportPeriod.setBalance(SqlUtils.getInteger(rs, "IS_BALANCE_PERIOD") != 0);
            reportPeriod.setCorrectPeriod(rs.getDate("CORRECTION_DATE"));
			return reportPeriod;
		}
	};

	@Override
	public List<DepartmentReportPeriod> getByDepartment(Long departmentId) {
		return getJdbcTemplate()
				.query("select * from DEPARTMENT_REPORT_PERIOD where DEPARTMENT_ID=? order by REPORT_PERIOD_ID",
						new Object[] { departmentId },
						new int[] { Types.NUMERIC},
						mapper);
	}

	@Override
	public List<DepartmentReportPeriod> getByDepartmentAndTaxType(Long departmentId, TaxType taxType) {
		return getJdbcTemplate()
				.query("select drp.* from DEPARTMENT_REPORT_PERIOD drp " +
						"left join report_period rp on drp.report_period_id = rp.ID " +
						"left join tax_period tp on rp.TAX_PERIOD_ID=tp.ID where drp.DEPARTMENT_ID=? and tp.TAX_TYPE=? order by rp.calendar_start_date",
						new Object[] { departmentId, String.valueOf(taxType.getCode()) },
						new int[] { Types.NUMERIC, Types.CHAR},
						mapper);
	}

	@Override
	@Transactional(readOnly=false)
	public void save(DepartmentReportPeriod departmentReportPeriod) {
        Long id = departmentReportPeriod.getId();
        if (id == null) {
            id = generateId("seq_department_report_period", Long.class);
        }
		getJdbcTemplate()
				.update("insert into DEPARTMENT_REPORT_PERIOD (ID, DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, IS_BALANCE_PERIOD," +
                        "CORRECTION_DATE)"
						+ " values (?, ?, ?, ?, ?, ?)",
                        id,
						departmentReportPeriod.getDepartmentId(),
						departmentReportPeriod.getReportPeriod().getId(),
						departmentReportPeriod.isActive(),
						departmentReportPeriod.isBalance(),
                        departmentReportPeriod.getCorrectPeriod());
	}

    @Override
    public void updateDeadline(int reportPeriodId, List<Integer> departmentIds, Date deadline) {
        getJdbcTemplate()
                .update("update DEPARTMENT_REPORT_PERIOD set DEADLINE=? where REPORT_PERIOD_ID=? and " +
                        SqlUtils.transformToSqlInStatement("DEPARTMENT_ID", departmentIds),
                        deadline, reportPeriodId);
    }

    @Override
	@Transactional(readOnly=false)
	public void updateActive(int reportPeriodId, Long departmentId, Date correctionDate,
			boolean active) {
        if (correctionDate == null) {
            getJdbcTemplate()
                    .update("update DEPARTMENT_REPORT_PERIOD set IS_ACTIVE=? where REPORT_PERIOD_ID=? and DEPARTMENT_ID=? " +
                                    "and CORRECTION_DATE is null",
                            active ? 1 : 0, reportPeriodId, departmentId);
        } else {
            getJdbcTemplate()
                    .update("update DEPARTMENT_REPORT_PERIOD set IS_ACTIVE=? where REPORT_PERIOD_ID=? and DEPARTMENT_ID=? " +
                                    "and CORRECTION_DATE = ?",
                            active ? 1 : 0, reportPeriodId, departmentId, correctionDate);
        }
	}

    @Override
    @Transactional(readOnly=false)
    public void updateActive(long departmentReportPeriodId, boolean active) {
        getJdbcTemplate()
                .update("update DEPARTMENT_REPORT_PERIOD set IS_ACTIVE=? where ID=?",
                        active ? 1 : 0, departmentReportPeriodId);
    }

    @Override
    @Transactional(readOnly=false)
    public void updateCorrectionDate(long departmentReportPeriodId, Date correctionDate) {
        getJdbcTemplate()
                .update("update DEPARTMENT_REPORT_PERIOD set CORRECTION_DATE=? where ID=?",
                        correctionDate, departmentReportPeriodId);
    }

	@Override
	public DepartmentReportPeriod get(int reportPeriodId, Long departmentId) {
		return DataAccessUtils
				.singleResult(getJdbcTemplate()
						.query("select * from DEPARTMENT_REPORT_PERIOD where REPORT_PERIOD_ID=? and DEPARTMENT_ID=? and CORRECTION_DATE is null",
								new Object[] { reportPeriodId, departmentId },
								new int[] { Types.NUMERIC, Types.NUMERIC },
								mapper));
	}

    @Override
    public DepartmentReportPeriod get(int reportPeriodId, Long departmentId, Date correctionDate) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("reportPeriodId", reportPeriodId);
            params.addValue("departmentId", departmentId);
            if (correctionDate != null) {
                params.addValue("correctionDate", correctionDate);
            }

            return getNamedParameterJdbcTemplate().queryForObject(
                    "select * from DEPARTMENT_REPORT_PERIOD where REPORT_PERIOD_ID=:reportPeriodId and DEPARTMENT_ID= :departmentId" +
                            " and " + (correctionDate == null ? " CORRECTION_DATE is null" : " CORRECTION_DATE = :correctionDate"),
                    params,
                    mapper
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private static final String DRP_BY_DEPARTMENT_IDS_AND_TAX_TYPES =
            "SELECT * FROM DEPARTMENT_REPORT_PERIOD drp \n" +
                    "LEFT JOIN REPORT_PERIOD rp ON rp.ID = drp.REPORT_PERIOD_ID\n" +
                    "LEFT JOIN TAX_PERIOD tp ON tp.ID = rp.TAX_PERIOD_ID\n" +
                    "  WHERE %s AND tp.TAX_TYPE IN %s";
    @Override
    public List<DepartmentReportPeriod> getListDRPByDepartmentIds(List<TaxType> taxTypes, List<Long> departmentIds) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("departmentIds", departmentIds);
            params.addValue("taxTypes", taxTypes);
            return getNamedParameterJdbcTemplate().query(
                    String.format(DRP_BY_DEPARTMENT_IDS_AND_TAX_TYPES,
                            SqlUtils.transformToSqlInStatement("DEPARTMENT_ID", departmentIds),
                            SqlUtils.transformTaxTypeToSqlInStatement(taxTypes)),
                    params,
                    mapper
            );
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
	public void delete(int reportPeriodId, Integer departmentId) {
		getJdbcTemplate().update(
				"delete from department_report_period where department_id = ? and report_period_id = ?",
				new Object[] {departmentId, reportPeriodId},
				new int[] {Types.NUMERIC, Types.NUMERIC}
		);
	}

    @Override
    public void delete(long departmentReportPeriodId) {
        getJdbcTemplate().update(
                "delete from department_report_period where id = ?",
                new Object[] {departmentReportPeriodId},
                new int[] {Types.NUMERIC}
        );
    }

	@Override
	public boolean existForDepartment(Integer departmentId, long reportPeriodId) {
		Integer count = getJdbcTemplate().queryForInt(
				"select count(*) from department_report_period where department_id = ? and report_period_id = ?",
				new Object[] {departmentId, reportPeriodId},
				new int[] {Types.NUMERIC, Types.NUMERIC}
		) ;

		return count != 0;
	}

	@Override
	public boolean isPeriodOpen(int departmentId, long reportPeriodId) {
		try {
            int is_active = getJdbcTemplate().queryForInt(
                    "select distinct is_active from department_report_period where department_id = ? and report_period_id = ?",
                    new Object[] {departmentId, reportPeriodId},
                    new int[] {Types.NUMERIC, Types.NUMERIC}
            );

            return is_active != 0;
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }

	}

    @Override
    public int getCorrectionPeriodNumber(int reportPeriodId, long departmentId) {
        return getJdbcTemplate().queryForInt(
                "select count(CORRECTION_DATE) from DEPARTMENT_REPORT_PERIOD " +
                        "where DEPARTMENT_ID = ? and REPORT_PERIOD_ID = ? and CORRECTION_DATE is not null",
                new Object[] {departmentId, reportPeriodId},
                new int[] {Types.NUMERIC, Types.NUMERIC}
        );
    }

    @Override
    public List<DepartmentReportPeriod> getDepartmentCorrectionPeriods(long departmentId, int reportPeriodId) {
        return getJdbcTemplate().query(
                "select drp.* from REPORT_PERIOD rp " +
                        "left join DEPARTMENT_REPORT_PERIOD drp on rp.id = drp.REPORT_PERIOD_ID " +
                        "where rp.ID=? and drp.DEPARTMENT_ID=? and drp.CORRECTION_DATE is not null",
                new Object[]{reportPeriodId, departmentId},
                mapper
        );
    }

    @Override
    public DepartmentReportPeriod getById(long id) {
        return getJdbcTemplate().queryForObject(
                "select * from department_report_period where id = ?",
                new Object[]{id},
                mapper
        );
    }

    @Override
    public void changeBalance(int reportPeriodId, int departmentId, boolean isBalance) {
        getJdbcTemplate().update(
                "update department_report_period set is_balance_period = ? where department_id = ? and report_period_id = ?",
                new Object[] {isBalance ? 1 : 0, departmentId, reportPeriodId},
                new int[] {Types.NUMERIC, Types.NUMERIC, Types.NUMERIC}
        );
    }

}
