package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormType;
import org.springframework.cache.annotation.Cacheable;
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
			return result;
		}
	}

	@Cacheable("FormType")
	public FormType getType(int typeId) {
		logger.info("Fetching FormType with id = " + typeId);
		return getJdbcTemplate().queryForObject(
			"select * from form_type where id = ?",
			new Object[] { typeId },
			new int[] { Types.NUMERIC },
			new FormTypeMapper()
		);
	}

    public List<FormType> listFormTypes(){
        return getJdbcTemplate().query("select * from form_type", new FormTypeMapper());
    }
}
