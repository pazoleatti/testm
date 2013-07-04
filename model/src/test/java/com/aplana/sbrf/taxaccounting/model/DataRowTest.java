package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

/**
 * Тест для строки данных налоговой формы
 * @author dsultanbekov
 */
public class DataRowTest {
	private DataRow<Cell> createRow() {
		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setAlias("numeric");
		numericColumn.setId(1);
		
		StringColumn stringColumn = new StringColumn();
		stringColumn.setAlias("string");
		stringColumn.setId(2);
		
		DateColumn dateColumn = new DateColumn();
		dateColumn.setAlias("date");
		dateColumn.setId(3);
		
		List<Column> formColumns = new ArrayList<Column>(3);
		formColumns.add(numericColumn);
		formColumns.add(stringColumn);
		formColumns.add(dateColumn);

		FormStyle boldStyle = new FormStyle();
		boldStyle.setAlias("bold");
		boldStyle.setBold(true);
		
		FormStyle italicStyle = new FormStyle();
		italicStyle.setAlias("italic");
		italicStyle.setItalic(true);
		
		List<FormStyle> formStyles = new ArrayList<FormStyle>(2);
		formStyles.add(boldStyle);
		formStyles.add(italicStyle);
		
		
		
		DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(formColumns, formStyles));
		return row;
	}
	
	@Test
	public void testGetCell() {
		DataRow<Cell> row = createRow();
		Cell numericCell = row.getCell("numeric");
		assertNull(numericCell.getValue());
		assertEquals(NumericColumn.class, numericCell.getColumn().getClass());
		
		Cell stringCell = row.getCell("string");
		assertNull(stringCell.getValue());
		assertEquals(StringColumn.class, stringCell.getColumn().getClass());
		
		Cell dateCell = row.getCell("date");
		assertNull(stringCell.getValue());
		assertEquals(DateColumn.class, dateCell.getColumn().getClass());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetCellIncorrectId() {
		DataRow row = createRow();
		row.getCell("nonExistantColumn");
	}

	@Test
	public void testPutAndGet() {
		DataRow row = createRow();
		row.put("numeric", 10);
		row.put("string", "testString");
		Date date = new Date();
		row.put("date", date);
		
		// Значение числового столбца будет округлено
		assertEquals(new BigDecimal(10), row.get("numeric"));
		assertEquals("testString", row.get("string"));
		assertEquals(date, row.get("date"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPutIncorrectColumn() {
		DataRow row = createRow();
		row.put("nonExistantColumn", 10);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetIncorrectColumn() {
		DataRow row = createRow();
		row.get("nonExistantColumn");
	}
	
	@Test
	public void testGetKeys() {
		DataRow row = createRow();
		Set<String> keys = row.keySet();
		assertEquals(3, keys.size());
		assertTrue(keys.contains("numeric"));
		assertTrue(keys.contains("string"));
		assertTrue(keys.contains("date"));
	}

	@Test
	public void testGetValues() {
		DataRow row = createRow();
		row.put("numeric", 10);
		row.put("string", "test");
		Date dt = new Date();
		row.put("date", dt);
		
		Collection<Object> values = row.values();
		assertEquals(3, values.size());
		assertTrue(values.contains(new BigDecimal(10)));
		assertTrue(values.contains("test"));
		assertTrue(values.contains(dt));
	}

	@Test
	public void testChangeColumnAlias() {
		// Проверка ситуации с изменением алиаса столбца (возможна в 
		// предопределённых строках, когда шаблон редактируется через админку)
		DataRow row = createRow();
		row.put("numeric", 10);
		BigDecimal val = (BigDecimal)row.get("numeric");

		Column col = row.getCell("numeric").getColumn();
		col.setAlias("numeric2");
		
		assertEquals(val, row.get("numeric2"));
		try {
			row.get("numeric");
			fail();
		} catch (IllegalArgumentException e) {
			// Всё в порядке - по старому алиасу не должно быть данных
		}
	}
	

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
	private List<DataRow<Cell>> getTestDataRows() {
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

		DataRow row1 = fd.createDataRow();
		row1.getCell(STRING_ALIAS).setValue("книга");
		row1.getCell(NUMBER_ALIAS).setValue(1.04);
		row1.getCell(DATE_ALIAS).setValue(new Date());

		DataRow row2 = fd.createDataRow();
		row2.getCell(STRING_ALIAS).setValue("карандаш");
		row2.getCell(NUMBER_ALIAS).setValue(2.1);

		DataRow row3 = fd.createDataRow();
		row3.setAlias(ROW_ALIAS);
		row3.getCell(STRING_ALIAS).setValue("блокнот");
		row3.getCell(DATE_ALIAS).setValue(new Date());
		
		List<DataRow<Cell>> result = new ArrayList<DataRow<Cell>>();
		result.add(row1);
		result.add(row2);
		result.add(row3);

		return result;
	}
	
	@Test
	public void fillValueOners() {
		
		List<DataRow<Cell>> dataRows = getTestDataRows();
		
		String testValue = "блокнот";
		String testValueChange = "тетрадь";

		dataRows.get(1).getCell(STRING_ALIAS).setColSpan(2);
		dataRows.get(1).getCell(STRING_ALIAS).setRowSpan(2);

		dataRows.get(1).put(STRING_ALIAS, testValue);

		Assert.assertNotEquals(testValue,
				dataRows.get(1).get(NUMBER_ALIAS));

		FormDataUtils.setValueOners(dataRows);

		// Все ячейки диапазона должны возвращать значение главной ячейки
		Assert.assertEquals(testValue,
				dataRows.get(1).get(STRING_ALIAS));
		Assert.assertEquals(testValue,
				dataRows.get(1).get(NUMBER_ALIAS));
		Assert.assertEquals(testValue,
				FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).get(STRING_ALIAS));
		Assert.assertEquals(testValue,
				FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).get(NUMBER_ALIAS));

		// Меняем значение в одной из перекрытых ячеек
		FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).put(NUMBER_ALIAS, testValueChange);

		// Значение меняется в главной ячейке и во всех перекрытых ей ячейках
		Assert.assertEquals(testValueChange,
				dataRows.get(1).get(STRING_ALIAS));
		Assert.assertEquals(testValueChange,
				dataRows.get(1).get(NUMBER_ALIAS));
		Assert.assertEquals(testValueChange, FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS)
				.get(STRING_ALIAS));
		Assert.assertEquals(testValueChange, FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS)
				.get(NUMBER_ALIAS));

		//
		// Нужно проверить - не вальнется ли, если в диапазоне будет ещё один
		// диапазон.
		//
		dataRows.get(1).getCell(NUMBER_ALIAS).setColSpan(2);
		dataRows.get(1).getCell(NUMBER_ALIAS).setRowSpan(2);

		FormDataUtils.setValueOners(dataRows);

		// Проверяем, сбросились ли спаны для внутреннего (не используемого)
		// диапазона
		Assert.assertEquals(1,
				dataRows.get(1).getCell(NUMBER_ALIAS)
						.getColSpan());
		Assert.assertEquals(1,
				dataRows.get(1).getCell(NUMBER_ALIAS)
						.getRowSpan());

		Assert.assertEquals(testValueChange,
				dataRows.get(1).get(STRING_ALIAS));
		Assert.assertEquals(testValueChange,
				dataRows.get(1).get(NUMBER_ALIAS));
		Assert.assertTrue(dataRows.get(1).getCell(NUMBER_ALIAS).hasValueOwner());
		Assert.assertEquals(testValueChange, FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS)
				.get(STRING_ALIAS));
		Assert.assertEquals(testValueChange, FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS)
				.get(NUMBER_ALIAS));

		Assert.assertNotEquals(testValueChange,dataRows.get(1)
				.get(DATE_ALIAS));
		
		FormDataUtils.cleanValueOners(dataRows);
		
		Assert.assertNotEquals(testValueChange,
				dataRows.get(1).get(NUMBER_ALIAS));
		Assert.assertFalse(dataRows.get(1).getCell(NUMBER_ALIAS).hasValueOwner());
		
		

	}
	
}
