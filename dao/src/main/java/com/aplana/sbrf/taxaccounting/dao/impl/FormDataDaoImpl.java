package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
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

			int formTemplateId = SqlUtils.getInteger(rs,"form_template_id");
			FormTemplate formTemplate = formTemplateDao.get(formTemplateId);

			FormData fd = new FormData();
			fd.initFormTemplateParams(formTemplate);
			fd.setId(SqlUtils.getLong(rs, "id"));
			fd.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
			fd.setState(WorkflowState.fromId(SqlUtils.getInteger(rs,"state")));
			fd.setReturnSign(rs.getBoolean("return_sign"));
			fd.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
			fd.setReportPeriodId(SqlUtils.getInteger(rs,"report_period_id"));
            Integer periodOrder = SqlUtils.getInteger(rs,"period_order");
            fd.setPeriodOrder(rs.wasNull() ? null : periodOrder);
			fd.setSigners(formDataSignerDao.getSigners(fd.getId()));
			fd.setPerformer(formPerformerDao.get(fd.getId()));
            fd.setManual(rs.getBoolean("manual"));
            fd.setPreviousRowNumber(rs.getInt("number_previous_row"));

			result.formData = fd;
			return result;
		}

	}

    private class FormDataWithoutRowMapper implements RowMapper<FormData> {
        public FormData mapRow(ResultSet rs, int index)
                throws SQLException {
            FormData result = new FormData();
            result.setId(SqlUtils.getLong(rs, "id"));
            result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            result.setState(WorkflowState.fromId(SqlUtils.getInteger(rs,"state")));
            result.setReturnSign(rs.getBoolean("return_sign"));
            result.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            result.setReportPeriodId(SqlUtils.getInteger(rs,"report_period_id"));
            Integer periodOrder = SqlUtils.getInteger(rs,"period_order");
            result.setPeriodOrder(rs.wasNull() ? null : periodOrder);
            result.setPreviousRowNumber(rs.getInt("number_previous_row"));
            return result;
        }

    }

	private class FormDataWithoutRowMapperWithTypeId extends FormDataWithoutRowMapper {
		public FormData mapRow(ResultSet rs, int index)
				throws SQLException {
			FormData result = new FormData();
			result.setId(SqlUtils.getLong(rs, "id"));
			result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
			result.setState(WorkflowState.fromId(SqlUtils.getInteger(rs,"state")));
			result.setReturnSign(rs.getBoolean("return_sign"));
			result.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
			result.setReportPeriodId(SqlUtils.getInteger(rs,"report_period_id"));
            Integer periodOrder = SqlUtils.getInteger(rs,"period_order");
			result.setPeriodOrder(rs.wasNull() ? null : periodOrder);
			result.setFormType(formTypeDao.get(SqlUtils.getInteger(rs,"type_id")));
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
    public List<Long> findFormDataByDepartment(int departmentId) {
        try {
            return getJdbcTemplate().queryForList("select id from form_data where department_id = ?",
                    new Object[]{departmentId},
                    Long.class);
        }catch (EmptyResultDataAccessException e){
            return new ArrayList<Long>(0);
        }
        catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
	public List<FormData> find(List<Integer> departmentIds, int reportPeriodId) {
        Map paramMap = new HashMap();
        paramMap.put("rp", reportPeriodId);
        List<Long> formsId = getNamedParameterJdbcTemplate().queryForList(
                String.format(
                        "select id from form_data where %s and report_period_id = :rp",
                        SqlUtils.transformToSqlInStatement("department_id", departmentIds)),
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
					new FormDataWithoutRowMapperWithTypeId());
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
    public List<Long> getFormDataIds(List<TaxType> taxTypes, final List<Integer> departmentIds) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>(){{put("departmentIds", departmentIds);}};
            String sql = "select fd.id from FORM_DATA fd left join FORM_TEMPLATE ft on fd.FORM_TEMPLATE_ID = ft.id " +
                    "left join FORM_TYPE ftype on ft.TYPE_ID = ftype.ID " +
                    "where ftype.TAX_TYPE in " + SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) + " and fd.DEPARTMENT_ID in (:departmentIds)";
            return getNamedParameterJdbcTemplate().queryForList(
                    sql,
                    values,
                    Long.class
            );
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Long>(0);
        } catch (DataAccessException e) {
            logger.error("Ошибка при поиске налоговых форм с заданными параметрами: formTypeId = %s, kind = %s, departmentId = %s", e);
            throw new DaoException("Ошибка при поиске налоговых форм с заданными параметрами", e);
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

    @Override
    public List<FormData> getFormDataListForCrossNumeration(Integer year, Integer departmentId, String type, Integer kind) {
        return getJdbcTemplate().query("SELECT * FROM form_data fd " +
                        "JOIN form_column fc ON fc.form_template_id = fd.form_template_id " +
                        "LEFT JOIN report_period rp ON fd.report_period_id = rp.id " +
                        "LEFT JOIN tax_period tp ON tp.id = rp.tax_period_id " +
                        "WHERE fc.type = 'A' AND tp.year = ? AND fd.department_id = ? AND tp.tax_type = ? AND fd.kind = ? ORDER BY fd.period_order",
                new Object[]{year, departmentId, type, kind},
                new FormDataWithoutRowMapper()) ;
    }

    @Override
    public List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind) {
        return getJdbcTemplate().query(
                "select fd.*, ft.type_id from form_data fd " +
                "join department_form_type dft on dft.department_id = fd.department_id and dft.kind = fd.kind " +
                "join form_template ft on ft.id = fd.form_template_id and ft.type_id = dft.form_type_id " +
                "join form_type t on t.id = ft.type_id " +
                "join declaration_source ds on ds.src_department_form_type_id = dft.id " +
                "where " + SqlUtils.transformToSqlInStatement("dft.department_id", departments) + " and fd.report_period_id = ? and t.tax_type = ? and dft.kind = ? and exists (select 1 from data_row where form_data_id = fd.id and manual = 1)",
                new Object[]{reportPeriodId, String.valueOf(taxType.getCode()), kind.getId()},
                new FormDataWithoutRowMapperWithTypeId()) ;
    }


    private static final String UPDATE_FORM_DATA_PERFORMER_TB =
            "merge into FORM_DATA_PERFORMER fdp using (\n" +
                    "  with depNameParts as (select SUBSTR(rdn, 0, instr(rdn, '/', 1) - 1) first_dep_name, SUBSTR(rdn, instr(rdn, '/', 1) + 1,length(rdn)) second_dep_name, form_data_id from (select fdp_f.REPORT_DEPARTMENT_NAME rdn, fdp_f.FORM_DATA_ID form_data_id from FORM_DATA_PERFORMER fdp_f)),\n" +
                    "       formDataIdsWithRegExp as (select distinct fd.ID fd_id, depNameParts.first_dep_name, depNameParts.second_dep_name from FORM_DATA fd\n" +
                    "                     join REPORT_PERIOD rp on rp.ID = fd.REPORT_PERIOD_ID\n" +
                    "                     join depNameParts on depNameParts.form_data_id = fd.ID\n" +
                    "                      where %s \n" +
                    "                      and depNameParts.first_dep_name = :oldDepartmentName)" +
                    "SELECT * FROM formDataIdsWithRegExp) b on (fdp.form_data_id = b.fd_id) WHEN MATCHED THEN UPDATE SET REPORT_DEPARTMENT_NAME = (:newDepartmentName || '/' || b.second_dep_name)";
    @Override
    public void updateFDPerformerTBDepartmentNames(String newDepartmentName, String oldDepartmentName, Date dateFrom, Date dateTo) {
        String dateTag = dateFrom != null && dateTo != null ? "(rp.START_DATE between :dateFrom and :dateTo or rp.END_DATE  between :dateFrom and :dateTo)"
                : dateFrom != null ? "rp.START_DATE >= :dateFrom"
                : null;
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("dateFrom", dateFrom);
        values.put("dateTo", dateTo);
        values.put("newDepartmentName", newDepartmentName);
        values.put("oldDepartmentName", oldDepartmentName);
        try {
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_TB, dateTag), values);
        } catch (DataAccessException e){
            logger.error("Ошибка при обновлении значений.", e);
            throw new DaoException("Ошибка при обновлении значений.", e);
        }
    }

    private static final String UPDATE_FORM_DATA_PERFORMER_DEPARTMENT_NAME =
            "merge into FORM_DATA_PERFORMER fdp using (\n" +
                    "  with depNameParts as (select SUBSTR(rdn, 0, instr(rdn, '/', 1) - 1) first_dep_name, SUBSTR(rdn, instr(rdn, '/', 1) + 1,length(rdn)) second_dep_name, form_data_id from \n" +
                    "    (select fdp_f.REPORT_DEPARTMENT_NAME rdn, fdp_f.FORM_DATA_ID form_data_id from FORM_DATA_PERFORMER fdp_f where fdp_f.PRINT_DEPARTMENT_ID = :departmentId)),\n" +
                    "       formDataIdsWithRegExp as (select distinct fd.ID fd_id, depNameParts.first_dep_name, depNameParts.second_dep_name from FORM_DATA fd\n" +
                    "                     join REPORT_PERIOD rp on rp.ID = fd.REPORT_PERIOD_ID\n" +
                    "                     join depNameParts on depNameParts.form_data_id = fd.ID\n" +
                    "                      where %s)\n" +
                    "              SELECT * FROM formDataIdsWithRegExp) b on (fdp.form_data_id = b.fd_id) WHEN MATCHED THEN UPDATE SET REPORT_DEPARTMENT_NAME = (b.first_dep_name || '/' || :newDepartmentName)";
    @Override
    public void updateFDPerformerDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo) {
        String dateTag = dateFrom != null && dateTo != null ? "(rp.START_DATE between :dateFrom and :dateTo or rp.END_DATE  between :dateFrom and :dateTo)"
                : dateFrom != null ? "rp.START_DATE >= :dateFrom"
                : null;
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("dateFrom", dateFrom);
        values.put("dateTo", dateTo);
        values.put("newDepartmentName", newDepartmentName);
        values.put("departmentId", departmentId);
        try {
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_DEPARTMENT_NAME, dateTag), values);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }
}
