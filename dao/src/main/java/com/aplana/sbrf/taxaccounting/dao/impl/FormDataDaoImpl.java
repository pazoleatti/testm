package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSignerDao;
import com.aplana.sbrf.taxaccounting.dao.FormPerformerDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация DAO для работы с данными налоговых форм
 * @author dsultanbekov
 */
@Repository("formDataDao")
@Transactional(readOnly = true)
public class FormDataDaoImpl extends AbstractDao implements FormDataDao {

	private static final Log LOG = LogFactory.getLog(FormDataDaoImpl.class);

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
    private TaxPeriodDao taxPeriodDao;
    @Autowired
    private DataRowDao dataRowDao;

    // Общий маппер
    private void mapCommon(FormData formData, ResultSet rs) throws SQLException {
        formData.setId(SqlUtils.getLong(rs, "id"));
        formData.setFormTemplateId(SqlUtils.getInteger(rs, "form_template_id"));
        formData.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
        formData.setState(WorkflowState.fromId(SqlUtils.getInteger(rs, "state")));
        formData.setReturnSign(rs.getBoolean("return_sign"));
        formData.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "kind")));
        formData.setReportPeriodId(SqlUtils.getInteger(rs, "report_period_id"));
        formData.setDepartmentReportPeriodId(SqlUtils.getInteger(rs, "department_report_period_id"));
        formData.setPeriodOrder(rs.wasNull() ? null : SqlUtils.getInteger(rs, "period_order"));
        formData.setManual(rs.getBoolean("manual"));
        formData.setSorted(rs.getBoolean("SORTED"));
        formData.setComparativePeriodId(SqlUtils.getInteger(rs, "COMPARATIVE_DEP_REP_PER_ID"));
        formData.setAccruing(rs.getBoolean("ACCRUING"));
    }

    // Маппер экземпляра НФ с фиксированными строками из шаблона
    private class FormDataRowMapper implements RowMapper<FormData> {
        @Override
        public FormData mapRow(ResultSet rs, int index) throws SQLException {
            int formTemplateId = SqlUtils.getInteger(rs, "form_template_id");
            FormTemplate formTemplate = formTemplateDao.get(formTemplateId);
            FormData formData = new FormData(formTemplate);
            mapCommon(formData, rs);
            formData.setSigners(formDataSignerDao.getSigners(formData.getId()));
            formData.setPerformer(formPerformerDao.get(formData.getId()));
            formData.setPreviousRowNumber(rs.getInt("number_previous_row"));
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
                            "CASE WHEN ? IS NULL THEN fd.manual ELSE ? END manual, " +
                            "fd.department_report_period_id, drp.report_period_id, drp.department_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING from form_data fd, " +
                            "department_report_period drp where fd.id = ? and fd.department_report_period_id = drp.id",
                    new Object[]{
                            manual == null ? null : manual ? 1 : 0,
                            manual == null ? null : manual ? 1 : 0,
                            formDataId}, new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new FormDataRowMapper());

        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Записи в таблице FORM_DATA с id = " + formDataId + " не найдена", e);
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
                            "return_sign, period_order, number_previous_row, COMPARATIVE_DEP_REP_PER_ID, ACCRUING) " +
                            "values (?, ?, ?, ?, ?, 0, ?, ?, ?, ?)",
                    formDataId, formData.getFormTemplateId(),
                    formData.getDepartmentReportPeriodId(), formData.getKind().getId(),
                    formData.getState().getId(), formData.getPeriodOrder(), formData.getPreviousRowNumber(),
                    formData.getComparativePeriodId(), formData.isAccruing());
            formData.setId(formDataId);
            savePerformerSigner(formData);
        } else {
            formDataId = formData.getId();
        }
        return formDataId;
    }

    @Override
    @Transactional(readOnly = false)
    public void savePerformerSigner(FormData formData) {
        if (formData.getPerformer() != null) {
            formPerformerDao.save(formData.getId(), formData.isManual(), formData.getPerformer());
        }
        if (formData.getSigners() != null) {
            formDataSignerDao.saveSigners(formData.getId(), formData.getSigners());
        }
    }

    private static final String DELETE_FROM_FD_NNN = "delete from form_data_%s where form_data_id=?";

    @Override
    @Transactional(readOnly = false)
    public void delete(int ftId, long fdId) {
        Object[] params = {fdId};
        int[] types = {Types.NUMERIC};
        getJdbcTemplate().update("delete from form_data where id = ?", params, types);
        getJdbcTemplate().update(String.format(DELETE_FROM_FD_NNN, ftId), fdId);
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
            throw new DaoException(String.format("Ошибка поиска НФ для заданного шаблона %d", formTemplateId), e);
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
					String.format(
                    "Для заданного сочетания параметров найдено несколько налоговых форм: вид \"%s\", тип \"%s\", подразделение \"%s\", налоговый период \"%s\", месяц \"%s\"",
                    formTypeDao.get(formTypeId).getName(),
                    kind.getTitle(),
                    departmentDao.getDepartment(departmentId).getName(),
                    taxPeriod.getYear(),
                    periodOrder <= 12 && periodOrder >= 1 ? Formats.months[periodOrder] : periodOrder),
					e
            );
        }
    }

    @Override
    public FormData find(int formTypeId, FormDataKind kind, int departmentReportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("formTypeId", formTypeId);
            params.put("kind", kind.getId());
            params.put("departmentReportPeriodId", departmentReportPeriodId);
            params.put("periodOrder", periodOrder);
            params.put("comparativePeriodId", comparativePeriodId);
            params.put("accruing", accruing);
            String sql = "select fd.id, fd.form_template_id, fd.state, fd.kind, \n" +
                    "fd.return_sign, fd.period_order, fd.manual, fd.number_previous_row, fd.department_report_period_id, \n" +
                    "drp.report_period_id, drp.department_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING, \n" +
                    "(SELECT type_id FROM form_template ft WHERE ft.id = fd.form_template_id) type_id \n" +
                    "from form_data fd \n" +
                    "join department_report_period drp on drp.id = fd.department_report_period_id \n" +
                    "join form_template ft on ft.id = fd.form_template_id \n" +
                    "left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID \n" +
                    "left join report_period crp on crp.id = cdrp.report_period_id \n" +
                    "where ft.type_id = :formTypeId and fd.kind = :kind and drp.id = :departmentReportPeriodId \n" +
                    "and (:periodOrder is null or fd.period_order = :periodOrder) \n" +
                    "and (:comparativePeriodId is null or crp.id = (select report_period_id from department_report_period where id = :comparativePeriodId)) and fd.accruing = :accruing \n";
            return getNamedParameterJdbcTemplate().queryForObject(sql,
                    params,
                    new FormDataWithoutRowMapperWithType());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<FormData> find(List<Integer> departmentIds, int reportPeriodId) {
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("rp", reportPeriodId);
        String sql = "select fd.id, fd.form_template_id, fd.state, fd.kind, fd.form_template_id, " +
                "fd.return_sign, fd.period_order, fd.manual, fd.number_previous_row, fd.department_report_period_id, " +
                "drp.report_period_id, drp.department_id, ft.type_id as type_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING " +
                "from form_data fd \n" +
                "join department_report_period drp on drp.id = fd.department_report_period_id \n" +
                "join form_template ft on ft.id = fd.form_template_id \n" +
                "join form_type t on t.id = ft.type_id \n" +
                "where " + (!departmentIds.isEmpty() ? SqlUtils.transformToSqlInStatement("drp.department_id", departmentIds) : "") +
                "and drp.report_period_id = :rp order by drp.id";
        return getNamedParameterJdbcTemplate().query(sql, paramMap, new FormDataWithoutRowMapperWithType());
    }


    @Override
    public List<FormData> getIfrsForm(int reportPeriodId) {
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("rp", reportPeriodId);
        return getNamedParameterJdbcTemplate().query("select fd.id, fd.form_template_id, fd.state, fd.kind, fd.form_template_id, " +
                "fd.return_sign, fd.period_order, fd.manual, fd.number_previous_row, fd.department_report_period_id, " +
                "drp.report_period_id, drp.department_id, ft.type_id as type_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING " +
                "from form_data fd \n" +
                "join department_report_period drp on drp.id = fd.department_report_period_id \n" +
                "join form_template ft on ft.id = fd.form_template_id \n" +
                "join form_type t on t.id = ft.type_id \n" +
                "where t.is_ifrs = 1 and drp.correction_date is null \n" +
                "and drp.report_period_id = :rp", paramMap, new FormDataWithoutRowMapperWithType());
    }

    @Override
    public FormData getWithoutRows(long id) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT fd.id, drp.department_id, fd.state, fd.kind, drp.report_period_id, fd.return_sign, fd.form_template_id, " +
                            "fd.period_order, fd.manual, fd.department_report_period_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING, " +
                            "(SELECT type_id FROM form_template ft WHERE ft.id = fd.form_template_id) type_id " +
                            "FROM form_data fd, " +
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
            return getJdbcTemplate().queryForList("SELECT fd.id FROM form_data fd, department_report_period drp " +
                            "WHERE drp.id = fd.department_report_period_id and fd.form_template_id = ? AND drp.report_period_id " +
                            "IN (SELECT id FROM report_period WHERE calendar_start_date >= ?)",
                    new Object[]{templateId, startDate},
                    Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        } catch (DataAccessException e) {
			LOG.error("Ошибка при поиске используемых версий", e);
            throw new DaoException("Ошибка при поиске используемых версий", e);
        }
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
			LOG.error(errorMsg, e);
            throw new DaoException(errorMsg, e);
        }
    }

    @Override
    public List<Long> getFormDataIds(List<TaxType> taxTypes, final List<Integer> departmentIds) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            String sql = "select fd.id from FORM_DATA fd left join FORM_TEMPLATE ft on fd.FORM_TEMPLATE_ID = ft.id " +
                    "join FORM_TYPE ftype on ft.TYPE_ID = ftype.ID, department_report_period drp " +
                    "where drp.id = fd.department_report_period_id and " +
                    "ftype.TAX_TYPE in " + SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) + " " +
                    "and " + SqlUtils.transformToSqlInStatement("drp.DEPARTMENT_ID", departmentIds);
            return getNamedParameterJdbcTemplate().queryForList(
                    sql,
                    values,
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>(0);
        } catch (DataAccessException e) {
			LOG.error("Ошибка при поиске налоговых форм с заданными параметрами: formTypeId = %s, kind = %s, departmentId = %s", e);
            throw new DaoException("Ошибка при поиске налоговых форм с заданными параметрами", e);
        }
    }

    @Override
    public void deleteManual(FormData formData) {
        dataRowDao.removeAllManualRows(formData);
        formData.setManual(false);
        updateManual(formData);
    }

    private static final String GET_FORM_DATA_LIST_QUERY = "WITH list AS (SELECT ROWNUM as row_number, sorted.* from " +
            "(SELECT fd.id, drp.department_id, fd.state, fd.return_sign, fd.kind, drp.report_period_id, fd.period_order, fd.number_previous_row, fd.form_template_id, fd.manual, " +
            "fd.department_report_period_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING " +
            "FROM form_data fd " +
            "JOIN department_report_period drp ON drp.id = fd.department_report_period_id " +
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
    public void updatePreviousRowNumber(FormData formData, Integer previousRowNumber) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("count", previousRowNumber);
        params.put("form_data_id", formData.getId());
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET number_previous_row = :count WHERE id = :form_data_id", params);
    }

    @Override
    public void updateCurrentRowNumber(FormData formData) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("count", dataRowDao.getAutoNumerationRowCount(formData));
        params.put("form_data_id", formData.getId());
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET number_current_row = :count WHERE id = :form_data_id", params);
    }

    private static final String GET_MANUAL_UNPUTS_FORMS = "select fd.*, drp.report_period_id, drp.department_id, ft.type_id \n" +
            "from form_data fd \n" +
            "join department_form_type dft on dft.kind = fd.kind \n" +
            "join form_template ft on ft.id = fd.form_template_id and ft.type_id = dft.form_type_id \n" +
            "join form_type t on t.id = ft.type_id \n" +
            "join declaration_source ds on ds.src_department_form_type_id = dft.id \n" +
            "join department_report_period drp on drp.id = fd.department_report_period_id \n" +
            "where %s and drp.report_period_id = :reportPeriodId and t.tax_type = :taxType and dft.kind = :kind and fd.manual = 1 \n" +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))";

    @Override
    public List<FormData> getManualInputForms(List<Integer> departments, int reportPeriodId, TaxType taxType, FormDataKind kind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("reportPeriodId", reportPeriodId);
        params.put("taxType", String.valueOf(taxType.getCode()));
        params.put("kind", kind.getId());
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        String sql = String.format(GET_MANUAL_UNPUTS_FORMS, SqlUtils.transformToSqlInStatement("drp.department_id", departments));
        return getNamedParameterJdbcTemplate().query(sql, params, new FormDataWithoutRowMapperWithType());
    }

    @Override
    public List<FormData> getFormDataListByTemplateId(Integer formTemplateId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT fd.id, fd.form_template_id, fd.state, fd.kind, fd.return_sign, fd.period_order, fd.number_previous_row, " +
                            "fd.manual, fd.department_report_period_id, drp.report_period_id, drp.department_id, fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING " +
                            "FROM FORM_DATA fd, department_report_period drp " +
                            "WHERE drp.id = fd.department_report_period_id and fd.form_template_id = ?",
                    new Object[]{formTemplateId},
                    new FormDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Записи в таблице FORM_DATA с form_template_id = " + formTemplateId + " не найдены", e);
        }
    }

    @Override
    public FormData getLast(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId,
                            Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
        return getLastByDate(formTypeId, kind, departmentId, reportPeriodId, periodOrder, null, comparativePeriodId, accruing);
    }

    @Override
    public FormData getLastByDate(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId,
                                  Integer periodOrder, Date correctionDate, Integer comparativePeriodId, boolean accruing) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("departmentId", departmentId);
            params.put("reportPeriodId", reportPeriodId);
            params.put("formTypeId", formTypeId);
            params.put("kindId", kind.getId());
            params.put("periodOrder", periodOrder);
            params.put("correctionDate", correctionDate);
            params.put("comparativePeriodId", comparativePeriodId);
            params.put("accruing", accruing);
            String sql = "select * from " +
                    "(select fd.id, fd.form_template_id, fd.state, fd.kind, fd.return_sign, fd.period_order, \n" +
                    "fd.number_previous_row, fd.department_report_period_id, drp.report_period_id, drp.department_id, fd.manual, \n" +
                    "fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING \n" +
                    "from form_data fd \n" +
                    "join department_report_period drp on drp.id = fd.department_report_period_id \n" +
                    "join form_template ft on ft.id = fd.form_template_id\n" +
                    "left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID \n" +
                    "left join report_period crp on crp.id = cdrp.report_period_id \n" +
                    "where drp.department_id = :departmentId \n" +
                    "and drp.report_period_id = :reportPeriodId \n" +
                    "and ft.type_id = :formTypeId \n" +
                    "and fd.kind = :kindId \n" +
                    "and (:periodOrder is null or fd.period_order = :periodOrder) \n" +
                    "and (:correctionDate is null or drp.correction_date is null or drp.correction_date <= :correctionDate) \n" +
                    "and (:comparativePeriodId is null or crp.id = (select report_period_id from department_report_period where id = :comparativePeriodId)) and fd.accruing = :accruing \n" +
                    "order by drp.correction_date desc nulls last) \n" +
                    (isSupportOver() ? "where rownum = 1" : "limit 1");
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new FormDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<FormData> getLastListByDate(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Date correctionDate) {
        try {
            return getJdbcTemplate().query(
                    "select * from " +
                            "(select fd.id, fd.form_template_id, fd.state, fd.kind, fd.return_sign, fd.period_order, " +
                            "fd.number_previous_row, fd.department_report_period_id, drp.report_period_id, drp.department_id, fd.manual, " +
                            "fd.SORTED, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING " +
                            "from form_data fd, department_report_period drp, form_template ft " +
                            "where drp.id = fd.department_report_period_id " +
                            "and ft.id = fd.form_template_id " +
                            "and drp.department_id = ? " +
                            "and drp.report_period_id = ? " +
                            "and ft.type_id = ? " +
                            "and fd.kind = ? " +
                            "and (? is null or fd.period_order = ?) " +
                            "and (? is null or drp.correction_date is null or drp.correction_date <= ?) " +
                            "order by drp.correction_date desc nulls last) ",
                    new Object[]{departmentId, reportPeriodId, formTypeId, kind.getId(), periodOrder, periodOrder,
                            correctionDate, correctionDate},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC,
                            Types.DATE, Types.DATE},
                    new FormDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<FormData>();
        }
    }


    private final static String FIND_FD_BY_RANGE_IN_RP =
            "select fd.id " +
                    " from form_data fd\n" +
                    "  INNER JOIN DEPARTMENT_REPORT_PERIOD drp ON fd.DEPARTMENT_REPORT_PERIOD_ID = drp.ID\n" +
                    "  INNER JOIN REPORT_PERIOD rp ON drp.REPORT_PERIOD_ID = rp.ID\n" +
                    "  where fd.FORM_TEMPLATE_ID = :formTemplateId and (rp.CALENDAR_START_DATE NOT BETWEEN :startDate AND :endDate\n" +
                    "    OR rp.END_DATE NOT BETWEEN :startDate AND :endDate)";

    @Override
    public List<Integer> findFormDataIdsByRangeInReportPeriod(int formTemplateId, Date startDate, Date endDate) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("formTemplateId", formTemplateId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            return getNamedParameterJdbcTemplate().queryForList(FIND_FD_BY_RANGE_IN_RP, params, Integer.class);
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Integer>(0);
        }
    }

    @Override
    public void updateManual(FormData formData) {
        int manual = formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode();
        getJdbcTemplate().update("UPDATE form_data SET manual = ? WHERE id = ?", manual, formData.getId());
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

    //Обновляет имена в печатных формах по частям
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
                    "SELECT * FROM formDataIdsWithRegExp) b on (fdp.form_data_id = b.fd_id) WHEN MATCHED THEN UPDATE SET REPORT_DEPARTMENT_NAME = %s";

    @Override
    public void updateFDPerformerTBDepartmentNames(int departmentId, String newDepartmentName, Date dateFrom, Date dateTo, boolean isChangeTB) {
        //В случае если поменяли тип подразделения с ТБ на другой, то надо удалить наименование ТБ
        String moveFromTB = isChangeTB? "b.second_dep_name" : "(:newDepartmentName || '/' || b.second_dep_name)";

        String dateTag = dateFrom != null && dateTo != null ? "(rp.CALENDAR_START_DATE between :dateFrom and :dateTo or rp.END_DATE between :dateFrom and :dateTo or :dateFrom between rp.CALENDAR_START_DATE and rp.END_DATE)"
                : dateFrom != null ? "(rp.END_DATE >= :dateFrom)"
                : null;
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("dateFrom", dateFrom);
        values.put("dateTo", dateTo);
        values.put("newDepartmentName", newDepartmentName);
        values.put("departmentId", departmentId);
        try {
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_TB2, dateTag, moveFromTB), values);
            getNamedParameterJdbcTemplate().update(String.format(UPDATE_FORM_DATA_PERFORMER_TB, dateTag), values);
        } catch (DataAccessException e) {
			LOG.error("Ошибка при обновлении значений.", e);
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
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void updateSorted(long formDataId, boolean isSorted) {
        int sorted = isSorted ? 1 : 0;
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        values.put("sorted", sorted);
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET sorted = :sorted WHERE id = :formDataId AND NOT (sorted = :sorted) ", values);
    }

    @Override
    public void backupSorted(long formDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET sorted_backup = sorted WHERE id = :formDataId AND NOT (sorted_backup = sorted) ", values);
    }

    @Override
    public void restoreSorted(long formDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET sorted = sorted_backup WHERE id = :formDataId and not (sorted = sorted_backup) ", values);
    }

    @Override
    public void updateEdited(long formDataId, boolean isEdited) {
        int edited = (isEdited ? 1 : 0);
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        values.put("edited", edited);
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET edited = :edited WHERE id = :formDataId", values);
        dataRowDao.deleteSearchResults(null, formDataId);
    }

    @Override
    public boolean isEdited(long formDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        return getNamedParameterJdbcTemplate().queryForObject("SELECT edited FROM form_data WHERE id = :formDataId", values, Boolean.class);
    }

    @Override
    public void updateNote(long formDataId, String note) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        values.put("note", note);
        getNamedParameterJdbcTemplate().update("UPDATE form_data SET note = :note WHERE id = :formDataId", values);
    }

    @Override
    public String getNote(long formDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        return getNamedParameterJdbcTemplate().queryForObject("SELECT note FROM form_data WHERE id = :formDataId", values, String.class);
    }

    @Override
    public boolean existFormData(long formDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("formDataId", formDataId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("SELECT id FROM form_data WHERE id = :formDataId", values, Long.class) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}