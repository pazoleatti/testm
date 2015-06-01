package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange;
import com.aplana.sbrf.taxaccounting.model.script.range.Range;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.script.RefBookService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.mock;

/**
 * Тесты для ScriptUtils
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 28.01.13 14:31
 */
public class ScriptUtilsTest {

    private static final Log logger = LogFactory.getLog(ScriptUtilsTest.class);

    private static final String STRING_NAME = "Строка";
    private static final String STRING_ALIAS = "string";
    private static final String NUMBER_NAME = "Число";
    private static final String NUMBER_ALIAS = "number";
    private static final String DATE_NAME = "Дата";
    private static final String DATE_ALIAS = "date";
    private static final String ROW1_ALIAS = "book";
    private static final String ROW2_ALIAS = "pencil";
    private static final String ROW3_ALIAS = "sampleRowAlias";
    private static final Date DATE_CONST = new Date();

    /**
     * Возвращает таблицу с тестовыми данными
     *
     * @return
     */
    private Pair<FormData, List<DataRow<Cell>>> getTestFormData() {
        FormTemplate temp = new FormTemplate();
        temp.setId(1);
        Column strColumn = new StringColumn();
        strColumn.setName(STRING_NAME);
        strColumn.setAlias(STRING_ALIAS);
        NumericColumn numColumn = new NumericColumn();
        numColumn.setName(NUMBER_NAME);
        numColumn.setAlias(NUMBER_ALIAS);
        numColumn.setPrecision(4);
        Column dateColumn = new DateColumn();
        dateColumn.setName(DATE_NAME);
        dateColumn.setAlias(DATE_ALIAS);
        temp.getColumns().addAll(Arrays.asList(new Column[]{strColumn, numColumn, dateColumn}));

        FormData fd = new FormData(temp);

        DataRow row1 = fd.createDataRow();
        row1.setAlias(ROW1_ALIAS);
        row1.getCell(STRING_ALIAS).setValue("книга", null);
        row1.getCell(NUMBER_ALIAS).setValue(1.04, null);
        row1.getCell(DATE_ALIAS).setValue(DATE_CONST, null);

        DataRow row2 = fd.createDataRow();
        row2.setAlias(ROW2_ALIAS);
        row2.getCell(STRING_ALIAS).setValue("карандаш", null);
        row2.getCell(NUMBER_ALIAS).setValue(2.1, null);

        DataRow row3 = fd.createDataRow();
        row3.setAlias(ROW3_ALIAS);
        row3.getCell(STRING_ALIAS).setValue("блокнот", null);
        row3.getCell(DATE_ALIAS).setValue(DATE_CONST, null);

        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
        dataRows.add(row1);
        dataRows.add(row2);
        dataRows.add(row3);

        return new Pair<FormData, List<DataRow<Cell>>>(fd, dataRows);
    }

    @Test
    public void summTest() {
        FormData fd = getTestFormData().getFirst();
        logger.info(fd);
        double r = ScriptUtils.summ(fd, getTestFormData().getSecond(), new ColumnRange(NUMBER_ALIAS, 0, 1));
        Assert.assertTrue(Math.abs(r) > Constants.EPS);
    }

    @Test
    public void roundTest1() {
        Assert.assertEquals(3.123, ScriptUtils.round(3.12345, 3), Constants.EPS);
    }

    @Test
    public void roundTest2() {
        Assert.assertEquals(2, ScriptUtils.round(1.5, 0), Constants.EPS);
    }

