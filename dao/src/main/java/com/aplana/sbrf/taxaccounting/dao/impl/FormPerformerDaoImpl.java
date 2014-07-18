package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;

/**
 * Реализация DAO для работы с информацией об @{link FormDataPerformer исполнителе налоговой формы}
 */
@Repository
public class FormPerformerDaoImpl extends AbstractDao implements FormPerformerDao {

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
}
