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
        Filter.getFilterQuery("Alias123 like 'any \\'key' and (1=1 OR 1=1)", new UniversalFilterTreeListener(refBook, result6));
        assertTrue(result6.getQuery().toString().equals("aAlias123.STRING_value like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\'key")));


        PreparedStatementData result7 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like '11' AND Alias123 LIKE '1234'", new UniversalFilterTreeListener(refBook, result7));
        assertTrue(result7.getQuery().toString().equals("aAlias123.STRING_value like ? AND aAlias123.STRING_value LIKE ?"));
        assertTrue(result7.getParams().size() == 2);
    }

    @Test
    public void DepartmentFilterTreeListener(){
        PreparedStatementData result1 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123", new DepartmentFilterTreeListener(refBook, result1));
        assertTrue(result1.getQuery().toString().equals("Alias123 = 123"));

        PreparedStatementData result2 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", new DepartmentFilterTreeListener(refBook, result2));
        assertTrue(result2.getQuery().toString().equals("Alias123 = 123 and(Alias123 > 10 or Alias123 = 15)"));

        PreparedStatementData result3 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 is null", new DepartmentFilterTreeListener(refBook, result3));
        assertTrue(result3.getQuery().toString().equals("Alias123 is null"));

        PreparedStatementData result4 = new PreparedStatementData();
        Filter.getFilterQuery("LOWER(Alias123) like 'any key'", new DepartmentFilterTreeListener(refBook, result4));
        assertTrue(result4.getQuery().toString().equals("LOWER(Alias123) like ?"));
        assertTrue(result4.getParams().size() == 1);
        assertTrue(result4.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result5 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any key' and (1=1 OR 1=1)", new DepartmentFilterTreeListener(refBook, result5));
        assertTrue(result5.getQuery().toString().equals("Alias123 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result5.getParams().size() == 1);
        assertTrue(result5.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result6 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any \\'key' and (1=1 OR 1=1)", new DepartmentFilterTreeListener(refBook, result6));
        assertTrue(result6.getQuery().toString().equals("Alias123 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\'key")));

        PreparedStatementData result7 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like '11' AND Alias123 LIKE '1234'", new DepartmentFilterTreeListener(refBook, result7));
        assertTrue(result7.getQuery().toString().equals("Alias123 like ? AND Alias123 LIKE ?"));
        assertTrue(result7.getParams().size() == 2);
    }

    @Test
    public void BookerStatementsFilterTreeListener(){
        PreparedStatementData result1 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123", new BookerStatementsFilterTreeListener(refBook, result1));
        assertTrue(result1.getQuery().toString().equals("Alias123 = 123"));

        PreparedStatementData result2 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", new BookerStatementsFilterTreeListener(refBook, result2));
        assertTrue(result2.getQuery().toString().equals("Alias123 = 123 and(Alias123 > 10 or Alias123 = 15)"));

        PreparedStatementData result3 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 is null", new BookerStatementsFilterTreeListener(refBook, result3));
        assertTrue(result3.getQuery().toString().equals("Alias123 is null"));

        PreparedStatementData result4 = new PreparedStatementData();
        Filter.getFilterQuery("LOWER(Alias123) like 'any key'", new BookerStatementsFilterTreeListener(refBook, result4));
        assertTrue(result4.getQuery().toString().equals("LOWER(Alias123) like ?"));
        assertTrue(result4.getParams().size() == 1);
        assertTrue(result4.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result5 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any key' and (1=1 OR 1=1)", new BookerStatementsFilterTreeListener(refBook, result5));
        assertTrue(result5.getQuery().toString().equals("Alias123 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result5.getParams().size() == 1);
        assertTrue(result5.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result6 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like 'any \\'key' and (1=1 OR 1=1)", new BookerStatementsFilterTreeListener(refBook, result6));
        assertTrue(result6.getQuery().toString().equals("Alias123 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\'key")));

        PreparedStatementData result7 = new PreparedStatementData();
        Filter.getFilterQuery("Alias123 like '11' AND Alias123 LIKE '1234'", new BookerStatementsFilterTreeListener(refBook, result7));
        assertTrue(result7.getQuery().toString().equals("Alias123 like ? AND Alias123 LIKE ?"));
        assertTrue(result7.getParams().size() == 2);
    }
}
