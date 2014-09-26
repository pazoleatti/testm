package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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

    // Общий маппер
    private void mapCommon(FormData formData, ResultSet rs) throws SQLException {
        formData.setId(SqlUtils.getLong(rs, "id"));
        formData.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
        formData.setState(WorkflowState.fromId(SqlUtils.getInteger(rs, "state")));
        formData.setReturnSign(rs.getBoolean("return_sign"));
        formData.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "kind")));
        formData.setReportPeriodId(SqlUtils.getInteger(rs, "report_period_id"));
        formData.setDepartmentReportPeriodId(rs.getInt("department_report_period_id"));
        formData.setPeriodOrder(rs.wasNull() ? null : SqlUtils.getInteger(rs, "period_order"));
        formData.setManual(rs.getBoolean("manual"));
    }

    // Маппер экземпляра НФ с фиксированными строками из шаблона
    private class FormDataRowMapper implements RowMapper<FormData> {
        @Override
        public FormData mapRow(ResultSet rs, int index) throws SQLException {
            int formTemplateId = SqlUtils.getInteger(rs, "form_template_id");
            FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
            FormData formData = new FormData();
            formData.initFormTemplateParams(formTemplate);
            mapCommon(formData, rs);
            formData.setSigners(formDataSignerDao.getSigners(formData.getId()));
            formData.setPerformer(formPerformerDao.get(formData.getId()));
            formData.setPreviousRowNumber(rs.getInt("number_previous_row"));
            return formData;
        }
    }

    // Маппер экземпляра НФ без фиксированных строк из шаблона
    private class FormDataWithoutRowMapper implements RowMapper<FormData> {
        @Override
        public FormData mapRow(ResultSet rs, int index) throws SQLException {
            FormData formData = new FormData();
            mapCommon(formData, rs);
            return formData;
        }
    }

    // Маппер экземпляра НФ без фиксированных строк из шаблона, но с FormType
    private class FormDataWithoutRowMapperWithType implements RowMapper<FormData> {
        @Override
        public FormData mapRow(ResultSet rs, int index) throws SQLException {
            FormData formData = new FormData();
            mapCommon(formData, rs);
            formData.setFormType(formTypeDao.get(SqlUtils.getInteger(rs, "type_id")));
            return formData;
        }
    }

    @Override
    public FormData get(final long formDataId, Boolean manual) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select fd.id, fd.form_template_id, fd.state, fd.kind, fd.return_sign, fd.period_order, fd.number_previous_row, " +
                            "r.manual, fd.department_report_period_id, drp.report_period_id, drp.department_id from form_data fd " +
                            "left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r " +
                            "on (r.form_data_id = fd.id and (? is null or r.manual = ?)), " +
                            "department_report_period drp where fd.id = ? and fd.department_report_period_id = drp.id",
                    new Object[]{
                            manual == null ? null : manual ? 1 : 0,
                            manual == null ? null : manual ? 1 : 0,
                            formDataId}, new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new FormDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Записи в таблице FORM_DATA с id = " + formDataId + " не найдено");
        }
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

        if (formData.getDepartmentReportPeriodId() == null) {
            throw new DaoException("Не указан идентификатор отчётного периода подразделения");
        }

        Long formDataId;
        if (formData.getId() == null) {
            formDataId = generateId("seq_form_data", Long.class);
            getJdbcTemplate().update(
                    "insert into form_data (id, form_template_id, department_report_period_id, kind, state, " +
                            "return_sign, period_order, number_previous_row) " +
                            "values (?, ?, ?, ?, ?, 0, ?, ?)",
                    formDataId, formData.getFormTemplateId(),
                    formData.getDepartmentReportPeriodId(), formData.getKind().getId(),
                    formData.getState().getId(), formData.getPeriodOrder(), formData.getPreviousRowNumber());
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
        Object[] params = {formDataId};
        int[] types = {Types.NUMERIC};
        getJdbcTemplate().update("delete from form_data where id = ?", params, types);
    }
    @Override
    public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
        try {
            Long formDataId = getJdbcTemplate().queryForLong(
                    "select fd.id from form_data fd, department_report_period drp where fd.department_report_period_id = drp.id " +
                            "and exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?) " +
                            "and fd.kind = ? and drp.department_id = ? and drp.report_period_id = ? and drp.correction_date is null",
                    new Object[]{
                            formTypeId,
                            kind.getId(),
                            departmentId,
                            reportPeriodId
                    },
                    new int[]{
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
                    new Object[]{formTemplateId},
                    new int[]{Types.NUMERIC},
                    Long.class
            );
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка поиска НФ для заданного шаблона %d", formTemplateId);
        }
    }

    @Override
    public FormData findMonth(int formTypeId, FormDataKind kind, int departmentId, int taxPeriodId, int periodOrder) {
        try {
            Long formDataId = getJdbcTemplate().queryForLong(
                    "select fd.id from form_data fd, department_report_period drp " +
                            "where fd.department_report_period_id = drp.id " +
                            "and exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?) " +
                            "and fd.kind = ? and drp.department_id = ? and drp.correction_date is null " +
                            "and drp.report_period_id in (select id from report_period where tax_period_id = ?) " +
                            "and fd.period_order = ?",
                    new Object[]{
                            formTypeId,
                            kind.getId(),
                            departmentId,
                            taxPeriodId,
                            periodOrder
                    },
                    new int[]{
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
    public FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder) {
        try {
            return getJdbcTemplate().queryForObject("select fd.id, fd.form_template_id, fd.state, fd.kind, " +
                    "fd.return_sign, fd.period_order, r.manual, fd.number_previous_row, fd.department_report_period_id, " +
                    "drp.report_period_id, drp.department_id, " +
                    "(SELECT type_id FROM form_template ft WHERE ft.id = fd.form_template_id) type_id " +
                    "from form_data fd, department_report_period drp " +
                    "left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r " +
                    "on r.form_data_id = fd.id " +
                    "where drp.id = fd.department_report_period_id " +
                    "and exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?) " +
                    "and fd.kind = ? and drp.id = ? and (? is null or fd.period_order = ?) ",
                    new Object[]{
                            formTypeId,
                            kind.getId(),
                            departmentReportPeriodId,
                            periodOrder,
                            periodOrder},
                    new FormDataWithoutRowMapperWithType());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<FormData> find(List<Integer> departmentIds, int reportPeriodId) {
        Map paramMap = new HashMap();
        paramMap.put("rp", reportPeriodId);
        return getNamedParameterJdbcTemplate().query("select fd.id, fd.form_template_id, fd.state, fd.kind, " +
                "fd.return_sign, fd.period_order, r.manual, fd.number_previous_row, fd.department_report_period_id, " +
                "drp.report_period_id, drp.department_id, ft.type_id as type_id " +
                "from form_data fd, department_report_period drp, form_template ft, form_type t " +
                "left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r " +
                "on r.form_data_id = fd.id " +
                "where drp.id = fd.department_report_period_id and ft.id = fd.form_template_id and t.id = ft.type_id " +
                (!departmentIds.isEmpty() ? "and " + SqlUtils.transformToSqlInStatement("drp.department_id", departmentIds) : "") +
                "and drp.report_period_id = :rp order by drp.id", paramMap, new FormDataWithoutRowMapperWithType());
    }

    @Override
    public FormData getWithoutRows(long id) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT fd.id, drp.department_id, fd.state, fd.kind, drp.report_period_id, fd.return_sign, " +
                            "fd.period_order, r.manual, fd.department_report_period_id, " +
                            "(SELECT type_id FROM form_template ft WHERE ft.id = fd.form_template_id) type_id " +
                            "FROM form_data fd " +
                            "left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r " +
                            "on r.form_data_id = fd.id, " +
                            "department_report_period drp " +
                            "WHERE fd.id = ? and drp.id = fd.department_report_period_id",
                    new Object[]{id}, new int[]{Types.NUMERIC},
                    new FormDataWithoutRowMapperWithType());
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
    public List<Long> getFormDataListInActualPeriodByTemplate(int templateId, Date startDate) {
        try {
            return getJdbcTemplate().queryForList("SELECT fd.id FROM form_data fd , department_report_period drp " +
                    "WHERE drp.id = fd.department_report_period_id and fd.form_template_id = ? AND drp.report_period_id " +
                    "IN (SELECT id FROM report_period WHERE calendar_start_date >= ?)",
                    new Object[]{templateId, startDate},
                    Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        } catch (DataAccessException e) {
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
                    "select fd.id from form_data fd, department_report_period drp " +
                            "where drp.id = fd.department_report_period_id " +
                            "and exists (select 1 from form_template ft where fd.form_template_id = ft.id and ft.type_id = ?) " +
                            "and fd.kind = ? and drp.department_id = ?",
                    new Object[]{
                            formTypeId,
                            kind.getId(),
                            departmentId
                    },
                    new int[]{
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC
                    },
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>(0);
        } catch (DataAccessException e) {
            String errorMsg = String.format("Ошибка при поиске налоговых форм с заданными параметрами: formTypeId = %s, kind = %s, departmentId = %s", formTypeId, kind.getId(), departmentId);
            logger.error(errorMsg, e);
            throw new DaoException(errorMsg, e);
        }
    }

    @Override
    public List<Long> getFormDataIds(List<TaxType> taxTypes, final List<Integer> departmentIds) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>() {{
                put("departmentIds", departmentIds);
            }};
            String sql = "select fd.id from FORM_DATA fd left join FORM_TEMPLATE ft on fd.FORM_TEMPLATE_ID = ft.id " +
                    "join FORM_TYPE ftype on ft.TYPE_ID = ftype.ID, department_report_period drp " +
                    "where drp.id = fd.department_report_period_id and " +
                    "ftype.TAX_TYPE in " + SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) + " " +
                    "and drp.DEPARTMENT_ID in (:departmentIds)";
            return getNamedParameterJdbcTemplate().queryForList(
                    sql,
                    values,
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
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
		return getJdbcTemplate().queryForList("SELECT sv.svalue FROM form_column fc\n" +
				"LEFT JOIN form_data fd ON fc.form_template_id = fd.form_template_id\n" +
				"LEFT JOIN data_row dr ON dr.form_data_id = fd.id\n" +
				"LEFT JOIN data_cell sv ON sv.row_id = dr.id AND sv.column_id = fc.id\n" +
				"WHERE fc.id = ? AND fd.form_template_id = ?",
				new Object[]{columnId, formTemplateTypeId},
				String.class);
	}

    private static final String GET_FORM_DATA_LIST_QUERY = "WITH list AS (SELECT ROWNUM as row_number, sorted.* from " +
            "(SELECT fd.id, drp.department_id, fd.state, fd.return_sign, fd.kind, drp.report_period_id, fd.period_order, fd.number_previous_row, fd.form_template_id, manual, " +
            "fd.department_report_period_id " +
            "FROM form_data fd " +
            "JOIN department_report_period drp ON drp.id = fd.department_report_period_id " +
            "LEFT JOIN (SELECT MAX(manual) AS manual, form_data_id FROM data_row WHERE manual = 0 GROUP BY form_data_id) r ON r.form_data_id = fd.id " +
            "JOIN report_period rp ON drp.report_period_id = rp.id " +
            "JOIN tax_period tp ON tp.id = rp.tax_period_id " +
            "WHERE tp.year = :year AND tp.tax_type = :taxType AND drp.department_id = :departmentId AND fd.kind = :kind AND fd.form_template_id = :templateId " +
            "ORDER BY rp.calendar_start_date, fd.period_order, drp.correction_date nulls first) sorted) " +
            "SELECT * FROM list ";

    @Override
    public List<FormData> getNextFormDataList(FormData formData, TaxPeriod taxPeriod) {
        String whereClause = "WHERE row_number > (SELECT row_number FROM list WHERE id = :formId)";
        return getFormDataList(formData, taxPeriod, whereClause);
    }

    @Override
    public List<FormData> getPrevFormDataList(FormData formData, TaxPeriod taxPeriod) {
        String whereClause = "WHERE row_number < (SELECT row_number FROM list WHERE id = :formId)";
        return getFormDataList(formData, taxPeriod, whereClause);
    }

    private List<FormData> getFormDataList(FormData formData, TaxPeriod taxPeriod, String whereClause) {
        int year = taxPeriod.getYear();
        String taxType = String.valueOf(taxPeriod.getTaxType().getCode());

        StringBuilder sql = new StringBuilder(GET_FORM_DATA_LIST_QUERY);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("year", year)
                .addValue("taxType", taxType)
                .addValue("departmentId", formData.getDepartmentId())
                .addValue("kind", formData.getKind().getId())
                .addValue("templateId", formData.getFormTemplateId())
                .addValue("formId", formData.getId());

        if (formData.getId() != null) {
            sql.append(whereClause);
        }

        return getNamedParameterJdbcTemplate().query(sql.toString(), params, new FormDataRowMapper());
    }

    @Override
    public void updatePreviousRowNumber(Long formDataId, Integer previousRowNumber) {
        getJdbcTemplate().update("UPDATE form_data SET number_previous_row =? WHERE id=?", previousRowNumber, formDataId);
    }

    private static final String GET_MANUAL_UNPUTS_FORMS = "select fd.*, drp.report_period_id, drp.department_id, ft.type_id from form_data fd " +
            "join department_form_type dft on dft.kind = fd.kind " +
            "join form_template ft on ft.id = fd.form_template_id and ft.type_id = dft.form_type_id " +
            "join form_type t on t.id = ft.type_id " +
            "join declaration_source ds on ds.src_department_form_type_id = dft.id " +
            "join department_report_period drp on ds.src_department_form_type_id = dft.id " +
            "where %s and drp.report_period_id = :reportPeriodId and t.tax_type = :taxType and dft.kind = :kind and exists (select 1 from data_row where form_data_id = fd.id and manual = 1) " +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))";

    @Override
    public List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("reportPeriodId", reportPeriodId);
        params.put("taxType", String.valueOf(taxType.getCode()));
        params.put("kind", kind.getId());
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(
                String.format(GET_MANUAL_UNPUTS_FORMS, SqlUtils.transformToSqlInStatement("drp.department_id", departments)),
                params,
                new FormDataWithoutRowMapperWithType());
    }

    @Override
    public List<FormData> getFormDataListByTemplateId(Integer formTemplateId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT fd.id, fd.form_template_id, fd.state, fd.kind, fd.return_sign, fd.period_order, fd.number_previous_row, " +
                            "r.manual, fd.department_report_period_id, drp.report_period_id, drp.department_id FROM FORM_DATA fd " +
                            "LEFT JOIN (SELECT MAX(manual) AS manual, form_data_id FROM data_row WHERE manual = 0 GROUP BY form_data_id) r ON r.form_data_id = fd.id, " +
                            "department_report_period drp " +
                            "WHERE drp.id = fd.department_report_period_id and fd.form_template_id = ?",
                    new Object[]{formTemplateId},
                    new FormDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Записи в таблице FORM_DATA с form_template_id = " + formTemplateId + " не найдены");
        }
    }

    @Override
    public FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select * from " +
                            "(select fd.id, fd.form_template_id, fd.state, fd.kind, fd.return_sign, fd.period_order, " +
                            "fd.number_previous_row, fd.department_report_period_id, drp.report_period_id, drp.department_id, r.manual " +
                            "from form_data fd left join (select max(manual) as manual, form_data_id from data_row group by form_data_id) r " +
                            "on r.form_data_id = fd.id, department_report_period drp, form_template ft " +
                            "where drp.id = fd.department_report_period_id " +
                            "and ft.id = fd.form_template_id " +
                            "and drp.department_id = ? " +
                            "and drp.report_period_id = ? " +
                            "and ft.type_id = ? " +
                            "and fd.kind = ? " +
                            "and (? is null or fd.period_order = ?) " +
                            "order by drp.correction_date desc nulls last) " +
                            (isSupportOver() ? "where rownum = 1" : "limit 1"),
                    new Object[]{departmentId, reportPeriodId, formTypeId, kind.getId(), periodOrder, periodOrder},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new FormDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String UPDATE_FORM_DATA_PERFORMER_TB =
            "merge into FORM_DATA_PERFORMER fdp using ( " +
                    "  with formDataIdsWithRegExp as (select distinct fd.ID fd_id, FORM_DATA_PERFORMER.REPORT_DEPARTMENT_NAME from FORM_DATA fd " +
                    "                     join department_report_period drp on drp.ID = fd.department_report_period_id " +
                    "                     join REPORT_PERIOD rp on rp.ID = drp.REPORT_PERIOD_ID " +
                    "                     join FORM_DATA_PERFORMER on FORM_DATA_PERFORMER.form_data_id = fd.ID " +
                    "                     where %s " +
                    "                     and FORM_DATA_PERFORMER.PRINT_DEPARTMENT_ID = :departmentId) " +
                    "SELECT * FROM formDataIdsWithRegExp) b on (fdp.form_data_id = b.fd_id) WHEN MATCHED THEN UPDATE SET REPORT_DEPARTMENT_NAME = :newDepartmentName";

    private static final String UPDATE_FORM_DATA_PERFORMER_TB2 =
            "merge into FORM_DATA_PERFORMER fdp using ( " +
                    "  with depNameParts as (select (SELECT CONNECT_BY_ROOT ID as ID_ROOT FROM DEPARTMENT where id = PRINT_DEPARTMENT_ID START WITH (type = 2) CONNECT BY PRIOR id = PARENT_ID) PRINT_DEPARTMENT_TB_ID, " +
                    "    SUBSTR(rdn, instr(rdn, '/', 1) + 1,length(rdn)) second_dep_name, form_data_id from (select fdp_f.REPORT_DEPARTMENT_NAME rdn, fdp_f.FORM_DATA_ID form_data_id, fdp_f.PRINT_DEPARTMENT_ID from FORM_DATA_PERFORMER fdp_f where fdp_f.REPORT_DEPARTMENT_NAME like '%%/%%')), " +
                    "       formDataIdsWithRegExp as (select distinct fd.ID fd_id, depNameParts.second_dep_name from FORM_DATA fd " +
                    "                     join department_report_period drp on drp.ID = fd.department_report_period_id " +
                    "                     join REPORT_PERIOD rp on rp.ID = drp.REPORT_PERIOD_ID " +
                    "                     join depNameParts on depNameParts.form_data_id = fd.ID " +
                    "                     where %s " +
                    "                     and depNameParts.PRINT_DEPARTMENT_TB_ID = :departmentId) " +
                    "SELECT * FROM formDataIdsWithRegExp) b on (fdp.form_data_id = b.fd_id) WHEN MATCHED THEN UPDATE SET REPORT_DEPARTMENT_NAME = (:newDepartmentName || '/' || b.second_dep_name)";

    @Override
    public void updateFDPerformerTBDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo) {
        String dateTag = dateFrom != null && dateTo != null ? "(rp.CALENDAR_START_DATE between :dateFrom and :dateTo or rp.END_DATE between :dateFrom and :dateTo or :dateFrom between rp.CALENDAR_START_DATE and rp.END_DATE)"
                : dateFrom != null ? "(rp.END_DATE >= :dateFrom)"
                : null;
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("dateFrom", dateFrom);
        values.put("dateTo", dateTo);
        values.put("newDepartmentName", newDepartmentName);
        values.put("departmentId", departmentId);
        try {
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_TB2, dateTag), values);
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_TB, dateTag), values);
        } catch (DataAccessException e) {
            logger.error("Ошибка при обновлении значений.", e);
            throw new DaoException("Ошибка при обновлении значений.", e);
        }
    }

    private static final String UPDATE_FORM_DATA_PERFORMER_DEPARTMENT_NAME =
            "merge into FORM_DATA_PERFORMER fdp using ( " +
                    "  with depNameParts as (select SUBSTR(rdn, 0, instr(rdn, '/', 1) - 1) first_dep_name, SUBSTR(rdn, instr(rdn, '/', 1) + 1,length(rdn)) second_dep_name, form_data_id from " +
                    "    (select fdp_f.REPORT_DEPARTMENT_NAME rdn, fdp_f.FORM_DATA_ID form_data_id from FORM_DATA_PERFORMER fdp_f where fdp_f.PRINT_DEPARTMENT_ID = :departmentId)), " +
                    "       formDataIdsWithRegExp as (select distinct fd.ID fd_id, depNameParts.first_dep_name, depNameParts.second_dep_name from FORM_DATA fd " +
                    "                     join department_report_period drp on drp.ID = fd.department_report_period_id " +
                    "                     join REPORT_PERIOD rp on rp.ID = drp.REPORT_PERIOD_ID " +
                    "                     join depNameParts on depNameParts.form_data_id = fd.ID " +
                    "                     where %s) " +
                    "              SELECT * FROM formDataIdsWithRegExp) b on (fdp.form_data_id = b.fd_id) WHEN MATCHED THEN UPDATE SET REPORT_DEPARTMENT_NAME = (b.first_dep_name || '/' || :newDepartmentName)";

    @Override
    public void updateFDPerformerDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo) {
        String dateTag = dateFrom != null && dateTo != null ? "(rp.CALENDAR_START_DATE between :dateFrom and :dateTo or rp.END_DATE between :dateFrom and :dateTo or :dateFrom between rp.CALENDAR_START_DATE and rp.END_DATE)"
                : dateFrom != null ? "(rp.END_DATE >= :dateFrom)"
                : null;
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("dateFrom", dateFrom);
        values.put("dateTo", dateTo);
        values.put("newDepartmentName", newDepartmentName);
        values.put("departmentId", departmentId);
        try {
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_DEPARTMENT_NAME, dateTag), values);
        } catch (DataAccessException e) {
            logger.error("", e);
            throw new DaoException("", e);
        }
    }
}
