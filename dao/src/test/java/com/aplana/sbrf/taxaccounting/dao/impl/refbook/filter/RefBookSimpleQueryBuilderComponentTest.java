package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.QueryBuilder;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookSimpleQueryBuilderComponentTest.xml" })
public class RefBookSimpleQueryBuilderComponentTest {

    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilderComponent;

    private RefBook mockedRefBook;

    private PagingParams mockedPagingParams;

    private RefBookAttribute mockedRefBookAttribute;

    private Date mockedDate;

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

    @Before
    public void setUp() {
        mockedRefBook = mock(RefBook.class);
        mockedPagingParams = mock(PagingParams.class);
        mockedRefBookAttribute = mock(RefBookAttribute.class);
        mockedDate = mock(Date.class);
    }

    @Test
    public void testGetColumnFilterQueryEmptySearchPatternEmptyFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilderComponent, columnsFilter, null, null);
        Assert.assertEquals("", result);
    }

    @Test
    public void testGetColumnFilterQueryEmptySearchPatternFilledFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilderComponent, columnsFilter, null, "id = 1");
        Assert.assertEquals("(id = 1)", result);
    }

    @Test
    public void testGetColumnFilterQueryFilledSearchPatternEmptyFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilderComponent, columnsFilter, "A", null);
        Assert.assertEquals("(lower(frb.test_column1) like '%a%' or lower(frb.test_column2) like '%a%')", result);
    }

    @Test
    public void testGetColumnFilterQueryFilledSearchPatternFilledFilter() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String result = (String) getColumnFilterQueryMethod.invoke(queryBuilderComponent, columnsFilter, "A", "id = 1");
        Assert.assertEquals("(id = 1) and (lower(frb.test_column1) like '%a%' or lower(frb.test_column2) like '%a%')", result);
    }

    @Test
    @Ignore
    public void test_AllRecordsByVersion_withEmptyFilter() {
        String filter = "";
        String direction = "asc";
        QueryBuilder q = queryBuilderComponent.allRecordsByVersion(mockedRefBook, mockedDate, filter, mockedPagingParams, mockedRefBookAttribute, direction);
        String expected = new String("SELECT * FROM (\n" +
                "SELECT r.*, row_number() over (order by null asc) as rn FROM (\n" +
                "SELECT p.*, p.version as record_version_from, (SELECT min(version) - interval '1' day FROM null WHERE status in (0,2) and record_id = p.record_id and version > p.version) as record_version_to \n" +
                "FROM ( \n" +
                " SELECT frb.* FROM null frb\n" +
                " WHERE frb.status = 0 and (:version is null or frb.version = (select max(version) from null where version <= :version and record_id = frb.record_id))\n" +
                " ) p\n" +
                ") r\n" +
                ") where rn between :paging_start and :paging_end").toLowerCase();
        Assert.assertNotNull(q);
        Assert.assertEquals(expected, q.getPagedQuery().toLowerCase());
    }

    @Test
    @Ignore
    public void test_AllRecordsByVersion_withFilledFilter() {
        String filter = "test filter";
        String direction = "asc";
        QueryBuilder q = queryBuilderComponent.allRecordsByVersion(mockedRefBook, mockedDate, filter, mockedPagingParams, mockedRefBookAttribute, direction);
        String expected = new String("SELECT * FROM (\n" +
                "SELECT r.*, row_number() over (order by null asc) as rn FROM (\n" +
                "SELECT p.*, p.version as record_version_from, (SELECT min(version) - interval '1' day FROM null WHERE status in (0,2) and record_id = p.record_id and version > p.version) as record_version_to \n" +
                "FROM ( \n" +
                " SELECT frb.* FROM null frb\n" +
                " WHERE frb.status = 0 and (:version is null or frb.version = (select max(version) from null where version <= :version and record_id = frb.record_id))\n" +
                " and (test filter)\n" +
                " ) p\n" +
                ") r\n" +
                ") where rn between :paging_start and :paging_end").toLowerCase();
        Assert.assertNotNull(q);
        Assert.assertEquals(expected, q.getPagedQuery().toLowerCase());
    }


    @Test
    public void test_AllRecords_withEmptyFilter() {
        String filter = "";
        String direction = "asc";
        QueryBuilder q = queryBuilderComponent.allRecords(mockedRefBook, filter, mockedPagingParams, mockedRefBookAttribute, direction);
        String expected = new String("SELECT * FROM (\n" +
                "SELECT r.*, row_number() over (order by null asc) as rn FROM (\n" +
                "SELECT frb.* from null frb\n" +
                " WHERE frb.status = 0) r\n" +
                ") WHERE rn between :paging_start and :paging_end").toLowerCase();
        Assert.assertNotNull(q);
        Assert.assertEquals(expected, q.getPagedQuery().toLowerCase());
    }

    @Test
    public void test_AllRecords_withFilledFilter() {
        String filter = "test filter";
        String direction = "asc";
        QueryBuilder q = queryBuilderComponent.allRecords(mockedRefBook, filter, mockedPagingParams, mockedRefBookAttribute, direction);
        String expected = new String("SELECT * FROM (\n" +
                "SELECT r.*, row_number() over (order by null asc) as rn FROM (\n" +
                "SELECT frb.* from null frb\n" +
                " WHERE frb.status = 0 and (test filter)\n" +
                ") r\n" +
                ") WHERE rn between :paging_start and :paging_end").toLowerCase();
        Assert.assertNotNull(q);
        Assert.assertEquals(expected, q.getPagedQuery().toLowerCase());
    }
}
