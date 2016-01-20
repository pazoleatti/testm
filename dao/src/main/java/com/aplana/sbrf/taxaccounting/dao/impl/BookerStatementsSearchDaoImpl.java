package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsSearchDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.BookerStatementsSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * @author lhaziev
 */
@Repository
public class BookerStatementsSearchDaoImpl extends AbstractDao implements BookerStatementsSearchDao {

    private static final int ATTR_YEAR = 1071;
    private static final int ATTR_ACCOUNT_PERIOD_ID = 1072;
    private static final int ATTR_DEPARTMENT_ID = 1073;
    private static final int ATTR_NAME = 1062;

    private void appendFromAndWhereClause(StringBuilder sql, BookerStatementsFilter filter) {
        StringBuilder from101 = new StringBuilder(" select distinct account_period_id, 0 as type from income_101 ");
        StringBuilder from102 = new StringBuilder(" select distinct account_period_id, 1 as type from income_102 ");
        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            from101.append(", department dep");
            from102.append(", department dep");
        }

        StringBuilder whereBlock = new StringBuilder();
        // фильтр по типу БО
        if (filter.getBookerStatementsType() != null) {
            whereBlock.append(" and 0 = ").append(filter.getBookerStatementsType().getId());
        }
        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            // фильтр по подразделениям
            whereBlock.append(" and dep.id = (select reference_value from ref_book_value rbv where record_id = account_period_id and attribute_id = ")
                    .append(ATTR_DEPARTMENT_ID).append(")")
                    .append(" and ").append(transformToSqlInStatement("dep.id", filter.getDepartmentIds()));
        }
        if (filter.getAccountPeriodIds() != null && !filter.getAccountPeriodIds().isEmpty()) {
            // фильтр по периодам
            StringBuilder subAccounts = new StringBuilder();
            subAccounts.append(" (select t2.record_id from ")
                    .append(" (select max(number_value) as year, max(reference_value) as period from ref_book_value where ")
                    .append(transformToSqlInStatement("record_id", filter.getAccountPeriodIds()))
                    .append(" and attribute_id in (").append(ATTR_YEAR).append(",").append(ATTR_ACCOUNT_PERIOD_ID)
                    .append(") group by record_id) t1, ")
                    .append(" (select record_id, max(number_value) as year, max(reference_value) as period ")
                    .append(" from ref_book_value where record_id in (select id from ref_book_record where ref_book_id = 107) ")
                    .append(" and attribute_id in (").append(ATTR_YEAR).append(",").append(ATTR_ACCOUNT_PERIOD_ID)
                    .append(") group by record_id) t2 ")
                    .append(" where t1.year = t2.year and t1.period = t2.period)");
            List<Integer> recordIds = getJdbcTemplate().queryForList(subAccounts.toString(), Integer.class);
            whereBlock.append(" and ").append(transformToSqlInStatement("account_period_id", recordIds));
        }
        if (whereBlock.length() != 0) {
            whereBlock.delete(0, 4);
            from101.append(" where ").append(whereBlock);
            from102.append(" where ").append(whereBlock.toString().replaceFirst("0 = ", "1 = "));
        }

        if (filter.getBookerStatementsType() == null) {
            // обе
            sql.append(from101).append(" union all ").append(from102);
        } else if (filter.getBookerStatementsType().getId() == 0) {
            // 101
            sql.append(from101);
        } else if (filter.getBookerStatementsType().getId() == 1) {
            // 102
            sql.append(from102);
        }
    }

    private void appendOrderByClause(StringBuilder sql, BookerStatementsSearchOrdering ordering, boolean ascSorting) {
        List<String> columns = new ArrayList<String>();
        switch (ordering) {
            case ID:
                break;
            case DEPARTMENT_NAME:
                columns.add("dep.name");
                break;
            case YEAR:
                columns.add("ap.number_value");
                columns.add("period.string_value");
                break;
            case ACCOUNT_PERIOD_NAME:
                columns.add("period.string_value");
                break;
            case BOOKER_STATEMENTS_TYPE_NAME:
                columns.add("bs.type");
                break;
        }

        if (!columns.isEmpty()) {
            sql.append(" order by ");
            Iterator<String> iterator = columns.iterator();
            while (iterator.hasNext()) {
                sql.append(iterator.next());
                if (!ascSorting) {
                    sql.append(" desc ");
                }
                if (iterator.hasNext()) {
                    sql.append(", ");
                }
            }
        }
    }

    @Override
    public PagingResult<BookerStatementsSearchResultItem> findPage(BookerStatementsFilter filter, BookerStatementsSearchOrdering ordering, boolean ascSorting, PagingParams pageParams) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        sql.append("select bs.account_period_id, bs.type, ap.number_value as year, period.string_value as account_period_name, dep.name as department_name, dep.id as department_id from (");
        appendFromAndWhereClause(sql, filter);
        sql.append(" ) bs, department dep, ref_book_value ap, ref_book_value period")
                .append(" where dep.id = (select reference_value from ref_book_value rbv where record_id = account_period_id and attribute_id = ").append(ATTR_DEPARTMENT_ID).append(")")
                .append(" and ap.record_id = account_period_id and ap.attribute_id = ").append(ATTR_YEAR)
                .append(" and period.record_id = (select reference_value from ref_book_value rbv where record_id = account_period_id and attribute_id = ").append(ATTR_ACCOUNT_PERIOD_ID).append(")")
                .append(" and period.attribute_id = ").append(ATTR_NAME);

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

    private int getCount(BookerStatementsFilter filter) {
        StringBuilder sql = new StringBuilder("select count(*) from (");
        appendFromAndWhereClause(sql, filter);
        sql.append(")");
        return getJdbcTemplate().queryForInt(sql.toString());
    }
}
