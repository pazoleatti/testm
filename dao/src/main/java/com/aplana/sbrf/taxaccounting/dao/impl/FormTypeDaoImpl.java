package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.cache.annotation.CacheEvict;
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
			result.setId(SqlUtils.getInteger(rs,"id"));
			result.setName(rs.getString("name"));
			String taxCode = rs.getString("tax_type");
			result.setTaxType(TaxType.fromCode(taxCode.charAt(0)));
            result.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"status")));
            result.setCode(rs.getString("code"));
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
				"select id, name, tax_type, status, code from form_type where id = ?",
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
			"select id, name, tax_type, status, code from form_type ft where ft.tax_type = ?",
			new Object[]{String.valueOf(taxType.getCode())},
			new int[]{Types.CHAR},
			new FormTypeMapper()
		);
	}

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        try {
            StringBuilder query = new StringBuilder("select id from form_type where status = 0");
            if (filter.getTaxType() != null) {
                query.append(" and tax_type = \'").append(filter.getTaxType().getCode()).append("\'");
            }
            if (!filter.getSearchText().isEmpty()) {
                query.append(" and LOWER(name) like \'%").append(filter.getSearchText().toLowerCase()).append("%\'");
            }
            return getJdbcTemplate().queryForList(query.toString(), Integer.class);
        } catch (DataAccessException e) {
            logger.error("Ошибка при получении данных НФ по фильтру", e);
            throw new DaoException("Ошибка при получении данных НФ по фильтру", e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int save(FormType formType) {
        try {

            int formTypeId = generateId("seq_form_type", Integer.class);
            getJdbcTemplate().update("insert into form_type (id, name, tax_type, status, code) values (?,?,?,?,?)",
                    new Object[]{formTypeId,
                    formType.getName(),
                    formType.getTaxType().getCode(),
                    formType.getStatus().getId(),
                    formType.getCode()},
                    new int[]{Types.NUMERIC,  Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR});
            return formTypeId;
        } catch (DataAccessException e){
            logger.error("Ошибка при создании макета", e);
            throw new DaoException("Ошибка при создании макета", e.getMessage());
        }
    }

    @CacheEvict(value = "FormType", beforeInvocation = true ,key = "#formTypeId")
    @Override
    public void updateFormTypeName(int formTypeId, String newName) {
        getJdbcTemplate().update(
                "update form_type set name = ? where id = ?",
                new Object[]{
                        newName,
                        formTypeId
                }
        );
    }

    @CacheEvict("FormType")
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

	@Override
	public List<FormType> getFormTypes(int departmentId, ReportPeriod reportPeriod, TaxType taxType, List<FormDataKind> kind) {
		return getJdbcTemplate().query(
			"with templatesByVersion as (select id, type_id, status, version, row_number() over(partition by type_id order by version) rn from FORM_TEMPLATE where status != -1)," +
					"      allTemplates as (select tv.id," +
					"                         tv.type_id," +
					"                         tv.VERSION versionFrom," +
					"                         tv2.version - interval '1' day as versionTo, tv.status as STATUS " +
					"                       from templatesByVersion tv left outer join templatesByVersion tv2 on tv.type_id = tv2.type_id and tv.rn+1 = tv2.rn)" +
					"  select distinct t.* from form_type t" +
					"    join department_form_type dft on t.id=dft.form_type_id" +
					"    join allTemplates ft on dft.form_type_id=ft.type_id" +
					"  where dft.kind in " + SqlUtils.transformFormKindsToSqlInStatement(kind) +
					"  and dft.department_id=? and t.tax_type=? and ft.status = 0 and ((ft.versionFrom <= ? and ft.versionTo >= ?) or (ft.versionFrom <= ? and ft.versionTo is null))",
			new Object[]{departmentId, String.valueOf(taxType.getCode()), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate(), reportPeriod.getCalendarStartDate()},
			new int[]{Types.NUMERIC, Types.CHAR, Types.DATE, Types.DATE, Types.DATE},
			new FormTypeMapper()
		);
    }

    @Override
    public FormType getByCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return getJdbcTemplate().queryForObject(
                    "select id, name, tax_type, status, code from form_type where code = ?",
                    new Object[]{code},
                    new int[]{Types.VARCHAR},
                    new FormTypeMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
