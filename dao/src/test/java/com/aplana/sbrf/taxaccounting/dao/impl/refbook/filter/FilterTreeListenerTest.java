package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;


import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
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
        PreparedStatementData result1 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123", new UniversalFilterTreeListener(refBook, result1));
        assertTrue(result1.getQuery().toString().equals("aAlias123.STRING_value = 123"));

        PreparedStatementData result2 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", new UniversalFilterTreeListener(refBook, result2));
        assertTrue(result2.getQuery().toString().equals("aAlias123.STRING_value = 123 and(aAlias123.STRING_value > 10 or aAlias123.STRING_value = 15)"));

        PreparedStatementData result3 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 is null", new UniversalFilterTreeListener(refBook, result3));
        assertTrue(result3.getQuery().toString().equals("aAlias123.STRING_value is null"));

        PreparedStatementData result4 = new PreparedStatementData();
        Filter.getFilterQuery("LOWER(Alias123) like 'any key'", new UniversalFilterTreeListener(refBook, result4));
        assertTrue(result4.getQuery().toString().equals("LOWER(aAlias123.STRING_value) like ?"));
        assertTrue(result4.getParams().size() == 1);
        assertTrue(result4.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result5 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any key' and (1=1 OR 1=1)", new UniversalFilterTreeListener(refBook, result5));
        assertTrue(result5.getQuery().toString().equals("aAlias123.STRING_value like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result5.getParams().size() == 1);
        assertTrue(result5.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result6 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any \\\'key' and (1=1 OR 1=1)", new UniversalFilterTreeListener(refBook, result6));
        assertTrue(result6.getQuery().toString().equals("aAlias123.STRING_value like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\\'key")));

        PreparedStatementData result7 = new PreparedStatementData();
        Filter.getFilterQuery("LOWER(Alias123) = LOWER('Организация')", new UniversalFilterTreeListener(refBook, result7));
        assertTrue(result7.getQuery().toString().equals("LOWER(aAlias123.STRING_value) = LOWER(?)"));
        assertTrue(result7.getParams().size() == 1);
        assertTrue(result7.getParams().get(0).equals(new String("Организация")));

        PreparedStatementData result8 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any \\\'key1' and Alias123 like 'any key2' and Alias123 like 'any key3'", new UniversalFilterTreeListener(refBook, result8));
        assertTrue(result8.getQuery().toString().equals("aAlias123.STRING_value like ? and aAlias123.STRING_value like ? and aAlias123.STRING_value like ?"));
        assertTrue(result8.getParams().size() == 3);
        assertTrue(result8.getParams().get(0).equals(new String("any \\\'key1")));
        assertTrue(result8.getParams().get(1).equals(new String("any key2")));
        assertTrue(result8.getParams().get(2).equals(new String("any key3")));
    }

    @Test
    public void SimpleFilterTreeListener(){
        PreparedStatementData result1 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123", new SimpleFilterTreeListener(refBook, result1));
        assertTrue(result1.getQuery().toString().equals("Alias123 = 123"));

        PreparedStatementData result2 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", new SimpleFilterTreeListener(refBook, result2));
        assertTrue(result2.getQuery().toString().equals("Alias123 = 123 and(Alias123 > 10 or Alias123 = 15)"));

        PreparedStatementData result3 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 is null", new SimpleFilterTreeListener(refBook, result3));
        assertTrue(result3.getQuery().toString().equals("Alias123 is null"));

        PreparedStatementData result4 = new PreparedStatementData();
        Filter.getFilterQuery("LOWER(Alias123) like 'any key'", new SimpleFilterTreeListener(refBook, result4));
        assertTrue(result4.getQuery().toString().equals("LOWER(Alias123) like ?"));
        assertTrue(result4.getParams().size() == 1);
        assertTrue(result4.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result5 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any key' and (1=1 OR 1=1)", new SimpleFilterTreeListener(refBook, result5));
        assertTrue(result5.getQuery().toString().equals("Alias123 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result5.getParams().size() == 1);
        assertTrue(result5.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result6 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any \\\'key' and (1=1 OR 1=1)", new SimpleFilterTreeListener(refBook, result6));
        assertTrue(result6.getQuery().toString().equals("Alias123 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\\'key")));

        PreparedStatementData result7 = new PreparedStatementData();
        Filter.getFilterQuery("LOWER(Alias123) = LOWER('Организация')", new SimpleFilterTreeListener(refBook, result7));
        assertTrue(result7.getQuery().toString().equals("LOWER(Alias123) = LOWER(?)"));
        assertTrue(result7.getParams().size() == 1);
        assertTrue(result7.getParams().get(0).equals(new String("Организация")));

        PreparedStatementData result8 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any \\\'key1' and Alias123 like 'any key2' and Alias123 like 'any key3'", new SimpleFilterTreeListener(refBook, result8));
        assertTrue(result8.getQuery().toString().equals("Alias123 like ? and Alias123 like ? and Alias123 like ?"));
        assertTrue(result8.getParams().size() == 3);
        assertTrue(result8.getParams().get(0).equals(new String("any \\\'key1")));
        assertTrue(result8.getParams().get(1).equals(new String("any key2")));
        assertTrue(result8.getParams().get(2).equals(new String("any key3")));
    }
}
