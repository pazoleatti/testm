package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

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

	@Override
	public List<DeclarationType> listAllByTaxType(TaxType taxType){
		return getJdbcTemplate().query(
				"select * from declaration_type dt where dt.tax_type = ?",
				new Object[]{String.valueOf(taxType.getCode())},
				new int[]{Types.VARCHAR},
				new DeclarationTypeRowMapper()
		);
	}

    @Override
    public int save(DeclarationType type) {
        try {

            int typeId = generateId("seq_declaration_type", Integer.class);
            getJdbcTemplate().update("insert into declaration_type (id, name, tax_type, status) values (?,?,?,?)",
                    new Object[]{typeId,
                            type.getName(),
                            type.getTaxType().getCode(),
                            type.getStatus().getId()},
                    new int[]{Types.NUMERIC,  Types.VARCHAR, Types.VARCHAR, Types.NUMERIC});
            return typeId;
        } catch (DataAccessException e){
            logger.error("Ошибка при создании макета", e);
            throw new DaoException("Ошибка при создании макета", e.getMessage());
        }
    }

    @Override
    public void delete(int typeId) {
        try {
            getJdbcTemplate().update("update declaration_type set status = ? where id = ?",
                    new Object[]{VersionedObjectStatus.DELETED.getId(), typeId},
                    new int[]{Types.INTEGER,Types.INTEGER});
        } catch (DataAccessException e){
            logger.error("Ошибка при удалении макета", e);
            throw new DaoException("Ошибка при удалении макета", e);
        }
    }
}
