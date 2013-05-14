package com.aplana.sbrf.taxaccounting.model;

import org.junit.Assert;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Тесты для FormData
 * 
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
		temp.getColumns()
				.addAll(Arrays.asList(new Column[] { strColumn, numColumn,
						dateColumn }));

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
		FormData formData = getTestFormData();
		int rowIndex = formData.getDataRowIndex(ROW_ALIAS);
		Assert.assertEquals(2, rowIndex);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getDataRowIndexTest2() {
		FormData formData = getTestFormData();
		formData.getDataRowIndex("non_existant");
	}

	@Test(expected = NullPointerException.class)
	public void getDataRowIndexTest3() {
		FormData formData = getTestFormData();
		formData.getDataRowIndex(null);
	}

	@Test
	public void deleteDataRowTest() {
		FormData formData = getTestFormData();
		Assert.assertEquals(3, formData.getDataRows().size());
		DataRow dataRow = formData.getDataRows().get(1);
		Assert.assertTrue(formData.deleteDataRow(dataRow));
		Assert.assertEquals(2, formData.getDataRows().size());
	}

	@Test
	public void deleteDataRowTest2() {
		FormData formData = getTestFormData();
		Assert.assertEquals(3, formData.getDataRows().size());
		DataRow dataRow = new DataRow();
		Assert.assertFalse(formData.deleteDataRow(dataRow));
		Assert.assertEquals(3, formData.getDataRows().size());
	}

	@Test
	public void fillValueOners() {
		String testValue = "блокнот";
		String testValueChange = "тетрадь";

		FormData formData = getTestFormData();

		formData.getDataRows().get(1).getCell(STRING_ALIAS).setColSpan(2);
		formData.getDataRows().get(1).getCell(STRING_ALIAS).setRowSpan(2);

		formData.getDataRows().get(1).put(STRING_ALIAS, testValue);

		Assert.assertNotEquals(testValue,
				formData.getDataRows().get(1).get(NUMBER_ALIAS));

		FormDataUtils.setValueOners(formData.getDataRows());

		// Все ячейки диапазона должны возвращать значение главной ячейки
		Assert.assertEquals(testValue,
				formData.getDataRows().get(1).get(STRING_ALIAS));
		Assert.assertEquals(testValue,
				formData.getDataRows().get(1).get(NUMBER_ALIAS));
		Assert.assertEquals(testValue,
				formData.getDataRow(ROW_ALIAS).get(STRING_ALIAS));
		Assert.assertEquals(testValue,
				formData.getDataRow(ROW_ALIAS).get(NUMBER_ALIAS));

		// Меняем значение в одной из перекрытых ячеек
		formData.getDataRow(ROW_ALIAS).put(NUMBER_ALIAS, testValueChange);

		// Значение меняется в главной ячейке и во всех перекрытых ей ячейках
		Assert.assertEquals(testValueChange,
				formData.getDataRows().get(1).get(STRING_ALIAS));
		Assert.assertEquals(testValueChange,
				formData.getDataRows().get(1).get(NUMBER_ALIAS));
		Assert.assertEquals(testValueChange, formData.getDataRow(ROW_ALIAS)
				.get(STRING_ALIAS));
		Assert.assertEquals(testValueChange, formData.getDataRow(ROW_ALIAS)
				.get(NUMBER_ALIAS));

		//
		// Нужно проверить - не вальнется ли, если в диапазоне будет ещё один
		// диапазон.
		//
		formData.getDataRows().get(1).getCell(NUMBER_ALIAS).setColSpan(2);
		formData.getDataRows().get(1).getCell(NUMBER_ALIAS).setRowSpan(2);

		FormDataUtils.setValueOners(formData.getDataRows());

		// Проверяем, сбросились ли спаны для внутреннего (не используемого)
		// диапазона
		Assert.assertEquals(1,
				formData.getDataRows().get(1).getCell(NUMBER_ALIAS)
						.getColSpan());
		Assert.assertEquals(1,
				formData.getDataRows().get(1).getCell(NUMBER_ALIAS)
						.getRowSpan());

		Assert.assertEquals(testValueChange,
				formData.getDataRows().get(1).get(STRING_ALIAS));
		Assert.assertEquals(testValueChange,
				formData.getDataRows().get(1).get(NUMBER_ALIAS));
		Assert.assertTrue(formData.getDataRows().get(1).getCell(NUMBER_ALIAS).hasValueOwner());
		Assert.assertEquals(testValueChange, formData.getDataRow(ROW_ALIAS)
				.get(STRING_ALIAS));
		Assert.assertEquals(testValueChange, formData.getDataRow(ROW_ALIAS)
				.get(NUMBER_ALIAS));

		Assert.assertNotEquals(testValueChange, formData.getDataRows().get(1)
				.get(DATE_ALIAS));
		
		FormDataUtils.cleanValueOners(formData.getDataRows());
		
		Assert.assertNotEquals(testValueChange,
				formData.getDataRows().get(1).get(NUMBER_ALIAS));
		Assert.assertFalse(formData.getDataRows().get(1).getCell(NUMBER_ALIAS).hasValueOwner());
		
		

	}

}
