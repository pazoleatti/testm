package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.DeclarationSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformTaxTypeToSqlInStatement;
import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * Реализация Dao для работы с декларациями
 * @author dsultanbekov
 */
@Repository
@Transactional
public class DeclarationDaoImpl extends AbstractDao implements DeclarationDao {
	
	private static final class DeclarationRowMapper implements RowMapper<Declaration> {
		@Override
		public Declaration mapRow(ResultSet rs, int index) throws SQLException {
			Declaration d = new Declaration();
			d.setId(rs.getLong("id"));
			d.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
			d.setDepartmentId(rs.getInt("department_id"));			
			d.setReportPeriodId(rs.getInt("report_period_id"));			
			d.setAccepted(rs.getBoolean("is_accepted"));
			return d;
		}
	}
	
	@Override
	public Declaration get(long declarationId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from declaration where id = ?",
				new Object[] { declarationId },
				new DeclarationRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Декларация с id = %d не найдена в БД", declarationId);
		}
	}

	@Override
	public String getXmlData(long declarationId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select data from declaration where id = ?",
				new Object[] { declarationId },
				String.class
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Декларация с id = %d не найдена в БД", declarationId);
		}
	}

	@Override
	public void setXmlData(long declarationId, String xmlData) {
		int count = getJdbcTemplate().update(
			"update declaration set data = ? where id = ?",
			new Object[] {
				xmlData,
				declarationId
			},
			new int[] {
				Types.VARCHAR,
				Types.NUMERIC
			}
		);
		if (count == 0) {
			throw new DaoException("Не удалось сохранить данные в формате законодателя для декларации с id = %d, так как она не существует.");
		}
	}

	@Override
	public void delete(long declarationId) {
		int count = getJdbcTemplate().update(
			"delete from declaration where id = ?",
			declarationId
		);
		if (count == 0) {
			throw new DaoException("Не удалось удалить декларацию с id = %d, так как она не существует", declarationId);
		}
	}

	@Override
	public PaginatedSearchResult<DeclarationSearchResultItem> findPage(DeclarationFilter declarationFilter, DeclarationSearchOrdering ordering,
	                                                 boolean ascSorting,PaginatedSearchParams pageParams) {
		StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
		appendSelectClause(sql);
		appendFromAndWhereClause(sql, declarationFilter);
		appendOrderByClause(sql, ordering, ascSorting);
		sql.append(") dat) ordDat where ordDat.rn between ? and ?")
				.append(" order by ordDat.rn");
		List<DeclarationSearchResultItem> records = getJdbcTemplate().query(
				sql.toString(),
				new Object[] {
						pageParams.getStartIndex() + 1,	// В java нумерация с 0, в БД row_number() нумерует с 1
						pageParams.getStartIndex() + pageParams.getCount()
				},
				new int[] {
						Types.NUMERIC,
						Types.NUMERIC
				},
				new DeclarationSearchResultItemMapper()
		);
		long count = getCount(declarationFilter);
		PaginatedSearchResult<DeclarationSearchResultItem>  result = new PaginatedSearchResult<DeclarationSearchResultItem>();
		result.setRecords(records);
		result.setTotalRecordCount(count);
		return result;
	}

	@Override
	public long saveNew(Declaration declaration) {
		JdbcTemplate jt = getJdbcTemplate();
		
		Long id = declaration.getId();
		if (id != null) {
			throw new DaoException("Произведена попытка перезаписать уже сохранённую декларацию");
		}
		id = generateId("seq_declaration", Long.class);
		jt.update(
			"insert into declaration (id, declaration_template_id, report_period_id, department_id, is_accepted) values (?, ?, ?, ?, ?)",
			id,
			declaration.getDeclarationTemplateId(),
			declaration.getReportPeriodId(),
			declaration.getDepartmentId(),
			declaration.isAccepted() ? 1 : 0
		);
		declaration.setId(id);
		return id.longValue();
	}

	@Override
	public void setAccepted(long declarationId, boolean accepted) {
		int count = getJdbcTemplate().update(
			"update declaration set is_accepted = ? where id = ?",
			accepted,
			declarationId
		);
		if (count == 0) {
			throw new DaoException("Не удалось изменить статус декларации с id = %d, так как она не существует.", declarationId);
		}		
	}

	private long getCount(DeclarationFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*)");
		appendFromAndWhereClause(sql, filter);
		return getJdbcTemplate().queryForLong(sql.toString());
	}

	private void appendFromAndWhereClause(StringBuilder sql, DeclarationFilter filter) {
		sql.append(" FROM declaration dec, declaration_type dectype, department dp, report_period rp")
				.append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
				.append(" AND dp.id = dec.department_id AND rp.id = dec.report_period_id");

		if (filter.getTaxTypes() != null && !filter.getTaxTypes().isEmpty()) {
			sql.append(" AND dectype.tax_type in ").append(transformTaxTypeToSqlInStatement(filter.getTaxTypes()));
		}

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
			sql.append(" AND rp.id in ").append(transformToSqlInStatement(filter.getReportPeriodIds()));
		}

		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql.append(" AND dec.department_id in ").append(transformToSqlInStatement(filter.getDepartmentIds()));
		}
	}

	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT dec.ID as declaration_id, dec.declaration_template_id, dec.is_accepted,")
				.append(" dectype.ID as declaration_type_id, dectype.NAME as declaration_type_name,")
				.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
				.append(" rp.ID as report_period_id, rp.NAME as report_period_name, dectype.TAX_TYPE");
	}

	public void appendOrderByClause(StringBuilder sql, DeclarationSearchOrdering ordering, boolean ascSorting) {
		sql.append(" order by ");

		String column = null;
		switch (ordering) {
			case ID:
				// Сортировка по ID делается всегда, поэтому здесь оставляем null
				break;
			case DEPARTMENT_NAME:
				column = "dp.name";
				break;
			case REPORT_PERIOD_NAME:
				column = "rp.name";
				break;
		}

		if (column != null) {
			sql.append(column);
			if (!ascSorting) {
				sql.append(" desc");
			}
			sql.append(", ");
		}

		sql.append("dec.id");
		if (!ascSorting) {
			sql.append(" desc");
		}
	}
}