    @Test
    public void checkNumericColumnsTest1() {
        ScriptUtils.checkNumericColumns(getTestFormData().getFirst(), getTestFormData().getSecond(), new Range(NUMBER_ALIAS, 0, NUMBER_ALIAS, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNumericColumnsTest2() {
        ScriptUtils.checkNumericColumns(getTestFormData().getFirst(), getTestFormData().getSecond(), new Range(STRING_ALIAS, 0, NUMBER_ALIAS, 2));
    }

    @Test
    public void parseNumberTest1() {
        String str1 = "-0.999934534534543654565464576657";
        String str2 = " -  0,999 934 534 534543654565464576657 ";
        String str3 = "0.0.1";
        String str4 = "  000AA";
        String str5 = " 99 9 9 00";
        String str6 = "";
        double d1 = -0.9999345345345436d;
        long l1 = 999900L;

        Assert.assertEquals(d1, ScriptUtils.parseNumber(str1, 1, 1, null, true).doubleValue(), 0);
        Assert.assertEquals(d1, ScriptUtils.parseNumber(str1, 1, 1, null, false).doubleValue(), 0);
        Assert.assertEquals(d1, ScriptUtils.parseNumber(str2, 1, 1, null, true).doubleValue(), 0);
        Assert.assertEquals(d1, ScriptUtils.parseNumber(str2, 1, 1, null, false).doubleValue(), 0);
        Assert.assertNull(ScriptUtils.parseNumber(str3, 1, 1, null, false));
        Assert.assertNull(ScriptUtils.parseNumber(str4, 1, 1, null, false));
        Assert.assertEquals(l1, ScriptUtils.parseNumber(str5, 1, 1, null, false).longValue());
        Assert.assertNull(ScriptUtils.parseNumber(str6, 1, 1, null, true));
        Assert.assertNull(ScriptUtils.parseNumber(str6, 1, 1, null, false));
    }

    @Test
    public void parseNumberTest2() {
        Logger logger = new Logger();
        Assert.assertNull(ScriptUtils.parseNumber("0.0.1", 1, 1, logger, true));
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
    }

    @Test
    public void parseNumberTest3() {
        Logger logger = new Logger();
        Assert.assertNull(ScriptUtils.parseNumber("  000AA", 1, 1, logger, true));
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
    }

    @Test
    public void parseDate1() {
        String str1 = "05.02.2009";
        String str2 = " 05.02.2009 ";
        Date date1 = new Date(2009 - 1900, 2 - 1, 5);
        String format = "dd.MM.yyyy";
        Assert.assertEquals(date1, ScriptUtils.parseDate(str1, format, 1, 1, null, true));
        Assert.assertEquals(date1, ScriptUtils.parseDate(str1, format, 1, 1, null, false));
        Assert.assertEquals(date1, ScriptUtils.parseDate(str2, format, 1, 1, null, true));
        Assert.assertEquals(date1, ScriptUtils.parseDate(str2, format, 1, 1, null, false));
    }

    @Test
    public void parseDate2() {
        Logger logger = new Logger();
        Assert.assertNull(ScriptUtils.parseDate("Hello", "dd.MM.yyyy", 1, 1, logger, true));
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
    }

    @Test
    public void parseDate3() {
        Date date = new Date(2009 - 1900, 0, 1);
        Assert.assertEquals(date, ScriptUtils.parseDate("01.02.2009", "yyyy", 1, 1, null, true));
        Assert.assertEquals(date, ScriptUtils.parseDate("2009", "dd.MM.yyyy", 1, 1, null, true));
        Assert.assertEquals(date, ScriptUtils.parseDate("2009", "yyyy", 1, 1, null, true));
    }

    @Test
    public void normalizeTest() {
        String str1 = null;
        String str2 = "  ";
        String str3 = "x";//икс
        String str4 = "  х  ";//х
        String str5 = "a  b  c   d";
        String str6 = "  a  b  c ч  d";
        String str7 = "  a\n  b  c ч  d";

        Assert.assertEquals("", ScriptUtils.normalize(str1));
        Assert.assertEquals("", ScriptUtils.normalize(str2));
        Assert.assertEquals("", ScriptUtils.normalize(str3));
        Assert.assertEquals("", ScriptUtils.normalize(str4));
        Assert.assertEquals("a b c d", ScriptUtils.normalize(str5));
        Assert.assertEquals("a b c ч d", ScriptUtils.normalize(str6));
        Assert.assertEquals("a b c ч d", ScriptUtils.normalize(str7));
    }

    @Test
    public void checkHeaderEquals() {
        Map<Object, String> headerMapping = new HashMap<Object, String>();
        headerMapping.put("столбец 1", "столбец 1");
        headerMapping.put("", "");
        headerMapping.put(null, null);
        ScriptUtils.checkHeaderEquals(headerMapping);
    }

    @Test
    public void checkFormatTest() {
        Assert.assertTrue(ScriptUtils.checkFormat("99", "[0-9]{2}"));
        Assert.assertFalse(ScriptUtils.checkFormat("ss", "[0-9]{2}"));
        Assert.assertFalse(ScriptUtils.checkFormat("3333", "[0-9]{2}"));
        Assert.assertFalse(ScriptUtils.checkFormat(null, "[0-9]{2}"));
        Assert.assertFalse(ScriptUtils.checkFormat("22", null));
    }

    @Test(expected = ServiceException.class)
    public void checkOverflow1Test() {
        BigDecimal a = new BigDecimal("10");
        // в числе "10" знаков больше 1
        ScriptUtils.checkOverflow(a, null, "test", 2, 1, "test");
    }

    @Test
    public void checkOverflow2Test() {
        BigDecimal a = new BigDecimal("10");
        // в числе "10" знаков не больше 2
        ScriptUtils.checkOverflow(a, null, "test", 2, 2, "test");
        // в числе "10" знаков не больше 3
        ScriptUtils.checkOverflow(a, null, "test", 2, 3, "test");
        // проверяем число null
        ScriptUtils.checkOverflow(null, null, "test", 2, 2, "test");
    }

    @Test(expected = ServiceException.class)
    public void checkHeaderEqualsError() {
        // неодинаковые
        equalsColumns("столбец 2", "столбец 1");
        // неодинаковые с %
        equalsColumns("столбец 1 % столбец 2", "столбец 1");
        // неодинаковые с %%
        equalsColumns("столбец 1 %% столбец 2", "столбец 1");
        // неодинаковые с %%%
        equalsColumns("столбец 1 % столбец 2", "столбец 1 %% столбец 2");
    }

    private void equalsColumns(String column1, String column2) {
        Map<Object, String> headerMapping = new HashMap<Object, String>();
        headerMapping.put(column1, column2);
        ScriptUtils.checkHeaderEquals(headerMapping);
    }

    @Test
    public void getColumnNumberSuccess() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "A");
        map.put(26, "Z");
        map.put(27, "AA");
        map.put(28, "AB");

        map.put(51, "AY");
        map.put(52, "AZ");
        map.put(53, "BA");

        map.put(100, "CV");
        map.put(1000, "ALL");

        for (Integer index : map.keySet()) {
            String value = map.get(index);
            Assert.assertEquals(ScriptUtils.getXLSColumnNumber(index), value);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getColumnNumberFailByZero() {
        ScriptUtils.getXLSColumnNumber(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getColumnNumberFailByNegative() {
        ScriptUtils.getXLSColumnNumber(-10);
    }

    private void printRows(List<DataRow<Cell>> dataRows) {
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() != null) {
                System.out.print("(" + row.getAlias() + ") ");
            } else {
                System.out.print("(" + row.getIndex() + ") ");
            }
            for (String key : row.keySet()) {
                Cell cell = row.getCell(key);
                if (cell.getColumn().getColumnType() == ColumnType.REFERENCE || cell.getColumn().getColumnType() == ColumnType.REFBOOK) {
                    System.out.print(cell.getRefBookDereference() + ", ");
                } else {
                    System.out.print(cell.getValue() + ", ");
                }
            }
            System.out.println();
        }
    }

    // Проверка простой сортировки
    @Test
    public void sortRowsSimpleTest() {
        int[] indexesBefore = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] indexesAfter = {9, 7, 8, 5, 4, 3, 10, 2, 6, 1};
        List<DataRow<Cell>> dataRows = getTestSimpleRows();

        //System.out.println("Before sort:");
        //printRows(dataRows);

        int index = 0;
        for (DataRow<Cell> row : dataRows) {
            Assert.assertEquals(indexesBefore[index++], row.getIndex().intValue());
        }

        ScriptUtils.sortRowsSimple(dataRows);

        //System.out.println("After sort:");

        index = 0;
        for (DataRow<Cell> row : dataRows) {
            Assert.assertEquals(indexesAfter[index++], row.getIndex().intValue());
        }

        //printRows(dataRows);
    }

    // Проверка сортировки с наличием подитоговых и итоговой строки (итоговые в начале)
    //@Test
    public void sortRows1Test() {
        int[] indexesAfter = {3, 2, 1, 9, 8, 10, 7, 5, 4, 6};
        String[] aliasesAfter = {"subtotal3", "subtotal4", "subtotal1", "subtotal2", "total"};

        List<DataRow<Cell>> dataRows = getTestRows1();
        List<DataRow<Cell>> subtotalRows = Arrays.asList(dataRows.get(3), dataRows.get(4), dataRows.get(9), dataRows.get(12));
        DataRow<Cell> totalRow = dataRows.get(14);

        //System.out.println("Before:");
        //printRows(dataRows);

        // Реализация не требуется, т.к. строки уже разыменованы
        RefBookService refBookService = mock(RefBookService.class);

        ScriptUtils.sortRows(refBookService, new Logger(), dataRows, subtotalRows, totalRow, true);

        //System.out.println("After:");
        //printRows(dataRows);

        int index1 = 0;
        int index2 = 0;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() == null) {
                Assert.assertEquals(indexesAfter[index1++], row.getIndex().intValue());
            } else {
                Assert.assertEquals(aliasesAfter[index2++], row.getAlias());
            }
        }
    }

