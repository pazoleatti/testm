package com.aplana.sbrf.taxaccounting.service.script.util;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange;
import com.aplana.sbrf.taxaccounting.model.script.range.Range;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

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
	private static final String ROW_ALIAS = "sampleRowAlias";

	/**
	 * Возвращает таблицу с тестовыми данными
	 *
	 * @return
	 */
	private FormData getTestFormData() {
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

		DataRow row1 = fd.appendDataRow();
		row1.getCell(STRING_ALIAS).setValue("книга");
		row1.getCell(NUMBER_ALIAS).setValue(1.04);
		row1.getCell(DATE_ALIAS).setValue(new Date());

		DataRow row2 = fd.appendDataRow();
		row2.getCell(STRING_ALIAS).setValue("карандаш");
		row2.getCell(NUMBER_ALIAS).setValue(2.1);

		DataRow row3 = fd.appendDataRow();
		row3.setAlias(ROW_ALIAS);
		row3.getCell(STRING_ALIAS).setValue("блокнот");
		row3.getCell(DATE_ALIAS).setValue(new Date());

		return fd;
	}

	@Test
	public void summTest() {
		FormData fd = getTestFormData();
		logger.info(fd);
		double r = ScriptUtils.summ(fd, new ColumnRange(NUMBER_ALIAS, 0, 1));
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
		ScriptUtils.checkNumericColumns(getTestFormData(), new Range(NUMBER_ALIAS, 0, NUMBER_ALIAS, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkNumericColumnsTest2() {
		ScriptUtils.checkNumericColumns(getTestFormData(), new Range(STRING_ALIAS, 0, NUMBER_ALIAS, 2));
	}

	@Test
	public void summBDTest() {
		BigDecimal A = new BigDecimal(2);
		BigDecimal B = null;
		Assert.assertEquals(ScriptUtils.summ(A, B), 2, Constants.EPS);
	}

	@Test
	public void summBDTest2() {
		BigDecimal A = BigDecimal.valueOf(2);
		BigDecimal B = BigDecimal.valueOf(-3);
		Assert.assertEquals(ScriptUtils.summ(A, B), -1, Constants.EPS);
	}

	@Test
	public void substractBD() {
		BigDecimal A = BigDecimal.valueOf(2);
		BigDecimal B = BigDecimal.valueOf(-3);
		Assert.assertEquals(ScriptUtils.substract(A, B), 5, Constants.EPS);
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

}
