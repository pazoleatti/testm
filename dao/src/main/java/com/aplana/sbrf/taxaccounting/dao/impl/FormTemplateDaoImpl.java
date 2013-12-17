package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
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
			formTemplate.setActive(rs.getBoolean("is_active"));
			formTemplate.setVersion(rs.getString("version"));
			formTemplate.setName(rs.getString("name"));
			formTemplate.setFullName(rs.getString("fullname"));
            formTemplate.setType(formTypeDao.get(rs.getInt("type_id")));
            formTemplate.setEdition(rs.getInt("edition"));
            formTemplate.setNumberedColumns(rs.getBoolean("numbered_columns"));
            formTemplate.setFixedRows(rs.getBoolean("fixed_rows"));
            formTemplate.setCode(rs.getString("code"));
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
					"select id, is_active, version, name, fullname, type_id, edition, numbered_columns, fixed_rows, code, script, data_rows, data_headers " +
                            "from form_template where id = ?",
					new Object[]{formId},
					new int[]{Types.NUMERIC},
					new FormTemplateMapper(true)
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти описание налоговой формы с id = " + formId);
		}
	}

	/**
	 * Кэш инфалидируется перед вызовом. Т.е. несмотря на результат выполнения, кэш будет сброшен.
	 * Иначе, если версии не совпадают кэш продолжает возвращать старую версию.
	 */
	@Transactional(readOnly = false)
    @Caching(evict = {@CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id + new String(\"_script\")", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id + new String(\"_data_rows\")", beforeInvocation = true),
            @CacheEvict(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id + new String(\"_data_headers\")", beforeInvocation = true)})
	@Override
	public int save(final FormTemplate formTemplate) {
		final Integer formTemplateId = formTemplate.getId();

		if (formTemplateId == null) {
			throw new UnsupportedOperationException("Saving of new FormTemplate is not implemented");
		}

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
			"update form_template set data_rows = ?, data_headers = ?, edition = ?, numbered_columns = ?, is_active = ?, version = ?, fixed_rows = ?, name = ?, " +
			"fullname = ?, code = ?, script=? where id = ?",
			dataRowsXml,
			dataHeadersXml,
			storedEdition + 1,
			formTemplate.isNumberedColumns(),
			formTemplate.isActive(),
			formTemplate.getVersion(),
			formTemplate.isFixedRows(),
			formTemplate.getName(),
			formTemplate.getFullName(),
			formTemplate.getCode(),
			formTemplate.getScript(),
			formTemplateId
		);
		formStyleDao.saveFormStyles(formTemplate);
		columnDao.saveFormColumns(formTemplate);
		return formTemplateId;
	}

	@Override
	public List<FormTemplate> listAll() {
		return getJdbcTemplate().query("select id, is_active, version, name, fullname, type_id, edition, numbered_columns, fixed_rows, code" +
                " from form_template", new FormTemplateMapper(false));
	}

	@Override
	public int getActiveFormTemplateId(int formTypeId) {
		JdbcTemplate jt = getJdbcTemplate();
		FormTemplate form;
		try {
			form =jt.queryForObject(
                    "select * from form_template where type_id = ? and is_active = ?",
                    new Object[]{formTypeId, 1},
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

    @Cacheable(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id + new String(\"_data_rows\")")
    @Override
    public List<DataRow<Cell>> getDataCells(FormTemplate formTemplate) {
        String dataRowXml = getJdbcTemplate().queryForObject("select data_rows from form_template where id = ?",
                new Object[]{formTemplate.getId()},
                new int[]{Types.INTEGER},
                String.class);
        return dataRowXml != null ? xmlSerializationUtils.deserialize(dataRowXml, formTemplate.getColumns(), formTemplate.getStyles(), Cell.class):
                new ArrayList<DataRow<Cell>>();
    }

    @Cacheable(value = CacheConstants.FORM_TEMPLATE, key = "#formTemplate.id + new String(\"_data_headers\")")
    @Override
    public List<DataRow<HeaderCell>> getHeaderCells(FormTemplate formTemplate) {
        String headerDataXml = getJdbcTemplate().queryForObject("select data_headers from form_template where id = ?",
                new Object[]{formTemplate.getId()},
                new int[]{Types.INTEGER},
                String.class);
        return headerDataXml != null ? xmlSerializationUtils.deserialize(headerDataXml, formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class):
                new ArrayList<DataRow<HeaderCell>>();
    }
}