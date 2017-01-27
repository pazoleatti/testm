package com.aplana.sbrf.taxaccounting.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PreparedStatementDataTest {
    @Test
    public void appendWorksInRightOrder() throws Exception {
        String expectedQuery = "123";
        PreparedStatementData ps = new PreparedStatementData();
        ps.append("1").append("2").append("3");

        assertEquals(expectedQuery, ps.getQuery().toString());
    }

    @Test
    public void appendCanAppendInteger() throws Exception {
        PreparedStatementData ps = new PreparedStatementData();
        ps.append(42);

        assertEquals("42", ps.getQueryString());
    }


    @Test
    public void getQueryStringReturnsQuery() throws Exception {
        PreparedStatementData ps = new PreparedStatementData();
        ps.append("myquery");

        assertEquals(ps.getQuery().toString(), ps.getQueryString());
    }
}