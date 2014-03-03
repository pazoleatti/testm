package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
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
            res.setStatus(VersionedObjectStatus.getStatusById(rs.getInt("status")));
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
		return getJdbcTemplate().query("SELECT * FROM declaration_type where status = 0", new DeclarationTypeRowMapper());
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
            getJdbcTemplate().update("update declaration_type set status = -1 where id = ?",
                    new Object[]{typeId},
                    new int[]{Types.INTEGER});
        } catch (DataAccessException e){
            logger.error("Ошибка при удалении макета", e);
            throw new DaoException("Ошибка при удалении макета", e);
        }
    }

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        StringBuilder query = new StringBuilder("select id from declaration_type where status = ");
        query.append(filter.isActive()?0:1);
        if (filter.getTaxType() != null) {
            query.append(" and TAX_TYPE = \'").append(filter.getTaxType().getCode()).append("\'");
        }
        return getJdbcTemplate().queryForList(query.toString(), Integer.class);
    }

	@Override
	public List<DeclarationType> getTypes(int departmentId, ReportPeriod reportPeriod, TaxType taxType) {
		return getJdbcTemplate().query(
				"with templatesByVersion as (select id, declaration_type_id, status, is_active, version, row_number() over(partition by declaration_type_id order by version) rn from declaration_template), " +
						"allTemplates as (select tv.id, " +
						"tv.declaration_type_id, " +
						"tv.is_active," +
						"tv.VERSION versionFrom, " +
						"case" +
						" when tv2.status=2 then tv2.version" +
						" when (tv2.status=0 or tv2.status=1) then tv2.version - interval '1' day" +
						" end as versionTo" +
						" from templatesByVersion tv left outer join templatesByVersion tv2 on tv.declaration_type_id = tv2.declaration_type_id and tv.rn+1 = tv2.rn)" +
						" select distinct t.* from declaration_type t" +
						" join department_declaration_type ddt on t.id = ddt.declaration_type_id" +
						" join allTemplates at on ddt.declaration_type_id = at.declaration_type_id" +
						" where ddt.department_id=? and t.tax_type=? and at.is_active=1 and ((at.versionFrom <= ? and at.versionTo >= ?) or (at.versionFrom <= ? and at.versionTo is null))",
				new Object[]{departmentId, String.valueOf(taxType.getCode()), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate(), reportPeriod.getCalendarStartDate()},
				new int[]{Types.NUMERIC, Types.CHAR, Types.DATE, Types.DATE, Types.DATE},
				new DeclarationTypeRowMapper()
		);
	}
}
