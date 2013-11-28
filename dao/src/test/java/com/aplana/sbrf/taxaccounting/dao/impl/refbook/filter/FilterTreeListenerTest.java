package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;


import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тест для фильтра данных справочника
 * @author auldanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "FilterTreeListenerTest.xml" })
public class FilterTreeListenerTest {
    @Autowired
    private ApplicationContext applicationContext;

    private RefBook refBook;

    UniversalFilterTreeListener universalFilterTreeListener;

    @Before
    public void init(){
        refBook =  mock(RefBook.class);
        RefBookAttribute attributeNumber = new RefBookAttribute();
        attributeNumber.setAttributeType(RefBookAttributeType.NUMBER);

        RefBookAttribute attributeString = new RefBookAttribute();
        attributeString.setAttributeType(RefBookAttributeType.STRING);
        when(refBook.getAttribute("Alias123")).thenReturn(attributeNumber);
        when(refBook.getAttribute("AliasStringType11")).thenReturn(attributeString);
    }

    @Test
    public void UniversalFilterTreeListener()
    {
        UniversalFilterTreeListener universalFilterTreeListener = applicationContext.getBean("universalFilterTreeListener", UniversalFilterTreeListener.class);
        universalFilterTreeListener.setRefBook(refBook);


        PreparedStatementData result1 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result1);
        Filter.getFilterQuery("Alias123 = 123", universalFilterTreeListener);
        assertTrue(result1.getQuery().toString().equals("aAlias123.NUMBER_value = 123"));

