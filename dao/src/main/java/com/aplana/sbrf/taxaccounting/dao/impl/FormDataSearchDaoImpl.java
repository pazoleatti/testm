package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

@Repository("formDataSearchDao")
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

	private void appendFromAndWhereClause(StringBuilder sql, FormDataDaoFilter filter) {
		sql.append(" FROM form_data fd, form_type ft, department dp, report_period rp")
			.append(" WHERE EXISTS (SELECT 1 FROM FORM f WHERE f.id = fd.form_id AND f.type_id = ft.id)")
			.append(" AND dp.id = fd.department_id AND rp.id = fd.report_period_id");
	
		if (filter.getFormTypeIds() != null && !filter.getFormTypeIds().isEmpty()) {
			sql.append(" AND ft.id in ").append(transformToSqlInStatement(filter.getFormTypeIds())); 
		}

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
			sql.append(" AND rp.id in ").append(transformToSqlInStatement(filter.getReportPeriodIds())); 
		}

		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql.append(" AND fd.DEPARTMENT_ID in ").append(transformToSqlInStatement(filter.getDepartmentIds()));
		}
		
		if (filter.getFormDataKinds() != null && !filter.getFormDataKinds().isEmpty()) {
			sql.append(" AND fd.kind in ").append(transformFormKindsToSqlInStatement(filter.getFormDataKinds()));
		}

		if (filter.getStates() != null && !filter.getStates().isEmpty()) {
			sql.append(" AND fd.state in ").append(transformFormStatesToSqlInStatement(filter.getStates()));
		}
	}
	
	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT fd.ID as form_data_id, fd.form_id as form_template_id, fd.KIND as form_data_kind_id, fd.STATE,")
			.append(" ft.ID as form_type_id, ft.NAME as form_type_name,")		
			.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
			.append(" rp.ID as report_period_id, rp.NAME as report_period_name, rp.TAX_TYPE");
	}
	
	@Override
	public List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter dataFilter) {
		// TODO: Зачем это условие здесь???
		if(dataFilter.getFormTypeIds().isEmpty() || dataFilter.getReportPeriodIds().isEmpty()){
			return (new ArrayList<FormDataSearchResultItem>());
		}

		StringBuilder sql = new StringBuilder();
		appendSelectClause(sql);
		appendFromAndWhereClause(sql, dataFilter);
		
		sql.append(" order by fd.id");
		return getJdbcTemplate().query(sql.toString(), new FormDataSearchResultItemMapper());
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
	public PaginatedSearchResult<FormDataSearchResultItem> findPage(FormDataDaoFilter filter, FormDataSearchOrdering ordering, boolean ascSorting, PaginatedSearchParams pageParams) {
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
		long count = getCount(filter);
		PaginatedSearchResult<FormDataSearchResultItem>  result = new PaginatedSearchResult<FormDataSearchResultItem>();
		result.setRecords(records);
		result.setTotalRecordCount(count);
		return result;
	}

	@Override
	public long getCount(FormDataDaoFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*)");
		appendFromAndWhereClause(sql, filter);
		return getJdbcTemplate().queryForLong(sql.toString());
	}
}
