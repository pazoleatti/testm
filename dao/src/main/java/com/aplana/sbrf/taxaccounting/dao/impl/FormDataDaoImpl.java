package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSignerDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

/**
 * Реализация DAO для работы с данными налоговых форм
 * @author dsultanbekov
 */
@Repository("formDataDao")
@Transactional(readOnly = true)
public class FormDataDaoImpl extends AbstractDao implements FormDataDao {
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataSignerDao formDataSignerDao;
	@Autowired
	private FormPerformerDao formPerformerDao;
	@Autowired
	private FormTypeDao formTypeDao;
	
	private static class RowMapperResult {
		FormData formData;
	}

	private class FormDataRowMapper implements RowMapper<RowMapperResult> {
		public RowMapperResult mapRow(ResultSet rs, int index)
				throws SQLException {
			RowMapperResult result = new RowMapperResult();

			int formTemplateId = rs.getInt("form_template_id");
			FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

			FormData fd = new FormData();
			fd.initFormTemplateParams(formTemplate);
			fd.setId(rs.getLong("id"));
			fd.setDepartmentId(rs.getInt("department_id"));
			fd.setState(WorkflowState.fromId(rs.getInt("state")));
			fd.setKind(FormDataKind.fromId(rs.getInt("kind")));
			fd.setReportPeriodId(rs.getInt("report_period_id"));
			fd.setSigners(formDataSignerDao.getSigners(fd.getId()));
			fd.setPerformer(formPerformerDao.get(fd.getId()));

			result.formData = fd;
			return result;
		}

	}

	private class FormDataWithoutRowMapper implements RowMapper<FormData> {
		public FormData mapRow(ResultSet rs, int index)
				throws SQLException {
			FormData result = new FormData();
			result.setId(rs.getLong("id"));
			result.setDepartmentId(rs.getInt("department_id"));
			result.setState(WorkflowState.fromId(rs.getInt("state")));
			result.setKind(FormDataKind.fromId(rs.getInt("kind")));
			result.setReportPeriodId(rs.getInt("report_period_id"));
			result.setFormType(formTypeDao.getType(rs.getInt("type_id")));
			return result;
		}

	}

	public FormData get(final long formDataId) {
		JdbcTemplate jt = getJdbcTemplate();
		final FormData formData;
		try {
			RowMapperResult res = jt.queryForObject(
					"select * from form_data where id = ?",
					new Object[] { formDataId }, new int[] { Types.NUMERIC },
					new FormDataRowMapper());
			formData = res.formData;
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Записи в таблице FORM_DATA с id = "
					+ formDataId + " не найдено");
		}
		
		return formData;
	}

	@Override
	@Transactional(readOnly = false)
	public long save(final FormData formData) {
		if (formData.getState() == null) {
			throw new DaoException("Не указана стадия жизненного цикла");
		}

		if (formData.getKind() == null) {
			throw new DaoException("Не указан тип налоговой формы");
		}

		if (formData.getDepartmentId() == null) {
			throw new DaoException(
					"Не указано подразделение, к которому относится налоговая форма");
		}

		if (formData.getReportPeriodId() == null) {
			throw new DaoException("Не указан идентификатор отчётного периода");
		}

		Long formDataId;
		JdbcTemplate jt = getJdbcTemplate();
		if (formData.getId() == null) {
			formDataId = generateId("seq_form_data", Long.class);
			jt.update(
					"insert into form_data (id, form_template_id, department_id, kind, state, report_period_id)" +
							" values (?, ?, ?, ?, ?, ?)",
					formDataId, formData.getFormTemplateId(),
					formData.getDepartmentId(), formData.getKind().getId(),
					formData.getState().getId(), formData.getReportPeriodId());
			formData.setId(formDataId);
		} else {
			formDataId = formData.getId();
			jt.update("delete from data_row where form_data_id = ?",
					new Object[] { formDataId }, new int[] { Types.NUMERIC });
		}
		if (formData.getPerformer() != null &&
				(!formData.getPerformer().getName().isEmpty() || !formData.getPerformer().getPhone().isEmpty())
			) {
			formPerformerDao.save(formDataId, formData.getPerformer());
		} else {
			formPerformerDao.clear(formDataId);
		}
		if (formData.getSigners() != null) {
			formDataSignerDao.saveSigners(formDataId, formData.getSigners());
		}
		return formDataId;
	}

	
	@Override
	public List<Long> listFormDataIdByType(int typeId) {
		return getJdbcTemplate()
				.queryForList(
						"select id from form_data fd where exists (select 1 from form_template ft where ft.id = fd.form_template_id and ft.type_id = ?)",
						new Object[] { typeId }, new int[] { Types.NUMERIC },
						Long.class);
	}

	@Override
	@Transactional(readOnly = false)
	public void delete(long formDataId) {
		JdbcTemplate jt = getJdbcTemplate();

		Object[] params = { formDataId };
		int[] types = { Types.NUMERIC };

		jt.update("delete from form_data where id = ?", params, types);
	}

	@Override
	public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
		try {
			Long formDataId = getJdbcTemplate().queryForLong(
				"select fd.id from form_data fd where exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?)"
				+ " and fd.kind = ? and fd.department_id = ? and fd.report_period_id = ?",
				new Object[] {
					formTypeId,
					kind.getId(),
					departmentId,
					reportPeriodId
				},
				new int[] {
					Types.NUMERIC,
					Types.NUMERIC,
					Types.NUMERIC,
					Types.NUMERIC
				}
			);
			return get(formDataId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new DaoException(
				"Для заданного сочетания параметров найдено несколько налоговых форм: formTypeId = %d, formDataKind = '%s', departmentId = %d, reportPeriodId = %d",
				formTypeId,
				kind.name(),
				departmentId,
				reportPeriodId
			);
		}
	}

	@Override
	public FormData getWithoutRows(long formDataId){
		JdbcTemplate jt = getJdbcTemplate();
		try{
			return jt.queryForObject(
					"SELECT fd.id, fd.department_id, fd.state, fd.kind, fd.report_period_id, " +
					"(SELECT type_id FROM form_template ft WHERE ft.id = fd.form_template_id) type_id " +
							"FROM form_data fd WHERE fd.id = ?",
					new Object[] { formDataId }, new int[] { Types.NUMERIC },
					new FormDataWithoutRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Записи в таблице FORM_DATA с id = "
					+ formDataId + " не найдено");
		}
	}
}
