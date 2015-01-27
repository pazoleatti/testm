package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DiffService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class DiffServiceTest {
    private final DiffService diffService = new DiffServiceImpl();

    private static final String ALIAS_1 = "column1";
    private static final String ALIAS_2 = "column2";
    private static final String ALIAS_3 = "column3";
    private static final String ALIAS_4 = "column4";
    private static final String ALIAS_5 = "column5";

    @Before
    public void init() {
        ReflectionTestUtils.setField(diffService, "refBookHelper", mock(RefBookHelper.class));
    }

    @Test
    public void computeDiffSimpleTest() throws IOException {
        // Нет изменений
        List<Diff> diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "BBB", "CCC"));
        Assert.assertTrue(diffList.isEmpty());
        // Удалена строка
        diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "BBB"));
        Assert.assertEquals(1, diffList.size());
        Diff diff = diffList.get(0);
        Assert.assertEquals(DiffType.DELETE, diff.getDiffType());
        Assert.assertNull(diff.getRevisedRowNumber());
        Assert.assertNotNull(diff.getOriginalRowNumber());
        Assert.assertEquals(2, diff.getOriginalRowNumber().intValue());
        // Добавлена строка
        diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "BBB", "CCC", "DDD"));
        Assert.assertEquals(1, diffList.size());
        diff = diffList.get(0);
        Assert.assertEquals(DiffType.INSERT, diff.getDiffType());
        Assert.assertNotNull(diff.getRevisedRowNumber());
        Assert.assertNull(diff.getOriginalRowNumber());
        Assert.assertEquals(3, diff.getRevisedRowNumber().intValue());
        // Изменена строка
        diffList = diffService.computeDiff(Arrays.asList("AAA", "BBB", "CCC"), Arrays.asList("AAA", "DDD", "CCC"));
        Assert.assertEquals(1, diffList.size());
        diff = diffList.get(0);
        Assert.assertEquals(DiffType.CHANGE, diff.getDiffType());
        Assert.assertNotNull(diff.getOriginalRowNumber());
        Assert.assertNotNull(diff.getRevisedRowNumber());
        Assert.assertEquals(1, diff.getOriginalRowNumber().intValue());
        Assert.assertEquals(1, diff.getRevisedRowNumber().intValue());
    }

    @Test
    public void computeDiffComplexTest() throws IOException {
        List<Diff> diffList = diffService.computeDiff(streamToLines(getOriginalInputStream()),
                streamToLines(getRevisedInputStream()));
        Assert.assertEquals(8, diffList.size());
        DiffType[] diffTypes = new DiffType[]{DiffType.CHANGE, DiffType.INSERT, DiffType.CHANGE, DiffType.CHANGE,
                DiffType.DELETE, DiffType.DELETE, DiffType.INSERT, DiffType.INSERT};
        for (int i = 0; i < diffList.size(); i++) {
            Assert.assertEquals(diffTypes[i], diffList.get(i).getDiffType());
        }
    }

    @Test
    public void getMergedOrderTest() {
        List<Diff> diffList;
        List<Pair<Integer, Integer>> pairList;
        diffList = Arrays.asList(new Diff(0, 0, DiffType.CHANGE));
        pairList = diffService.getMergedOrder(diffList, 1);
        Assert.assertEquals(1, pairList.size());
        Assert.assertEquals(0, pairList.get(0).getFirst().intValue());
        Assert.assertEquals(0, pairList.get(0).getSecond().intValue());

        diffList = Arrays.asList(new Diff(1, 1, DiffType.CHANGE));
        pairList = diffService.getMergedOrder(diffList, 3);
        Assert.assertEquals(3, pairList.size());
        Assert.assertEquals(0, pairList.get(0).getFirst().intValue());
        Assert.assertEquals(0, pairList.get(0).getSecond().intValue());
        Assert.assertEquals(1, pairList.get(1).getFirst().intValue());
        Assert.assertEquals(1, pairList.get(1).getSecond().intValue());
        Assert.assertEquals(2, pairList.get(2).getFirst().intValue());
        Assert.assertEquals(2, pairList.get(2).getSecond().intValue());

        diffList = Arrays.asList(new Diff(null, 1, DiffType.INSERT));
        pairList = diffService.getMergedOrder(diffList, 3);
        Assert.assertEquals(3, pairList.size());
        Assert.assertEquals(0, pairList.get(0).getFirst().intValue());
        Assert.assertEquals(0, pairList.get(0).getSecond().intValue());
        Assert.assertNull(pairList.get(1).getFirst());
        Assert.assertEquals(1, pairList.get(1).getSecond().intValue());
        Assert.assertEquals(1, pairList.get(2).getFirst().intValue());
        Assert.assertEquals(2, pairList.get(2).getSecond().intValue());

        diffList = Arrays.asList(new Diff(1, 1, DiffType.CHANGE), new Diff(null, 2, DiffType.INSERT),
                new Diff(4, 5, DiffType.CHANGE), new Diff(5, 6, DiffType.CHANGE), new Diff(6, null, DiffType.DELETE),
                new Diff(12, null, DiffType.DELETE), new Diff(null, 13, DiffType.INSERT), new Diff(null, 14, DiffType.INSERT));
        pairList = diffService.getMergedOrder(diffList, 14);
        Assert.assertEquals(17, pairList.size());
        Assert.assertEquals(0, pairList.get(0).getFirst().intValue());
        Assert.assertEquals(0, pairList.get(0).getSecond().intValue());
        Assert.assertEquals(4, pairList.get(5).getFirst().intValue());
        Assert.assertEquals(5, pairList.get(5).getSecond().intValue());
        Assert.assertNull(pairList.get(16).getFirst());
        Assert.assertEquals(14, pairList.get(16).getSecond().intValue());
    }

    private static List<String> streamToLines(InputStream inputStream) throws IOException {
        List<String> lines = new LinkedList<String>();
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private static InputStream getOriginalInputStream() {
        return getInputStream("com/aplana/sbrf/taxaccounting/service/impl/diff_original.csv");
    }

    private static InputStream getRevisedInputStream() {
        return getInputStream("com/aplana/sbrf/taxaccounting/service/impl/diff_revised.csv");
    }

    private static InputStream getInputStream(String path) {
        return DiffServiceTest.class.getClassLoader().getResourceAsStream(path);
    }

    @Test
    public void getRowAsStringTest() {
        List<Column> columnList = getColumnList();
        DataRow<Cell> dataRow = new DataRow<Cell>(FormDataUtils.createCells(columnList, null));

        // Null-значения
        String string = diffService.getRowAsString(dataRow);
        Assert.assertEquals(";;;;;", string);
        // Заполненные значения
        Date date = new Date();
        dataRow.getCell(ALIAS_1).setStringValue("str1");
        dataRow.getCell(ALIAS_2).setNumericValue(BigDecimal.valueOf(1));
        dataRow.getCell(ALIAS_3).setDateValue(date);
        dataRow.getCell(ALIAS_4).setNumericValue(BigDecimal.valueOf(1));
        dataRow.getCell(ALIAS_5).setNumericValue(BigDecimal.valueOf(1));
        string = diffService.getRowAsString(dataRow);
        Assert.assertEquals("str1;1;" + date + ";1;;", string);
    }

    @Test
    public void getDiffTest() {
        List<FormStyle> formStyleList = new LinkedList<FormStyle>();
        FormStyle formStyle = new FormStyle();
        formStyle.setAlias(DiffService.STYLE_CHANGE);
        formStyleList.add(formStyle);
        formStyle = new FormStyle();
        formStyle.setAlias(DiffService.STYLE_NO_CHANGE);
        formStyleList.add(formStyle);
        formStyle = new FormStyle();
        formStyle.setAlias(DiffService.STYLE_INSERT);
        formStyleList.add(formStyle);
        formStyle = new FormStyle();
        formStyle.setAlias(DiffService.STYLE_DELETE);
        formStyleList.add(formStyle);
        List<Column> columnList = getColumnList();
        DataRow<Cell> dataRow1 = new DataRow<Cell>(FormDataUtils.createCells(columnList, formStyleList));
        DataRow<Cell> dataRow2 = new DataRow<Cell>(FormDataUtils.createCells(columnList, formStyleList));
        Date date = new Date();
        dataRow1.getCell(ALIAS_1).setStringValue("str1");
        dataRow1.getCell(ALIAS_2).setNumericValue(BigDecimal.valueOf(1));
        dataRow1.getCell(ALIAS_3).setDateValue(date);
        dataRow1.getCell(ALIAS_4).setNumericValue(BigDecimal.valueOf(1));
        dataRow1.getCell(ALIAS_4).setRefBookDereference("A");
        dataRow1.getCell(ALIAS_5).setRefBookDereference("B");
        dataRow2.getCell(ALIAS_1).setStringValue("str1");
        dataRow2.getCell(ALIAS_2).setNumericValue(null);
        dataRow2.getCell(ALIAS_4).setNumericValue(BigDecimal.valueOf(2));
        dataRow1.getCell(ALIAS_4).setRefBookDereference("AA");
        dataRow2.getCell(ALIAS_5).setRefBookDereference("BB");

        List<DataRow<Cell>> diffList = diffService.getDiff(Arrays.asList(dataRow1), Arrays.asList(dataRow2));
        Assert.assertEquals(1, diffList.size());
        DataRow<Cell> dataRow = diffList.get(0);
        Assert.assertEquals("str1", dataRow.get(ALIAS_1));
        Assert.assertEquals(BigDecimal.valueOf(-1), dataRow.get(ALIAS_2));
        Assert.assertNull(dataRow.get(ALIAS_3));
        Assert.assertEquals(BigDecimal.valueOf(2).longValue(), dataRow.get(ALIAS_4));
        Assert.assertEquals(null, dataRow.get(ALIAS_5));
        // Проверка стилей
        List<String> styleList = new LinkedList<String>();
        for (String key : dataRow.keySet()) {
            Cell cell = dataRow.getCell(key);
            styleList.add(cell.getStyleAlias());
        }
        Assert.assertEquals(DiffService.STYLE_NO_CHANGE, styleList.get(0));
        Assert.assertEquals(DiffService.STYLE_CHANGE, styleList.get(1));
        Assert.assertEquals(DiffService.STYLE_CHANGE, styleList.get(2));
        Assert.assertEquals(DiffService.STYLE_CHANGE, styleList.get(3));
        Assert.assertEquals(DiffService.STYLE_CHANGE, styleList.get(4));
    }

    // Тестовые графы
    private List<Column> getColumnList() {
        List<Column> columnList = new LinkedList<Column>();
        Column column1 = new StringColumn();
        Column column2 = new NumericColumn();
        Column column3 = new DateColumn();
        Column column4 = new RefBookColumn();
		ReferenceColumn column5 = new ReferenceColumn();
        column1.setAlias(ALIAS_1);
		column1.setId(1);
        column2.setAlias(ALIAS_2);
		column2.setId(2);
        column3.setAlias(ALIAS_3);
		column3.setId(3);
        column4.setAlias(ALIAS_4);
		column4.setId(4);
        column5.setAlias(ALIAS_5);
		column5.setId(5);
		column5.setParentId(4);
        columnList.addAll(Arrays.asList(column1, column2, column3, column4, column5));
        return columnList;
    }
}
