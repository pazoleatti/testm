package com.aplana.sbrf.taxaccounting.model;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 29.01.13 14:03
 */

public class FormDataTest {

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
		temp.getColumns().addAll(Arrays.asList(new Column[] {strColumn, numColumn, dateColumn}));

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
	public void getDataRowIndexTest() {
		Assert.assertEquals(getTestFormData().getDataRowIndex(ROW_ALIAS), 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getDataRowIndexTest2() {
		getTestFormData().getDataRowIndex("not_existing");
	}

	@Test(expected = NullPointerException.class)
	public void getDataRowIndexTest3() {
		getTestFormData().getDataRowIndex(null);
	}

}
