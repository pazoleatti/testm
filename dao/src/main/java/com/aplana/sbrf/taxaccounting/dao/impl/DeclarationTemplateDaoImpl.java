package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
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

	private final class DeclarationTemplateRowMapper implements RowMapper<DeclarationTemplate> {
		@Override
		public DeclarationTemplate mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationTemplate d = new DeclarationTemplate();
			d.setId(rs.getInt("id"));
			d.setActive(rs.getBoolean("is_active"));
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
					"select id, is_active, version, edition, declaration_type_id, xsd, jrxml, status from declaration_template where id = ?",
					new Object[] { declarationTemplateId },
					new DeclarationTemplateRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Шаблон декларации с id = %d не найдена в БД", declarationTemplateId);
		}
	}

	@Override
	public int getActiveDeclarationTemplateId(int declarationTypeId) {
		JdbcTemplate jt = getJdbcTemplate();
		DeclarationTemplate declarationTemplate;
		try {
			declarationTemplate = jt.queryForObject(
					"select * from declaration_template where declaration_type_id = ? and is_active = ?",
					new Object[]{declarationTypeId, 1},
					new int[]{Types.NUMERIC,Types.NUMERIC},
					new DeclarationTemplateRowMapper()
			);
			return declarationTemplate.getId();
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Для данного вида декларации %d не найдено активного шаблона декларации.", declarationTypeId);
		}catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Для даного вида декларации %d найдено несколько активных шаблонов деклараций.", declarationTypeId);
		}
	}

	@Override
	@Transactional(readOnly = false)
    @Caching(evict = {@CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplateId", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplateId + new String(\"_script\")", beforeInvocation = true)})
	public int save(DeclarationTemplate declarationTemplate) {
		int count = 0;
		int declarationTemplateId;
		if (declarationTemplate.getId() == null) {
			declarationTemplateId = generateId("seq_declaration_template", Integer.class);
			count = getJdbcTemplate().update(
					"insert into declaration_template (id, edition, version, is_active, create_script, declaration_type_id, xsd, status) values (?, ?, ?, ?, ?, ?, ?, ?)",
					new Object[] {
							declarationTemplateId,
							declarationTemplate.getEdition(),
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
					"update declaration_template set edition = ?, version = ?, is_active = ?, create_script = ?, declaration_type_id = ?, xsd = ?, status = ? where id = ?",
					new Object[] {
                            declarationTemplate.getEdition(),
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
        return getJdbcTemplate().queryForObject(
                "select create_script from declaration_template where id = ?",
                new Object[] { declarationTemplateId },
                new int[]{Types.INTEGER},
                String.class);
    }

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        if (filter == null) {
            return listAllId();
        }
        StringBuilder query = new StringBuilder("select declaration_template.id " +
                       "from declaration_template " +
                       "left join declaration_type on declaration_template.declaration_type_id = declaration_type.id " +
                       "where is_active = ?"
        );

        if (filter.getTaxType() != null) {
            query.append(" and declaration_type.TAX_TYPE = \'" + filter.getTaxType().getCode() + "\'");
        }
        return getJdbcTemplate().queryForList(
                query.toString(),
                new Object[] { filter.isActive()},
                new int[]{Types.NUMERIC},
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
        valueMap.put("formTemplateId", decTemplateId);

        StringBuilder builder = new StringBuilder("select id");
        builder.append(" from declaration_template where declaration_type_id = :typeId");
        if (!statusList.isEmpty())
            builder.append(" and status in (:statusList)");

        if (actualBeginVersion != null && actualEndVersion != null)
            builder.append(" and version between :actualStartVersion and :actualEndVersion");
        else if (actualBeginVersion != null)
            builder.append(" and version > :actualStartVersion");

        if (decTemplateId != 0)
            builder.append(" and id <> :formTemplateId");
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
    public int getNearestDTVersionIdRight(int typeId, List<Integer> statusList, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("typeId", typeId);
            valueMap.put("statusList", statusList);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            StringBuilder builder = new StringBuilder("select * from (select id");
            builder.append(" from declaration_template where declaration_type_id = :typeId");
            builder.append(" and version > :actualBeginVersion");
            builder.append(" and status in (:statusList) order by version, edition) where rownum = 1");
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
