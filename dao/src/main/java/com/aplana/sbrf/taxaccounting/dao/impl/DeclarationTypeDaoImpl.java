package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
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

	private static final Log LOG = LogFactory.getLog(DeclarationTypeDaoImpl.class);
	
	private static final class DeclarationTypeRowMapper implements RowMapper<DeclarationType> {
		@Override
		public DeclarationType mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationType res = new DeclarationType();
			res.setId(SqlUtils.getInteger(rs, "id"));
			res.setName(rs.getString("name"));
			res.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            res.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"status")));
            res.setIsIfrs(rs.getBoolean("is_ifrs"));
            res.setIfrsName(rs.getString("ifrs_name"));
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
				"SELECT * FROM declaration_type dt WHERE dt.tax_type = ? AND status = ?",
				new Object[]{String.valueOf(taxType.getCode()), 0},
				new int[]{Types.VARCHAR, Types.INTEGER},
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
			LOG.error("Ошибка при создании макета", e);
            throw new DaoException("Ошибка при создании макета. %s", e.getMessage());
        }
    }

    @CacheEvict(value = "DeclarationType", beforeInvocation = true ,key = "#type.id")
    @Override
    public void updateDT(DeclarationType type) {
        try {
            getJdbcTemplate().update(
                    "update declaration_type set name = ?, is_ifrs = ?, ifrs_name = ? where id = ?",
                    type.getName(), type.getIsIfrs(), type.getIfrsName(), type.getId());
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @CacheEvict("DeclarationType")
    @Override
    public void delete(int typeId) {
        try {
            getJdbcTemplate().update("delete from declaration_type where id = ?",
                    new Object[]{typeId},
                    new int[]{Types.INTEGER});
        } catch (DataAccessException e){
			LOG.error("Ошибка при удалении макета", e);
            throw new DaoException("Ошибка при удалении макета", e);
        }
    }

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("select id from declaration_type where status = 0");
        if (filter.getTaxType() != null) {
            ps.appendQuery(" and TAX_TYPE = ?");
            ps.addParam(filter.getTaxType().getCode());
        }
        if (!filter.getSearchText().isEmpty()) {
            ps.appendQuery(" and LOWER(name) like LOWER(?)");
            ps.addParam("%"+filter.getSearchText().toLowerCase()+"%");
        }
        return getJdbcTemplate().queryForList(ps.getQuery().toString(),
                ps.getParams().toArray(),
                Integer.class);
    }

	@Override
	public List<DeclarationType> getTypes(int departmentId, ReportPeriod reportPeriod, TaxType taxType) {
		return getJdbcTemplate().query(
				"with templatesByVersion as (select id, declaration_type_id, status, version, row_number() " +
                        (isSupportOver() ? "over(partition by declaration_type_id order by version)" : "over()") +
                        " rn from declaration_template where status != -1), " +
						"allTemplates as (select tv.id, " +
						"tv.declaration_type_id, " +
						"tv.VERSION versionFrom,  " +
						" tv2.version - interval '1' day as versionTo, tv.status" +
						" from templatesByVersion tv left outer join templatesByVersion tv2 on tv.declaration_type_id = tv2.declaration_type_id and tv.rn+1 = tv2.rn)" +
						" select distinct t.* from declaration_type t" +
						" join department_declaration_type ddt on t.id = ddt.declaration_type_id" +
						" join allTemplates all_t on ddt.declaration_type_id = all_t.declaration_type_id" +
						" where ddt.department_id=? and t.tax_type=? and all_t.status = 0 and ((all_t.versionFrom <= ? and all_t.versionTo >= ?) or (all_t.versionFrom <= ? and all_t.versionTo is null))",
				new Object[]{departmentId, String.valueOf(taxType.getCode()), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate(), reportPeriod.getCalendarStartDate()},
				new int[]{Types.NUMERIC, Types.CHAR, Types.DATE, Types.DATE, Types.DATE},
				new DeclarationTypeRowMapper()
		);
	}

    @Override
    public List<Integer> getIfrsDeclarationTypes() {
        return getJdbcTemplate().queryForList("SELECT id FROM declaration_type where status = 0 and is_ifrs = 1", Integer.class);
    }
}