    // Проверка сортировки с наличием подитоговых и итоговой строки (итоговые в конце)
    @Test
    public void sortRows2Test() {
        int[] indexesAfter = {3, 2, 1, 9, 8, 10, 7, 5, 4, 6};
        String[] aliasesAfter = {"subtotal3", "subtotal4", "subtotal2", "subtotal1", "total"};

        List<DataRow<Cell>> dataRows = getTestRows1();
        List<DataRow<Cell>> subtotalRows = Arrays.asList(dataRows.get(3), dataRows.get(4), dataRows.get(9), dataRows.get(12));
        DataRow<Cell> totalRow = dataRows.get(14);

        //System.out.println("Before:");
        //printRows(dataRows);

        // Реализация не требуется, т.к. строки уже разыменованы
        RefBookService refBookService = mock(RefBookService.class);

        ScriptUtils.sortRows(refBookService, new Logger(), dataRows, subtotalRows, totalRow, false);

        //System.out.println("After:");
        //printRows(dataRows);

        int index1 = 0;
        int index2 = 0;
        for (DataRow<Cell> row : dataRows) {
            if (row.getAlias() == null) {
                Assert.assertEquals(indexesAfter[index1++], row.getIndex().intValue());
            } else {
                Assert.assertEquals(aliasesAfter[index2++], row.getAlias());
            }
        }
    }

