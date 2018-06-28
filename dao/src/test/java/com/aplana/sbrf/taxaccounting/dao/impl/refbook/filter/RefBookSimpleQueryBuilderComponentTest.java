package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookSimpleQueryBuilderComponentTest.xml" })
public class RefBookSimpleQueryBuilderComponentTest {

    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;

    private static List<String> columnsFilter;

    private static Method getColumnFilterQueryMethod;

    @BeforeClass
    public static void initClass() throws NoSuchMethodException {
        columnsFilter = new ArrayList<>();
        columnsFilter.add("test_column1");
        columnsFilter.add("test_column2");
        Class<? extends RefBookSimpleQueryBuilderComponent> clazz = RefBookSimpleQueryBuilderComponent.class;
        getColumnFilterQueryMethod = clazz.getDeclaredMethod("getColumnFilterQuery", List.class, String.class, String.class);
        getColumnFilterQueryMethod.setAccessible(true);
    }

    @Test
    public void testGetColumnFilterQueryEmptySearchPatternEmptyFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilder, columnsFilter, null, null);
        Assert.assertEquals("", result);
    }

    @Test
    public void testGetColumnFilterQueryEmptySearchPatternFilledFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilder, columnsFilter, null, "id = 1");
        Assert.assertEquals("(id = 1)", result);
    }

    @Test
    public void testGetColumnFilterQueryFilledSearchPatternEmptyFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilder, columnsFilter, "A", null);
        Assert.assertEquals("(lower(frb.test_column1) like '%a%' or lower(frb.test_column2) like '%a%')", result);
    }

    @Test
    public void testGetColumnFilterQueryFilledSearchPatternFilledFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilder, columnsFilter, "A", "id = 1");
        Assert.assertEquals("(id = 1) and (lower(frb.test_column1) like '%a%' or lower(frb.test_column2) like '%a%')", result);
    }
}
