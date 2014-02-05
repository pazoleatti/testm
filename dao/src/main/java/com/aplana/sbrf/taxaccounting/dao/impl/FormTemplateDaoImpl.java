package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository
public class FormTemplateDaoImpl extends AbstractDao implements FormTemplateDao {
	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private ColumnDao columnDao;
	@Autowired
	private FormStyleDao formStyleDao;
	private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();

	private class FormTemplateMapper implements RowMapper<FormTemplate> {
		private boolean deepFetch;

		public FormTemplateMapper(boolean deepFetch) {
			this.deepFetch = deepFetch;
		}

		@Override
		public FormTemplate mapRow(ResultSet rs, int index) throws SQLException {
			FormTemplate formTemplate = new FormTemplate();
			formTemplate.setId(rs.getInt("id"));
            formTemplate.setVersion(rs.getDate("version"));
			formTemplate.setName(rs.getString("name"));
			formTemplate.setFullName(rs.getString("fullname"));
            formTemplate.setType(formTypeDao.get(rs.getInt("type_id")));
            formTemplate.setEdition(rs.getInt("edition"));
            formTemplate.setFixedRows(rs.getBoolean("fixed_rows"));
            formTemplate.setCode(rs.getString("code"));
            formTemplate.setStatus(VersionedObjectStatus.getStatusById(rs.getInt("status")));
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
					"select id, version, name, fullname, type_id, edition, fixed_rows, code, script, status " +
                            "from form_template where id = ?",
					new Object[]{formId},
					new int[]{Types.NUMERIC},
					new FormTemplateMapper(true)
			);
		} catch (EmptyResultDataAccessException e) {
            logger.error("Не удалось найти описание налоговой формы с id = " + formId, e);
			throw new DaoException("Не удалось найти описание налоговой формы с id = " + formId);
		} catch (Error error) {
            logger.error("",error);
            throw new DaoException("", error.getMessage());
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
		final Integer formTemplateId = formTemplate.getId();

		/*if (formTemplateId == null) {
			throw new UnsupportedOperationException("Saving of new FormTemplate is not implemented");
		}*/

		JdbcTemplate jt = getJdbcTemplate();
		int storedEdition = jt.queryForInt("select edition from form_template where id = ? for update", formTemplateId);

		if (storedEdition != formTemplate.getEdition()) {
			throw new DaoException("Сохранение описания налоговой формы невозможно, так как её состояние в БД" +
				" было изменено после того, как данные по ней были считаны");
		}

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

        // TODO: создание новых версий формы потребует инсертов в form_template
		getJdbcTemplate().update(
			"update form_template set data_rows = ?, data_headers = ?, edition = ?, version = ?, fixed_rows = ?, name = ?, " +
			"fullname = ?, code = ?, script=?, status=? where id = ?",
			dataRowsXml,
			dataHeadersXml,
			storedEdition,
			formTemplate.getVersion(),
			formTemplate.isFixedRows(),
            formTemplate.getName() != null ? formTemplate.getName() : " ",
            formTemplate.getFullName() != null ? formTemplate.getFullName() : " ",
			formTemplate.getCode(),
            formTemplate.getScript() != null ? formTemplate.getScript() : " ",
            formTemplate.getStatus().getId(),
			formTemplateId
		);
		formStyleDao.saveFormStyles(formTemplate);
		columnDao.saveFormColumns(formTemplate);
		return formTemplateId;
	}

	@Override
	public List<FormTemplate> listAll() {
		return getJdbcTemplate().query("select id, version, name, fullname, type_id, edition, fixed_rows, code, status" +
                " from form_template where status = 0", new FormTemplateMapper(false));
	}

