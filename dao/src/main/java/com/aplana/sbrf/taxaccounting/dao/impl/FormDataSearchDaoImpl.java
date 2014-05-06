package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter.AccessFilterType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

@Repository("formDataSearchDao")
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

	private static final Log logger = LogFactory.getLog(FormDataSearchDaoImpl.class);

	private void appendFromAndWhereClause(StringBuilder sql, FormDataDaoFilter filter) {
		sql.append(" FROM form_data fd, form_type ft, department dp, report_period rp, tax_period tp")
			.append(" WHERE EXISTS (SELECT 1 FROM FORM_TEMPLATE t WHERE t.id = fd.form_template_id AND t.type_id = ft.id)")
			.append(" AND dp.id = fd.department_id AND rp.id = fd.report_period_id AND tp.id=rp.tax_period_id");
	
		if (filter.getFormTypeIds() != null && !filter.getFormTypeIds().isEmpty()) {
			sql.append(" AND ft.id in ").append(transformToSqlInStatement(filter.getFormTypeIds())); 
		}

		if (filter.getTaxTypes() != null && !filter.getTaxTypes().isEmpty()) {
			sql.append(" AND ft.tax_type in ").append(transformTaxTypeToSqlInStatement(filter.getTaxTypes()));
		}

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
			sql.append(" AND rp.id in ").append(transformToSqlInStatement(filter.getReportPeriodIds()));
		}

		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql.append(" AND fd.department_id in ").append(transformToSqlInStatement(filter.getDepartmentIds()));
		}

		if (filter.getFormDataKinds() != null && !filter.getFormDataKinds().isEmpty()) {
			sql.append(" AND fd.kind in ").append(transformFormKindsToSqlInStatement(filter.getFormDataKinds()));
		}

		if (filter.getStates() != null && !filter.getStates().isEmpty()) {
			sql.append(" AND fd.state in ").append(transformFormStatesToSqlInStatement(filter.getStates()));
		}

		if (filter.getReturnState() != null) {
			sql.append(" AND fd.return_sign = ").append(filter.getReturnState() == Boolean.TRUE ? "1" : "0");
		}
		
		// Добавляем условия для отбрасывания форм, на которые у пользователя нет прав доступа
		// Эта реализация должна быть согласована с реализацией в FormDataAccessServiceImpl
		if (filter.getAccessFilterType() == null) {
			throw new IllegalArgumentException("AccessFilterType cannot be null");
		}

		if (filter.getAccessFilterType() == AccessFilterType.AVAILABLE_DEPARTMENTS
                || filter.getAccessFilterType() == AccessFilterType.AVAILABLE_DEPARTMENTS_WITH_KIND) {
            sql.append(" and fd.department_id in ").append(SqlUtils.transformToSqlInStatement(
                    filter.getAvailableDepartmentIds()));

            if (filter.getAccessFilterType() == AccessFilterType.AVAILABLE_DEPARTMENTS_WITH_KIND) {
                sql.append(" and fd.kind in ").append(SqlUtils.transformFormKindsToSqlInStatement(
                        filter.getAvailableFormDataKinds()));
            }
        }
	}
	
	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT fd.ID as form_data_id, fd.form_template_id, fd.return_sign, fd.KIND as form_data_kind_id, fd.STATE, fd.PERIOD_ORDER as period_order, tp.year,")
			.append(" ft.ID as form_type_id, ft.NAME as form_type_name, ft.TAX_TYPE,")		
			.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
			.append(" rp.ID as report_period_id, rp.NAME as report_period_name");
	}
	
	@Override
	public List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter dataFilter) {
		StringBuilder sql = new StringBuilder();
		appendSelectClause(sql);
		appendFromAndWhereClause(sql, dataFilter);

		if (logger.isTraceEnabled()) {
			logger.trace(sql);
		}

		sql.append(" order by fd.id desc");

		return getJdbcTemplate().query(sql.toString(), new FormDataSearchResultItemMapper());
	}

    @Override
    public List<Long> findIdsByFilter(FormDataDaoFilter filter) {
        StringBuilder sql = new StringBuilder();
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, filter);
        if (logger.isTraceEnabled()) {
            logger.trace(sql);
        }
        sql.append(" order by fd.id desc");

        return getJdbcTemplate().query(sql.toString(), new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                return SqlUtils.getLong(rs, "form_data_id");
            }
        });
    }

    public void appendOrderByClause(StringBuilder sql, FormDataSearchOrdering ordering, boolean ascSorting) {
		sql.append(" order by ");
		
		String column = null;
		switch (ordering) {
		case ID: 
			// Сортировка по ID делается всегда, поэтому здесь оставляем null
			break;
		case DEPARTMENT_NAME:
			column = "dp.name";
			break;
		case FORM_TYPE_NAME:
			column = "ft.name";
			break;
		case KIND:
			column = "fd.kind";
			break;
		case REPORT_PERIOD_NAME:
			column = "rp.name";
			break;
		case STATE:
			column = "fd.state";
			break;
		case YEAR:
			column = "tp.year";
			break;
		case RETURN:
			column = "fd.return_sign";
			break;
		}
		
		if (column != null) {
			sql.append(column);
			if (!ascSorting) {
				sql.append(" desc");
			}
			sql.append(", ");
		}
		
		sql.append("fd.id");
		if (!ascSorting) {
			sql.append(" desc");
		}
	}
	
	@Override
	public PagingResult<FormDataSearchResultItem> findPage(FormDataDaoFilter filter, FormDataSearchOrdering ordering, boolean ascSorting, PagingParams pageParams) {
		StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
		appendSelectClause(sql);
		appendFromAndWhereClause(sql, filter);
		appendOrderByClause(sql, ordering, ascSorting);
		sql.append(") dat) ordDat where ordDat.rn between ? and ?")
			.append(" order by ordDat.rn");
		List<FormDataSearchResultItem> records = getJdbcTemplate().query(
				sql.toString(),
				new Object[] {
					pageParams.getStartIndex() + 1,	// В java нумерация с 0, в БД row_number() нумерует с 1
					pageParams.getStartIndex() + pageParams.getCount()
				},
				new int[] {
					Types.NUMERIC,
					Types.NUMERIC
				},
				new FormDataSearchResultItemMapper()
		);
		return new PagingResult<FormDataSearchResultItem>(records, getCount(filter));
	}

	@Override
	public int getCount(FormDataDaoFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*)");
		appendFromAndWhereClause(sql, filter);
		return getJdbcTemplate().queryForInt(sql.toString());
	}
}