    private FormData getSortTestFormData() {
        AutoNumerationColumn col1 = new AutoNumerationColumn();
        col1.setId(1);
        col1.setAlias("c1");
        col1.setNumerationType(NumerationType.SERIAL);

        NumericColumn col2 = new NumericColumn();
        col2.setId(2);
        col2.setAlias("c2");

        StringColumn col3 = new StringColumn();
        col3.setId(3);
        col3.setAlias("c3");

        RefBookColumn col4 = new RefBookColumn();
        col4.setId(4);
        col4.setAlias("c4");

        ReferenceColumn col5 = new ReferenceColumn();
        col5.setId(5);
        col5.setAlias("c5");

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        formTemplate.getColumns().addAll(Arrays.asList(col1, col2, col3, col4, col5));

        FormData formData = new FormData(formTemplate);
        formData.setId(1L);

        return formData;
    }

    /**
     * Тестовые строки для проверки сортировки без учета групп
     * с1 с2 с3 с4 с5
     * 1) 1, -, -, -, -
     * 2) 2, 1, -, -, -
     * 3) 3, 1, 'F', -, -
     * 4) 3, 1, 'F', 'G', 'H'
     * 5) 4, 1, 'F', 'G', 'A'
     * 6) 5, 2, -, -, -
     * 7) 6, 1, 'B', -, -
     * 8) 7, 1, 'B', -, -
     * 9) 8, 1, 'B', 'G', -
     * 10) 9, 1, 'Z', 'T', -
     */
    private List<DataRow<Cell>> getTestSimpleRows() {
        List<DataRow<Cell>> list = new ArrayList<DataRow<Cell>>(10);

        FormData formData = getSortTestFormData();

        // 1
        DataRow<Cell> row = formData.createDataRow();
        row.setIndex(1);
        list.add(row);
        // 2
        row = formData.createDataRow();
        row.setIndex(2);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        list.add(row);
        // 3
        row = formData.createDataRow();
        row.setIndex(3);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("F");
        list.add(row);
        // 4
        row = formData.createDataRow();
        row.setIndex(4);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("F");
        row.getCell("c4").setRefBookDereference("G");
        row.getCell("c5").setRefBookDereference("H");
        list.add(row);
        // 5
        row = formData.createDataRow();
        row.setIndex(5);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("F");
        row.getCell("c4").setRefBookDereference("G");
        row.getCell("c5").setRefBookDereference("A");
        list.add(row);
        // 6
        row = formData.createDataRow();
        row.setIndex(6);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(2));
        list.add(row);
        // 7
        row = formData.createDataRow();
        row.setIndex(7);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("B");
        list.add(row);
        // 8
        row = formData.createDataRow();
        row.setIndex(8);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("B");
        list.add(row);
        // 9
        row = formData.createDataRow();
        row.setIndex(9);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("B");
        row.getCell("c4").setRefBookDereference("G");
        list.add(row);
        // 10
        row = formData.createDataRow();
        row.setIndex(10);
        row.getCell("c2").setNumericValue(BigDecimal.valueOf(1));
        row.getCell("c3").setStringValue("Z");
        row.getCell("c4").setRefBookDereference("T");
        list.add(row);

