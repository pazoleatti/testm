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
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private static final Log LOG = LogFactory.getLog(ScriptUtilsTest.class);

    @Test
    public void roundTest1() {
        Assert.assertEquals(3.123, ScriptUtils.round(3.12345, 3), Constants.EPS);
    }

    @Test
    public void roundTest2() {
        Assert.assertEquals(2, ScriptUtils.round(1.5, 0), Constants.EPS);
    }

    @Test
    public void parseNumberTest1() {
        String str1 = "-0.999934534534543654565464576657";
        String str2 = " -  0,999 934 534 534543654565464576657 ";
        String str3 = "0.0.1";
        String str4 = "  000AA";
        String str5 = " 99 9 9 00";
        String str6 = "";
        String str7 = "1 016,94";
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
        Assert.assertEquals(1016.94, ScriptUtils.parseNumber(str7, 1, 1, null, false).doubleValue(), 0);
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
    public void parseDate4() {
        Logger logger = new Logger();
        Assert.assertNull(ScriptUtils.parseDate("01012009", "dd.MM.yyyy", 1, 1, logger, true));
        Assert.assertNull(ScriptUtils.parseDate("01012009", "yyyy", 1, 1, logger, true));
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
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
        headerMapping.put("столбец 1", "СТОЛБЕЦ 1");
        headerMapping.put("столбец 1", "столбец  1");
        headerMapping.put("столбец 1 ", " столбец 1");
        headerMapping.put("", "");
        headerMapping.put(null, null);
        ScriptUtils.checkHeaderEquals(headerMapping);

        Logger logger = new Logger();
        ScriptUtils.checkHeaderEquals(headerMapping, logger);
        Assert.assertTrue(logger.getEntries().isEmpty());

        ArrayList<Map<Object, String>> headerMappingList = new ArrayList<Map<Object, String>>();
        headerMappingList.add(headerMapping);
        ScriptUtils.checkHeaderEquals(headerMappingList, logger);
        Assert.assertTrue(logger.getEntries().isEmpty());

        headerMapping.put("столбец 1", "столбец  2");
        ScriptUtils.checkHeaderEquals(headerMapping, logger);
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));

        ScriptUtils.checkHeaderEquals(headerMappingList, logger);
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
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

    @Test(expected = ServiceException.class)
    public void checkHeaderEqualsError2() {
        Map<Object, String> headerMapping = new HashMap<Object, String>();
        headerMapping.put("столбец 1", "столбец 2");
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

    @Test
    public void checkAndReadFileTest() throws IOException, OpenXML4JException, SAXException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/importFile.xlsm")));
        List<List<String>> allValues = new ArrayList<List<String>>();
        List<List<String>> headerValues = new ArrayList<List<String>>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("rowOffset", 0);
        paramsMap.put("colOffset", 0);

        ScriptUtils.checkAndReadFile(bufferedInputStream, "importFile.xlsm", allValues, headerValues, "№ пп", null, 3, paramsMap);

        Assert.assertEquals("", allValues.get(0).get(4));
        Assert.assertEquals("", allValues.get(1).get(4));
        Assert.assertEquals("nameA", allValues.get(2).get(4));
        Assert.assertEquals("nameB", allValues.get(3).get(4));

        Assert.assertEquals(1000d, ScriptUtils.parseNumber(allValues.get(0).get(5), 1, 1, null, true).doubleValue(), 0);

        Date date1 = new Date(2011 - 1900, 1 - 1, 1);
        String format = "dd.MM.yyyy";
        Assert.assertEquals(date1, ScriptUtils.parseDate(allValues.get(0).get(6), format, 1, 1, null, true));

        Assert.assertEquals(11, allValues.size());
        Assert.assertEquals(9, allValues.get(0).size());

        Assert.assertEquals(3, headerValues.size());
        Assert.assertEquals(8, headerValues.get(0).size());
    }

    @Test
    public void testCheckSnils() {
        Assert.assertTrue(ScriptUtils.checkSnils("112-233-445 95"));
        Assert.assertTrue(ScriptUtils.checkSnils("112 233 445 95"));
        Assert.assertTrue(ScriptUtils.checkSnils("137-199-816-00"));
        Assert.assertTrue(ScriptUtils.checkSnils("11223344595"));
        Assert.assertTrue(ScriptUtils.checkSnils("001001998 00"));
        Assert.assertTrue(ScriptUtils.checkSnils("001001997 00"));
        Assert.assertFalse(ScriptUtils.checkSnils("001001999 00"));
        Assert.assertFalse(ScriptUtils.checkSnils("11223344596"));
    }

    @Test
    public void testCheckDul() {
        Assert.assertTrue(ScriptUtils.checkDul("445 95"));
        Assert.assertTrue(ScriptUtils.checkDul("445N 95"));
        Assert.assertFalse(ScriptUtils.checkDul("445 95N"));
        Assert.assertFalse(ScriptUtils.checkSnils("44596"));
        Assert.assertFalse(ScriptUtils.checkSnils("4N596"));
        Assert.assertFalse(ScriptUtils.checkSnils("4№596"));
        Assert.assertFalse(ScriptUtils.checkSnils("4№ 596"));
        Assert.assertFalse(ScriptUtils.checkSnils("4f №596"));
    }

    @Test
    public void testEqualsNullSafe() throws ParseException {
        Date d1 = ScriptUtils.parseDate("dd.MM.yyyy", "01.02.2017");
        Date d2 = ScriptUtils.parseDate("dd.MM.yyyy", "01.02.2017");
        Date d3 = ScriptUtils.parseDate("dd.MM.yyyy", "02.02.2017");
        Assert.assertTrue(ScriptUtils.equalsNullSafe(d1, d2));
        Assert.assertFalse(ScriptUtils.equalsNullSafe(d1, d3));
        BigDecimal bd1 = new BigDecimal("1000.00");
        Assert.assertTrue(ScriptUtils.equalsNullSafe(bd1, 1000L));
        Assert.assertTrue(ScriptUtils.equalsNullSafe(bd1, 1000));
        Assert.assertTrue(ScriptUtils.equalsNullSafe("", ""));
        Assert.assertFalse(ScriptUtils.equalsNullSafe("foo", "bar"));
        Assert.assertFalse(ScriptUtils.equalsNullSafe("foo", null));
        Assert.assertTrue(ScriptUtils.equalsNullSafe(null, null));
        Assert.assertFalse(ScriptUtils.equalsNullSafe(null, 1111));
        Assert.assertFalse(ScriptUtils.equalsNullSafe(1111, null));
    }

    @Test
    public void testIsEmpty() throws ParseException {

        Assert.assertTrue(ScriptUtils.isEmpty(null));
        Assert.assertTrue(ScriptUtils.isEmpty(""));

        Assert.assertTrue(ScriptUtils.isEmpty(0));
        Assert.assertTrue(ScriptUtils.isEmpty(0L));
        Assert.assertTrue(ScriptUtils.isEmpty(new BigDecimal("0")));
        Assert.assertTrue(ScriptUtils.isEmpty(new BigDecimal(".00")));
        Assert.assertTrue(ScriptUtils.isEmpty(new BigDecimal("0.00")));

        Assert.assertFalse(ScriptUtils.isEmpty(new BigDecimal("0.01")));
    }
}