        PreparedStatementData result2 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result2);
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", universalFilterTreeListener);
        assertTrue(result2.getQuery().toString().equals("aAlias123.NUMBER_value = 123 and(aAlias123.NUMBER_value > 10 or aAlias123.NUMBER_value = 15)"));

        PreparedStatementData result3 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result3);
        Filter.getFilterQuery("Alias123 is null", universalFilterTreeListener);
        assertTrue(result3.getQuery().toString().equals("aAlias123.NUMBER_value is null"));

        PreparedStatementData result4 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result4);
        Filter.getFilterQuery("LOWER(AliasStringType11) like 'any key'", universalFilterTreeListener);
        assertTrue(result4.getQuery().toString().equals("LOWER(aAliasStringType11.STRING_value) like ?"));
        assertTrue(result4.getParams().size() == 1);
        assertTrue(result4.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result5 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result5);
        Filter.getFilterQuery("AliasStringType11 like 'any key' and (1=1 OR 1=1)", universalFilterTreeListener);
        assertTrue(result5.getQuery().toString().equals("aAliasStringType11.STRING_value like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result5.getParams().size() == 1);
        assertTrue(result5.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result6 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result6);
        Filter.getFilterQuery("AliasStringType11 like 'any \\\'key' and (1=1 OR 1=1)", universalFilterTreeListener);
        assertTrue(result6.getQuery().toString().equals("aAliasStringType11.STRING_value like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\\'key")));

        PreparedStatementData result7 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result7);
        Filter.getFilterQuery("LOWER(AliasStringType11) = LOWER('Организация')", universalFilterTreeListener);
        assertTrue(result7.getQuery().toString().equals("LOWER(aAliasStringType11.STRING_value) = LOWER(?)"));
        assertTrue(result7.getParams().size() == 1);
        assertTrue(result7.getParams().get(0).equals(new String("Организация")));

        PreparedStatementData result8 = new PreparedStatementData();
        universalFilterTreeListener.setPs(result8);
        Filter.getFilterQuery("AliasStringType11 like 'any \\\'key1' and AliasStringType11 like 'any key2' and AliasStringType11 like 'any key3'", universalFilterTreeListener);
        assertTrue(result8.getQuery().toString().equals("aAliasStringType11.STRING_value like ? and aAliasStringType11.STRING_value like ? and aAliasStringType11.STRING_value like ?"));
        assertTrue(result8.getParams().size() == 3);
        assertTrue(result8.getParams().get(0).equals(new String("any \\\'key1")));
        assertTrue(result8.getParams().get(1).equals(new String("any key2")));
        assertTrue(result8.getParams().get(2).equals(new String("any key3")));
    }

    @Test
    public void SimpleFilterTreeListener(){

        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);

        PreparedStatementData result1 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result1);
        Filter.getFilterQuery("Alias123 = 123", simpleFilterTreeListener);
        assertTrue(result1.getQuery().toString().equals("Alias123 = 123"));

        PreparedStatementData result2 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result2);
        Filter.getFilterQuery("Alias123 = 123 and (Alias123 > 10 or Alias123 = 15)", simpleFilterTreeListener);
        assertTrue(result2.getQuery().toString().equals("Alias123 = 123 and(Alias123 > 10 or Alias123 = 15)"));

        PreparedStatementData result3 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result3);
        Filter.getFilterQuery("Alias123 is null", simpleFilterTreeListener);
        assertTrue(result3.getQuery().toString().equals("Alias123 is null"));

        PreparedStatementData result4 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result4);
        Filter.getFilterQuery("LOWER(AliasStringType11) like 'any key'", simpleFilterTreeListener);
        assertTrue(result4.getQuery().toString().equals("LOWER(AliasStringType11) like ?"));
        assertTrue(result4.getParams().size() == 1);
        assertTrue(result4.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result5 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result5);
        Filter.getFilterQuery("AliasStringType11 like 'any key' and (1=1 OR 1=1)", simpleFilterTreeListener);
        assertTrue(result5.getQuery().toString().equals("AliasStringType11 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result5.getParams().size() == 1);
        assertTrue(result5.getParams().get(0).equals(new String("any key")));

        PreparedStatementData result6 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result6);
        Filter.getFilterQuery("AliasStringType11 like 'any \\\'key' and (1=1 OR 1=1)", simpleFilterTreeListener);
        assertTrue(result6.getQuery().toString().equals("AliasStringType11 like ? and(1 = 1 OR 1 = 1)"));
        assertTrue(result6.getParams().size() == 1);
        assertTrue(result6.getParams().get(0).equals(new String("any \\\'key")));

        PreparedStatementData result7 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result7);
        Filter.getFilterQuery("LOWER(AliasStringType11) = LOWER('Организация')", simpleFilterTreeListener);
        assertTrue(result7.getQuery().toString().equals("LOWER(AliasStringType11) = LOWER(?)"));
        assertTrue(result7.getParams().size() == 1);
        assertTrue(result7.getParams().get(0).equals(new String("Организация")));

        PreparedStatementData result8 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result8);
        Filter.getFilterQuery("AliasStringType11 like 'any \\\'key1' and AliasStringType11 like 'any key2' and AliasStringType11 like 'any key3'", simpleFilterTreeListener);
        assertTrue(result8.getQuery().toString().equals("AliasStringType11 like ? and AliasStringType11 like ? and AliasStringType11 like ?"));
        assertTrue(result8.getParams().size() == 3);
        assertTrue(result8.getParams().get(0).equals(new String("any \\\'key1")));
        assertTrue(result8.getParams().get(1).equals(new String("any key2")));
        assertTrue(result8.getParams().get(2).equals(new String("any key3")));
    }

    /**
     * Проверка исплючения если в
     * функцию оберуть не строковый параметр
     */
    @Test(expected = RuntimeException.class)
    public void mustThrowException(){
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);

        PreparedStatementData result = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result);
        Filter.getFilterQuery("LOWER(Alias123) like 'xx' ", simpleFilterTreeListener);

    }

    @Test(expected = RuntimeException.class)
    public void mustThrowException2(){
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);

        PreparedStatementData result2 = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result2);
        Filter.getFilterQuery("LENGTH(Alias123) like 'xx' ", simpleFilterTreeListener);
    }

    /**
     * Проверка типов в простых выражениях
     */
    @Test(expected = RuntimeException.class)
    public void simpleExpr(){
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);

        PreparedStatementData result = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result);
        Filter.getFilterQuery("Alias123 like 'any key1'", simpleFilterTreeListener);
    }

    @Test(expected = RuntimeException.class)
    public void simpleExpr2(){
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);

        PreparedStatementData result = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result);
        Filter.getFilterQuery("AliasStringType11 > 5", simpleFilterTreeListener);
    }

    /**
     * Проверка типов в сложных выражениях
     */
    @Test(expected = RuntimeException.class)
    public void complexExpr(){
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);

        PreparedStatementData result = new PreparedStatementData();
        simpleFilterTreeListener.setPs(result);
        Filter.getFilterQuery("LOWER(AliasStringType11) > 5", simpleFilterTreeListener);
    }
}

