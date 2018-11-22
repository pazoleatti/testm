package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
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
	
	private static class DeclarationTypeRowMapper implements RowMapper<DeclarationType> {
		@Override
		public DeclarationType mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationType res = new DeclarationType();
			res.setId(SqlUtils.getInteger(rs, "id"));
			res.setName(rs.getString("name"));
            res.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"status")));
			return res;
		}
	}

    private static class DeclarationTypeWithVersionsCountRowMapper extends DeclarationTypeRowMapper {
        @Override
        public DeclarationType mapRow(ResultSet rs, int index) throws SQLException {
            DeclarationType res = super.mapRow(rs, index);
            res.setVersionsCount(SqlUtils.getInteger(rs, "versions_count"));
            return res;
        }
    }
	
	@Override
	@Cacheable(CacheConstants.DECLARATION_TYPE)
	public DeclarationType get(int declarationTypeId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from declaration_type where id = ?",
				new Object[] { declarationTypeId },
				new int[] { Types.NUMERIC },
				new DeclarationTypeRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти вид налоговой формы с id = %d", declarationTypeId);
		}
	}

    @Override
    public DeclarationType getTypeByTemplateId(int declarationTemplateId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select p.* from declaration_type p " +
                    " inner join declaration_template m on p.id = m.declaration_type_id " +
                    " where m.id = ?",
                    new Object[] { declarationTemplateId },
                    new int[] { Types.NUMERIC },
                    new DeclarationTypeRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти вид декларации с id = %d", declarationTemplateId);
        }
    }

	@Override
	public List<DeclarationType> fetchAll(){
		return getJdbcTemplate().query(
		        "SELECT dt.*," +
                        "(select count(*) from declaration_template where declaration_type_id = dt.id and status in (0,1)) AS versions_count " +
                        " FROM declaration_type dt where status = 0 " +
                        " order by dt.name",
                new DeclarationTypeWithVersionsCountRowMapper());
	}

	@Override
	public List<DeclarationType> listAllByTaxType(TaxType taxType){
		return getJdbcTemplate().query(
				"SELECT * FROM declaration_type dt WHERE status = ?",
				new Object[]{0},
				new int[]{Types.INTEGER},
				new DeclarationTypeRowMapper()
		);
	}

    @Override
    public int save(DeclarationType type) {
        try {

            int typeId = generateId("seq_declaration_type", Integer.class);
            getJdbcTemplate().update("insert into declaration_type (id, name, status) values (?,?,?)",
                    new Object[]{typeId,
                            type.getName(),
                            type.getStatus().getId()},
                    new int[]{Types.NUMERIC,  Types.VARCHAR, Types.NUMERIC});
            return typeId;
        } catch (DataAccessException e){
			LOG.error("Ошибка при создании макета", e);
            throw new DaoException("Ошибка при создании макета. %s", e.getMessage());
        }
    }

    @CacheEvict(value = CacheConstants.DECLARATION_TYPE, beforeInvocation = true ,key = "#type.id")
    @Override
    public void updateDT(DeclarationType type) {
        try {
            getJdbcTemplate().update(
                    "update declaration_type set name = ? where id = ?",
                    type.getName(), type.getId());
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Integer> getByFilter(TemplateFilter filter) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("select id from declaration_type where status = 0");
        if (!filter.getSearchText().isEmpty()) {
            ps.appendQuery(" and LOWER(name) like LOWER(?)");
            ps.addParam("%"+filter.getSearchText().toLowerCase()+"%");
        }
        return getJdbcTemplate().queryForList(ps.getQuery().toString(),
                ps.getParams().toArray(),
                Integer.class);
    }

    public List<DeclarationType> getTypes(int departmentId, ReportPeriod reportPeriod, TaxType taxType, List<DeclarationFormKind> declarationFormKinds) {
        return getJdbcTemplate().query(
                "with templatesByVersion as (select id, declaration_type_id, status, version, form_kind, row_number() " +
                        (isSupportOver() ? "over(partition by declaration_type_id order by version)" : "over()") +
                        " rn from declaration_template where status != -1)," +
                        " allTemplates as (select tv.id," +
                        " tv.declaration_type_id," +
                        " tv.VERSION versionFrom," +
                        " tv2.version - interval '1' day as versionTo, tv.status" +
                        " from templatesByVersion tv left outer join templatesByVersion tv2 on tv.declaration_type_id = tv2.declaration_type_id and tv.rn+1 = tv2.rn" +
                        " where tv.form_kind in " + SqlUtils.transformDeclarationFormKindsToSqlInStatement(declarationFormKinds) +")"+
                        " select distinct t.* from declaration_type t" +
                        " join department_declaration_type ddt on t.id = ddt.declaration_type_id" +
                        " join allTemplates all_t on ddt.declaration_type_id = all_t.declaration_type_id" +
                        " where ddt.department_id=? and all_t.status = 0 and ((all_t.versionFrom <= ? and all_t.versionTo >= ?) or (all_t.versionFrom <= ? and all_t.versionTo is null))",
                new Object[]{departmentId, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate(), reportPeriod.getCalendarStartDate()},
                new int[]{Types.NUMERIC, Types.DATE, Types.DATE, Types.DATE},
                new DeclarationTypeRowMapper()
        );
    }
}