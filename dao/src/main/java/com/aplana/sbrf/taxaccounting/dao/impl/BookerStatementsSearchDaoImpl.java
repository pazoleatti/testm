package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.BookerStatementsSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * @author lhaziev
 */
@Repository
public class BookerStatementsSearchDaoImpl extends AbstractDao implements BookerStatementsSearchDao {

    private void appendFromAndWhereClause(StringBuilder sql, BookerStatementsFilter filter) {
        sql.append(" select distinct account_period_id, 0 as type")
                .append(" from income_101 ");

        StringBuilder b = new StringBuilder();
        if (filter.getBookerStatementsType() != null) {
            b.append(" and 0 = ").append(filter.getBookerStatementsType().getId());
        }
        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            sql.append(", department dep");
            b.append(" and dep.id = (select reference_value from ref_book_value rbv where record_id = account_period_id and attribute_id = 1073)");
            b.append(" and ").append(transformToSqlInStatement("dep.id", filter.getDepartmentIds()));
        }
        if (filter.getAccountPeriodIds() != null && !filter.getAccountPeriodIds().isEmpty()) {
            b.append(" and ").append(transformToSqlInStatement("account_period_id", filter.getAccountPeriodIds()));
        }

        if (b.length() != 0) {
            b.delete(0, 4);
            sql.append(" where").append(b);
        }
        sql.append("union select distinct account_period_id, 1 as type")
                .append(" from income_102 ");
        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            sql.append(", department dep");
        }
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
                column = "dep.name";
                break;
            case YEAR:
                column = "ap.number_value";
                break;
            case ACCOUNT_PERIOD_NAME:
                column = "period.string_value";
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
        sql.append(" select bs.account_period_id, bs.type, ap.number_value as year, period.string_value as account_period_name, dep.name as department_name, dep.id as department_id from (");
        appendFromAndWhereClause(sql, filter);
        sql.append(" ) bs, department dep, ref_book_value ap, ref_book_value period")
                .append(" where dep.id = (select reference_value from ref_book_value rbv where record_id = account_period_id and attribute_id = 1073)")
                .append(" and ap.record_id = account_period_id and ap.attribute_id = 1071")
                .append(" and period.record_id = (select reference_value from ref_book_value rbv where record_id = account_period_id and attribute_id = 1072) and period.attribute_id = 1062");

        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(" ) dat) ordDat where ordDat.rn between ? and ?")
                .append(" order by ordDat.rn");
        List<BookerStatementsSearchResultItem> records = getJdbcTemplate().query(
                sql.toString(),
                new Object[]{
                        pageParams.getStartIndex() + 1,    // В java нумерация с 0, в БД row_number() нумерует с 1
                        pageParams.getStartIndex() + pageParams.getCount()
                },
                new int[]{
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
