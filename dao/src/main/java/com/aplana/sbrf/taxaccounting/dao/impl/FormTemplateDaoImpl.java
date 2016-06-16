package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Repository
public class FormTemplateDaoImpl extends AbstractDao implements FormTemplateDao {

	private static final Log LOG = LogFactory.getLog(FormTemplateDaoImpl.class);

	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private ColumnDao columnDao;
	@Autowired
	private FormStyleDao formStyleDao;
    @Autowired
    private ReportPeriodDao reportPeriodDao;

	private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();

	private class FormTemplateMapper implements RowMapper<FormTemplate> {

		@Override
		public FormTemplate mapRow(ResultSet rs, int index) throws SQLException {
			FormTemplate formTemplate = new FormTemplate();
			formTemplate.setId(SqlUtils.getInteger(rs,"id"));
            formTemplate.setVersion(rs.getDate("version"));
			formTemplate.setName(rs.getString("name"));
			formTemplate.setFullName(rs.getString("fullname"));
            formTemplate.setType(formTypeDao.get(SqlUtils.getInteger(rs,"type_id")));
            formTemplate.setFixedRows(rs.getBoolean("fixed_rows"));
            formTemplate.setHeader(rs.getString("header"));
            formTemplate.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"status")));
            formTemplate.setMonthly(rs.getBoolean("monthly"));
			formTemplate.setScript(rs.getString("script"));
            formTemplate.setComparative(rs.getBoolean("COMPARATIVE"));
            formTemplate.setAccruing(rs.getBoolean("accruing"));
            formTemplate.setUpdating(rs.getBoolean("updating"));

			// стили и графы
			formTemplate.getStyles().addAll(formStyleDao.getFormStyles(formTemplate.getId()));
			formTemplate.getColumns().addAll(columnDao.getFormColumns(formTemplate.getId()));
			// фиксированные строки
			String dataRowXml = rs.getString("data_rows");
			if (dataRowXml != null && !dataRowXml.trim().isEmpty()) {
				List<DataRow<Cell>> fixedRows =
					xmlSerializationUtils.deserialize(dataRowXml, formTemplate.getColumns(), formTemplate.getStyles(), Cell.class);
				FormDataUtils.setValueOwners(fixedRows);
				formTemplate.getRows().addAll(fixedRows);
			}
			// шапка
			String headerDataXml = rs.getString("data_headers");
			if (headerDataXml != null && !headerDataXml.trim().isEmpty()) {
				List<DataRow<HeaderCell>> headerRows =
					xmlSerializationUtils.deserialize(headerDataXml, formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class);
				FormDataUtils.setValueOwners(headerRows);
				formTemplate.getHeaders().addAll(headerRows);
			}
			return formTemplate;
		}
	}

	@Cacheable(CacheConstants.FORM_TEMPLATE)
	@Override
	public FormTemplate get(int formId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Fetching FormTemplate with id = " + formId);
		}
		JdbcTemplate jt = getJdbcTemplate();
		try {
			return jt.queryForObject(
					"select id, type_id, data_rows, fixed_rows, name, fullname, script, data_headers, version, status, monthly, header, COMPARATIVE, accruing, updating " +
                            "from form_template where id = ?",
					new Object[]{formId},
					new int[]{Types.NUMERIC},
					new FormTemplateMapper()
			);
		} catch (EmptyResultDataAccessException e) {
            LOG.error("Не удалось найти описание налоговой формы с id = " + formId, e);
			throw new DaoException("Не удалось найти описание налоговой формы с id = " + formId);
		}
	}

    private static final String FORM_DATA_COLUMNS_DELETE = "alter table form_data_%d drop (%s)";
    private static final String FORM_DATA_COLUMNS_ADD = "alter table FORM_DATA_%d add (%s)";
    private static final String FORM_DATA_COLUMNS_UPDATE = "alter table FORM_DATA_%d modify (%s)";
    private static final String FORM_DATA_COLUMNS_COMMENT = "comment on column form_data_%d.c%d is '%s'";
	/**
	 * Кэш инвалидируется перед вызовом. Т.е. несмотря на результат выполнения, кэш будет сброшен.
	 * Иначе, если версии не совпадают кэш продолжает возвращать старую версию.
	 */
    @Caching(evict = {@CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id + new String(\"_script\")", beforeInvocation = true)})
	@Override
	public int save(final FormTemplate formTemplate) {
        try {
            final Integer formTemplateId = formTemplate.getId();

            String dataRowsXml = null;
            List<DataRow<Cell>> rows = formTemplate.getRows();
            if (rows != null && !rows.isEmpty()) {
                dataRowsXml = xmlSerializationUtils.serialize(rows);
            }

            String dataHeadersXml = null;
            List<DataRow<HeaderCell>> headers = formTemplate.getHeaders();
            if (headers != null && !headers.isEmpty()) {
                FormDataUtils.cleanValueOwners(headers);
                dataHeadersXml = xmlSerializationUtils.serialize(headers);
            }

            getJdbcTemplate().update(
                    "update form_template set data_rows = ?, data_headers = ?, version = ?, fixed_rows = ?, name = ?, " +
                            " monthly = ?, fullname = ?, header = ?, script=?, status=?, comparative = ?, accruing = ?, updating = ? where id = ?",
                    dataRowsXml,
                    dataHeadersXml,
                    formTemplate.getVersion(),
                    formTemplate.isFixedRows(),
                    formTemplate.getName() != null ? formTemplate.getName() : " ",
                    formTemplate.isMonthly(),
                    formTemplate.getFullName() != null ? formTemplate.getFullName() : " ",
                    formTemplate.getHeader(),
                    formTemplate.getScript() != null ? formTemplate.getScript() : " ",
					formTemplate.getStatus().getId(),
					formTemplate.isComparative(),
					formTemplate.isAccruing(),
                    formTemplate.isUpdating(),
                    formTemplateId
            );

            //http://jira.aplana.com/browse/SBRFACCTAX-11384
            final Collection<Integer> removedStyleIds = formStyleDao.saveFormStyles(formTemplate);
            final Map<ColumnKeyEnum, Collection<Long>> columns  = columnDao.updateFormColumns(formTemplate);

            //Очистка полей содержащих значения удаленных стилей
            if (!removedStyleIds.isEmpty()){
                ArrayList<Long> allColumnsForUpdate = new ArrayList<Long>() {{
                    addAll(columns.get(ColumnKeyEnum.UPDATED)!=null?columns.get(ColumnKeyEnum.UPDATED):new ArrayList<Long>(0));
                    addAll(columns.get(ColumnKeyEnum.ADDED)!=null?columns.get(ColumnKeyEnum.ADDED):new ArrayList<Long>(0));
                }};
                StringBuilder sb = new StringBuilder();
                for (Long colId : allColumnsForUpdate) {
                    sb.append(SqlUtils.transformToSqlInStatement(String.format("c%d_style_id", colId), removedStyleIds));
                    sb.append(" OR ");
                }
                if (!sb.toString().isEmpty()){
                    String sqlForUpdateStyles = sb.toString().substring(0, sb.toString().lastIndexOf("OR") - 1);

                    int num = getJdbcTemplate().update(
                            String.format(
                                    "update form_data_%d set %s where %s",
                                    formTemplate.getId(),
                                    transformForCleanColumns(allColumnsForUpdate, "_style_id"), sqlForUpdateStyles)
                    );
                    LOG.info("Number of updated styles " + num);
                }
            }

            return formTemplateId;
        } catch (DataAccessException e){
            LOG.error("Ошибка при сохранении шаблона.", e);
            throw new DaoException("Ошибка при сохранении шаблона.", e);
        }
	}

    @CacheEvict(value = CacheConstants.FORM_TEMPLATE, allEntries = true)
    @Override
    public int[] update(final List<FormTemplate> formTemplates) {
        try {
            return getJdbcTemplate().batchUpdate("UPDATE form_template SET status=? WHERE id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            FormTemplate formTemplate = formTemplates.get(i);
                            ps.setInt(1, formTemplate.getStatus().getId());
                            ps.setInt(2, formTemplate.getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return formTemplates.size();
                        }
                    });
        } catch (DataAccessException e){
            LOG.error("Ошибка обновления деклараций.", e);
            throw new DaoException("Ошибка обновления деклараций.", e);
        }
    }

	@Override
	public List<FormTemplate> listAll() {
		return getJdbcTemplate().query("select id, type_id, data_rows, fixed_rows, name, fullname, script, data_headers, version, status, monthly, header, COMPARATIVE, accruing, updating " +
                " from form_template where status in (0,1)", new FormTemplateMapper());
	}

    private String getActiveVersionSql(){
        return "with templatesByVersion as (Select ID, TYPE_ID, STATUS, VERSION, row_number() " +
                (isSupportOver() ? "over(partition by TYPE_ID order by version)" : "over()") +
                " rn from FORM_TEMPLATE where TYPE_ID=:formTypeId and status in (0,1,2))" +
                " select ID from (select rv.ID ID, rv.STATUS, rv.TYPE_ID RECORD_ID, rv.VERSION versionFrom, rv2.version versionTo from templatesByVersion rv " +
                " left outer join templatesByVersion rv2 on rv.TYPE_ID = rv2.TYPE_ID and rv.rn+1 = rv2.rn) " +
                " where ((:excludeInactiveTemplate = 1 and STATUS = 0) or (:excludeInactiveTemplate = 0 and STATUS in (0,1))) and ((TRUNC(versionFrom, 'DD') <= :startDate and TRUNC(versionTo, 'DD') >= :endDate) or (TRUNC(versionFrom, 'DD') <= :startDate and versionTo is null))";
    }

    @Override
	public int getActiveFormTemplateId(int formTypeId, int reportPeriodId) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
		try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("formTypeId", formTypeId);
            params.put("startDate", reportPeriod.getStartDate());
            params.put("endDate", reportPeriod.getEndDate());
            params.put("excludeInactiveTemplate", 1);
            return getNamedParameterJdbcTemplate().queryForObject(
                    getActiveVersionSql(),
                    params,
                    Integer.class);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Выбранный вид %s не существует в выбранном периоде", MessageGenerator.mesSpeckSingleD(formTypeDao.get(formTypeId).getTaxType()));
		}catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Для данного вида %s найдено несколько активных шаблонов налоговой формы в одном отчетном периоде.",
                    MessageGenerator.mesSpeckSingleD(formTypeDao.get(formTypeId).getTaxType()));
		}
	}

    private static final String GET_FORM_TEMPLATE_BY_FT_AND_RP =
            "WITH templatesByVersion AS (SELECT\n" +
                    "                              ID,\n" +
                    "                              TYPE_ID,\n" +
                    "                              STATUS,\n" +
                    "                              VERSION,\n" +
                    "                              row_number() over(partition by TYPE_ID order by version) rn\n" +
                    "                            FROM FORM_TEMPLATE\n" +
                    "                            WHERE TYPE_ID = :formTypeId AND status IN (0, 1, 2))\n" +
                    "SELECT ID\n" +
                    "FROM (SELECT\n" +
                    "        rv.ID       ID,\n" +
                    "        rv.STATUS,\n" +
                    "        rv.TYPE_ID  RECORD_ID,\n" +
                    "        rv.VERSION  versionFrom,\n" +
                    "        rv2.version versionTo\n" +
                    "      FROM templatesByVersion rv\n" +
                    "        LEFT OUTER JOIN templatesByVersion rv2 ON rv.TYPE_ID = rv2.TYPE_ID AND rv.rn + 1 = rv2.rn)\n" +
                    "WHERE STATUS in (0,1) AND ((TRUNC(versionFrom, 'DD') <= :startDate AND TRUNC(versionTo, 'DD') >= :endDate) OR\n" +
                    "                      (TRUNC(versionFrom, 'DD') <= :startDate AND versionTo IS NULL))";

    @Override
    public int getFormTemplateIdByFTAndReportPeriod(int formTypeId, Date startDate, Date endDate) {
        try{
            HashMap<String, Object> params = new HashMap<String, Object>(3);
            params.put("formTypeId", formTypeId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            return getNamedParameterJdbcTemplate().queryForObject(GET_FORM_TEMPLATE_BY_FT_AND_RP, params, Integer.class);
        } catch (EmptyResultDataAccessException e){
            throw new DaoException("Выбранный вид %s не существует в выбранном периоде", MessageGenerator.mesSpeckSingleD(formTypeDao.get(formTypeId).getTaxType()));
        } catch(IncorrectResultSizeDataAccessException e){
            throw new DaoException("Для данного вида %s найдено несколько активных шаблонов налоговой формы в одном отчетном периоде.",
                    MessageGenerator.mesSpeckSingleD(formTypeDao.get(formTypeId).getTaxType()));
        }
    }

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        if (filter == null) {
            return listAllId();
        }
        StringBuilder query = new StringBuilder("select form_template.id " +
                       "from form_template " +
                       "left join form_type on form_template.type_id = form_type.id " +
                       "where form_template.status = 0 "
        );

        if (filter.getTaxType() != null) {
            query.append(" and form_type.TAX_TYPE = \'").append(filter.getTaxType().getCode()).append("\'");
        }
        return getJdbcTemplate().queryForList(
                query.toString(),
                Integer.class
        );
    }

    @Override
    public List<Integer> listAllId() {
        return getJdbcTemplate().queryForList(
                "select form_template.id from form_template",
                Integer.class
        );
    }

    @Override
    public List<Integer> getFormTemplateVersions(int formTypeId, List<Integer> statusList) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", formTypeId);
        valueMap.put("statusList", statusList);

        StringBuilder builder = new StringBuilder("select id");
        builder.append(" from form_template where type_id = :typeId");
        if (!statusList.isEmpty())
            builder.append(" and status in (:statusList)");

        builder.append(" order by version");
        try {
            return getNamedParameterJdbcTemplate().queryForList(builder.toString(), valueMap, Integer.class);
        } catch (DataAccessException e){
            throw new DaoException("Ошибка при получении списка версий макетов.", e);
        }
    }

    private static final String INTERSECTION_VERSION_SQL = "with segmentIntersection as (Select ID, TYPE_ID, STATUS, VERSION, row_number() %s" +
            " rn from FORM_TEMPLATE where TYPE_ID = :typeId AND STATUS in (0,1,2)) " +
            " select * from (select rv.ID ID, rv.STATUS, rv.TYPE_ID RECORD_ID, rv.VERSION versionFrom, rv2.version - interval '1' day versionTo" +
            " FROM segmentIntersection rv " +
            " left outer join segmentIntersection rv2 on rv.TYPE_ID = rv2.TYPE_ID and rv.rn+1 = rv2.rn) where";
    @Override
    public List<VersionSegment> findFTVersionIntersections(int formTypeId, int formTemplateId, Date actualStartVersion, Date actualEndVersion) {
        String overTag = isSupportOver() ? " over(partition by TYPE_ID order by version)" : " over()";

        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", formTypeId);
        valueMap.put("actualStartVersion", actualStartVersion);
        valueMap.put("actualEndVersion", actualEndVersion);
        valueMap.put("templateId", formTemplateId);

        StringBuilder builder = new StringBuilder(String.format(INTERSECTION_VERSION_SQL, overTag));
        builder.append(" ((versionFrom <= :actualStartVersion and versionTo >= :actualStartVersion)");
        if (actualEndVersion != null)
            builder.append(" OR versionFrom BETWEEN :actualStartVersion AND :actualEndVersion + interval '1' day OR versionTo BETWEEN :actualStartVersion AND :actualEndVersion");
        else
            builder.append(" OR ID = (select id from (select id, row_number() ").
                    append(isSupportOver() ? " over(partition by TYPE_ID order by version)" : " over()").
                    append(" rn FROM FORM_TEMPLATE where TRUNC(version, 'DD') > :actualStartVersion AND STATUS in (0,1,2) AND TYPE_ID = :typeId AND id <> :templateId) WHERE rn = 1)");
        builder.append(" OR (versionFrom <= :actualStartVersion AND versionTo is null))");
        if (formTemplateId != 0)
            builder.append(" and ID <> :templateId");
        try {
            return getNamedParameterJdbcTemplate().query(builder.toString(), valueMap, new RowMapper<VersionSegment>() {
                @Override
                public VersionSegment mapRow(ResultSet resultSet, int i) throws SQLException {
                    VersionSegment segment = new VersionSegment();
                    segment.setTemplateId(SqlUtils.getInteger(resultSet, "ID"));
                    segment.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(resultSet,"STATUS")));
                    segment.setBeginDate(resultSet.getDate("versionFrom"));
                    segment.setEndDate(resultSet.getDate("versionTo"));
                    return segment;
                }
            });
        } catch (DataAccessException e){
            throw new DaoException("Ошибка при получении списка версий макетов.", e);
        }
    }

    @Override
    public Date getFTVersionEndDate(int formTypeId, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуальности версии не должна быть null");

            Date date = getJdbcTemplate().queryForObject("select  MIN(version) - INTERVAL '1' day" +
                    " from form_template where type_id = ? and TRUNC(version, 'DD') > ? and status in (0,1,2)",
                    new Object[]{formTypeId, actualBeginVersion},
                    new int[]{Types.NUMERIC, Types.DATE},
                    Date.class);
            return date != null ? new Date(date.getTime()) : null;
        } catch (DataAccessException e){
            throw new DaoException("Ошибки при получении даты окончания версии.", e);
        }
    }

    @Override
    public int getNearestFTVersionIdRight(int formTypeId, List<Integer> statusList, Date actualBeginVersion) {
        HashMap<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", formTypeId);
        valueMap.put("statusList", statusList);
        valueMap.put("actualBeginVersion", actualBeginVersion);
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуальности версии не должна быть null");

            return getNamedParameterJdbcTemplate().queryForObject(
                    "select * from (select id from form_template where type_id = :typeId" +
                            " and TRUNC(version, 'DD') > :actualBeginVersion and status in (:statusList) order by version) where rownum = 1",
                    valueMap,
                    Integer.class);
        } catch(EmptyResultDataAccessException e){
            return 0;
        } catch (DataAccessException e){
            throw new DaoException("Ошибки при получении ближайшей версии.", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.FORM_TEMPLATE, beforeInvocation = true)
    public int delete(int formTemplateId) {
        try {
            getJdbcTemplate().update("delete from form_template where id = ?", new Object[]{formTemplateId}, new int[]{Types.INTEGER});
            return formTemplateId;
        }catch (DataAccessException e){
            LOG.error("Ошибка во время удаления.", e);
            throw new DaoException("Ошибка во время удаления.", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.FORM_TEMPLATE, beforeInvocation = true, allEntries = true)
    public void delete(final Collection<Integer> formTemplateIds) {
        try {
            getNamedParameterJdbcTemplate().update("delete from form_template where " + SqlUtils.transformToSqlInStatement("id", formTemplateIds),
                    new HashMap<String, Object>(){{put("ids", formTemplateIds);}});
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public int saveNew(FormTemplate formTemplate) {

        String dataRowsXml = null;
        List<DataRow<Cell>> rows = formTemplate.getRows();
        if (rows != null && !rows.isEmpty()) {
            dataRowsXml = xmlSerializationUtils.serialize(rows);
        }

        String dataHeadersXml = null;
        List<DataRow<HeaderCell>> headers = formTemplate.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            FormDataUtils.cleanValueOwners(headers);
            dataHeadersXml = xmlSerializationUtils.serialize(headers);
        }

        int formTemplateId = generateId("seq_form_template", Integer.class);

        try {
            formTemplate.setId(formTemplateId);
            getJdbcTemplate().
                    update("insert into form_template (id, data_rows, data_headers, version, fixed_rows, name, fullname, header, script, status, type_id, comparative, accruing, updating) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            formTemplateId,
                            dataRowsXml,
                            dataHeadersXml,
                            formTemplate.getVersion(),
                            formTemplate.isFixedRows(),
                            formTemplate.getName() != null ? formTemplate.getName() : " ",
                            formTemplate.getFullName() != null ? formTemplate.getFullName() : " ",
                            formTemplate.getHeader() != null?formTemplate.getHeader() : "",
                            formTemplate.getScript() != null ? formTemplate.getScript() : " ",
                            formTemplate.getStatus().getId(),
                            formTemplate.getType().getId(),
                            formTemplate.isComparative(),
							formTemplate.isAccruing(),
                            formTemplate.isUpdating()
                            );

            formStyleDao.saveFormStyles(formTemplate);
            columnDao.updateFormColumns(formTemplate);

            return formTemplateId;
        }catch (DataAccessException e){
            LOG.error("Ошибка при сохранении новой версии макета.", e);
            throw new DaoException("Ошибка при сохранении новой версии макета.", e);
        }
    }

    @Override
    public int versionTemplateCount(int formTypeId, List<Integer> statusList) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", formTypeId);
        valueMap.put("statusList", statusList);

        StringBuilder builder = new StringBuilder("select count(id)");
        builder.append(" from form_template where type_id = :typeId");
        if (!statusList.isEmpty())
            builder.append(" and status in (:statusList)");
        try {
            return getNamedParameterJdbcTemplate().queryForObject(builder.toString(), valueMap, Integer.class);
        } catch (DataAccessException e){
            LOG.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий. %s", e.getMessage());
        }
    }

    @Override
    public List<Map<String,Object>> versionTemplateCountByType(Collection<Integer> formTypeIds) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", formTypeIds);
        String sql = "SELECT type_id, COUNT(id) as version_count FROM form_template where type_id in (:typeId) and status in (0,1) GROUP BY type_id";

        try {
            return getNamedParameterJdbcTemplate().queryForList(sql, valueMap);
        } catch (DataAccessException e){
            LOG.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий. %s", e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.FORM_TEMPLATE, beforeInvocation = true, key = "#formTemplateId")
    public int updateVersionStatus(VersionedObjectStatus versionStatus, int formTemplateId) {
        try {
            return getJdbcTemplate().update("update form_template set status=? where id = ?", versionStatus.getId(), formTemplateId);
        } catch (DataAccessException e){
            LOG.error("Ошибка при обновлении статуса версии " + formTemplateId, e);
            throw new DaoException("Ошибка при обновлении статуса версии", e);
        }
    }

    @Override
    public boolean existFormTemplate(int formTypeId, int reportPeriodId, boolean excludeInactiveTemplate) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("formTypeId", formTypeId);
            params.put("startDate", reportPeriod.getStartDate());
            params.put("endDate", reportPeriod.getEndDate());
            params.put("excludeInactiveTemplate", excludeInactiveTemplate ? 1 : 0);
            return getNamedParameterJdbcTemplate().queryForObject(
                    getActiveVersionSql(),
                    params,
                    Integer.class) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }catch(IncorrectResultSizeDataAccessException e){
            throw new DaoException("Для даного вида налоговой формы %d - %s найдено несколько активных шаблонов налоговой формы в одном отчетном периоде %s.",
                    formTypeId, formTypeDao.get(formTypeId).getName(), reportPeriod.getName());
        }
    }

    private static String COLUMN_PATTERN_DELETED = "c%d, c%d_style_id, c%d_editable, c%d_colspan, c%d_rowspan,";
    private static String transformToCommaDeleted(Collection<Long> collection){
        StringBuilder sb = new StringBuilder();
        for (Long colId : collection){
            sb.append(String.format(COLUMN_PATTERN_DELETED, colId, colId, colId, colId, colId));
        }

        return sb.toString().substring(0, sb.length() - 1);
    }

    private static String COLUMN_PATTERN_ADDED =
                    "c%d %s null, " +
                    "c%d_style_id number(9) null, " +
                    "c%d_editable number(1) null, " +
                    "c%d_colspan number(3) null, " +
                    "c%d_rowspan number(3) null,";
    private static String transformToCommaAdded(Collection<Long> columns, FormTemplate ft){
        StringBuilder sb = new StringBuilder();
        for (Long colId : columns){
            switch (ft.getColumn(colId.intValue()).getColumnType()){
                case STRING:
                    sb.append(String.format(COLUMN_PATTERN_ADDED, colId, "VARCHAR(4000)", colId, colId, colId, colId));
                    break;
                case NUMBER:
                    NumericColumn numericColumn = (NumericColumn)ft.getColumn(colId.intValue());
                    sb.append(String.format(
                                    COLUMN_PATTERN_ADDED,
                                    colId, String.format("NUMBER(%d,%d)", numericColumn.getMaxLength(), numericColumn.getPrecision()), colId, colId, colId, colId)
                    );
                    break;
                case REFBOOK:
                case REFERENCE:
                case AUTO:
                    sb.append(String.format(COLUMN_PATTERN_ADDED, colId, "NUMBER(18)", colId, colId, colId, colId));
                    break;
                case DATE:
                    sb.append(String.format(COLUMN_PATTERN_ADDED, colId, "DATE", colId, colId, colId, colId));
                    break;
            }
        }

        return sb.toString().substring(0, sb.length() - 1);
    }

    /*private static String COLUMN_PATTERN_UPDATED = "c%d %s,";
    private static String transformToCommaUpdated(Collection<Long> columns, FormTemplate ft){
        StringBuilder sb = new StringBuilder();
        for (Long colId : columns){
            switch (ft.getColumn(colId.intValue()).getColumnType()){
                case STRING:
                    sb.append(String.format(COLUMN_PATTERN_UPDATED, colId, "VARCHAR(4000)", colId, colId, colId, colId));
                    break;
                case NUMBER:
                    NumericColumn numericColumn = (NumericColumn)ft.getColumn(colId.intValue());
                    sb.append(String.format(
                            COLUMN_PATTERN_UPDATED, colId, String.format("NUMBER(%d,%d)",
                            numericColumn.getMaxLength(), numericColumn.getPrecision()), colId, colId, colId, colId)
                    );
                    break;
                case REFBOOK:
                case REFERENCE:
                case AUTO:
                    sb.append(String.format(COLUMN_PATTERN_UPDATED, colId, "NUMBER(18)", colId, colId, colId, colId));
                    break;
                case DATE:
                    sb.append(String.format(COLUMN_PATTERN_UPDATED, colId, "DATE", colId, colId, colId, colId));
                    break;
            }
        }

        return sb.toString().substring(0, sb.length()-1);
    }*/

    private static final String CLEAR_COLS = "c%s%s = null,";
    private static <T extends Number> String transformForCleanColumns(Collection<T> columns, String prefix){
        StringBuilder sb = new StringBuilder();
        for (T colId : columns){
            sb.append(String.format(CLEAR_COLS, colId, prefix));
        }

        return sb.toString().substring(0, sb.length()-1);
    }

    //Пока еще не используем, просто блокируем столбцы
    /*private void modifyTables(FormTemplate formTemplate, final Map<ColumnKeyEnum, Collection<Long>> columns){
        //Получаем макет ради колонок, чтобы потом сравнить тип
        FormTemplate dbT = getJdbcTemplate().queryForObject(
                "select id, type_id, data_rows, fixed_rows, name, fullname, script, data_headers, version, status, monthly, header, COMPARATIVE " +
                        "from form_template where id = ?",
                new Object[]{formTemplate.getId()},
                new int[]{Types.NUMERIC},
                new FormTemplateMapper()
        );

        *//*if (columns.get(ColumnDao.KEYS.DELETED) != null){
            String sqlColumnForDrop =
                    String.format(FORM_DATA_COLUMNS_DELETE, formTemplate.getId(), transformToCommaDeleted(columns.get(ColumnDao.KEYS.DELETED)));
            getJdbcTemplate().execute(sqlColumnForDrop);
            logger.info(
                    String.format(
                            "Deleted columns in table FORM_DATA_%d",
                            formTemplate.getId()
                    )
            );
        }*//*

        if (columns.get(ColumnKeyEnum.ADDED) !=null){
            String sqlColumnForAdd =
                    String.format(FORM_DATA_COLUMNS_ADD, formTemplate.getId(), transformToCommaAdded(columns.get(ColumnKeyEnum.ADDED), formTemplate));
            getJdbcTemplate().execute(sqlColumnForAdd);
            *//*for (Long aLong : columns.get(ColumnDao.KEYS.ADDED)){
                Column column = formTemplate.getColumn(aLong.intValue());
                getJdbcTemplate().execute(
                        String.format(FORM_DATA_COLUMNS_COMMENT,
                                formTemplate.getId(),
                                aLong,
                                column.getAlias() + "-" + column.getName())
                );
            }*//*
            logger.info(
                    String.format(
                            "Added columns in table FORM_DATA_%d",
                            formTemplate.getId()
                    )
            );
        }

        if (columns.get(ColumnKeyEnum.UPDATED) !=null){
            Collection<Long> longs = columns.get(ColumnKeyEnum.UPDATED);
            ArrayList<Long> changeType = new ArrayList<Long>();
            for (Long aLong : longs){
                Column newCol = formTemplate.getColumn(aLong.intValue());
                Column oldCol = dbT.getColumn(aLong.intValue());
                if (newCol.getColumnType() != oldCol.getColumnType()){
                    changeType.add(aLong);
                }
                if (newCol.getName().equals(oldCol.getName()) || !newCol.getAlias().equals(oldCol.getAlias())){
                    getJdbcTemplate().execute(
                            String.format(FORM_DATA_COLUMNS_COMMENT,
                                    formTemplate.getId(),
                                    aLong,
                                    newCol.getAlias() + "-" + newCol.getName())
                    );
                }
            }
            if (!changeType.isEmpty()){
                int num = getJdbcTemplate().update(
                        String.format(
                                "update form_data_%d set %s",
                                formTemplate.getId(),
                                transformForCleanColumns(changeType, ""))
                );
                logger.info("Number of updated columns " + num);
                String sqlColumnForUpdate =
                        String.format(FORM_DATA_COLUMNS_UPDATE, formTemplate.getId(), transformToCommaUpdated(changeType, formTemplate));
                getJdbcTemplate().execute(sqlColumnForUpdate);
                logger.info(
                        String.format(
                                "Updated columns in table FORM_DATA_%d",
                                formTemplate.getId()
                        )
                );
            }
        }
    }*/

    private static final String COUNT_EXIST_LARGE_STRING = "SELECT COUNT(*) FROM form_data_%d WHERE length(c%d) > ?";
	@Override
	public boolean checkExistLargeString(Integer formTemplateId, Integer columnId, int maxLength) {
        return getJdbcTemplate().queryForObject(
                String.format(COUNT_EXIST_LARGE_STRING, formTemplateId, columnId),
                new Object[]{maxLength},
                new int[]{Types.INTEGER},
                Integer.class) > 0;
	}

    @Override
    public void createFDTable(final int ftId) {
        getJdbcTemplate().call(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                CallableStatement statement = con.prepareCall("call create_form_data_nnn(?)");
                statement.setInt(1, ftId);
                return statement;
            }
        }, new ArrayList<SqlParameter>() {{
            add(new SqlParameter("FT_ID", ftId));
        }});
    }

    @Override
    public void dropFDTable(int ftId) {
        getJdbcTemplate().execute(String.format("drop table form_data_%d", ftId));
    }

    @Override
    public void dropFTTable(final List<Integer> ftIds) {
        getJdbcTemplate().call(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                CallableStatement statement = con.prepareCall("call delete_form_template(?)");
                statement.setObject(1, StringUtils.join(ftIds.toArray(), ','));
                return statement;
            }
        }, new ArrayList<SqlParameter>(0));
    }

    @Override
    public boolean isFDTableExist(int ftId) {
        return getJdbcTemplate().queryForObject("SELECT * FROM USER_TABLES where table_name = ?",
                new Object[]{String.format("form_data_%d", ftId)}, Integer.class) > 0;
    }

    @Override
    public Integer get(int formTypeId, int year) {
        try {
            return getJdbcTemplate().queryForObject("select id from form_template where type_id = ? and extract(year from version) = ?",
                    new Object[]{formTypeId, year}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplateId", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplateId + new String(\"_script\")", beforeInvocation = true)})
    public void updateScript(final int formTemplateId, final String script) {
        getJdbcTemplate().update("UPDATE FORM_TEMPLATE SET SCRIPT = ? WHERE ID = ?", script, formTemplateId);
    }

    //@Override
    /*public void modifyAdd(FormTemplate formTemplate, Map<ColumnKeyEnum, Collection<Long>> columns) {
        if (columns.get(ColumnKeyEnum.ADDED) !=null){
            String sqlColumnForAdd =
                    String.format(FORM_DATA_COLUMNS_ADD, formTemplate.getId(), transformToCommaAdded(columns.get(ColumnKeyEnum.ADDED), formTemplate));
            getJdbcTemplate().execute(sqlColumnForAdd);
            for (Long aLong : columns.get(ColumnKeyEnum.ADDED)){
                Column column = formTemplate.getColumn(aLong.intValue());
                getJdbcTemplate().execute(
                        String.format(FORM_DATA_COLUMNS_COMMENT,
                                formTemplate.getId(),
                                aLong,
                                column.getAlias() + "-" + column.getName())
                );
            }
            logger.info(
                    String.format(
                            "Added columns in table FORM_DATA_%d",
                            formTemplate.getId()
                    )
            );
        }
    }*/
}