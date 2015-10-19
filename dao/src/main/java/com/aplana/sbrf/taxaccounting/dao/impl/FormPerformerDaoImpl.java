package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Реализация DAO для работы с информацией об @{link FormDataPerformer исполнителе налоговой формы}
 */
@Repository
public class FormPerformerDaoImpl extends AbstractDao implements FormPerformerDao {

	private static final Log LOG = LogFactory.getLog(FormPerformerDaoImpl.class);

	@Autowired
	private FormDataDao formDataDao;

	private static final class FormPerformerRowMapper implements RowMapper<FormDataPerformer> {
		@Override
		public FormDataPerformer mapRow(ResultSet rs, int index) throws SQLException {
			FormDataPerformer res = new FormDataPerformer();
			res.setName(rs.getString("name"));
			res.setPhone(rs.getString("phone"));
            res.setPrintDepartmentId(SqlUtils.getInteger(rs, "print_department_id"));
            res.setReportDepartmentName(rs.getString("report_department_name"));
			return res;
		}
	}

	@Override
	public void save(long formDataId, boolean manual, FormDataPerformer performer) {
		if (formDataDao.get(formDataId, manual) != null) {
			if (this.get(formDataId) != null) {
				getJdbcTemplate().update(
					"update form_data_performer set name = ?, phone = ?, print_department_id = ?, report_department_name = ? where form_data_id = ?",
					new Object[] {
							performer.getName(),
							performer.getPhone(),
                            performer.getPrintDepartmentId(),
                            performer.getReportDepartmentName(),
							formDataId
					},
					new int[] {
							Types.VARCHAR,
							Types.VARCHAR,
                            Types.NUMERIC,
                            Types.VARCHAR,
                            Types.NUMERIC
					}
				);
			} else {
				getJdbcTemplate().update(
						"insert into form_data_performer(form_data_id, name, phone, print_department_id, report_department_name) values (?, ?, ?, ?, ?)",
						formDataId,
						performer.getName(),
						performer.getPhone(),
                        performer.getPrintDepartmentId(),
                        performer.getReportDepartmentName()
				);
			}
		}
	}

	@Override
	public FormDataPerformer get(long formDataId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from form_data_performer where form_data_id = ?",
					new Object[] { formDataId },
					new int[] { Types.NUMERIC },
					new FormPerformerRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Для даной формы %d найдено несколько исполнителей.", formDataId);
		}
	}

	@Override
	public void clear(long formDataId) {
		getJdbcTemplate().update(
				"delete from form_data_performer where form_data_id = ?",
				formDataId
		);
	}

    private static final String GET_FORM_DATA_IDS_BY_DEPARTMENT = "select fd.id\n"+
            "from form_data fd\n"+
            "join DEPARTMENT_REPORT_PERIOD drp on drp.ID = fd.DEPARTMENT_REPORT_PERIOD_ID\n" +
            "join REPORT_PERIOD rp on rp.ID = drp.REPORT_PERIOD_ID\n"+
            "where fd.id in (select form_data_id\n"+
            "  from form_data_performer\n"+
            "  where print_department_id = :departmentId or (SELECT CONNECT_BY_ROOT ID as ID_ROOT FROM DEPARTMENT where id = PRINT_DEPARTMENT_ID START WITH (type = 2) CONNECT BY PRIOR id = PARENT_ID) = :departmentId)\n"+
            "and %s";

    @Override
    public List<Long> getFormDataId(final int departmentId, Date dateFrom, Date dateTo) {
        try {
            String dateTag = dateFrom != null && dateTo != null ? "(rp.CALENDAR_START_DATE between :dateFrom and :dateTo or rp.END_DATE between :dateFrom and :dateTo or :dateFrom between rp.CALENDAR_START_DATE and rp.END_DATE)"
                    : dateFrom != null ? "(rp.END_DATE >= :dateFrom)"
                    : null;
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);
            values.put("departmentId", departmentId);

            return getNamedParameterJdbcTemplate().queryForList(String.format(GET_FORM_DATA_IDS_BY_DEPARTMENT, dateTag),
                    values,
                    Long.class
            );
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Long>(0);
        } catch (DataAccessException e) {
			LOG.error("Ошибка при поиске налоговых форм с заданными параметрами: departmentId = %s, dateFrom = %s, dateTo = %s", e);
            throw new DaoException("Ошибка при поиске налоговых форм с заданными параметрами", e);
        }
    }
}
