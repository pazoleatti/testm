package com.aplana.sbrf.taxaccounting.script.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.ColumnType;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;


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
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File("src/test/resources/script/importFile.xlsm")));
        List<List<String>> allValues = new ArrayList<List<String>>();
        List<List<String>> headerValues = new ArrayList<List<String>>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("rowOffset", 0);
        paramsMap.put("colOffset", 0);

        ScriptUtils.checkAndReadFile(bufferedInputStream, "script/importFile.xlsm", allValues, headerValues, "№ пп", null, 3, paramsMap);

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
    public void checkAndReadMultiheadFileTest() throws IOException, OpenXML4JException, SAXException {
        File file = new File("src/test/resources/script/multiHeaderFile.xlsx");
        List<List<String>> allValues = new ArrayList<List<String>>();
        List<List<String>> headerValues = new ArrayList<List<String>>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("rowOffset", 0);
        paramsMap.put("colOffset", 0);

        ScriptUtils.readSheetsRange(file, allValues, headerValues, "Start read from", 1, paramsMap, 1, null);

        Assert.assertEquals(8, allValues.size());
        Assert.assertEquals("initial data1", allValues.get(0).get(0));
        Assert.assertEquals("1", allValues.get(0).get(1));
        Assert.assertEquals("final data1", allValues.get(0).get(2));
        Assert.assertEquals("initial data2", allValues.get(1).get(0));
        Assert.assertEquals("2", allValues.get(1).get(1));
        Assert.assertEquals("final data2", allValues.get(1).get(2));
        Assert.assertEquals("initial data3", allValues.get(2).get(0));
        Assert.assertEquals("3", allValues.get(2).get(1));
        Assert.assertEquals("final data3", allValues.get(2).get(2));
        Assert.assertEquals("initial data4", allValues.get(3).get(0));
        Assert.assertEquals("4", allValues.get(3).get(1));
        Assert.assertEquals("final data4", allValues.get(3).get(2));
        Assert.assertEquals("initial data5", allValues.get(4).get(0));
        Assert.assertEquals("5", allValues.get(4).get(1));
        Assert.assertEquals("final data5", allValues.get(4).get(2));
        Assert.assertEquals("initial data6", allValues.get(5).get(0));
        Assert.assertEquals("6", allValues.get(5).get(1));
        Assert.assertEquals("final data6", allValues.get(5).get(2));
        Assert.assertEquals("initial data7", allValues.get(6).get(0));
        Assert.assertEquals("7", allValues.get(6).get(1));
        Assert.assertEquals("final data7", allValues.get(6).get(2));
        Assert.assertEquals("initial data8", allValues.get(7).get(0));
        Assert.assertEquals("8", allValues.get(7).get(1));
        Assert.assertEquals("final data8", allValues.get(7).get(2));
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

    @Test
    public void checkInn() {
        Assert.assertEquals("Значение гр. \"ИНН в РФ\" (\"100\") должно содержать 12 символов", ScriptUtils.checkInn("100"));
        Assert.assertEquals("Значение гр. \"ИНН в РФ\" (\"123456789 12\") должно содержать только цифры", ScriptUtils.checkInn("123456789 12"));
        Assert.assertEquals("Значение гр. \"ИНН в РФ\" (\"003456789012\") некорректно. Первые два разряда ИНН не могут быть равны одному из значений: \"00\",\"90\",\"93\",\"94\",\"95\",\"96\",\"98\", может быть отказано в приеме", ScriptUtils.checkInn("003456789012"));
        Assert.assertEquals("Некорректное контрольное число в значении гр. \"ИНН в РФ\" (\"123456789012\")", ScriptUtils.checkInn("123456789012"));
        Assert.assertNull(ScriptUtils.checkInn("500100732259"));
    }

    @Test
    public void testCheckFirstName() {
        // Проверка русских имен
        List<String> checkSimpleRussianNameResult = ScriptUtils.checkFirstName("Иван", "643");
        assertThat(checkSimpleRussianNameResult).isEmpty();

        List<String> checkRussianNameWithHyphenResult = ScriptUtils.checkFirstName("Иван-да-Марья", "643");
        assertThat(checkRussianNameWithHyphenResult).isEmpty();

        List<String> checkRussianNameWithSpaceResult = ScriptUtils.checkFirstName("Иван Котлован", "643");
        assertThat(checkRussianNameWithSpaceResult).isEmpty();

        List<String> checkRussianNameWithApostropheResult = ScriptUtils.checkFirstName("Иван Д'Артаньян", "643");
        assertThat(checkRussianNameWithApostropheResult)
                .containsExactly("Значение параметра \"Имя\" (\"Иван Д'Артаньян\") содержит недопустимые символы. Значение может содержать только буквы русского алфавита (кириллица), пробелы и дефисы");

        List<String> checkRussianNameWithDotResult = ScriptUtils.checkFirstName("Иван.Капкан", "643");
        assertThat(checkRussianNameWithDotResult)
                .containsExactly("Значение параметра \"Имя\" (\"Иван.Капкан\") содержит недопустимые символы. Значение может содержать только буквы русского алфавита (кириллица), пробелы и дефисы");

        // Проверка иностранных имен
        List<String> checkSimpleForeignNameResult = ScriptUtils.checkFirstName("John", "1");
        assertThat(checkSimpleForeignNameResult).isEmpty();

        List<String> checkForeignNameWithHyphenResult = ScriptUtils.checkFirstName("John-Mahjong", "1");
        assertThat(checkForeignNameWithHyphenResult).isEmpty();

        List<String> checkForeignNameWithSpaceResult = ScriptUtils.checkFirstName("John Photon", "1");
        assertThat(checkForeignNameWithSpaceResult).isEmpty();

        List<String> checkForeignNameWithApostropheResult = ScriptUtils.checkFirstName("Charles d'Artagnan", "1");
        assertThat(checkForeignNameWithApostropheResult).isEmpty();

        List<String> checkForeignNameWithDotResult = ScriptUtils.checkFirstName("John.von.Micron", "1");
        assertThat(checkForeignNameWithDotResult)
                .containsExactly("Значение параметра \"Имя\" (\"John.von.Micron\") содержит недопустимые символы. Значение может содержать только буквы русского (кириллица) или латинского алфавитов, пробелы, апострофы и дефисы");

        // Проверка первых символов
        List<String> checkFirstSymbolHyphenResult = ScriptUtils.checkFirstName("-Иван", "643");
        assertThat(checkFirstSymbolHyphenResult)
                .containsExactly("Значение параметра \"Имя\" (\"-Иван\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел");

        List<String> checkFirstSymbolSpaceResult = ScriptUtils.checkFirstName(" John", "1");
        assertThat(checkFirstSymbolSpaceResult)
                .containsExactly("Значение параметра \"Имя\" (\" John\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел");

        List<String> checkFirstSymbolHardSignResult = ScriptUtils.checkFirstName("ъИван", "643");
        assertThat(checkFirstSymbolHardSignResult)
                .containsExactly("Значение параметра \"Имя\" (\"ъИван\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел");

        List<String> checkFirstSymbolSoftSignResult = ScriptUtils.checkFirstName("Ьиван", "643");
        assertThat(checkFirstSymbolSoftSignResult)
                .containsExactly("Значение параметра \"Имя\" (\"Ьиван\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел");

        // Проверка ситуации, когда обе ошибки
        List<String> checkBothErrorsNameResult = ScriptUtils.checkFirstName("-Иван.John", "1");
        assertThat(checkBothErrorsNameResult).containsExactly(
                "Значение параметра \"Имя\" (\"-Иван.John\") содержит недопустимые символы. Значение может содержать только буквы русского (кириллица) или латинского алфавитов, пробелы, апострофы и дефисы",
                "Значение параметра \"Имя\" (\"-Иван.John\") некорректно. Первый символ не может быть равен одному из значений: \"Ъ\", \"Ь\", дефис или пробел"
        );
    }

    @Test
    public void checkDul() {
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"00 01 12345\") не соответствует формату \"99 99 999999\", где 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("21", "00 01 12345", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"00 00 000000\") не должно быть нулевым", ScriptUtils.checkDul("21", "00 00 000000", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("21", "80 00 010006", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП 12345678\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("07", "АП 12345678", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"Ап 1234567\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("07", "Ап 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"GT 1234567\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("07", "GT 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП 000000\") не должно быть нулевым", ScriptUtils.checkDul("07", "АП 000000", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП 0000000\") не должно быть нулевым", ScriptUtils.checkDul("07", "АП 0000000", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("07", "АП 1234567", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("07", "АП 123456", "dulNumber"));


        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП-012 12345678\") не соответствует формату \"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("18", "АП-012 12345678", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"Ап-012 1234567\") не соответствует формату \"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("18", "Ап-012 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП-000 0000000\") не должно быть нулевым", ScriptUtils.checkDul("18", "АП-000 0000000", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("18", "АП-012 1234567", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("18", "УУ-900 9999999", "dulNumber"));


        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП 12345678\") не соответствует формату \"ББ 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("24", "АП 12345678", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"Ап 1234567\") не соответствует формату \"ББ 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("24", "Ап 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"GT 1234567\") не соответствует формату \"ББ 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("24", "GT 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АП 0000000\") не должно быть нулевым", ScriptUtils.checkDul("07", "АП 0000000", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("24", "АП 1234567", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"W-АП 123456\") не соответствует формату \"R-ББ 999999\", где R - римское число, заданное символами \"I\", \"V\", \"X\", \"L\", \"C\", набранными на верхнем регистре латинской клавиатуры; Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная). Представление римских чисел только через латинскую клавиатуру", ScriptUtils.checkDul("01", "W-АП 123456", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"V-АZ 123456\") не соответствует формату \"R-ББ 999999\", где R - римское число, заданное символами \"I\", \"V\", \"X\", \"L\", \"C\", набранными на верхнем регистре латинской клавиатуры; Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная). Представление римских чисел только через латинскую клавиатуру", ScriptUtils.checkDul("01", "V-АZ 123456", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"V-ББ 1234567\") не соответствует формату \"R-ББ 999999\", где R - римское число, заданное символами \"I\", \"V\", \"X\", \"L\", \"C\", набранными на верхнем регистре латинской клавиатуры; Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная). Представление римских чисел только через латинскую клавиатуру", ScriptUtils.checkDul("01", "V-ББ 1234567", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("01", "!V@-Б#Б$ 1%2^3&4*5(6)", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"12 12345678\") не соответствует формату \"99 0999999\", где 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("02", "12 12345678", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("02", "!1@-2#1$ 2%3^4&5*6(7)", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АZ 1234567\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("04", "АZ 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"ББ 12345678\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("04", "ББ 12345678", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("04", "!Б@-Б#1$ 2%3^4&5*6(7)", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АZ 123456\") не соответствует формату \"ББ 999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("06", "АZ 123456", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"ББ 1234567\") не соответствует формату \"ББ 999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("06", "ББ 1234567", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("06", "!Б@-Б#1$ 2%3^4&5*6()", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"12 12345678\") не соответствует формату \"99 9999999\", где 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("09", "12 12345678", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("09", "!1@-2#1$ 2%3^4&5*6(7)", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АZ 1234567890\") не соответствует формату \"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("19", "АZ 1234567890", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"ББ 12345678901\") не соответствует формату \"ББ-999 9999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("19", "ББ 12345678901", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("19", "!Б@-Б#1$ 2%3^4&5*6(7)8-9=0/", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"12 12345678\") не соответствует формату \"99 9999999\", где 9 - любая десятичная цифра (обязательная)", ScriptUtils.checkDul("22", "12 12345678", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("22", "!1@-2#1$ 2%3^4&5*6(7)", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АZ 1234567\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("26", "АZ 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"ББ 12345678\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("26", "ББ 12345678", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("26", "!Б@-Б#1$ 2%3^4&5*6(7)", "dulNumber"));

        Assert.assertEquals("Значение гр. \"dulNumber\" (\"АZ 1234567\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("27", "АZ 1234567", "dulNumber"));
        Assert.assertEquals("Значение гр. \"dulNumber\" (\"ББ 12345678\") не соответствует формату \"ББ 0999999\", где Б - любая русская заглавная буква, 9 - любая десятичная цифра (обязательная), 0 - любая десятичная цифра (необязательная, может отсутствовать)", ScriptUtils.checkDul("27", "ББ 12345678", "dulNumber"));
        Assert.assertNull(ScriptUtils.checkDul("27", "!Б@-Б#1$ 2%3^4&5*6(7)", "dulNumber"));
    }

    @Test
    public void checkGetWordByNumeric() {
        Assert.assertEquals("Форма", ScriptUtils.getFirstDeclensionByNumeric("Форма", 1));
        Assert.assertEquals("Формы", ScriptUtils.getFirstDeclensionByNumeric("Форма", 2));
        Assert.assertEquals("Форм", ScriptUtils.getFirstDeclensionByNumeric("Форма", 5));
        Assert.assertEquals("Форм", ScriptUtils.getFirstDeclensionByNumeric("Форма", 11));
        Assert.assertEquals("Форм", ScriptUtils.getFirstDeclensionByNumeric("Форма", 112));
        Assert.assertEquals("Форма", ScriptUtils.getFirstDeclensionByNumeric("Форма", 121));
        Assert.assertEquals("Формы", ScriptUtils.getFirstDeclensionByNumeric("Форма", 123));
        Assert.assertEquals("Форм", ScriptUtils.getFirstDeclensionByNumeric("Форма", 126));
    }

    @Test
    public void formatDocNumberTest() {
        Assert.assertEquals("99 99 999999", ScriptUtils.formatDocNumber("21", "9999999999"));
        Assert.assertEquals("ББ 0999999", ScriptUtils.formatDocNumber("07", "ББ0999999"));
        Assert.assertEquals("ББ-999 9999999", ScriptUtils.formatDocNumber("18", "ББ9999999999"));
        Assert.assertEquals("ББ 9999999", ScriptUtils.formatDocNumber("24", "ББ9999999"));
        Assert.assertEquals("SSSSSSSSSSSSSSSSSSSSSSSSS", ScriptUtils.formatDocNumber("00", "SSSSSSSSSSSSSSSSSSSSSSSSS"));
    }

    @Test
    public void testGetConsolidationIncomeUUID() {
        ConsolidationIncome income11 = new ConsolidationIncome();
        income11.setInp("1445019531");
        income11.setIncomeCode("2000");
        income11.setIncomeType("05");
        income11.setIncomeAccruedDate(new Date(1000000000000L));
        income11.setIncomePayoutDate(new Date(1000000000001L));
        income11.setKpp("773643001");
        income11.setOktmo("45381000");
        income11.setIncomeAccruedSumm(new BigDecimal("10000.01"));
        income11.setIncomePayoutSumm(new BigDecimal("10000.01"));
        income11.setTotalDeductionsSumm(new BigDecimal("500"));
        income11.setTaxBase(new BigDecimal("9000"));
        income11.setTaxRate(13);
        income11.setTaxDate(new Date(1000000000002L));
        income11.setCalculatedTax(new BigDecimal("1000"));
        income11.setWithholdingTax(new BigDecimal("1000"));
        income11.setNotHoldingTax(new BigDecimal("0"));
        income11.setOverholdingTax(new BigDecimal("0"));
        income11.setRefoundTax(0L);
        income11.setTaxTransferDate(new Date(1000000000003L));
        income11.setPaymentDate(new Date(1000000000004L));
        income11.setPaymentNumber("1200");
        income11.setTaxSumm(1000L);
        ConsolidationIncome income12 = new ConsolidationIncome();
        income12.setInp("1445019531");
        income12.setIncomeCode("2000");
        income12.setIncomeType("05");
        income12.setIncomeAccruedDate(new Date(1000000000000L));
        income12.setIncomePayoutDate(new Date(1000000000001L));
        income12.setKpp("773643001");
        income12.setOktmo("45381000");
        income12.setIncomeAccruedSumm(new BigDecimal("10000.01"));
        income12.setIncomePayoutSumm(new BigDecimal("10000.01"));
        income12.setTotalDeductionsSumm(new BigDecimal("500"));
        income12.setTaxBase(new BigDecimal("9000"));
        income12.setTaxRate(13);
        income12.setTaxDate(new Date(1000000000002L));
        income12.setCalculatedTax(new BigDecimal("1000"));
        income12.setWithholdingTax(new BigDecimal("1000"));
        income12.setNotHoldingTax(new BigDecimal("0"));
        income12.setOverholdingTax(new BigDecimal("0"));
        income12.setRefoundTax(0L);
        income12.setTaxTransferDate(new Date(1000000000003L));
        income12.setPaymentDate(new Date(1000000000004L));
        income12.setPaymentNumber("1200");
        income12.setTaxSumm(1000L);
        ConsolidationIncome income21 = new ConsolidationIncome();
        income21.setInp("1445019531");
        income21.setIncomeCode("2000");
        income21.setIncomeType("05");
        income21.setIncomeAccruedDate(new Date(1000000000000L));
        income21.setIncomePayoutDate(new Date(1000000000001L));
        income21.setKpp("773643001");
        income21.setOktmo("45381000");
        income21.setIncomeAccruedSumm(new BigDecimal("10000.01"));
        income21.setIncomePayoutSumm(new BigDecimal("10000.01"));
        income21.setTotalDeductionsSumm(new BigDecimal("500"));
        income21.setTaxDate(new Date(1000000000002L));
        income21.setCalculatedTax(new BigDecimal("1000"));
        income21.setWithholdingTax(new BigDecimal("1000"));
        income21.setNotHoldingTax(new BigDecimal("0"));
        income21.setOverholdingTax(new BigDecimal("0"));
        income21.setTaxTransferDate(new Date(1000000000003L));
        income21.setPaymentDate(new Date(1000000000004L));
        income21.setPaymentNumber("1200");
        income21.setTaxSumm(1000L);
        ConsolidationIncome income22 = new ConsolidationIncome();
        income22.setInp("1445019531");
        income22.setIncomeCode("2000");
        income22.setIncomeType("05");
        income22.setIncomeAccruedDate(new Date(1000000000000L));
        income22.setIncomePayoutDate(new Date(1000000000001L));
        income22.setKpp("773643001");
        income22.setOktmo("45381000");
        income22.setIncomeAccruedSumm(new BigDecimal("10000.01"));
        income22.setIncomePayoutSumm(new BigDecimal("10000.01"));
        income22.setTotalDeductionsSumm(new BigDecimal("500"));
        income22.setTaxDate(new Date(1000000000002L));
        income22.setCalculatedTax(new BigDecimal("1000"));
        income22.setWithholdingTax(new BigDecimal("1000"));
        income22.setNotHoldingTax(new BigDecimal("0"));
        income22.setOverholdingTax(new BigDecimal("0"));
        income22.setTaxTransferDate(new Date(1000000000003L));
        income22.setPaymentDate(new Date(1000000000004L));
        income22.setPaymentNumber("1200");
        income22.setTaxSumm(1000L);

        String uuid11 = ScriptUtils.getConsolidationIncomeUUID(income11);
        String uuid12 = ScriptUtils.getConsolidationIncomeUUID(income12);
        String uuid21 = ScriptUtils.getConsolidationIncomeUUID(income21);
        String uuid22 = ScriptUtils.getConsolidationIncomeUUID(income22);

        Assert.assertEquals(uuid11, uuid12);
        Assert.assertEquals(uuid21, uuid22);
        Assert.assertNotEquals(uuid11, uuid21);
    }

}
