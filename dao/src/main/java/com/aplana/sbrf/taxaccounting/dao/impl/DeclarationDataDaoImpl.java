package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.DeclarationDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.source.DeclarationDataInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * Реализация Dao для работы с декларациями
 * @author dsultanbekov
 */
@Repository
@Transactional
public class DeclarationDataDaoImpl extends AbstractDao implements DeclarationDataDao {

	private static final String DECLARATION_NOT_FOUND_MESSAGE = "Декларация с id = %d не найдена в БД";

	private static final class DeclarationDataRowMapper implements RowMapper<DeclarationData> {
		@Override
		public DeclarationData mapRow(ResultSet rs, int index) throws SQLException {
			DeclarationData d = new DeclarationData();
			d.setId(rs.getLong("id"));
			d.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
			d.setDepartmentId(rs.getInt("department_id"));
			d.setReportPeriodId(rs.getInt("report_period_id"));
			d.setAccepted(rs.getBoolean("is_accepted"));
            d.setPdfDataUuid(rs.getString("data_pdf"));
            d.setXlsxDataUuid(rs.getString("data_xlsx"));
            d.setXmlDataUuid(rs.getString("data"));
            d.setJasperPrintUuid(rs.getString("jasper_print"));
			return d;
		}
	}

	@Override
	public DeclarationData get(long declarationDataId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from declaration_data where id = ?",
				new Object[] { declarationDataId },
				new DeclarationDataRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(DECLARATION_NOT_FOUND_MESSAGE, declarationDataId);
		}
	}

	@Override
	public boolean hasXmlData(long declarationDataId) {
		return getJdbcTemplate().queryForInt("select count(*) from declaration_data where data is not null and id = ?", declarationDataId) == 1;
	}


	@Override
	public void delete(long id) {
		int count = getJdbcTemplate().update(
			"delete from declaration_data where id = ?",
			id
		);
		if (count == 0) {
			throw new DaoException("Не удалось удалить декларацию с id = %d, так как она не существует", id);
		}
	}

	@Override
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId){
		try {
			Long declarationDataId = getJdbcTemplate().queryForLong(
					"select dec.id from declaration_data dec where exists (select 1 from declaration_template dt where dec.declaration_template_id=dt.id and dt.declaration_type_id = ?)"
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
	public PagingResult<DeclarationDataSearchResultItem> findPage(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering,
	                                                 boolean ascSorting,PagingParams pageParams) {
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
		return new PagingResult<DeclarationDataSearchResultItem>(records, getCount(declarationFilter));
	}

    @Override
    public List<Long> findIdsByFilter(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, declarationDataFilter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat");
        return getJdbcTemplate().query(
                sql.toString(),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getLong("declaration_data_id");
                    }
                }
        );
    }

    @Override
	public long saveNew(DeclarationData declarationData) {
		JdbcTemplate jt = getJdbcTemplate();
		
		Long id = declarationData.getId();
		if (id != null) {
			throw new DaoException("Произведена попытка перезаписать уже сохранённую декларацию");
		}

		int countOfExisted = jt.queryForInt("SELECT COUNT(*) FROM declaration_data WHERE declaration_template_id = ?" +
				" AND report_period_id = ? AND department_id = ?",
				new Object[]{declarationData.getDeclarationTemplateId(), declarationData.getReportPeriodId(), declarationData.getDepartmentId()},
				new int[] {Types.INTEGER, Types.INTEGER, Types.INTEGER});

		if(countOfExisted != 0){
			throw new DaoException("Декларация с указанными параметрами уже существует");
		}

		id = generateId("seq_declaration_data", Long.class);
		jt.update(
			"insert into declaration_data (id, declaration_template_id, report_period_id, department_id, is_accepted) values (?, ?, ?, ?, ?)",
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
			"update declaration_data set is_accepted = ? where id = ?",
			accepted,
			declarationDataId
		);
		if (count == 0) {
			throw new DaoException("Не удалось изменить статус декларации с id = %d, так как она не существует.", declarationDataId);
		}		
	}

	@Override
	public int getCount(DeclarationDataFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*)");
		appendFromAndWhereClause(sql, filter);
		return getJdbcTemplate().queryForInt(sql.toString());
	}

	private void appendFromAndWhereClause(StringBuilder sql, DeclarationDataFilter filter) {
		sql.append(" FROM declaration_data dec, declaration_type dectype, department dp, report_period rp, tax_period tp")
				.append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
				.append(" AND dp.id = dec.department_id AND rp.id = dec.report_period_id AND tp.id=rp.tax_period_id");

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

        if (filter.getFormState() != null) {
            if (filter.getFormState() == WorkflowState.CREATED) {
                sql.append(" AND dec.is_accepted = 0");
            } else if (filter.getFormState() == WorkflowState.ACCEPTED) {
                sql.append(" AND dec.is_accepted = 1");
            }
        }
	}

	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT dec.ID as declaration_data_id, dec.declaration_template_id, dec.is_accepted,")
				.append(" dectype.ID as declaration_type_id, dectype.NAME as declaration_type_name,")
				.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
				.append(" rp.ID as report_period_id, rp.NAME as report_period_name, dectype.TAX_TYPE, tp.year");
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

    @Override
    public void update(DeclarationData declarationData) {
        int count = getJdbcTemplate().update(
                "update declaration_data set data = ?, data_pdf = ?, data_xlsx = ?, jasper_print = ? where id = ?",
                new Object[] {
                        declarationData.getXmlDataUuid(),
                        declarationData.getPdfDataUuid(),
                        declarationData.getXlsxDataUuid(),
                        declarationData.getJasperPrintUuid(),
                        declarationData.getId()
                },
                new int[] {
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.NUMERIC
                }
        );
        if (count == 0) {
            throw new DaoException("Не удалось обновить декларацию с id = %d, так как она не существует.");
        }
    }

    @Override
    public List<Long> findDeclarationDataByFormTemplate(int templateId, Date startDate) {
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT id FROM declaration_data WHERE declaration_template_id = ? AND report_period_id IN" +
                            " (SELECT id FROM report_period WHERE calendar_start_date >= ?)",
                    new Object[] {templateId, startDate},
                    new int[] {Types.NUMERIC, Types.DATE},
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        } catch (DataAccessException e) {
            logger.error(String.format("Ошибка поиска деклараций для заданного шаблона %d", templateId), e);
            throw new DaoException("Ошибка поиска деклараций для заданного шаблона %d", templateId);
        }
    }


}
