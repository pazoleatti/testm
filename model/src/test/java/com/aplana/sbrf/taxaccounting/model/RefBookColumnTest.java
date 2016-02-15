package com.aplana.sbrf.taxaccounting.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fail Mukhametdinov
 */
public class RefBookColumnTest {

    @Test
    public void testGetFormatter() throws Exception {
        RefBookColumn column = new RefBookColumn();
        Column.Formatter formatter = column.getFormatter();
        assertEquals("12 345", formatter.format("12345"));
        assertEquals("12345abc", formatter.format("12345abc"));
    }
}