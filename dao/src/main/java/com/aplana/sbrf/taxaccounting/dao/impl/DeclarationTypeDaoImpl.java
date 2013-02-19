package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Реализация DAO для работы с декларациями
 * @author dsultanbekov
 */
@Repository
public class DeclarationTypeDaoImpl extends AbstractDao implements DeclarationTypeDao {
	
	private static final class DeclarationTypeRowMapper implements RowMapper<DeclarationType> {
		@Override
		public DeclarationType mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationType res = new DeclarationType();
			res.setId(rs.getInt("id"));
			res.setName(rs.getString("name"));
			res.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
			return res;
		}
		
	}
	
	@Override
	@Cacheable("DeclarationType")
	public DeclarationType get(int declarationTypeId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from declaration_type where id = ?",
				new Object[] { declarationTypeId },
				new int[] { Types.NUMERIC },
				new DeclarationTypeRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти вид декларации с id = %d", declarationTypeId);
		}
	}

	@Override
	public List<DeclarationType> listAll(){
		return getJdbcTemplate().query("SELECT * FROM declaration_type", new DeclarationTypeRowMapper());
	}
}
