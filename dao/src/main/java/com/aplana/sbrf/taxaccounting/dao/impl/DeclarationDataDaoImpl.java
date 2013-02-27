package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.DeclarationDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * Реализация Dao для работы с декларациями
 * @author dsultanbekov
 */
@Repository
@Transactional
public class DeclarationDataDaoImpl extends AbstractDao implements DeclarationDataDao {
	
	private static final class DeclarationDataRowMapper implements RowMapper<DeclarationData> {
		@Override
		public DeclarationData mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationData d = new DeclarationData();
			d.setId(rs.getLong("id"));
			d.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
			d.setDepartmentId(rs.getInt("department_id"));			
			d.setReportPeriodId(rs.getInt("report_period_id"));			
			d.setAccepted(rs.getBoolean("is_accepted"));
			return d;
		}
	}
	
	@Override
	public DeclarationData get(long declarationDataId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from declaration where id = ?",
				new Object[] { declarationDataId },
				new DeclarationDataRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Декларация с id = %d не найдена в БД", declarationDataId);
		}
	}

	@Override
	public String getXmlData(long declarationDataId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select data from declaration where id = ?",
				new Object[] { declarationDataId },
				String.class
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Декларация с id = %d не найдена в БД", declarationDataId);
		}
	}

	@Override
	public void setXmlData(long declarationDataId, String xmlData) {
		int count = getJdbcTemplate().update(
			"update declaration set data = ? where id = ?",
			new Object[] {
				xmlData,
				declarationDataId
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
	public void delete(long declarationDataId) {
		int count = getJdbcTemplate().update(
			"delete from declaration where id = ?",
			declarationDataId
		);
		if (count == 0) {
			throw new DaoException("Не удалось удалить декларацию с id = %d, так как она не существует", declarationDataId);
		}
	}

	@Override
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId){
		try {
			Long declarationDataId = getJdbcTemplate().queryForLong(
					"select dec.id from declaration dec where exists (select 1 from declaration_template dt where dec.declaration_template_id=dt.id and dt.declaration_type_id = ?)"
							+ " and dec.department_id = ? and dec.report_period_id = ?",
					new Object[] {
							declarationTypeId,
							departmentId,
							reportPeriodId
					},
					new int[] {
							Types.NUMERIC,
							Types.NUMERIC,
							Types.NUMERIC
					}
			);
			return get(declarationDataId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new DaoException(
					"Для заданного сочетания параметров найдено несколько деклараций: declarationTypeId = %d, departmentId = %d, reportPeriodId = %d",
					declarationTypeId,
					departmentId,
					reportPeriodId
			);
		}
	}

	@Override
	public PaginatedSearchResult<DeclarationDataSearchResultItem> findPage(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering,
	                                                 boolean ascSorting,PaginatedSearchParams pageParams) {
		StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
		appendSelectClause(sql);
		appendFromAndWhereClause(sql, declarationFilter);
		appendOrderByClause(sql, ordering, ascSorting);
		sql.append(") dat) ordDat where ordDat.rn between ? and ?")
				.append(" order by ordDat.rn");
		List<DeclarationDataSearchResultItem> records = getJdbcTemplate().query(
				sql.toString(),
				new Object[] {
						pageParams.getStartIndex() + 1,	// В java нумерация с 0, в БД row_number() нумерует с 1
						pageParams.getStartIndex() + pageParams.getCount()
				},
				new int[] {
						Types.NUMERIC,
						Types.NUMERIC
				},
				new DeclarationDataSearchResultItemMapper()
		);
		long count = getCount(declarationFilter);
		PaginatedSearchResult<DeclarationDataSearchResultItem>  result = new PaginatedSearchResult<DeclarationDataSearchResultItem>();
		result.setRecords(records);
		result.setTotalRecordCount(count);
		return result;
	}

	@Override
	public long saveNew(DeclarationData declarationData) {
		JdbcTemplate jt = getJdbcTemplate();
		
		Long id = declarationData.getId();
		if (id != null) {
			throw new DaoException("Произведена попытка перезаписать уже сохранённую декларацию");
		}
		id = generateId("seq_declaration", Long.class);
		jt.update(
			"insert into declaration (id, declaration_template_id, report_period_id, department_id, is_accepted) values (?, ?, ?, ?, ?)",
			id,
			declarationData.getDeclarationTemplateId(),
			declarationData.getReportPeriodId(),
			declarationData.getDepartmentId(),
			declarationData.isAccepted() ? 1 : 0
		);
		declarationData.setId(id);
		return id.longValue();
	}

	@Override
	public void setAccepted(long declarationDataId, boolean accepted) {
		int count = getJdbcTemplate().update(
			"update declaration set is_accepted = ? where id = ?",
			accepted,
			declarationDataId
		);
		if (count == 0) {
			throw new DaoException("Не удалось изменить статус декларации с id = %d, так как она не существует.", declarationDataId);
		}		
	}

	@Override
	public long getCount(DeclarationDataFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*)");
		appendFromAndWhereClause(sql, filter);
		return getJdbcTemplate().queryForLong(sql.toString());
	}

	private void appendFromAndWhereClause(StringBuilder sql, DeclarationDataFilter filter) {
		sql.append(" FROM declaration dec, declaration_type dectype, department dp, report_period rp")
				.append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
				.append(" AND dp.id = dec.department_id AND rp.id = dec.report_period_id");

		if (filter.getTaxType() != null) {
			sql.append(" AND dectype.tax_type = ").append("\'").append(filter.getTaxType().getCode()).append("\'");
		}

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
			sql.append(" AND rp.id in ").append(transformToSqlInStatement(filter.getReportPeriodIds()));
		}

		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql.append(" AND dec.department_id in ").append(transformToSqlInStatement(filter.getDepartmentIds()));
		}

		if (filter.getDeclarationTypeId() != null) {
			sql.append(" AND dectype.id = ").append(filter.getDeclarationTypeId());
		}
	}

	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT dec.ID as declaration_id, dec.declaration_template_id, dec.is_accepted,")
				.append(" dectype.ID as declaration_type_id, dectype.NAME as declaration_type_name,")
				.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
				.append(" rp.ID as report_period_id, rp.NAME as report_period_name, dectype.TAX_TYPE");
	}

	public void appendOrderByClause(StringBuilder sql, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
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
			case DECLARATION_TYPE_NAME:
				column = "dectype.name";
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