        return list;
    }

    /**
     * Тестовые строки для проверки сортировки c учетом групп (итоговая в конце)
     * с1 с2 с3 с4 с5
     * 1) 1, -, -, -, -
     * 2) 2, 1, -, -, -
     * 3) 3, 1, 'F', -, -
     * П) -, T, -, -, -
     * П) -, T, -, -, -
     * 4) 3, 1, 'F', 'G', 'H'
     * 5) 4, 1, 'F', 'G', 'A'
     * 6) 5, 2, -, -, -
     * 7) 6, 1, 'B', -, -
     * П) -, A, -, -, -
     * 8) 7, 1, 'B', -, -
     * 9) 8, 1, 'B', 'G', -
     * П) -, K, -, -, -
     * 10) 9, 1, 'Z', 'T', -
     * И) -, -, -, -, -
     */
    private List<DataRow<Cell>> getTestRows1() {
        List<DataRow<Cell>> list = new LinkedList<DataRow<Cell>>();
        list.addAll(getTestSimpleRows());

        FormData formData = getSortTestFormData();
        // И
        DataRow<Cell> row = formData.createDataRow();
        row.setAlias("total");
        list.add(10, row);
        // П
        row = formData.createDataRow();
        row.setAlias("subtotal4");
        row.getCell("c3").setStringValue("K");
        list.add(9, row);
        // П
        row = formData.createDataRow();
        row.setAlias("subtotal3");
        row.getCell("c3").setStringValue("A");
        list.add(7, row);
        // П
        row = formData.createDataRow();
        row.setAlias("subtotal2");
        row.getCell("c3").setStringValue("T");
        list.add(3, row);
        // П
        row = formData.createDataRow();
        row.setAlias("subtotal1");
        row.getCell("c3").setStringValue("T");
        list.add(3, row);

        return list;
    }

    @Test
    public void updateIndexesTest() {
        List<DataRow<Cell>> dataRows = getTestSimpleRows();
        ScriptUtils.updateIndexes(dataRows);
        int i = 1;
        for (DataRow<Cell> row : dataRows) {
            Assert.assertEquals(i++, row.getIndex().intValue());
        }

        for (DataRow<Cell> row : dataRows) {
            row.setIndex(null);
        }
        ScriptUtils.updateIndexes(dataRows);
        i = 1;
        for (DataRow<Cell> row : dataRows) {
            Assert.assertEquals(i++, row.getIndex().intValue());
        }
    }

    private FormData getDatePatternValidTestFormData() {
        DateColumn col1 = new DateColumn();
        col1.setId(1);
        col1.setAlias("c1");
        col1.setName("Тестовый колумн");
        col1.setFormatId(Formats.DD_MM_YYYY.getId());

        StringColumn col2 = new StringColumn();
        col2.setId(2);
        col2.setAlias("c2");
        col2.setName("Тестовый колумн2");

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        formTemplate.getColumns().addAll(Arrays.asList(col1, col2));

        FormData formData = new FormData(formTemplate);
        formData.setId(1L);

        return formData;
    }

    private List<DataRow<Cell>> getDatePatternValidTestRows() {
        List<DataRow<Cell>> list = new LinkedList<DataRow<Cell>>();

        FormData formData = getDatePatternValidTestFormData();
        try {
            // корректная дата
            DataRow<Cell> row = formData.createDataRow();
            row.setIndex(1);
            row.setAlias("total");
            list.add(row);
            row.getCell("c1").setDateValue(new SimpleDateFormat("dd.MM.yyyy").parse("13.06.2066"));
            row.getCell("c2").setStringValue("7723643863");
            // некорректная дата
            row = formData.createDataRow();
            row.setIndex(2);
            row.setAlias("subtotal4");
            row.getCell("c1").setDateValue(new SimpleDateFormat("dd.MM.yyyy").parse("13.06.1066"));
            row.getCell("c2").setStringValue("77236438634");
            list.add(row);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Test
    public void checkDateValidTest() {
        Logger logger = new Logger();
        List<DataRow<Cell>> dataRows = getDatePatternValidTestRows();
        DataRow<Cell> row = dataRows.get(0);
        // в виде даты
        Assert.assertTrue(ScriptUtils.checkDateValid(logger, row, "c1", row.get("c1"), true));
        // в виде строки
        Assert.assertTrue(ScriptUtils.checkDateValid(logger, row, "c1", new SimpleDateFormat("dd.MM.yyyy").format(row.get("c1")), true));
        Assert.assertTrue(ScriptUtils.checkDateValid(logger, row, "c1", "12.12.2012", true));
        row = dataRows.get(1);
        Assert.assertFalse(ScriptUtils.checkDateValid(logger, row, "c1", row.get("c1"), true));
        Assert.assertEquals(1, logger.getEntries().size());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(0).getMessage());
        Assert.assertFalse(ScriptUtils.checkDateValid(logger, row, "c1", new SimpleDateFormat("dd.MM.yyyy").format(row.get("c1")), true));
        Assert.assertEquals(2, logger.getEntries().size());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(1).getMessage());
        Assert.assertFalse(ScriptUtils.checkDateValid(logger, row, "c1", "12.02.2112", true));
        Assert.assertEquals(3, logger.getEntries().size());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(2).getMessage());
        // при некорректном формате даты выводится две ошибки.
        Assert.assertFalse(ScriptUtils.checkDateValid(logger, row, "c1", "12022112", true));
        Assert.assertEquals(5, logger.getEntries().size());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(3).getMessage());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(4).getMessage());
    }

    @Test
    public void checkPatternTest() {
        Logger logger = new Logger();
        List<DataRow<Cell>> dataRows = getDatePatternValidTestRows();
        DataRow<Cell> row = dataRows.get(0);
        Assert.assertTrue(ScriptUtils.checkPattern(logger, row, "c2", row.getCell("c2").getStringValue(), Arrays.asList(ScriptUtils.INN_JUR_PATTERN, ScriptUtils.INN_IND_PATTERN), true));
        Assert.assertFalse(logger.containsLevel(LogLevel.ERROR));
        row = dataRows.get(1);
        Assert.assertFalse(ScriptUtils.checkPattern(logger, row, "c2", row.getCell("c2").getStringValue(), ScriptUtils.INN_JUR_PATTERN, true));
        Assert.assertEquals(1, logger.getEntries().size());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(0).getMessage());
    }

    @Test
    public void checkControlSumInnTest() {
        Logger logger = new Logger();
        List<DataRow<Cell>> dataRows = getDatePatternValidTestRows();
        DataRow<Cell> row = dataRows.get(0);
        ScriptUtils.checkControlSumInn(logger, row, "c2", "7723643863", true);
        Assert.assertFalse(logger.containsLevel(LogLevel.ERROR));
        row = dataRows.get(1);
        ScriptUtils.checkControlSumInn(logger, row, "c2", "7723643862", true);
        Assert.assertEquals(1, logger.getEntries().size());
        //ScriptUtilsTest.logger.info(logger.getEntries().get(0).getMessage());
    }
}
