package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * User: lhaziev
 */
public class DataRowServiceImplTest {
/*
    private static DataRowService dataRowService = new DataRowServiceImpl();
    private static DataRowDao dataRowDao = Mockito.mock(DataRowDao.class);
    private static FormData formData;
    private static List<Column> columnList;

    private static final String ALIAS_1 = "column1";
    private static final String ALIAS_2 = "column2";
    private static final String ALIAS_3 = "column3";
    private static final String ALIAS_4 = "column4";
    private static final String ALIAS_5 = "column5";

    @BeforeClass
    public static void init() {
        RefBookHelper refBookHelper = Mockito.mock(RefBookHelper.class);
        ReflectionTestUtils.setField(dataRowService, "refBookHelper", refBookHelper);

        formData = new FormData();
        formData.setId(1L);
        formData.setManual(false);

        columnList = new LinkedList<Column>();
        Column column1 = new StringColumn();
        Column column2 = new NumericColumn();
        Column column3 = new DateColumn();
        Column column4 = new RefBookColumn();
        ReferenceColumn column5 = new ReferenceColumn();
        column1.setAlias(ALIAS_1);
        column1.setId(1);
        column1.setOrder(1);
        column2.setAlias(ALIAS_2);
        column2.setId(2);
        column2.setOrder(1);
        column3.setAlias(ALIAS_3);
        column3.setId(3);
        column3.setOrder(3);
        column4.setAlias(ALIAS_4);
        column4.setId(4);
        column4.setOrder(4);
        column5.setAlias(ALIAS_5);
        column5.setId(5);
        column5.setParentId(4);
        column5.setOrder(5);
        columnList.addAll(Arrays.asList(column1, column2, column3, column4, column5));
        formData.setFormColumns(columnList);

        FormDataDao formDataDao = Mockito.mock(FormDataDao.class);
        ReflectionTestUtils.setField(dataRowService, "formDataDao", formDataDao);
        when(formDataDao.get(formData.getId(), formData.isManual())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                formData.setFormColumns(columnList);
                return formData;
            }
        });

        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> row1 = formData.createDataRow();
        row1.setIndex(1);
        row1.getCell(ALIAS_1).setStringValue("row keeyyy");
        row1.getCell(ALIAS_2).setNumericValue(new BigDecimal(123.45));
        row1.getCell(ALIAS_3).setDateValue(new Date());
        row1.getCell(ALIAS_4).setRefBookDereference("ref keeyyy");
        row1.getCell(ALIAS_5).setRefBookDereference("ref2 keeyyy");
        rows.add(row1);

        DataRow<Cell> row2 = formData.createDataRow();
        row2.setIndex(2);
        row2.getCell(ALIAS_1).setStringValue("row 2");
        row2.getCell(ALIAS_2).setNumericValue(new BigDecimal(0.00));
        row2.getCell(ALIAS_3).setDateValue(new Date());
        row2.getCell(ALIAS_4).setRefBookDereference("row keeYYyy");
        row2.getCell(ALIAS_5).setRefBookDereference("row keeYyy");
        rows.add(row2);

        ReflectionTestUtils.setField(dataRowService, "dataRowDao", dataRowDao);
        when(dataRowDao.getRows(formData, null)).thenReturn(rows);
    }

    @Test
    public void test(){
        String key = "keeyyy";

        DataRowRange range = new DataRowRange();
        range.setOffset(1);
        range.setCount(5);

        when(dataRowDao.searchByKey(formData.getId(), range, key, true, formData.isManual())).thenReturn(new PagingResult<FormDataSearchResult>());

        PagingResult<FormDataSearchResult> results = dataRowService.searchByKey(formData.getId(), range, key, true, false);
        Assert.assertEquals(2, results.size());

        Assert.assertEquals(new Long(1), results.get(0).getIndex());
        Assert.assertEquals("ref keeyyy", results.get(0).getStringFound());
        Assert.assertEquals(new Long(1), results.get(0).getRowIndex());
        Assert.assertEquals(new Long(4), results.get(0).getColumnIndex());

        Assert.assertEquals(new Long(2), results.get(1).getIndex());
        Assert.assertEquals("ref2 keeyyy", results.get(1).getStringFound());
        Assert.assertEquals(new Long(1), results.get(1).getRowIndex());
        Assert.assertEquals(new Long(5), results.get(1).getColumnIndex());
    }

    @Test
    public void test2(){
        String key = "keeyyy";

        DataRowRange range = new DataRowRange();
        range.setOffset(1);
        range.setCount(10);

        when(dataRowDao.searchByKey(formData.getId(), range, key, false, formData.isManual())).thenReturn(new PagingResult<FormDataSearchResult>());
        PagingResult<FormDataSearchResult>  results = dataRowService.searchByKey(formData.getId(), range, key, false, false);
        Assert.assertEquals(4, results.size());

        Assert.assertEquals(new Long(1), results.get(0).getIndex());
        Assert.assertEquals("ref keeyyy", results.get(0).getStringFound());
        Assert.assertEquals(new Long(1), results.get(0).getRowIndex());
        Assert.assertEquals(new Long(4), results.get(0).getColumnIndex());

        Assert.assertEquals(new Long(2), results.get(1).getIndex());
        Assert.assertEquals("ref2 keeyyy", results.get(1).getStringFound());
        Assert.assertEquals(new Long(1), results.get(1).getRowIndex());
        Assert.assertEquals(new Long(5), results.get(1).getColumnIndex());

        Assert.assertEquals(new Long(3), results.get(2).getIndex());
        Assert.assertEquals("row keeYYyy", results.get(2).getStringFound());
        Assert.assertEquals(new Long(2), results.get(2).getRowIndex());
        Assert.assertEquals(new Long(4), results.get(2).getColumnIndex());

        Assert.assertEquals(new Long(4), results.get(3).getIndex());
        Assert.assertEquals("row keeYyy", results.get(3).getStringFound());
        Assert.assertEquals(new Long(2), results.get(3).getRowIndex());
        Assert.assertEquals(new Long(5), results.get(3).getColumnIndex());

        range = new DataRowRange();
        range.setOffset(3);
        range.setCount(4);

        when(dataRowDao.searchByKey(formData.getId(), range, key, false, formData.isManual())).thenReturn(new PagingResult<FormDataSearchResult>());
        results = dataRowService.searchByKey(formData.getId(), range, key, false, false);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(new Long(3), results.get(0).getIndex());
        Assert.assertEquals("row keeYYyy", results.get(0).getStringFound());
        Assert.assertEquals(new Long(2), results.get(0).getRowIndex());
        Assert.assertEquals(new Long(4), results.get(0).getColumnIndex());

        Assert.assertEquals(new Long(4), results.get(1).getIndex());
        Assert.assertEquals("row keeYyy", results.get(1).getStringFound());
        Assert.assertEquals(new Long(2), results.get(1).getRowIndex());
        Assert.assertEquals(new Long(5), results.get(1).getColumnIndex());

    }

    @Test
    public void test3(){
        String key = "keeyyy";

        DataRowRange range = new DataRowRange();
        range.setOffset(1);
        range.setCount(10);

        FormDataSearchResult formDataSearchResult = new FormDataSearchResult();
        formDataSearchResult.setIndex(1L);
        formDataSearchResult.setStringFound("row keeyyy");
        formDataSearchResult.setRowIndex(2L);
        formDataSearchResult.setColumnIndex(1L);

        PagingResult<FormDataSearchResult> pagingResult = new PagingResult<FormDataSearchResult>();
        pagingResult.setTotalCount(1);
        pagingResult.add(formDataSearchResult);

        when(dataRowDao.searchByKey(formData.getId(), range, key, false, formData.isManual())).thenReturn(pagingResult);

        PagingResult<FormDataSearchResult> results = dataRowService.searchByKey(formData.getId(), range, key, false, formData.isManual());
        Assert.assertEquals(5, results.size());

        Assert.assertEquals(new Long(1), results.get(0).getIndex());
        Assert.assertEquals("ref keeyyy", results.get(0).getStringFound());

        Assert.assertEquals(new Long(2), results.get(1).getIndex());
        Assert.assertEquals("ref2 keeyyy", results.get(1).getStringFound());

        Assert.assertEquals(new Long(3), results.get(2).getIndex());
        Assert.assertEquals("row keeyyy", results.get(2).getStringFound());

        Assert.assertEquals(new Long(4), results.get(3).getIndex());
        Assert.assertEquals("row keeYYyy", results.get(3).getStringFound());

        Assert.assertEquals(new Long(5), results.get(4).getIndex());
        Assert.assertEquals("row keeYyy", results.get(4).getStringFound());
    }*/
}
