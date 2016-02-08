package com.aplana.sbrf.taxaccounting.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fail Mukhametdinov
 */
public class StringColumnTest {

    @Test
    public void testGetValidationStrategyWhenFilterIsNotNull() throws Exception {
        StringColumn column = new StringColumn();
        column.setFilter("[0-9][a-z]");
        Assert.assertTrue(column.matches("1a"));
        Assert.assertFalse(column.matches("12a"));
        Assert.assertFalse(column.matches("a1"));
        Assert.assertFalse(column.matches("23"));
    }

    @Test
    public void testGetValidationStrategyWhenFilterIsNull() throws Exception {
        StringColumn column = new StringColumn();
        Assert.assertTrue(column.matches("1a"));
        Assert.assertTrue(column.matches("12a"));
        Assert.assertTrue(column.matches("a1"));
        Assert.assertTrue(column.matches("23"));
    }
}