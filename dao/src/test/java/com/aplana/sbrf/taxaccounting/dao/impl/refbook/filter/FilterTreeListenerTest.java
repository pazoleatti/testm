package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;


import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тест для фильтра данных справочника
 * @author auldanov
 */
public class FilterTreeListenerTest {
    private RefBook refBook;

    @Before
    public void init(){
        refBook =  mock(RefBook.class);
        RefBookAttribute attribute = new RefBookAttribute();
        attribute.setAttributeType(RefBookAttributeType.STRING);
        when(refBook.getAttribute("Alias123")).thenReturn(attribute);
    }

    @Test
    public void UniversalFilterTreeListener()
    {
        StringBuffer result1 = new StringBuffer();
        Filter.getFilterQuery("Alias123 = 123", new UniversalFilterTreeListener(refBook, result1));
        assertTrue(result1.toString().equals("aAlias123.STRING_value = 123"));

        StringBuffer result2 = new StringBuffer();
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", new UniversalFilterTreeListener(refBook, result2));
        assertTrue(result2.toString().equals("aAlias123.STRING_value = 123 and(aAlias123.STRING_value > 10 or aAlias123.STRING_value = 15)"));

        StringBuffer result3 = new StringBuffer();
        Filter.getFilterQuery("Alias123 is null", new UniversalFilterTreeListener(refBook, result3));
        assertTrue(result3.toString().equals("aAlias123.STRING_value is null"));

        StringBuffer result4 = new StringBuffer();
        Filter.getFilterQuery("LOWER(Alias123) like 'any key'", new UniversalFilterTreeListener(refBook, result4));
        assertTrue(result4.toString().equals("LOWER(aAlias123.STRING_value) like 'any key'"));

        StringBuffer result5 = new StringBuffer();
        Filter.getFilterQuery("Alias123 like 'any key' and (1=1 OR 1=1)", new UniversalFilterTreeListener(refBook, result5));
        assertTrue(result5.toString().equals("aAlias123.STRING_value like 'any key' and(1 = 1 OR 1 = 1)"));
    }
}
