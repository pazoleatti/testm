package com.aplana.sbrf.taxaccounting.model;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QueryBuilderTest {

    @Test
    public void withPagingTest() {
        QueryBuilder queryBuilder = new QueryBuilder();
        PagingParams pagingParams = new PagingParams();
        pagingParams.setCount(10);
        pagingParams.setPage(1);
        String query = "query";
        queryBuilder = queryBuilder.append(query);
        queryBuilder = queryBuilder.withPaging(pagingParams);
        assertTrue(queryBuilder.getPagedQuery().equalsIgnoreCase("select * FROM (\n" + query + ") WHERE rn between :paging_start and :paging_end"));
    }
}
