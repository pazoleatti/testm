package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.cache.annotation.Cacheable;
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
            result.setFixedRows(rs.getBoolean("fixed_rows"));
			return result;
		}
	}

	@Cacheable("FormType")
	public FormType getType(int typeId) {
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

    public List<FormType> listFormTypes(){
        return getJdbcTemplate().query("select * from form_type", new FormTypeMapper());
    }

	public List<FormType> listAllByTaxType(TaxType taxType){
			return getJdbcTemplate().query(
					"select * from form_type ft where ft.tax_type = ?",
					new Object[]{String.valueOf(taxType.getCode())},
					new int[]{Types.CHAR},
					new FormTypeMapper()
			);
	}

    public List<FormType> listAllByDepartmentIdAndTaxType(int departmentId, TaxType taxType){
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching FormTypes by DepartmentId = " + departmentId + " and TaxType  = " + taxType);
        }
        return getJdbcTemplate().query(
                "SELECT * FROM form_type ft WHERE ft.tax_type = ? " +
                        "AND EXISTS (SELECT 1 FROM department_form_type dft WHERE ft.id = dft.form_type_id and dft.department_id = ?)",
                new Object[]{
                        String.valueOf(taxType.getCode()),
                        departmentId
                },
                new int[]{
                        Types.CHAR,
                        Types.INTEGER
                },
                new FormTypeMapper()
            );
    }
}
