package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
@Transactional(readOnly=true)
public class FormTypeDaoImpl extends AbstractDao implements FormTypeDao {
	
	private static final class FormTypeMapper implements RowMapper<FormType> {
		public FormType mapRow(ResultSet rs, int index) throws SQLException {
			FormType result = new FormType();
			result.setId(rs.getInt("id"));
			result.setName(rs.getString("name"));
			String taxCode = rs.getString("tax_type");
			result.setTaxType(TaxType.fromCode(taxCode.charAt(0)));
            result.setStatus(VersionedObjectStatus.getStatusById(rs.getInt("status")));
			return result;
		}
	}

	@Override
	@Cacheable("FormType")
	public FormType get(int typeId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching FormType with id = " + typeId);	
		}		
		try {
			return getJdbcTemplate().queryForObject(
				"select * from form_type where id = ?",
				new Object[] { typeId },
				new int[] { Types.NUMERIC },
				new FormTypeMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Вид формы с id = " + typeId + " не найден в БД");
		}
	}

	@Override
    public List<Integer> getAll(){
        return getJdbcTemplate().queryForList("select id from form_type where status = 0", Integer.class);
    }

    @Override
	public List<FormType> getByTaxType(TaxType taxType){
		return getJdbcTemplate().query(
			"select * from form_type ft where ft.tax_type = ?",
			new Object[]{String.valueOf(taxType.getCode())},
			new int[]{Types.CHAR},
			new FormTypeMapper()
		);
	}

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        try {
            StringBuilder query = new StringBuilder("select id from form_type where status = ");
            query.append(filter.isActive()?0:1);
            if (filter.getTaxType() != null) {
                query.append(" and form_type.TAX_TYPE = \'").append(filter.getTaxType().getCode()).append("\'");
            }
            return getJdbcTemplate().queryForList(query.toString(), Integer.class);
        } catch (DataAccessException e){
            logger.error("Ошибка при получении данных НФ по фильтру", e);
            throw new DaoException("Ошибка при получении данных НФ по фильтру", e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int save(FormType formType) {
        try {

            int formTypeId = generateId("seq_form_template", Integer.class);
            getJdbcTemplate().update("insert into form_type (id, name, tax_type, status) values (?,?,?,?)",
                    new Object[]{formTypeId,
                    formType.getName(),
                    formType.getTaxType().getCode(),
                    formType.getStatus().getId()},
                    new int[]{Types.NUMERIC,  Types.VARCHAR, Types.VARCHAR, Types.NUMERIC});
            return formTypeId;
        } catch (DataAccessException e){
            logger.error("Ошибка при создании макета", e);
            throw new DaoException("Ошибка при создании макета", e.getMessage());
        }
    }

    @Override
    public void delete(int formTypeId) {
        try {
            getJdbcTemplate().update("update form_type set status = -1 where id = ?",
                    new Object[]{formTypeId},
                    new int[]{Types.INTEGER});
        } catch (DataAccessException e){
            logger.error("Ошибка при удалении макета", e);
            throw new DaoException("Ошибка при удалении макета", e);
        }
    }
}
