package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.when;

/**
 * User: lhaziev
 */
public class DataRowServiceImplTest {
    private static DataRowService dataRowService = new DataRowServiceImpl();
    private static DataRowDao dataRowDao = Mockito.mock(DataRowDao.class);
    private static FormData formData;
    private static PagingResult<FormDataSearchResult> daoResult;

    private static final String ALIAS_1 = "column1";
    private static final String ALIAS_2 = "column2";
    private static final String ALIAS_3 = "column3";
    private static final String ALIAS_4 = "column4";
    private static final String ALIAS_5 = "column5";
    private static final int row_nums = 2;
    private static final int col_nums = 5;

    private static Object[][] table = {
            {"row keeyyy",  new BigDecimal(123.45), new Date(), "ref keeyyy",   "ref2 keeyyy"},
            {"row 2",       new BigDecimal(0.00),   new Date(), "row keeYYyy",  "row keeYyy"}
    };

    @BeforeClass
    public static void init() {
        RefBookHelper refBookHelper = Mockito.mock(RefBookHelper.class);
        ReflectionTestUtils.setField(dataRowService, "refBookHelper", refBookHelper);

        formData = new FormData();
        formData.setId(1L);
        formData.setManual(false);

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        formTemplate.setName("template1");

        for (int i = 1; i <= col_nums; i++) {
            Column column;
            switch (i) {
                case 1: column = new StringColumn(); break;
                case 2: column = new NumericColumn(); break;
                case 3: column = new DateColumn(); break;
                case 4: column = new RefBookColumn(); break;
                default: column = new ReferenceColumn(); break;
            }
            column.setAlias("column" + i);
            column.setId(i);
            column.setOrder(i);
            formTemplate.addColumn(column);
        }
        formData.initFormTemplateParams(formTemplate);

        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> row;
        for (int i = 0; i < row_nums; i++) {
            row = formData.createDataRow();
            row.setIndex(i + 1);
            row.getCell(ALIAS_1).setStringValue((String) table[i][0]);
            row.getCell(ALIAS_2).setNumericValue((BigDecimal) table[i][1]);
            row.getCell(ALIAS_3).setDateValue((Date) table[i][2]);
            row.getCell(ALIAS_4).setRefBookDereference((String) table[i][3]);
            row.getCell(ALIAS_5).setRefBookDereference((String) table[i][4]);
            rows.add(row);
        }

        FormDataDao formDataDao = Mockito.mock(FormDataDao.class);
        ReflectionTestUtils.setField(dataRowService, "formDataDao", formDataDao);
        when(formDataDao.get(formData.getId(), formData.isManual())).thenReturn(formData);

        ReflectionTestUtils.setField(dataRowService, "dataRowDao", dataRowDao);
        when(dataRowDao.getRowsRefColumnsOnly(formData, null, false)).thenReturn(rows);

        daoResult = new PagingResult<FormDataSearchResult>();
        FormDataSearchResult daoRow;
        daoRow = new FormDataSearchResult();
        daoRow.setRowIndex(1L);
        daoRow.setIndex(1L);
        daoRow.setColumnIndex(1L);
        daoRow.setStringFound("row keeyyy");
        daoResult.add(daoRow);
    }

    @Test
    public void test(){
        String key = "keeyyy";
        List<FormDataSearchResult> results = dataRowService.searchByKeyInRefColumns(formData, key, true, false);
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
}
