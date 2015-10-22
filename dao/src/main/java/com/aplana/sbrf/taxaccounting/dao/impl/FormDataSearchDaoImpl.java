package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

@Repository("formDataSearchDao")
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

	private static final Log LOG = LogFactory.getLog(FormDataSearchDaoImpl.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");

    private void appendFromClause(StringBuilder sql){
        sql.append(" FROM form_data fd \n")
                .append("join department_report_period drp on drp.id = fd.department_report_period_id\n")
                .append("join form_template t on t.id = fd.form_template_id\n")
                .append("join form_type ft on ft.id = t.type_id\n")
                .append("join department dp on dp.id = drp.department_id \n")
                .append("join report_period rp on rp.id = drp.report_period_id\n")
                .append("join tax_period tp on tp.id=rp.tax_period_id\n")
                .append("join log_business lb on lb.form_data_id = fd.id\n")
                .append("left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID\n")
                .append("left join report_period crp on crp.id = cdrp.report_period_id \n")
                .append("where lb.event_id = 1 ");
    }

	private void appendWhereClause(StringBuilder sql, FormDataDaoFilter filter) {
		if (filter.getFormTypeIds() != null && !filter.getFormTypeIds().isEmpty()) {
			sql
                .append(" AND ")
                .append(transformToSqlInStatement("ft.id", filter.getFormTypeIds()));
		}

		if (filter.getTaxTypes() != null && !filter.getTaxTypes().isEmpty()) {
			sql.append(" AND ft.tax_type in ").append(transformTaxTypeToSqlInStatement(filter.getTaxTypes()));
		}

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
			sql
                .append(" AND ")
                .append(transformToSqlInStatement("rp.id", filter.getReportPeriodIds()));
		}

		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql
                .append(" AND ")
                .append(transformToSqlInStatement("dp.id", filter.getDepartmentIds()));
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

        if (filter.getCorrectionTag() != null) {
            if (filter.getCorrectionDate() != null) {
                sql.append(" and drp.correction_date = '" + SDF.format(filter.getCorrectionDate()) + "\'");
            } else {
                sql.append(" and drp.correction_date is " +
                        (Boolean.TRUE.equals(filter.getCorrectionTag()) ? "not " : "") + "null");
            }
        }

	}

	private void appendSelectClause(StringBuilder sql) {
		sql.append("SELECT fd.ID as form_data_id, fd.department_report_period_id, fd.form_template_id, fd.return_sign, \n" +
                "fd.KIND as form_data_kind_id, fd.STATE, fd.PERIOD_ORDER as period_order, tp.year,\n")
			.append(" ft.ID as form_type_id, ft.NAME as form_type_name, ft.TAX_TYPE,\n")
			.append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,  drp.correction_date, \n")
			.append(" rp.ID as report_period_id, rp.NAME as report_period_name, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING \n");
	}

	@Override
	public List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter dataFilter) {
		StringBuilder sql = new StringBuilder();
		appendSelectClause(sql);
        appendFromClause(sql);
		appendWhereClause(sql, dataFilter);

		if (LOG.isTraceEnabled()) {
			LOG.trace(sql);
		}

		sql.append(" order by fd.id desc");

		return getJdbcTemplate().query(sql.toString(), new FormDataSearchResultItemMapper());
	}

    @Override
    public List<Long> findIdsByFilter(FormDataDaoFilter filter) {
        StringBuilder sql = new StringBuilder();
        appendSelectClause(sql);
        appendFromClause(sql);
        appendWhereClause(sql, filter);
        if (LOG.isTraceEnabled()) {
            LOG.trace(sql);
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
			column = "(100*extract(month from rp.calendar_start_date)+extract(day from rp.calendar_start_date))";
			break;
        case COMPARATIV_PERIOD_NAME:
            column = "(100*extract(month from crp.calendar_start_date)+extract(day from crp.calendar_start_date))";
            break;
        case REPORT_PERIOD_MONTH_NAME:
            column = "fd.period_order";
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
        case DATE:
            column = "lb.log_date";
        }

		if (column != null) {
			sql.append(column);
			if (!ascSorting) {
				sql.append(" desc");
			}
			sql.append(", ");
		}

        if (ordering != FormDataSearchOrdering.REPORT_PERIOD_MONTH_NAME) {
            sql.append(" fd.period_order, ");
        }

		sql.append("fd.id");
		if (!ascSorting) {
			sql.append(" desc");
		}
	}

    @Override
    public Long getRowNumByFilter(FormDataDaoFilter filter, FormDataSearchOrdering ordering, boolean ascSorting, Long formDataId) {
        StringBuilder sql = new StringBuilder("select rn from (select dat.*, rownum as rn from (");
        appendSelectClause(sql);
        appendFromClause(sql);
        appendWhereClause(sql, filter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat where form_data_id = ?");

        try {
            return getJdbcTemplate(). queryForLong(
                    sql.toString(),
                    formDataId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String FIND_PAGE = "%s" +
            "select ordDat.* from ( \n" +
            "select dat.*, count(*) over() cnt, rownum as rn from ( \n" +
            "SELECT fd.ID as form_data_id, fd.form_template_id, fd.return_sign, fd.KIND as form_data_kind_id, \n" +
            "fd.department_report_period_id, fd.STATE, \n" +
            "fd.PERIOD_ORDER as period_order, tp.year, ft.ID as form_type_id, ft.NAME as form_type_name, ft.TAX_TYPE, \n" +
            "dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type, rp.ID as report_period_id, \n" +
            "rp.NAME as report_period_name %s, drp.correction_date, fd.COMPARATIVE_DEP_REP_PER_ID, fd.ACCRUING \n" +
            "FROM form_data fd \n" +
            "join department_report_period drp on drp.id = fd.department_report_period_id \n" +
            "JOIN FORM_TEMPLATE t on t.id = fd.form_template_id \n" +
            "JOIN form_type ft on t.type_id = ft.id \n" +
            "JOIN department dp on dp.id = drp.department_id \n" +
            "JOIN report_period rp on rp.id = drp.report_period_id \n" +
            "LEFT JOIN department_report_period cdrp on fd.COMPARATIVE_DEP_REP_PER_ID = cdrp.id \n" +
            "LEFT JOIN report_period crp on crp.id = cdrp.report_period_id \n" +
            "JOIN tax_period tp on tp.id=rp.tax_period_id \n" +
            "JOIN log_business lb on lb.form_data_id = fd.id \n" +
            "%s \n" +
            "  WHERE lb.event_id = 1";

	@Override
	public PagingResult<FormDataSearchResultItem> findPage(FormDataDaoFilter filter, FormDataSearchOrdering ordering,
                                                           boolean ascSorting, PagingParams pageParams) {
        //Получаем иерархический имя подразделения
        String withClause = "with hierarchical_dep_name as (SELECT LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as path, id " +
                "                FROM department " +
                "                START WITH parent_id in (select id from department where parent_id is null) " +
                "                CONNECT BY PRIOR id = parent_id) ";
		StringBuilder sql = new StringBuilder(String.format(FIND_PAGE,
                isSupportOver() ? withClause : "",
                isSupportOver() ? ", hdn.path as  hierarchical_dep_name" : "",
                isSupportOver() ? " LEFT JOIN hierarchical_dep_name hdn on hdn.id = drp.department_id ": ""));
		appendWhereClause(sql, filter);
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
				new RowMapper<FormDataSearchResultItem>() {
                    @Override
                    public FormDataSearchResultItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                        FormDataSearchResultItem result = new FormDataSearchResultItem();

                        result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
                        result.setDepartmentName(rs.getString("department_name"));
                        result.setDepartmentType(DepartmentType.fromCode(SqlUtils.getInteger(rs,"department_type")));
                        result.setFormDataId(SqlUtils.getLong(rs,"form_data_id"));
                        result.setFormDataKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"form_data_kind_id")));
                        result.setFormTemplateId(SqlUtils.getInteger(rs,"form_template_id"));
                        result.setFormTypeId(SqlUtils.getInteger(rs,"form_type_id"));
                        result.setFormTypeName(rs.getString("form_type_name"));
                        result.setReportPeriodId(SqlUtils.getInteger(rs,"report_period_id"));
                        result.setReportPeriodName(rs.getString("report_period_name"));
                        result.setComparativePeriodId(SqlUtils.getInteger(rs,"COMPARATIVE_DEP_REP_PER_ID"));
                        result.setAccruing(rs.getBoolean("ACCRUING"));
                        result.setState(WorkflowState.fromId(SqlUtils.getInteger(rs,"state")));
                        result.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
                        result.setCorrectionDate(rs.getDate("correction_date"));
                        result.setReportPeriodYear(SqlUtils.getInteger(rs,"year"));
                        Integer reportPeriodMonth = SqlUtils.getInteger(rs,"period_order");
                        result.setReportPeriodMonth(rs.wasNull() ? null : reportPeriodMonth);
                        Integer returnSign = SqlUtils.getInteger(rs,"return_sign");
                        result.setReturnSign(rs.wasNull() ? null : 1 == returnSign);
                        result.setCount(rs.getInt("cnt"));
                        result.setHierarchicalDepName(isSupportOver() ? rs.getString("hierarchical_dep_name") : "");
                        result.setDepartmentReportPeriodId(SqlUtils.getInteger(rs, "department_report_period_id"));

                        return result;
                    }
                }
		);
		return new PagingResult<FormDataSearchResultItem>(records, (records.isEmpty()? 0:records.get(0).getCount()));
	}

	@Override
	public int getCount(FormDataDaoFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*)");
        appendFromClause(sql);
		appendWhereClause(sql, filter);
		return getJdbcTemplate().queryForInt(sql.toString());
	}
}
