package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.BookerStatementsSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

/**
 * @author lhaziev
 */
@Repository
public class BookerStatementsSearchDaoImpl extends AbstractDao implements BookerStatementsSearchDao {

    private void appendFromAndWhereClause(StringBuilder sql, BookerStatementsFilter filter) {
        sql.append(" select distinct REPORT_PERIOD_ID, DEPARTMENT_ID, 0 as TYPE")
                .append(" from income_101 ");

        StringBuilder b = new StringBuilder();
        if (filter.getBookerStatementsType() != null) {
            b.append(" and 0 = ").append(filter.getBookerStatementsType().getId());
        }

        if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
            b.append(" and ").append(transformToSqlInStatement("REPORT_PERIOD_ID", filter.getReportPeriodIds()));
        }

        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            b.append(" and ").append(transformToSqlInStatement("DEPARTMENT_ID", filter.getDepartmentIds()));
        }
        if (b.length() != 0) {
            b.delete(0, 4);
            sql.append(" where").append(b);
        }
        sql.append("union select distinct REPORT_PERIOD_ID, DEPARTMENT_ID, 1 as TYPE")
                .append(" from income_102 ");
        if (b.length() != 0) {
            sql.append(" where").append(b.toString().replace("0 = ", "1 = "));
        }
    }

    public void appendOrderByClause(StringBuilder sql, BookerStatementsSearchOrdering ordering, boolean ascSorting) {
        String column = null;
        switch (ordering) {
            case ID:
                break;
            case DEPARTMENT_NAME:
                column = "dp.name";
                break;
            case YEAR:
                column = "tp.year";
                break;
            case REPORT_PERIOD_NAME:
                column = "rp.NAME";
                break;
            case BOOKER_STATEMENTS_TYPE_NAME:
                column = "bs.type";
                break;
        }

        if (column != null) {
            sql.append(" order by ");
            sql.append(column);
            if (!ascSorting) {
                sql.append(" desc");
            }
        }
    }

    @Override
    public PagingResult<BookerStatementsSearchResultItem> findPage(BookerStatementsFilter filter, BookerStatementsSearchOrdering ordering, boolean ascSorting, PagingParams pageParams) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        sql.append("select bs.REPORT_PERIOD_ID, bs.DEPARTMENT_ID, bs.type, tp.year, rp.NAME as report_period_name, dp.NAME as department_name from (");
        appendFromAndWhereClause(sql, filter);
        sql.append(") bs, department dp, report_period rp, tax_period tp")
                .append(" where dp.id = bs.department_id AND rp.id = bs.report_period_id AND tp.id=rp.tax_period_id");

        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat where ordDat.rn between ? and ?")
                .append(" order by ordDat.rn");
        List<BookerStatementsSearchResultItem> records = getJdbcTemplate().query(
                sql.toString(),
                new Object[] {
                        pageParams.getStartIndex() + 1,	// В java нумерация с 0, в БД row_number() нумерует с 1
                        pageParams.getStartIndex() + pageParams.getCount()
                },
                new int[] {
                        Types.NUMERIC,
                        Types.NUMERIC
                },
                new BookerStatementsSearchResultItemMapper()
        );
        return new PagingResult<BookerStatementsSearchResultItem>(records, getCount(filter));
    }

    public int getCount(BookerStatementsFilter filter) {
        StringBuilder sql = new StringBuilder("select count(*) from (");
        appendFromAndWhereClause(sql, filter);
        sql.append(")");
        return getJdbcTemplate().queryForInt(sql.toString());
    }
}
