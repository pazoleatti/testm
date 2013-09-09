package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;

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
			d.setCreateScript(rs.getString("create_script"));
			d.setVersion(rs.getString("version"));
			d.setEdition(rs.getInt("edition"));
			d.setDeclarationType(declarationTypeDao.get(rs.getInt("declaration_type_id")));
            d.setXsdId(rs.getString("XSD"));
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
					"select * from declaration_template where id = ?",
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
	@CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE, key = "#declarationTemplate.id", beforeInvocation = true)
	public int save(DeclarationTemplate declarationTemplate) {
		int count = 0;
		int declarationTemplateId;
		if (declarationTemplate.getId() == null) {
			declarationTemplateId = generateId("seq_declaration_template", Integer.class);
			count = getJdbcTemplate().update(
					"insert into declaration_template (id, edition, version, is_active, create_script, declaration_type_id, xsd) values (?, ?, ?, ?, ?, ?, ?)",
					new Object[] {
							declarationTemplateId,
							1,
							declarationTemplate.getVersion(),
							declarationTemplate.isActive(),
							declarationTemplate.getCreateScript(),
							declarationTemplate.getDeclarationType().getId(),
                            declarationTemplate.getXsdId()
					},
					new int[] {
							Types.NUMERIC,
							Types.NUMERIC,
							Types.VARCHAR,
							Types.NUMERIC,
							Types.VARCHAR,
							Types.NUMERIC,
                            Types.VARCHAR
					}
			);

		} else {
			declarationTemplateId = declarationTemplate.getId();
			int storedEdition = getJdbcTemplate()
					.queryForInt("select edition from declaration_template where id = ? for update", declarationTemplateId);
			if (storedEdition != declarationTemplate.getEdition()) {
				throw new DaoException("Сохранение описания декларации невозможно, так как её состояние в БД" +
						" было изменено после того, как данные по ней были считаны");
			}
			count = getJdbcTemplate().update(
					"update declaration_template set edition = ?, version = ?, is_active = ?, create_script = ?, declaration_type_id = ?, xsd = ? where id = ?",
					new Object[] {
							storedEdition + 1,
							declarationTemplate.getVersion(),
							declarationTemplate.isActive(),
							declarationTemplate.getCreateScript(),
							declarationTemplate.getDeclarationType().getId(),
                            declarationTemplate.getXsdId(),
							declarationTemplateId
					},
					new int[] {
							Types.NUMERIC,
							Types.VARCHAR,
							Types.NUMERIC,
							Types.VARCHAR,
							Types.NUMERIC,
                            Types.VARCHAR,
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
	public void setJrxmlAndJasper(int declarationTemplateId, String jrxml, byte[] jasper) {
		int count = getJdbcTemplate().update(
				"update declaration_template set jrxml = ?, jasper = ? where id = ?",
				new Object[] {
						jrxml,
						jasper,
						declarationTemplateId
				},
				new int[] {
						Types.VARCHAR,
						Types.BINARY,
						Types.NUMERIC
				}
		);
		if (count == 0) {
			throw new DaoException("Не удалось сохранить данные с id = %d, так как она не существует.", declarationTemplateId);
		}
	}

	@Override
	public String getJrxml(int declarationTemplateId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select jrxml from declaration_template where id = ?",
					new Object[] { declarationTemplateId },
					String.class
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Шаблон декларации с id = %d не найдена в БД", declarationTemplateId);
		}
	}

	@Override
	public byte[] getJasper(int declarationTemplateId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select jasper from declaration_template where id = ?",
					new Object[] { declarationTemplateId },
					byte[].class
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Шаблон декларации с id = %d не найдена в БД", declarationTemplateId);
		}
	}
}
