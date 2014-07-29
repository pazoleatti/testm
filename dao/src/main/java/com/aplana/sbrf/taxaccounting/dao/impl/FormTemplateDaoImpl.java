package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Repository
public class FormTemplateDaoImpl extends AbstractDao implements FormTemplateDao {
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
		private boolean deepFetch;

		public FormTemplateMapper(boolean deepFetch) {
			this.deepFetch = deepFetch;
		}

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
            formTemplate.getStyles().addAll(formStyleDao.getFormStyles(formTemplate.getId()));

			if (deepFetch) {
                /*formTemplate.setScript(rs.getString("script"));*/
				formTemplate.getColumns().addAll(columnDao.getFormColumns(formTemplate.getId()));
				/*String stRowsData = rs.getString("data_rows");
				if (stRowsData != null) {
					formTemplate.getRows().addAll(xmlSerializationUtils.deserialize(stRowsData, formTemplate.getColumns(), formTemplate.getStyles(), Cell.class));
				}
				String stHeaderData = rs.getString("data_headers");
				if (stHeaderData != null) {
					formTemplate.getHeaders().addAll(xmlSerializationUtils.deserialize(stHeaderData, formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class));
					FormDataUtils.setValueOners(formTemplate.getHeaders());
				}*/
			}
			return formTemplate;
		}
	}

	@Cacheable(CacheConstants.FORM_TEMPLATE)
	@Override
	public FormTemplate get(int formId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching FormTemplate with id = " + formId);
		}
		JdbcTemplate jt = getJdbcTemplate();
		try {
			return jt.queryForObject(
					"select id, version, name, fullname, type_id, fixed_rows, header, script, status, monthly " +
                            "from form_template where id = ?",
					new Object[]{formId},
					new int[]{Types.NUMERIC},
					new FormTemplateMapper(true)
			);
		} catch (EmptyResultDataAccessException e) {
            logger.error("Не удалось найти описание налоговой формы с id = " + formId, e);
			throw new DaoException("Не удалось найти описание налоговой формы с id = " + formId);
		}
	}

	/**
	 * Кэш инфалидируется перед вызовом. Т.е. несмотря на результат выполнения, кэш будет сброшен.
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
                FormDataUtils.cleanValueOners(headers);
                dataHeadersXml = xmlSerializationUtils.serialize(headers);
            }

            getJdbcTemplate().update(
                    "update form_template set data_rows = ?, data_headers = ?, version = ?, fixed_rows = ?, name = ?, " +
                            " monthly = ?, fullname = ?, header = ?, script=?, status=? where id = ?",
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
                    formTemplateId
            );
            formStyleDao.saveFormStyles(formTemplate);
            columnDao.saveFormColumns(formTemplate);
            return formTemplateId;
        } catch (DataAccessException e){
            logger.error("Ошибка при сохранении шаблона.", e);
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
            logger.error("Ошибка обновления деклараций.", e);
            throw new DaoException("Ошибка обновления деклараций.", e);
        }
    }

	@Override
	public List<FormTemplate> listAll() {
		return getJdbcTemplate().query("select id, version, name, fullname, type_id, fixed_rows, header, status, monthly" +
                " from form_template where status in (0,1)", new FormTemplateMapper(true));
	}

    private String getActiveVersionSql(){
        return "with templatesByVersion as (Select ID, TYPE_ID, STATUS, VERSION, row_number() " +
                (isSupportOver() ? "over(partition by TYPE_ID order by version)" : "over()") +
                " rn from FORM_TEMPLATE where TYPE_ID=? and status in (0,1,2))" +
                " select ID from (select rv.ID ID, rv.STATUS, rv.TYPE_ID RECORD_ID, rv.VERSION versionFrom, rv2.version versionTo from templatesByVersion rv " +
                " left outer join templatesByVersion rv2 on rv.TYPE_ID = rv2.TYPE_ID and rv.rn+1 = rv2.rn) " +
                " where STATUS = 0 and ((TRUNC(versionFrom, 'DD') <= ? and TRUNC(versionTo, 'DD') >= ?) or (TRUNC(versionFrom, 'DD') <= ? and versionTo is null))";
    }


    @Override
	public int getActiveFormTemplateId(int formTypeId, int reportPeriodId) {
		JdbcTemplate jt = getJdbcTemplate();
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
		try {
            return jt.queryForInt(
                    getActiveVersionSql(),
                    new Object[]{formTypeId, reportPeriod.getStartDate(), reportPeriod.getEndDate(), reportPeriod.getStartDate()},
                    new int[]{Types.NUMERIC, Types.DATE, Types.DATE, Types.DATE});
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Для данного вида налоговой формы %d - %s не найдено активного шаблона налоговой формы.",formTypeId, formTypeDao.get(formTypeId).getName());
		}catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Для данного вида налоговой формы %d - %s найдено несколько активных шаблонов налоговой формы в одном отчетном периоде %s.",
                    formTypeId, formTypeDao.get(formTypeId).getName(), reportPeriod.getName());
		}
	}

    @Cacheable(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplateId + new String(\"_script\")")
    @Override
    public String getFormTemplateScript(int formTemplateId) {
        try {
            return getJdbcTemplate().queryForObject("select script from form_template where id = ?",
                    new Object[]{formTemplateId},
                    new int[]{Types.INTEGER},
                    String.class);
        } catch (DataAccessException e){
            throw new DaoException("Не удалось получить текст скрипта.", e);
        }
    }

    @Override
    public List<DataRow<Cell>> getDataCells(FormTemplate formTemplate) {
        try {
            String dataRowXml = getJdbcTemplate().queryForObject("select data_rows from form_template where id = ?",
                    new Object[]{formTemplate.getId()},
                    new int[]{Types.INTEGER},
                    String.class);
            return dataRowXml != null ? xmlSerializationUtils.deserialize(dataRowXml, formTemplate.getColumns(), formTemplate.getStyles(), Cell.class):
                    new ArrayList<DataRow<Cell>>();
        } catch (IllegalArgumentException e){
            logger.error(String.format("Шаблон %s версия %s", formTemplate.getType().getName(), formTemplate.getId()), e);
            throw new DaoException(e.getLocalizedMessage());
        } catch (DataAccessException e){
            logger.error(String.format("Ошибка при получении строк шаблона %s НФ.", formTemplate.getType().getName()), e);
            throw new DaoException("Ошибка при получении строк шаблона НФ.", e);
        }
    }

    @Override
    public List<DataRow<HeaderCell>> getHeaderCells(FormTemplate formTemplate) {
        try {
            String headerDataXml = getJdbcTemplate().queryForObject("select data_headers from form_template where id = ?",
                    new Object[]{formTemplate.getId()},
                    new int[]{Types.INTEGER},
                    String.class);
            return headerDataXml != null ? xmlSerializationUtils.deserialize(headerDataXml, formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class):
                    new ArrayList<DataRow<HeaderCell>>();
        }  catch (IllegalArgumentException e){
            logger.error(String.format("Шаблон %s версия %s", formTemplate.getType().getName(), formTemplate.getId()), e);
            throw new DaoException(e.getLocalizedMessage());
        } catch (DataAccessException e){
            logger.error(String.format("Ошибка при получении заголовка шаблона %s НФ.", formTemplate.getType().getName()), e);
            throw new DaoException("Ошибка при получении заголовка шаблона НФ.", e);
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

    private static String INTERSECTION_VERSION_SQL = "with segmentIntersection as (Select ID, TYPE_ID, STATUS, VERSION, row_number() %s" +
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
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

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
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            return getNamedParameterJdbcTemplate().queryForInt("select * from (select id from form_template where type_id = :typeId" +
                    " and TRUNC(version, 'DD') > :actualBeginVersion and status in (:statusList) order by version) where rownum = 1", valueMap);
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
            return getJdbcTemplate().update("delete from form_template where id = ?", new Object[]{formTemplateId}, new int[]{Types.INTEGER});
        }catch (DataAccessException e){
            logger.error("Ошибка во время удаления.", e);
            throw new DaoException("Ошибка во время удаления.", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.FORM_TEMPLATE, beforeInvocation = true, allEntries = true)
    public void delete(final Collection<Integer> formTemplateIds) {
        try {
            getNamedParameterJdbcTemplate().update("delete from form_template where id in (:ids)",
                    new HashMap<String, Object>(){{put("ids", formTemplateIds);}});
        } catch (DataAccessException e){
            logger.error("", e);
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
            FormDataUtils.cleanValueOners(headers);
            dataHeadersXml = xmlSerializationUtils.serialize(headers);
        }

        int formTemplateId = generateId("seq_form_template", Integer.class);

        try {
            formTemplate.setId(formTemplateId);
            getJdbcTemplate().
                    update("insert into form_template (id, data_rows, data_headers, version, fixed_rows, name, fullname, header, script, status, type_id) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?)",
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
                            formTemplate.getType().getId()
                            );

            formStyleDao.saveFormStyles(formTemplate);
            columnDao.saveFormColumns(formTemplate);
            return formTemplateId;
        }catch (DataAccessException e){
            logger.error("Ошибка при сохранении новой версии макета.",e);
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
            return getNamedParameterJdbcTemplate().queryForInt(builder.toString(), valueMap);
        } catch (DataAccessException e){
            logger.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий.", e.getMessage());
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
            logger.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий.", e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.FORM_TEMPLATE, beforeInvocation = true, key = "#formTemplateId")
    public int updateVersionStatus(VersionedObjectStatus versionStatus, int formTemplateId) {
        try {
            return getJdbcTemplate().update("update form_template set status=? where id = ?", versionStatus.getId(), formTemplateId);
        } catch (DataAccessException e){
            logger.error("Ошибка при обновлении статуса версии " + formTemplateId, e);
            throw new DaoException("Ошибка при обновлении статуса версии", e);
        }
    }

    @Override
    public boolean existFormTemplate(int formTypeId, int reportPeriodId) {
        JdbcTemplate jt = getJdbcTemplate();
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        try {
            return jt.queryForInt(
                    getActiveVersionSql(),
                    new Object[]{formTypeId, reportPeriod.getStartDate(), reportPeriod.getEndDate(), reportPeriod.getStartDate()},
                    new int[]{Types.NUMERIC, Types.DATE, Types.DATE, Types.DATE}) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }catch(IncorrectResultSizeDataAccessException e){
            throw new DaoException("Для даного вида налоговой формы %d - %s найдено несколько активных шаблонов налоговой формы в одном отчетном периоде %s.",
                    formTypeId, formTypeDao.get(formTypeId).getName(), reportPeriod.getName());
        }
    }
}