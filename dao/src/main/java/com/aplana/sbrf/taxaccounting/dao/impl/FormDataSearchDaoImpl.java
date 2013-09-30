package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter.AccessFilterType;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

@Repository("formDataSearchDao")
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

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
			sql.append(" AND fd.DEPARTMENT_ID in ").append(transformToSqlInStatement(filter.getDepartmentIds()));
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
		
		if (filter.getAccessFilterType() == AccessFilterType.USER_DEPARTMENT) {
			sql.append(" and fd.department_id = ").append(filter.getUserDepartmentId());
		} else if (filter.getAccessFilterType() == AccessFilterType.USER_DEPARTMENT_AND_SOURCES) {
			// Форма либо сама относится к подразделению пользователя
			sql.append(" and (fd.department_id = ").append(filter.getUserDepartmentId())
			// Либо является источником для одной из форм подразделения пользователя
				.append(" or exists (")
				.append("select 1 from form_data_source fds, department_form_type dftSrc, department_form_type dftDest where")
				.append(" fds.department_form_type_id = dftDest.id and fds.src_department_form_type_id = dftSrc.id")
				.append(" and dftSrc.form_type_id = ft.id and dftSrc.kind = fd.kind and dftSrc.department_id = fd.department_id")
				.append(" and dftDest.department_id = ").append(filter.getUserDepartmentId()).append(")")
			// Либо является источником для одной из деклараций подразделения пользователя
				.append(" or exists (")
				.append("select 1 from declaration_source ds, department_form_type dftSrc, department_declaration_type ddtDest where")
				.append(" ds.department_declaration_type_id = ddtDest.id and ds.src_department_form_type_id = dftSrc.id")
				.append(" and dftSrc.form_type_id = ft.id and dftSrc.kind = fd.kind and dftSrc.department_id = fd.department_id")
				.append(" and ddtDest.department_id = ").append(filter.getUserDepartmentId()).append(")")				
				.append(")");
		}
	}
	
	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT fd.ID as form_data_id, fd.form_template_id, fd.return_sign, fd.KIND as form_data_kind_id, fd.STATE, fd.PERIOD_ORDER as period_order, tp.START_DATE,")
			.append(" ft.ID as form_type_id, ft.NAME as form_type_name, ft.TAX_TYPE,")		
			.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
			.append(" rp.ID as report_period_id, rp.NAME as report_period_name");
	}
	
	@Override
	public List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter dataFilter) {
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
		case YEAR:
			column = "tp.start_date";
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
