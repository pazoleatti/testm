package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
            reportPeriod.setMonths(rs.getInt("months"));
            reportPeriod.setTaxPeriod(taxPeriodDao.get(rs.getInt("tax_period_id")));
            reportPeriod.setOrder(rs.getInt("ord"));
            reportPeriod.setDictTaxPeriodId(rs.getInt("dict_tax_period_id"));
            return reportPeriod;
        }
    }

	@Override
	public ReportPeriod get(int id) {
		try {
			return 0 == id? null :
                    getJdbcTemplate().queryForObject(
					"select * from report_period where id = ?",
					new Object[]{id},
					new int[]{Types.NUMERIC},
					new ReportPeriodMapper()
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
				"insert into report_period (id, name, months, tax_period_id, ord," +
						" dict_tax_period_id, start_date, end_date)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				reportPeriod.getName(),
				reportPeriod.getMonths(),
				reportPeriod.getTaxPeriod().getId(),
				reportPeriod.getOrder(),
				reportPeriod.getDictTaxPeriodId(),
				new Date(), // заменить на значение из модели (Marat Fayzullin 2014-01-10)
				new Date()  // заменить на значение из модели (Marat Fayzullin 2014-01-10)
		);
		reportPeriod.setId(id);
		return id;
	}

    @Override
    public List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList) {
        Object[] params = new Object[departmentList.size() + 1];
        int cnt = 0;
        for (Integer departmentId : departmentList) {
            params[cnt++] = departmentId;
        }
        params[cnt] = String.valueOf(taxType.getCode());

        return getJdbcTemplate().query(
                "select rp.* from report_period rp, tax_period tp where rp.id in " +
                        "(select distinct report_period_id from department_report_period " +
                        "where department_id in("+ SqlUtils.preparePlaceHolders(departmentList.size())+")) " +
                        "and rp.tax_period_id = tp.id " +
                        "and tp.tax_type = ?" +
                        "order by tp.year desc, rp.ord",
                new ReportPeriodMapper(), params);
    }

    @Override
    public ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select * from report_period where tax_period_id = ? and dict_tax_period_id = ?",
                    new Object[]{taxPeriodId, dictTaxPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
                    new ReportPeriodMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не существует периода с tax_period_id=" + taxPeriodId + " и dict_tax_period_id = " + dictTaxPeriodId);
        }
    }
}

