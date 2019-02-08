package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportParamDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookFormTypeDaoImpl;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Реализация Dao для работы с шаблонами деклараций
 * @author Eugene Stetsenko
 */
@Repository
@Transactional(readOnly = true)
public class DeclarationTemplateDaoImpl extends AbstractDao implements DeclarationTemplateDao {

	private static final Log LOG = LogFactory.getLog(DeclarationTemplateDaoImpl.class);

	@Autowired
	private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private ReportPeriodDao reportPeriodDao;
    @Autowired
    private DeclarationSubreportDao declarationSubreportDao;
    @Autowired
    private DeclarationSubreportParamDao declarationSubreportParamDao;
    @Autowired
    private DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao;

	private final class DeclarationTemplateRowMapper implements RowMapper<DeclarationTemplate> {
		@Override
		public DeclarationTemplate mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationTemplate d = new DeclarationTemplate();
			d.setId(SqlUtils.getInteger(rs,"id"));
            d.setName(rs.getString("name"));
			d.setVersion(rs.getDate("version"));
            d.setVersionEnd(rs.getDate("version_end"));

            DeclarationType declarationType = new DeclarationType();
            declarationType.setId(SqlUtils.getInteger(rs, "declaration_type_id"));
            declarationType.setName(rs.getString("dtype_name"));
            declarationType.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"dtype_status")));
			d.setType(declarationType);

            d.setXsdId(rs.getString("XSD"));
            d.setJrxmlBlobId(rs.getString("JRXML"));
            d.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "status")));
            d.setSubreports(declarationSubreportDao.getDeclarationSubreports(d.getId()));
            d.setEventScripts(declarationTemplateEventScriptDao.fetch(d.getId()));
            Long formKind = SqlUtils.getLong(rs, "form_kind");
            if (formKind != null) {
                d.setDeclarationFormKind(DeclarationFormKind.fromId(formKind));
            }

            d.setFormType(new RefBookFormTypeDaoImpl.RefBookFormTypeRowMapper("form_type_").mapRow(rs, index));

            d.setDeclarationTemplateFiles(getFiles(d.getId()));
            return d;
		}
	}

	private class DeclarationTemplateRowMapperLight implements RowMapper<DeclarationTemplate> {
        @Override
        public DeclarationTemplate mapRow(ResultSet rs, int index) throws SQLException {
            DeclarationTemplate declarationTemplate = new DeclarationTemplate();
            declarationTemplate.setId(SqlUtils.getInteger(rs,"id"));
            declarationTemplate.setName(rs.getString("name"));
            declarationTemplate.setVersion(rs.getDate("version"));
            declarationTemplate.setVersionEnd(rs.getDate("version_end"));
            return declarationTemplate;
        }
    }

	@Override
	public List<DeclarationTemplate> listAll() {
		try {
			return getJdbcTemplate().query(
					"select dt.*, " +
                            " ft.id form_type_id, ft.code form_type_code, ft.name form_type_name, dtype.name dtype_name, dtype.status dtype_status " +
                            " from " +
                            "(SELECT dt.*, " +
                            (isSupportOver() ?
                                    " lead(version) over(partition BY declaration_type_id order by version) - interval '1' DAY version_end " :
                                    " null version_end"
                            ) +
                            "  FROM declaration_template dt " +
                            "  where status != -1)" +
                            " dt " +
                            " left outer join ref_book_form_type ft on ft.id = dt.form_type " +
                            " left join declaration_type dtype on dtype.id = dt.declaration_type_id ",
					new DeclarationTemplateRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Невозможно получить список налоговых форм");
		}
	}

	@Override
	@Cacheable(cacheNames = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplateId")
	public DeclarationTemplate get(int declarationTemplateId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select dt.id, dt.name, dt.version, dt.declaration_type_id, dt.xsd, dt.jrxml, dt.status, dt.form_kind, " +
                            "  ft.id form_type_id, ft.code form_type_code, ft.name form_type_name, dt.version_end, dtype.name dtype_name, dtype.status dtype_status " +
                            " from " +
                            "(SELECT dt.*, " +
                            (isSupportOver() ?
                                " lead(version) over(partition BY declaration_type_id order by version) - interval '1' DAY version_end " :
                                " null version_end"
                            ) +
                            "  FROM declaration_template dt " +
                            "  where status != -1)" +
                            " dt " +
                            " left outer join ref_book_form_type ft on ft.id = dt.form_type " +
                            " left join declaration_type dtype on dtype.id = dt.declaration_type_id " +
                            " where dt.id = ?",
					new Object[] { declarationTemplateId },
					new DeclarationTemplateRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Шаблон налоговой формы с id = %d не найден в БД", declarationTemplateId);
		}
	}

    @Override
    public SecuredEntity findSecuredEntityById(long id) {
        return get((int) id);
    }

    private String getActiveVersionSql(){
            return "with templatesByVersion as (Select ID, DECLARATION_TYPE_ID, STATUS, VERSION, row_number() " +
                (isSupportOver() ? "over(partition by DECLARATION_TYPE_ID order by version)" : "over()") +
                " rn from declaration_template where DECLARATION_TYPE_ID = ? and status in (0, 1, 2))" +
                "select ID from (select rv.ID ID, rv.STATUS, rv.DECLARATION_TYPE_ID RECORD_ID, rv.VERSION versionFrom, rv2.version versionTo from templatesByVersion rv " +
                "left outer join templatesByVersion rv2 on rv.DECLARATION_TYPE_ID = rv2.DECLARATION_TYPE_ID and rv.rn+1 = rv2.rn) " +
                "where STATUS = 0 and ((TRUNC(versionFrom, 'DD') <= ? and TRUNC(versionTo, 'DD') >= ?) or (TRUNC(versionFrom, 'DD') <= ? and versionTo is null))";
    }

    @Override
	public int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId) {
        JdbcTemplate jt = getJdbcTemplate();
        ReportPeriod reportPeriod = reportPeriodDao.fetchOne(reportPeriodId);
        try {
            return jt.queryForObject(getActiveVersionSql(),
                    new Object[]{declarationTypeId, reportPeriod.getStartDate(), reportPeriod.getEndDate(), reportPeriod.getStartDate()},
                    new int[]{Types.NUMERIC, Types.DATE, Types.DATE, Types.DATE}, Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Выбранный вид налоговой формы %d - %s не существует в выбранном периоде.", declarationTypeId, declarationTypeDao.get(declarationTypeId).getName());
        }catch(IncorrectResultSizeDataAccessException e){
            throw new DaoException("Для даного вида налоговой формы %d - %s найдено несколько активных шаблонов деклараций.", declarationTypeId, declarationTypeDao.get(declarationTypeId).getName());
        }
	}

	@Override
	@Transactional(readOnly = false)
    @Caching(evict = {@CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplate.id", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplate.id + new String(\"_script\")", beforeInvocation = true)})
	public int save(DeclarationTemplate declarationTemplate) {
        try {
            int count = getJdbcTemplate().update(
                    "UPDATE declaration_template SET " +
                            "name = ?, version = ?, create_script = ?, declaration_type_id = ?, xsd = ?, status = ?, jrxml = ?, form_kind = ?, form_type = ? " +
                            "WHERE id = ?",
                    new Object[]{
                            declarationTemplate.getName(),
                            declarationTemplate.getVersion(),
                            declarationTemplate.getCreateScript(),
                            declarationTemplate.getType().getId(),
                            declarationTemplate.getXsdId(),
                            declarationTemplate.getStatus().getId(),
                            declarationTemplate.getJrxmlBlobId(),
                            declarationTemplate.getDeclarationFormKind() != null ? declarationTemplate.getDeclarationFormKind().getId() : null,
                            declarationTemplate.getDeclarationFormTypeId(),
                            declarationTemplate.getId()
                    },
                    new int[]{
                            Types.VARCHAR,
                            Types.DATE,
                            Types.VARCHAR,
                            Types.NUMERIC,
                            Types.VARCHAR,
                            Types.NUMERIC,
                            Types.VARCHAR,
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC
                    }
            );

            if (count == 0) {
                throw new DaoException("Не удалось сохранить данные");
            }

            declarationSubreportDao.updateDeclarationSubreports(declarationTemplate);
            declarationSubreportParamDao.updateDeclarationSubreports(declarationTemplate);

            declarationTemplateEventScriptDao.updateScriptList(declarationTemplate);

            return declarationTemplate.getId();
        } catch (DataAccessException e) {
			LOG.error("Ошибка при создании шаблона.", e);
            throw new DaoException("Ошибка при создании шаблона.", e);
        }
    }

    @Override
    public int create(DeclarationTemplate declarationTemplate) {
        try {
            int declarationTemplateId = generateId("seq_declaration_template", Integer.class);
            getJdbcTemplate().update(
                    "INSERT INTO declaration_template (id, name, version, create_script, declaration_type_id, xsd, status, form_kind, form_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{
                            declarationTemplateId,
                            declarationTemplate.getName(),
                            declarationTemplate.getVersion(),
                            declarationTemplate.getCreateScript(),
                            declarationTemplate.getType().getId(),
                            declarationTemplate.getXsdId(),
                            declarationTemplate.getStatus().getId(),
                            declarationTemplate.getDeclarationFormKind() != null ? declarationTemplate.getDeclarationFormKind().getId() : null,
                            declarationTemplate.getDeclarationFormTypeId()
                    },
                    new int[]{
                            Types.NUMERIC,
                            Types.VARCHAR,
                            Types.DATE,
                            Types.VARCHAR,
                            Types.NUMERIC,
                            Types.VARCHAR,
                            Types.NUMERIC,
                            Types.NUMERIC,
                            Types.NUMERIC
                    }
            );
            declarationTemplate.setId(declarationTemplateId);
            declarationSubreportDao.updateDeclarationSubreports(declarationTemplate);
            declarationSubreportParamDao.updateDeclarationSubreports(declarationTemplate);

            return declarationTemplateId;
        } catch (DataAccessException e){
			LOG.error("Ошибка при создании шаблона.", e);
            throw new DaoException("Ошибка при создании шаблона.", e);
        }

    }

    @Override
    public int[] update(final List<DeclarationTemplate> declarationTemplates) {
        try {
            return getJdbcTemplate().batchUpdate("UPDATE declaration_template SET status = ? WHERE id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            DeclarationTemplate declarationTemplate = declarationTemplates.get(i);
                            ps.setInt(1, declarationTemplate.getStatus().getId());
                            ps.setLong(2, declarationTemplate.getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return declarationTemplates.size();
                        }
                    });
        } catch (DataAccessException e){
			LOG.error("Ошибка обновления налоговых форм.", e);
            throw new DaoException("Ошибка обновления налоговых форм.", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplateId")
    public void setJrxml(int declarationTemplateId, String jrxmlBlobId) {
        int count = getJdbcTemplate().update(
                "update declaration_template set jrxml = ? where id = ?",
                new Object[] {
                        jrxmlBlobId,
                        declarationTemplateId
                },
                new int[] {
                        Types.VARCHAR,
                        Types.NUMERIC
                }
        );
        if (count == 0) {
            throw new DaoException("Не удалось сохранить данные с id = %d, так как она не существует.", declarationTemplateId);
        }
    }

    @Override
    @Cacheable(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplateId + new String(\"_script\")")
    public String getDeclarationTemplateScript(int declarationTemplateId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select create_script from declaration_template where id = ?",
                    new Object[] { declarationTemplateId },
                    new int[]{Types.INTEGER},
                    String.class);
        } catch (EmptyResultDataAccessException e){
            return "";
        } catch (DataAccessException e){
			LOG.error("Ошибка получения скрипта налоговой формы.", e);
            throw new DaoException("Ошибка получения скрипта налоговой формы.", e);
        }
    }

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        if (filter == null) {
            return listAllId();
        }
        StringBuilder query = new StringBuilder("select declaration_template.id " +
                       "from declaration_template " +
                       "left join declaration_type on declaration_template.declaration_type_id = declaration_type.id " +
                       "where declaration_template.status in (0,1)"
        );

        return getJdbcTemplate().queryForList(
                query.toString(),
                Integer.class
        );
    }

    @Override
    public List<Integer> listAllId() {
        return getJdbcTemplate().queryForList(
                "select DECLARATION_TEMPLATE.id from DECLARATION_TEMPLATE where status in (0,1)",
                Integer.class
        );
    }

    @Override
    public List<DeclarationTemplate> fetchAllByType(int declarationTypeId) {
	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue("declarationTypeId", declarationTypeId);
	    return getNamedParameterJdbcTemplate().query(
	            "select id, name, version, version_end from (" +
                        "  SELECT dt.*, " +
                        "    lead(version) over(partition BY declaration_type_id order by version) - interval '1' DAY version_end " +
                        "  FROM declaration_template dt " +
                        "  where status != -1 " +
                        ") WHERE declaration_type_id = :declarationTypeId and status != 2 " +
                        " order by version",
                params,
                new DeclarationTemplateRowMapperLight());
    }

    @Override
    public List<Integer> getDeclarationTemplateVersions(int decTypeId, int decTemplateId, List<Integer> statusList, Date actualBeginVersion, Date actualEndVersion) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", decTypeId);
        valueMap.put("statusList", statusList);
        valueMap.put("actualStartVersion", actualBeginVersion);
        valueMap.put("actualEndVersion", actualEndVersion);
        valueMap.put("decTemplateId", decTemplateId);

        StringBuilder builder = new StringBuilder("select id");
        builder.append(" from declaration_template where declaration_type_id = :typeId");
        if (!statusList.isEmpty())
            builder.append(" and status in (:statusList)");

        if (actualBeginVersion != null && actualEndVersion != null)
            builder.append(" and version between :actualStartVersion and :actualEndVersion");
        else if (actualBeginVersion != null)
            builder.append(" and version >= :actualStartVersion");

        if (decTemplateId != 0)
            builder.append(" and id <> :decTemplateId");
        builder.append(" order by version");

        try {
            return getNamedParameterJdbcTemplate().queryForList(builder.toString(), valueMap, Integer.class);
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Integer>();
        } catch (DataAccessException e){
			LOG.error("Ошибка при получении списка версий макетов.", e);
            throw new DaoException("Ошибка при получении списка версий макетов.", e);
        }
    }

    @Override
    public List<VersionSegment> findFTVersionIntersections(int typeId, int templateId, Date actualStartVersion, Date actualEndVersion) {
        String INTERSECTION_VERSION_SQL = "with segmentIntersection as (Select ID, DECLARATION_TYPE_ID, STATUS, VERSION, row_number()" + (isSupportOver()? " over(partition by DECLARATION_TYPE_ID order by version)" : " over()") +
                " rn from DECLARATION_TEMPLATE where DECLARATION_TYPE_ID = :typeId AND status in (0,1,2)) " +
                " select * " +
                " from (select rv.ID ID, rv.STATUS, rv.VERSION versionFrom," +
                " CASE " +
                " WHEN rv2.STATUS in (0,1) THEN rv2.version - interval '1' day" +
                " WHEN  rv2.STATUS = 2 THEN rv2.version" +
                " END versionTo" +
                " from segmentIntersection rv " +
                " left outer join segmentIntersection rv2 on rv.DECLARATION_TYPE_ID = rv2.DECLARATION_TYPE_ID and rv.rn+1 = rv2.rn) where";

        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", typeId);
        valueMap.put("actualStartVersion", actualStartVersion);
        valueMap.put("actualEndVersion", actualEndVersion);
        valueMap.put("templateId", templateId);

        StringBuilder builder = new StringBuilder(INTERSECTION_VERSION_SQL);
        builder.append(" ((versionFrom <= :actualStartVersion and versionTo >= :actualStartVersion)");
        if (actualEndVersion != null)
            builder.append(" OR versionFrom BETWEEN :actualStartVersion AND :actualEndVersion + interval '1' day OR versionTo BETWEEN :actualStartVersion AND :actualEndVersion");
        else
            builder.append(" OR ID = (select id from (select id, row_number() ").
                    append(isSupportOver() ? " over(partition by DECLARATION_TYPE_ID order by version)" : " over()").
                    append(" rn FROM DECLARATION_TEMPLATE where TRUNC(version, 'DD') > :actualStartVersion AND STATUS in (0,1,2) AND DECLARATION_TYPE_ID = :typeId AND id <> :templateId) WHERE rn = 1)");
        builder.append(" OR (versionFrom <= :actualStartVersion AND versionTo is null))");
        if (templateId != 0)
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
    public Date getDTVersionEndDate(int typeId, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуальности версии не должна быть null");

            Date date = getJdbcTemplate().queryForObject("select  MIN(version) - INTERVAL '1' day" +
                    " from declaration_template where declaration_type_id = ?" +
                    " and TRUNC(version,'DD') > ? and status in (0,1,2)",
                    new Object[]{typeId, actualBeginVersion},
                    Date.class);
            return date != null ? new Date(date.getTime()) : null;
        } catch (DataAccessException e){
            throw new DaoException("Ошибки при получении ближайшей версии.", e);
        }
    }

    @Override
    public int getNearestDTVersionIdRight(int typeId, List<Integer> statusList, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуальности версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("typeId", typeId);
            valueMap.put("statusList", statusList);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            return getNamedParameterJdbcTemplate().queryForObject("select * from (select id from declaration_template where declaration_type_id = :typeId " +
                    " and TRUNC(version, 'DD') > :actualBeginVersion and status in (:statusList) order by version) where rownum = 1",
                    valueMap, Integer.class);
        } catch(EmptyResultDataAccessException e){
            return 0;
        } catch (DataAccessException e){
            throw new DaoException("Ошибки при получении ближайшей версии.", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, beforeInvocation = true)
    public int delete(int declarationTemplateId) {
        try {
            getJdbcTemplate().update("delete from declaration_template where id = ?", new Object[]{declarationTemplateId},
                    new int[]{Types.INTEGER});
            return declarationTemplateId;
        }catch (DataAccessException e){
			LOG.error("Ошибка во время удаления.", e);
            throw new DaoException("Ошибка во время удаления.", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, beforeInvocation = true, allEntries = true)
    public void delete(final Collection<Integer> templateIds) {
        try {
            getNamedParameterJdbcTemplate().update("delete from declaration_template where " + SqlUtils.transformToSqlInStatement("id", templateIds),
                    new HashMap<String, Object>(){{put("ids", templateIds);}});
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public int versionTemplateCount(int decTypeId, List<Integer> statusList) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", decTypeId);
        valueMap.put("statusList", statusList);

        StringBuilder builder = new StringBuilder("select count(id)");
        builder.append(" from declaration_template where declaration_type_id = :typeId");
        if (!statusList.isEmpty())
            builder.append(" and status in (:statusList)");
        try {
            return getNamedParameterJdbcTemplate().queryForObject(builder.toString(), valueMap, Integer.class);
        }catch (EmptyResultDataAccessException e){
            return 0;
        }
        catch (DataAccessException e){
			LOG.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий. %s", e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> versionTemplateCountByType(Collection<Integer> typeIds) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeIds", typeIds);
        String sql = "SELECT declaration_type_id as type_id, COUNT(id) as version_count FROM declaration_template " +
                "where declaration_type_id in (:typeIds) and status in (0,1) GROUP BY declaration_type_id";

        try {
            return getNamedParameterJdbcTemplate().queryForList(sql, valueMap);
        } catch (DataAccessException e){
			LOG.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий. %s", e.getMessage());
        }
    }

    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, beforeInvocation = true ,key = "#decTemplateId")
    @Override
    public int updateVersionStatus(VersionedObjectStatus versionStatus, int decTemplateId) {
        try {
            return getJdbcTemplate().update("update declaration_template set status= ? where id = ?", versionStatus.getId(), decTemplateId);
        } catch (DataAccessException e){
			LOG.error("Ошибка при обновлении статуса версии " + decTemplateId, e);
            throw new DaoException("Ошибка при обновлении статуса версии", e);
        }
    }

    @Override
    public boolean existDeclarationTemplate(int declarationTypeId, int reportPeriodId) {
        JdbcTemplate jt = getJdbcTemplate();
        ReportPeriod reportPeriod = reportPeriodDao.fetchOne(reportPeriodId);
        try {
            return jt.queryForObject(getActiveVersionSql(),
                    new Object[]{declarationTypeId, reportPeriod.getStartDate(), reportPeriod.getEndDate(), reportPeriod.getStartDate()},
                    new int[]{Types.NUMERIC, Types.DATE, Types.DATE, Types.DATE}, Integer.class) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }catch(IncorrectResultSizeDataAccessException e){
            throw new DaoException("Для даного вида налоговой формы %d - %s найдено несколько активных шаблонов деклараций.", declarationTypeId, declarationTypeDao.get(declarationTypeId).getName());
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, beforeInvocation = true ,key = "#dtId")
    public void deleteXsd(int dtId) {
        try{
            getJdbcTemplate().update("update declaration_template set xsd = null where ID = ?", dtId);
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, beforeInvocation = true ,key = "#dtId")
    public void deleteJrxml(int dtId) {
        try{
            getJdbcTemplate().update("update declaration_template set jrxml = null WHERE ID = ?", dtId);
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
    public Integer get(int declarationTypeId, int year) {
        try {
            return getJdbcTemplate().queryForObject("select id from declaration_template where declaration_type_id = ? and extract(year from version) = ?",
                    new Object[]{declarationTypeId, year}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, beforeInvocation = true ,key = "#declarationTemplateId + new String(\"_script\")")
    public void updateScript(final int declarationTemplateId, final String script) {
        getJdbcTemplate().update("UPDATE DECLARATION_TEMPLATE SET CREATE_SCRIPT = ? WHERE ID = ?", script, declarationTemplateId);
    }

    private List<DeclarationTemplateFile> getFiles(long declarationTemplateId) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("declarationTemplateId", declarationTemplateId);
        String sql = "select bd.id blobDataId, bd.name name " +
                "from declaration_template_file dtf " +
                "join blob_data bd on bd.id = dtf.blob_data_id " +
                "where dtf.declaration_template_id = :declarationTemplateId";
        try {
            return getNamedParameterJdbcTemplate().query(sql, valueMap, new RowMapper<DeclarationTemplateFile>() {
                @Override
                public DeclarationTemplateFile mapRow(ResultSet rs, int rowNum) throws SQLException {
                    DeclarationTemplateFile declarationTemplateFile = new DeclarationTemplateFile();
                    declarationTemplateFile.setBlobDataId(rs.getString("blobDataId"));
                    declarationTemplateFile.setFileName(rs.getString("name"));
                    return declarationTemplateFile;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<DeclarationTemplateFile>();
        }
    }

    @Override
    public void deleteTemplateFile(final int declarationTemplateId, final List<String> blobDataIds) {
        getJdbcTemplate().batchUpdate("delete from DECLARATION_TEMPLATE_FILE where blob_data_id = ? and declaration_template_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, blobDataIds.get(i));
                        ps.setLong(2, declarationTemplateId);
                    }

                    @Override
                    public int getBatchSize() {
                        return blobDataIds.size();
                    }
                }
        );
    }

    @Override
    public void createTemplateFile(final int declarationTemplateId, final List<String> blobDataIds) {
        getJdbcTemplate().batchUpdate("insert into DECLARATION_TEMPLATE_FILE(blob_data_id, declaration_template_id) values(?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, blobDataIds.get(i));
                        ps.setLong(2, declarationTemplateId);
                    }

                    @Override
                    public int getBatchSize() {
                        return blobDataIds.size();
                    }
                }
        );
    }

    @Override
    public InputStream getTemplateFileContent(int declarationTemplateId, String fileName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTemplateId", declarationTemplateId);
        params.addValue("fileName", fileName);
        return getNamedParameterJdbcTemplate().queryForObject("select b.data from declaration_template_file f\n" +
                "join blob_data b on b.id = f.blob_data_id\n" +
                "where f.declaration_template_id = :declarationTemplateId and b.name = :fileName", params, new RowMapper<InputStream>() {
            @Override
            public InputStream mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getBlob("data").getBinaryStream();
            }
        });
    }

    @Override
    public boolean isCheckFatal(DeclarationCheckCode code, int templateId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("code", code.getCode());
            params.addValue("templateId", templateId);
            return getNamedParameterJdbcTemplate().queryForObject("select is_fatal from decl_template_checks where check_code = :code and declaration_template_id = :templateId", params, Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            LOG.error(String.format("Code %s not found in decl_template_checks!", code.getCode()));
            return false;
        }
    }

    @Override
    public List<DeclarationTemplateCheck> getChecks(int declarationTypeId, Integer declarationTemplateId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTypeId", declarationTypeId);
        params.addValue("declarationTemplateId", declarationTemplateId);
        return getNamedParameterJdbcTemplate().query(
                "select * from decl_template_checks where declaration_type_id = :declarationTypeId and ((:declarationTemplateId is null and declaration_template_id is null) " +
                        "or (:declarationTemplateId is not null and declaration_template_id = :declarationTemplateId))",
                params, new RowMapper<DeclarationTemplateCheck>() {
                    @Override
                    public DeclarationTemplateCheck mapRow(ResultSet rs, int i) throws SQLException {
                        DeclarationTemplateCheck entity = new DeclarationTemplateCheck();
                        entity.setId(rs.getInt("id"));
                        entity.setDeclarationTypeId(rs.getInt("declaration_type_id"));
                        entity.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
                        entity.setCode(DeclarationCheckCode.fromCode(rs.getString("check_code")));
                        entity.setCheckType(rs.getString("check_type"));
                        entity.setDescription(rs.getString("description"));
                        entity.setFatal(rs.getBoolean("is_fatal"));
                        return entity;
                    }
                });
    }

    @Override
    public void createChecks(final List<DeclarationTemplateCheck> checks, final Integer declarationTemplateId) {
        getJdbcTemplate().batchUpdate(
                "insert into decl_template_checks (id, declaration_type_id, declaration_template_id, check_code, check_type, description, is_fatal) values (seq_decl_template_checks.nextval, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        DeclarationTemplateCheck check = iterator.next();
                        ps.setInt(1, check.getDeclarationTypeId());
                        ps.setInt(2, declarationTemplateId);
                        ps.setString(3, check.getCode().getCode());
                        ps.setString(4, check.getCheckType());
                        ps.setString(5, check.getDescription());
                        ps.setBoolean(6, check.isFatal());
                    }

                    @Override
                    public int getBatchSize() {
                        return checks.size();
                    }

                    private Iterator<DeclarationTemplateCheck> iterator = checks.iterator();
                }
        );
    }

    @Override
    public void updateChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId) {
        Set<Integer> ids = new HashSet<>();
        for (DeclarationTemplateCheck check : checks) {
            if (check.isFatal()) {
                ids.add(check.getId());
            }
        }
        String sql = ids.isEmpty() ? "update decl_template_checks set is_fatal = 0 where declaration_template_id = ?" :
                String.format("update decl_template_checks set is_fatal = case when %s then 1 else 0 end where declaration_template_id = ?",
                SqlUtils.transformToSqlInStatement("id", ids));
        getJdbcTemplate().update(sql, declarationTemplateId);
    }

    @Override
    public String findXsdIdByTemplateId(Integer declarationTemplateId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("declarationTemplateId", declarationTemplateId);
            return getNamedParameterJdbcTemplate().queryForObject("select xsd from declaration_template where id = :declarationTemplateId", params, String.class);
        } catch (EmptyResultDataAccessException e) {
            LOG.error(String.format("XSD not found in declaration template with id %d!", declarationTemplateId));
            return null;
        }
    }
}
