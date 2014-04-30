package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Реализация DAO для работы с данными налоговых форм
 * @author dsultanbekov
 */
@Repository("formDataDao")
@Transactional(readOnly = true)
public class FormDataDaoImpl extends AbstractDao implements FormDataDao {
	
	public static final String MSG_FORM_NOT_FOUND = "Форма id=%s не найдена";
	
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataSignerDao formDataSignerDao;
	@Autowired
	private FormPerformerDao formPerformerDao;
	@Autowired
	private FormTypeDao formTypeDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private ReportPeriodDao reportPeriodDao;
    @Autowired
    private TaxPeriodDao taxPeriodDao;

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
			fd.setReturnSign(rs.getBoolean("return_sign"));
			fd.setKind(FormDataKind.fromId(rs.getInt("kind")));
			fd.setReportPeriodId(rs.getInt("report_period_id"));
            Integer periodOrder = rs.getInt("period_order");
            fd.setPeriodOrder(rs.wasNull() ? null : periodOrder);
			fd.setSigners(formDataSignerDao.getSigners(fd.getId()));
			fd.setPerformer(formPerformerDao.get(fd.getId()));
            fd.setManual(rs.getBoolean("manual"));

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
			result.setReturnSign(rs.getBoolean("return_sign"));
			result.setKind(FormDataKind.fromId(rs.getInt("kind")));
			result.setReportPeriodId(rs.getInt("report_period_id"));
            Integer periodOrder = rs.getInt("period_order");
			result.setPeriodOrder(rs.wasNull() ? null : periodOrder);
			result.setFormType(formTypeDao.get(rs.getInt("type_id")));
			return result;
		}

	}

    public FormData get(final long formDataId) {
        JdbcTemplate jt = getJdbcTemplate();
        final FormData formData;
        try {
            RowMapperResult res = jt.queryForObject(
                    "select f.*, r.manual from form_data f \n" +
                            "left join (select max(manual) as manual, form_data_id from data_row where manual = 0 group by form_data_id) r on r.form_data_id = f.id\n" +
                            "where f.id = ?",
                    new Object[] {
                            formDataId}, new int[] { Types.NUMERIC },
                    new FormDataRowMapper());
            formData = res.formData;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Записи в таблице FORM_DATA с id = "
                    + formDataId + " не найдено");
        }

        return formData;
    }

	public FormData get(final long formDataId, Boolean manual) {
		JdbcTemplate jt = getJdbcTemplate();
		final FormData formData;
		try {
			RowMapperResult res = jt.queryForObject(
					"select f.*, r.manual from form_data f \n" +
                            "left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r on (r.form_data_id = f.id and (? is null or r.manual = ?))\n" +
                            "where f.id = ?",
					new Object[] {
                            manual == null ? null : manual ? 1 : 0,
                            manual == null ? null : manual ? 1 : 0,
                            formDataId}, new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC },
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
					"insert into form_data (id, form_template_id, department_id, kind, state, report_period_id, return_sign, period_order)" +
							" values (?, ?, ?, ?, ?, ?, ?, ?)",
					formDataId, formData.getFormTemplateId(),
					formData.getDepartmentId(), formData.getKind().getId(),
					formData.getState().getId(), formData.getReportPeriodId(), 0, formData.getPeriodOrder());
			formData.setId(formDataId);
		} else {
			formDataId = formData.getId();
		}

		if (formData.getPerformer() != null &&
				(StringUtils.hasLength(formData.getPerformer().getName())
						|| StringUtils.hasLength(formData.getPerformer().getPhone())
                        || formData.getPerformer().getPrintDepartmentId() != null)
			) {
			formPerformerDao.save(formDataId, formData.isManual(), formData.getPerformer());
		} else {
			formPerformerDao.clear(formDataId);
		}
		if (formData.getSigners() != null) {
			formDataSignerDao.saveSigners(formDataId, formData.getSigners());
		}
		return formDataId;
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
			return get(formDataId, null);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
			throw new DaoException(
				"Для заданного сочетания параметров найдено несколько налоговых форм: вид \"%s\", тип \"%s\", подразделение \"%s\", отчетный период \"%s\", налоговый период \"%s\"",
				formTypeDao.get(formTypeId).getName(),
				kind.getName(),
				departmentDao.getDepartment(departmentId).getName(),
				reportPeriod.getName(),
				reportPeriod.getTaxPeriod().getYear()
			);
		}
	}

    @Override
    public List<Long> findFormDataByFormTemplate(int formTemplateId) {
        try {
            return getJdbcTemplate().queryForList(
                    "select fd.id from form_data fd where fd.form_template_id = ?",
                    new Object[] {formTemplateId},
                    new int[] {Types.NUMERIC},
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка поиска НФ для заданного шаблона %d", formTemplateId);
        }
    }

	@Override
	public List<FormData> find(List<Integer> departmentIds, int reportPeriodId) {
        Map paramMap = new HashMap();
        paramMap.put("rp", reportPeriodId);
        paramMap.put("ids", departmentIds);
        List<Long> formsId = getNamedParameterJdbcTemplate().queryForList(
            "select id from form_data where department_id in (:ids) and report_period_id = :rp",
            paramMap,
            Long.class
        );

		List<FormData> forms = new ArrayList<FormData>();
		for (Long id : formsId) {
			forms.add(getWithoutRows(id));
		}
		return forms;
	}

	@Override
    public FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder) {
        try {
            Long formDataId = getJdbcTemplate().queryForLong(
                    "select fd.id from form_data fd where exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?)"
                            + " and fd.kind = ? and fd.department_id = ? and fd.report_period_id in (select id from report_period where tax_period_id = ?) and fd.period_order = ?",
                    new Object[] {
                            formTypeId,
                            kind.getId(),
                            departmentId,
                            taxPeriodId,
                            periodOrder
                    },
                    new int[] {
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC
                    }
            );
            return get(formDataId, null);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            TaxPeriod taxPeriod = taxPeriodDao.get(taxPeriodId);
            throw new DaoException(
                    "Для заданного сочетания параметров найдено несколько налоговых форм: вид \"%s\", тип \"%s\", подразделение \"%s\", налоговый период \"%s\", месяц \"%s\"",
                    formTypeDao.get(formTypeId).getName(),
                    kind.getName(),
                    departmentDao.getDepartment(departmentId).getName(),
					taxPeriod.getYear(),
                    periodOrder <= 12 && periodOrder >= 1 ? Formats.months[periodOrder] : periodOrder
            );
        }
    }

    @Override
	public FormData getWithoutRows(long id){
		JdbcTemplate jt = getJdbcTemplate();
		try{
			return jt.queryForObject(
					"SELECT fd.id, fd.department_id, fd.state, fd.kind, fd.report_period_id, fd.return_sign, fd.period_order, r.manual,\n" +
                            "(SELECT type_id FROM form_template ft WHERE ft.id = fd.form_template_id) type_id\n" +
                            "FROM form_data fd \n" +
                            "left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r on r.form_data_id = fd.id\n" +
                            "WHERE fd.id = ?",
					new Object[] { id }, new int[] { Types.NUMERIC },
					new FormDataWithoutRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format(MSG_FORM_NOT_FOUND, id));
		}
	}

	@Override
	public void updateReturnSign(long id, boolean returnSign) {
		if (getJdbcTemplate().update("update form_data set return_sign=? where id=?", returnSign ? 1 : 0, id) == 0) {
			throw new DaoException(String.format(MSG_FORM_NOT_FOUND, id));
		}
	}

	@Override
	public void updateState(long id, WorkflowState workflowState) {
		if (getJdbcTemplate().update("update form_data set state=? where id=?", workflowState.getId(), id) == 0) {
			throw new DaoException(String.format(MSG_FORM_NOT_FOUND, id));
		}
	}

    @Override
    public void updatePeriodOrder(long id, int periodOrder) {
        if (periodOrder < 1 && periodOrder > 12){
            throw new DaoException(String.format("Неправильная очередность (месяц) периода: %s", periodOrder));
        }
        if (getJdbcTemplate().update("update form_data set period_order=? where id=?", periodOrder, id) == 0) {
            throw new DaoException(String.format(MSG_FORM_NOT_FOUND, id));
        }
    }

    @Override
    public List<Long> getFormDataListInActualPeriodByTemplate(int templateId, Date startDate) {
        try {
            return getJdbcTemplate().queryForList("SELECT fd.id FROM form_data fd WHERE fd.form_template_id = ? AND report_period_id IN" +
                    " (SELECT id FROM report_period WHERE calendar_start_date >= ?)",
                    new Object[]{templateId, startDate},
                    Long.class);
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Long>();
        } catch (DataAccessException e){
            logger.error("Ошибка при поиске используемых версий", e);
            throw new DaoException("Ошибка при поиске используемых версий", e);
        }
    }

    @Override
    public boolean existManual(Long formDataId) {
        return getJdbcTemplate()
                .queryForInt("select count(*) from data_row where form_data_id = ? and manual = 1", formDataId) > 0;
    }

    @Override
    public List<Long> getFormDataIds(int formTypeId, FormDataKind kind, int departmentId) {
        try {
            return getJdbcTemplate().queryForList(
                    "select fd.id from FORM_DATA fd where exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?)"
                            + " and fd.kind = ? and fd.department_id = ?",
                    new Object[] {
                            formTypeId,
                            kind.getId(),
                            departmentId
                    },
                    new int[] {
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC
                    },
                    Long.class
            );
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Long>(0);
        } catch (DataAccessException e) {
            String errorMsg = String.format("Ошибка при поиске налоговых форм с заданными параметрами: formTypeId = %s, kind = %s, departmentId = %s",formTypeId, kind.getId(), departmentId);
            logger.error(errorMsg, e);
            throw new DaoException(errorMsg, e);
        }
    }

    @Override
    public void deleteManual(long formDataId) {
        getJdbcTemplate().update("delete from data_row where manual = 1 and form_data_id = ?", formDataId);
    }

    @Override
    public List<String> getStringList(Integer columnId, Integer formTemplateTypeId) {
        return getJdbcTemplate().queryForList("SELECT sv.value FROM FORM_COLUMN FC\n" +
                "LEFT JOIN FORM_DATA FD ON FC.FORM_TEMPLATE_ID = FD.FORM_TEMPLATE_ID\n" +
                "LEFT JOIN DATA_ROW DR ON DR.FORM_DATA_ID = FD.ID\n" +
                "LEFT JOIN STRING_VALUE SV ON SV.ROW_ID = DR.ID AND SV.COLUMN_ID = FC.ID\n" +
                "WHERE FC.id = ? AND fd.form_template_id = ?",
                new Object[]{columnId, formTemplateTypeId},
                String.class);
    }
}
