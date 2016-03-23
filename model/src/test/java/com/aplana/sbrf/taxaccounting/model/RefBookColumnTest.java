package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Fail Mukhametdinov
 */
public class RefBookColumnTest {

    @Test
    public void testGetFormatter() throws Exception {
        RefBookAttribute refBookAttribute1 = new RefBookAttribute();
        refBookAttribute1.setAttributeType(RefBookAttributeType.NUMBER);
        refBookAttribute1.setPrecision(2);
        refBookAttribute1.setMaxLength(10);
        RefBookColumn column = new RefBookColumn();
        column.setRefBookAttribute(refBookAttribute1);
        ColumnFormatter formatter = column.getFormatter();
        assertEquals("12 345.10", formatter.format("12345.1"));
        assertEquals("10.13", formatter.format("10.126"));
        assertEquals("12345abc", formatter.format("12345abc"));
    }

    @Test
    public void testGetFormatter2() throws Exception {
        RefBookAttribute refBookAttribute1 = new RefBookAttribute();
        refBookAttribute1.setAttributeType(RefBookAttributeType.STRING);
        RefBookColumn column = new RefBookColumn();
        column.setRefBookAttribute(refBookAttribute1);
        ColumnFormatter formatter = column.getFormatter();
        assertEquals("12345", formatter.format("12345"));
        assertEquals("12345abc", formatter.format("12345abc"));
    }
}