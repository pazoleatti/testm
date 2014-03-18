package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.VersionSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Реализация Dao для работы с шаблонами деклараций
 * @author Eugene Stetsenko
 */
@Repository
@Transactional(readOnly = true)
public class DeclarationTemplateDaoImpl extends AbstractDao implements DeclarationTemplateDao {

	@Autowired
	private DeclarationTypeDao declarationTypeDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

	private final class DeclarationTemplateRowMapper implements RowMapper<DeclarationTemplate> {
		@Override
		public DeclarationTemplate mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationTemplate d = new DeclarationTemplate();
			d.setId(rs.getInt("id"));
			d.setActive(rs.getBoolean("is_active"));
            d.setName(rs.getString("name"));
			d.setVersion(rs.getDate("version"));
			d.setEdition(rs.getInt("edition"));
			d.setType(declarationTypeDao.get(rs.getInt("declaration_type_id")));
            d.setXsdId(rs.getString("XSD"));
            d.setJrxmlBlobId(rs.getString("JRXML"));
            d.setStatus(VersionedObjectStatus.getStatusById(rs.getInt("status")));
			return d;
		}
	}

	@Override
	public List<DeclarationTemplate> listAll() {
		try {
			return getJdbcTemplate().query(
					"select * from declaration_template",
					new DeclarationTemplateRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Невозможно получить список деклараций");
		}
	}

	@Override
	@Cacheable(CacheConstants.DECLARATION_TEMPLATE)
	public DeclarationTemplate get(int declarationTemplateId) {
		try {
			return getJdbcTemplate().queryForObject(
                    "select id, is_active, name, version, edition, declaration_type_id, xsd, jrxml, status from declaration_template where id = ?",
                    new Object[]{declarationTemplateId},
                    new DeclarationTemplateRowMapper()
            );
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Шаблон декларации с id = %d не найдена в БД", declarationTemplateId);
		}
	}

	@Override
	public int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId) {
        JdbcTemplate jt = getJdbcTemplate();
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        try {
            String ACTIVE_VERSION_SQL = "with templatesByVersion as (Select ID, DECLARATION_TYPE_ID, STATUS, VERSION, row_number() " +
                    (isSupportOver() ? "over(partition by DECLARATION_TYPE_ID order by version)" : "over()") +
                    " rn from declaration_template where DECLARATION_TYPE_ID = ? and status in (0, 1, 2))" +
                    "select ID from (select rv.ID ID, rv.STATUS, rv.DECLARATION_TYPE_ID RECORD_ID, rv.VERSION versionFrom, rv2.version versionTo from templatesByVersion rv " +
                    "left outer join templatesByVersion rv2 on rv.DECLARATION_TYPE_ID = rv2.DECLARATION_TYPE_ID and rv.rn+1 = rv2.rn) " +
                    "where STATUS = 0 and ((TRUNC(versionFrom, 'DD') <= ? and TRUNC(versionTo, 'DD') >= ?) or (TRUNC(versionFrom, 'DD') <= ? and versionTo is null))";
            return jt.queryForInt(ACTIVE_VERSION_SQL,
                    new Object[]{declarationTypeId, reportPeriod.getStartDate(), reportPeriod.getEndDate(), reportPeriod.getStartDate()},
                    new int[]{Types.NUMERIC, Types.DATE, Types.DATE, Types.DATE}
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Выбранный вид декларации %d не существует в выбранном периоде.", declarationTypeId);
        }catch(IncorrectResultSizeDataAccessException e){
            throw new DaoException("Для даного вида декларации %d найдено несколько активных шаблонов деклараций.", declarationTypeId);
        }
	}

	@Override
	@Transactional(readOnly = false)
    @Caching(evict = {@CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplate.id", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplate.id + new String(\"_script\")", beforeInvocation = true)})
	public int save(DeclarationTemplate declarationTemplate) {
		int count;
		int declarationTemplateId;
		if (declarationTemplate.getId() == null) {
			declarationTemplateId = generateId("seq_declaration_template", Integer.class);
			count = getJdbcTemplate().update(
					"insert into declaration_template (id, edition, name, version, is_active, create_script, declaration_type_id, xsd, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
					new Object[] {
							declarationTemplateId,
							declarationTemplate.getEdition(),
                            declarationTemplate.getName(),
							declarationTemplate.getVersion(),
							declarationTemplate.isActive(),
							declarationTemplate.getCreateScript(),
							declarationTemplate.getType().getId(),
                            declarationTemplate.getXsdId(),
                            declarationTemplate.getStatus().getId()
					},
					new int[] {
							Types.NUMERIC,
							Types.NUMERIC,
                            Types.VARCHAR,
							Types.DATE,
							Types.NUMERIC,
							Types.VARCHAR,
							Types.NUMERIC,
                            Types.VARCHAR,
                            Types.NUMERIC
					}
			);

		} else {
			declarationTemplateId = declarationTemplate.getId();
			/*int storedEdition = getJdbcTemplate()
					.queryForInt("select edition from declaration_template where id = ? for update", declarationTemplateId);
            if (storedEdition != declarationTemplate.getEdition()) {
				throw new DaoException("Сохранение описания декларации невозможно, так как её состояние в БД" +
						" было изменено после того, как данные по ней были считаны");
			}*/
			count = getJdbcTemplate().update(
					"update declaration_template set edition = ?, name = ?, version = ?, is_active = ?, create_script = ?, declaration_type_id = ?, xsd = ?, status = ? where id = ?",
					new Object[] {
                            declarationTemplate.getEdition(),
                            declarationTemplate.getName(),
							declarationTemplate.getVersion(),
							declarationTemplate.isActive(),
							declarationTemplate.getCreateScript(),
							declarationTemplate.getType().getId(),
                            declarationTemplate.getXsdId(),
                            declarationTemplate.getStatus().getId(),
							declarationTemplateId
					},
					new int[] {
							Types.NUMERIC,
                            Types.VARCHAR,
							Types.DATE,
							Types.NUMERIC,
							Types.VARCHAR,
							Types.NUMERIC,
                            Types.VARCHAR,
                            Types.NUMERIC,
                            Types.NUMERIC
					}
			);
		}

		if (count == 0) {
			throw new DaoException("Не удалось сохранить данные");
		}
		return declarationTemplateId;
	}

    @Override
    @Transactional(readOnly = false)
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplateId", beforeInvocation = true)
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
            logger.error("Ошибка получения скрипта декларации.", e);
            throw new DaoException("Ошибка получения скрипта декларации.", e);
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
                       "where declaration_template.status = 0"
        );

        if (filter.getTaxType() != null) {
            query.append(" and declaration_type.TAX_TYPE = \'").append(filter.getTaxType().getCode()).append("\'");
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
        builder.append(" order by version, edition");

        try {
            return getNamedParameterJdbcTemplate().queryForList(builder.toString(), valueMap, Integer.class);
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Integer>();
        } catch (DataAccessException e){
            logger.error("Ошибка при получении списка версий макетов.", e);
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
        builder.append(" (versionFrom <= :actualStartVersion and versionTo >= :actualEndVersion)");
        if (actualEndVersion != null)
            builder.append(" OR versionFrom BETWEEN :actualStartVersion AND :actualEndVersion OR versionTo BETWEEN :actualStartVersion AND :actualEndVersion");
        else
            builder.append(" OR ID = (select id from (select id, row_number() ").
                    append(isSupportOver() ? " over(partition by TYPE_ID order by version)" : " over()").
                    append(" rn FROM DECLARATION_TEMPLATE where TRUNC(version, 'DD') > :actualStartVersion AND STATUS in (0,1,2) AND DECLARATION_TYPE_ID = :typeId AND id <> :templateId) WHERE rn = 1)");
        builder.append(" OR (versionFrom <= :actualStartVersion AND versionTo is null)");
        if (templateId != 0)
            builder.append(" and ID <> :templateId");
        try {
            return getNamedParameterJdbcTemplate().query(builder.toString(), valueMap, new RowMapper<VersionSegment>() {
                @Override
                public VersionSegment mapRow(ResultSet resultSet, int i) throws SQLException {
                    VersionSegment segment = new VersionSegment();
                    segment.setTemplateId(resultSet.getInt("ID"));
                    segment.setStatus(VersionedObjectStatus.getStatusById(resultSet.getInt("STATUS")));
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
    public Date getDTVersionEndDate(int templateId, int typeId, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("templateId", templateId);
            valueMap.put("typeId", typeId);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            StringBuilder builder = new StringBuilder("select * from (select  version - INTERVAL '1' day");
            builder.append(" from declaration_template where declaration_type_id = :typeId");
            builder.append(" and version > :actualBeginVersion");
            builder.append(" and status in (0,1,2) and id <> :templateId order by version) where rownum = 1");
            return getNamedParameterJdbcTemplate().queryForObject(builder.toString(), valueMap, Date.class);
        } catch(EmptyResultDataAccessException e){
            return null;
        } catch (DataAccessException e){
            throw new DaoException("Ошибки при получении ближайшей версии.", e);
        }
    }

    @Override
    public int getNearestDTVersionIdRight(int typeId, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("typeId", typeId);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            StringBuilder builder = new StringBuilder("select * from (select id");
            builder.append(" from declaration_template where declaration_type_id = :typeId");
            builder.append(" and TRUNC(version, 'DD') > :actualBeginVersion");
            builder.append(" and status in (0,1,2) order by version) where rownum = 1");
            return getNamedParameterJdbcTemplate().queryForInt(builder.toString(), valueMap);
        } catch(EmptyResultDataAccessException e){
            return 0;
        } catch (DataAccessException e){
            throw new DaoException("Ошибки при получении ближайшей версии.", e);
        }
    }

    @Override
    public int getNearestDTVersionIdLeft(int typeId, List<Integer> statusList, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("typeId", typeId);
            valueMap.put("statusList", statusList);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            StringBuilder builder = new StringBuilder("select * from (select id");
            builder.append(" from declaration_template where declaration_type_id = :typeId");
            builder.append(" and version < :actualBeginVersion");
            builder.append(" and status in (:statusList) order by version desc, edition desc) where rownum = 1");
            return getNamedParameterJdbcTemplate().queryForInt(builder.toString(), valueMap);
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
            return getJdbcTemplate().update("delete from declaration_template where id = ?", new Object[]{declarationTemplateId},
                    new int[]{Types.INTEGER});
        }catch (DataAccessException e){
            logger.error("Ошибка во время удаления.", e);
            throw new DaoException("Ошибка во время удаления.", e);
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
            return getNamedParameterJdbcTemplate().queryForInt(builder.toString(), valueMap);
        }catch (EmptyResultDataAccessException e){
            return 0;
        }
        catch (DataAccessException e){
            logger.error("Ошибка при получении числа версий.", e);
            throw new DaoException("Ошибка при получении числа версий.", e.getMessage());
        }
    }
}
