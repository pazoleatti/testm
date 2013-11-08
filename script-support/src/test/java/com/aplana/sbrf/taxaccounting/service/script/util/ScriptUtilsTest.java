package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange;
import com.aplana.sbrf.taxaccounting.model.script.range.Range;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    // private static final String UNKNOWN_ALIAS = "unknown alias";


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
        row1.getCell(STRING_ALIAS).setValue("книга");
        row1.getCell(NUMBER_ALIAS).setValue(1.04);
        row1.getCell(DATE_ALIAS).setValue(DATE_CONST);

        DataRow row2 = fd.createDataRow();
        row2.setAlias(ROW2_ALIAS);
        row2.getCell(STRING_ALIAS).setValue("карандаш");
        row2.getCell(NUMBER_ALIAS).setValue(2.1);

        DataRow row3 = fd.createDataRow();
        row3.setAlias(ROW3_ALIAS);
        row3.getCell(STRING_ALIAS).setValue("блокнот");
        row3.getCell(DATE_ALIAS).setValue(DATE_CONST);

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
        Assert.assertEquals(ScriptUtils.round(3.12345, 3), 3.123, Constants.EPS);
    }

    @Test
    public void roundTest2() {
        Assert.assertEquals(ScriptUtils.round(1.5, 0), 2, Constants.EPS);
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

        Assert.assertEquals(ScriptUtils.parseNumber(str1, 1, 1, null, true).doubleValue(), d1, 0);
        Assert.assertEquals(ScriptUtils.parseNumber(str1, 1, 1, null, false).doubleValue(), d1, 0);
        Assert.assertEquals(ScriptUtils.parseNumber(str2, 1, 1, null, true).doubleValue(), d1, 0);
        Assert.assertEquals(ScriptUtils.parseNumber(str2, 1, 1, null, false).doubleValue(), d1, 0);
        Assert.assertNull(ScriptUtils.parseNumber(str3, 1, 1, null, false));
        Assert.assertNull(ScriptUtils.parseNumber(str4, 1, 1, null, false));
        Assert.assertEquals(ScriptUtils.parseNumber(str5, 1, 1, null, false).longValue(), l1);
        Assert.assertNull(ScriptUtils.parseNumber(str6, 1, 1, null, true));
        Assert.assertNull(ScriptUtils.parseNumber(str6, 1, 1, null, false));
    }

    @Test(expected = ServiceException.class)
    public void parseNumberTest2() {
        ScriptUtils.parseNumber("0.0.1", 1, 1, null, true);
    }

    @Test(expected = ServiceException.class)
    public void parseNumberTest3() {
        ScriptUtils.parseNumber("  000AA", 1, 1, null, true);
    }

    @Test
    public void parseDate1() {
        String str1 = "05.02.2009";
        String str2 = " 5.2.2009 ";
        Date date1 = new Date(2009 - 1900, 2 - 1, 5);
        String format = "dd.MM.yyyy";
        Assert.assertEquals(ScriptUtils.parseDate(str1, format, 1, 1, null, true), date1);
        Assert.assertEquals(ScriptUtils.parseDate(str1, format, 1, 1, null, false), date1);
        Assert.assertEquals(ScriptUtils.parseDate(str2, format, 1, 1, null, true), date1);
        Assert.assertEquals(ScriptUtils.parseDate(str2, format, 1, 1, null, false), date1);
    }

    @Test(expected = ServiceException.class)
    public void parseDate2() {
        ScriptUtils.parseDate("Hello", "dd.MM.yyyy", 1, 1, null, true);
    }

/*
    @Test
	public void summBDTest() {
		FormData fd = getTestFormData().get;
		Cell A = fd.getDataRow(ROW2_ALIAS).getCell(NUMBER_ALIAS);
		Cell B = fd.getDataRow(ROW1_ALIAS).getCell(NUMBER_ALIAS);
		Assert.assertEquals(ScriptUtils.summ(A, B), 3.14, Constants.EPS);
	}

	@Test
	public void substractBD() {
		FormData fd = getTestFormData();
		Cell A = fd.getDataRow(ROW2_ALIAS).getCell(NUMBER_ALIAS);
		Cell B = fd.getDataRow(ROW1_ALIAS).getCell(NUMBER_ALIAS);
		Assert.assertEquals(ScriptUtils.substract(A, B), 1.06, Constants.EPS);
	}

	@Test
	public void summIfEqualsTest() {
		FormData fd = getTestFormData();
		double r = ScriptUtils.summIfEquals(fd, new ColumnRange(DATE_ALIAS, 0, 2), DATE_CONST, new ColumnRange(NUMBER_ALIAS, 0, 2));
		Assert.assertEquals(r, 1.04, Constants.EPS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void summIfEqualsTest2() {
		FormData fd = getTestFormData();
		ScriptUtils.summIfEquals(fd, new ColumnRange(DATE_ALIAS, 0, 2), DATE_CONST, new ColumnRange(NUMBER_ALIAS, 0, 0));
	}

	@Test
	public void getCell1() {
		FormData fd = getTestFormData();
		Cell c = ScriptUtils.getCell(fd, NUMBER_ALIAS, ROW2_ALIAS);
		Assert.assertEquals(c.getNumericValue().doubleValue(), 2.1, Constants.EPS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getCell2() {
		FormData fd = getTestFormData();
		ScriptUtils.getCell(fd, UNKNOWN_ALIAS, ROW2_ALIAS);
	}

	@Test
	public void copyCellValuesTest() {
		double value = 999.0;
		FormData fdFrom = getTestFormData();
		Cell cellFrom = ScriptUtils.getCell(fdFrom, NUMBER_ALIAS, ROW1_ALIAS);
		cellFrom.setValue(value);

		Range range = new Range(STRING_ALIAS, fdFrom.getDataRowIndex(ROW1_ALIAS), NUMBER_ALIAS, fdFrom.getDataRowIndex(ROW2_ALIAS));

		FormData fdTo = getTestFormData();
		ScriptUtils.copyCellValues(fdFrom, fdTo, range, range);
		Cell cellTo = ScriptUtils.getCell(fdFrom, NUMBER_ALIAS, ROW1_ALIAS);
		Assert.assertEquals(cellTo.getNumericValue().doubleValue(), value, Constants.EPS);
	}

	//TODO перенести методы в RangeTest

	@Test
	public void getColumnIndexTest1() {
		Assert.assertEquals(Range.getColumnIndex(getTestFormData(), DATE_ALIAS), 2);
	}

	@Test
	public void getColumnIndexTest2() {
		Assert.assertEquals(Range.getColumnIndex(getTestFormData(), STRING_ALIAS), 0);
	}

	@Test
	public void checkRangeTest1() {
		new Range(STRING_ALIAS, 0, NUMBER_ALIAS, 2).getRangeRect(getTestFormData());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void checkRangeTest2() {
		new Range(DATE_ALIAS, 0, STRING_ALIAS, 6).getRangeRect(getTestFormData());
	}
*/
}