	@Override
	public int getActiveFormTemplateId(int formTypeId) {
		JdbcTemplate jt = getJdbcTemplate();
		FormTemplate form;
		try {
			form =jt.queryForObject(
                    "select * from form_template where type_id = ? and status = ?",
                    new Object[]{formTypeId, 0},
                    new int[]{Types.NUMERIC, Types.NUMERIC},
                    new FormTemplateMapper(false)
            );
			return form.getId();
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Для данного вида налоговой формы %d не найдено активного шаблона налоговой формы.",formTypeId);
		}catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Для даного вида налоговой формы %d найдено несколько активных шаблонов налоговой формы.",formTypeId);
		}
	}


    @Cacheable(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplateId + new String(\"_script\")")
    @Override
    public String getFormTemplateScript(int formTemplateId) {
        return getJdbcTemplate().queryForObject("select script from form_template where id = ?",
                new Object[]{formTemplateId},
                new int[]{Types.INTEGER},
                String.class);
    }

    @Override
    public List<DataRow<Cell>> getDataCells(FormTemplate formTemplate) {
        String dataRowXml = getJdbcTemplate().queryForObject("select data_rows from form_template where id = ?",
                new Object[]{formTemplate.getId()},
                new int[]{Types.INTEGER},
                String.class);
        return dataRowXml != null ? xmlSerializationUtils.deserialize(dataRowXml, formTemplate.getColumns(), formTemplate.getStyles(), Cell.class):
                new ArrayList<DataRow<Cell>>();
    }

    @Override
    public List<DataRow<HeaderCell>> getHeaderCells(FormTemplate formTemplate) {
        String headerDataXml = getJdbcTemplate().queryForObject("select data_headers from form_template where id = ?",
                new Object[]{formTemplate.getId()},
                new int[]{Types.INTEGER},
                String.class);
        return headerDataXml != null ? xmlSerializationUtils.deserialize(headerDataXml, formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class):
                new ArrayList<DataRow<HeaderCell>>();
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
    public List<Integer> getFormTemplateVersions(int formTypeId, int formTemplateId, List<Integer> statusList, Date actualBeginVersion, Date actualEndVersion) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("typeId", formTypeId);
        valueMap.put("statusList", statusList);
        valueMap.put("actualStartVersion", actualBeginVersion);
        valueMap.put("actualEndVersion", actualEndVersion);
        valueMap.put("formTemplateId", formTemplateId);

        StringBuilder builder = new StringBuilder("select id");
        builder.append(" from form_template where type_id = :typeId");
        if (!statusList.isEmpty())
            builder.append(" and status in (:statusList)");

        if (actualBeginVersion != null && actualEndVersion != null)
            builder.append(" and version between :actualStartVersion and :actualEndVersion");
        else if (actualBeginVersion != null)
            builder.append(" and version >= :actualStartVersion");

        if (formTemplateId != 0)
            builder.append(" and id <> :formTemplateId");


        builder.append(" order by version, edition");
        try {
            return getNamedParameterJdbcTemplate().queryForList(builder.toString(), valueMap, Integer.class);
        } catch (DataAccessException e){
            throw new DaoException("Ошибка при получении списка версий макетов.", e);
        }
    }

    @Override
    public int getNearestFTVersionIdRight(int formTypeId, List<Integer> statusList, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("typeId", formTypeId);
            valueMap.put("statusList", statusList);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            StringBuilder builder = new StringBuilder("select * from (select id");
            builder.append(" from form_template where type_id = :typeId");
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
    public int getNearestFTVersionIdLeft(int formTypeId, List<Integer> statusList, Date actualBeginVersion) {
        try {
            if (actualBeginVersion == null)
                throw new DataRetrievalFailureException("Дата начала актуализации версии не должна быть null");

            Map<String, Object> valueMap =  new HashMap<String, Object>();
            valueMap.put("typeId", formTypeId);
            valueMap.put("statusList", statusList);
            valueMap.put("actualBeginVersion", actualBeginVersion);

            StringBuilder builder = new StringBuilder("select * from (select id");
            builder.append(" from form_template where type_id = :typeId");
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
            formStyleDao.saveFormStyles(formTemplate);
            columnDao.saveFormColumns(formTemplate);
            getJdbcTemplate().
                    update("insert into form_template (id, data_rows, data_headers, edition, version, fixed_rows, name, fullname, code, script, status, type_id) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?)",
                            formTemplateId,
                            dataRowsXml,
                            dataHeadersXml,
                            formTemplate.getEdition(),
                            formTemplate.getVersion(),
                            formTemplate.isFixedRows(),
                            formTemplate.getName() != null ? formTemplate.getName() : " ",
                            formTemplate.getFullName() != null ? formTemplate.getFullName() : " ",
                            formTemplate.getCode() != null?formTemplate.getCode() : "",
                            formTemplate.getScript() != null ? formTemplate.getScript() : " ",
                            formTemplate.getStatus().getId(),
                            formTemplate.getType().getId()
                            );

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
}