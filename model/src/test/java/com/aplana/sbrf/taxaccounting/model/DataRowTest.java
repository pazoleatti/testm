package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
		
		List<Column> columns = new ArrayList<Column>(3);
		columns.add(numericColumn);
		columns.add(stringColumn);
		columns.add(dateColumn);

		FormStyle boldStyle = new FormStyle();
		boldStyle.setAlias("bold");
		boldStyle.setBold(true);
		
		FormStyle italicStyle = new FormStyle();
		italicStyle.setAlias("italic");
		italicStyle.setItalic(true);
		
		List<FormStyle> styles = new ArrayList<FormStyle>(2);
		styles.add(boldStyle);
		styles.add(italicStyle);

		FormTemplate formTemplate = new FormTemplate();
		formTemplate.getColumns().addAll(columns);
		formTemplate.getStyles().addAll(styles);
		
		DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(formTemplate));
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
		temp.getColumns().addAll(Arrays.asList(
			new Column[] {strColumn, numColumn, dateColumn}));

		FormData fd = new FormData(temp);

		DataRow row1 = fd.createDataRow();
		row1.getCell(STRING_ALIAS).setValue("книга", null);
		row1.getCell(NUMBER_ALIAS).setValue(1.04, null);
		row1.getCell(DATE_ALIAS).setValue(new Date(), null);

		DataRow row2 = fd.createDataRow();
		row2.getCell(STRING_ALIAS).setValue("карандаш", null);
		row2.getCell(NUMBER_ALIAS).setValue(2.1, null);

		DataRow row3 = fd.createDataRow();
		row3.setAlias(ROW_ALIAS);
		row3.getCell(STRING_ALIAS).setValue("блокнот", null);
		row3.getCell(DATE_ALIAS).setValue(new Date(), null);
		
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

		assertNotEquals(testValue,
				dataRows.get(1).get(NUMBER_ALIAS));

		FormDataUtils.setValueOwners(dataRows);

		// Все ячейки диапазона должны возвращать значение главной ячейки, если их типы совпадают
		assertEquals(testValue, dataRows.get(1).get(STRING_ALIAS));
		assertNull(dataRows.get(1).get(NUMBER_ALIAS));
		assertNull(FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).get(STRING_ALIAS));
		assertNull(FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).get(NUMBER_ALIAS));

		//
		// Нужно проверить - не вальнется ли, если в диапазоне будет ещё один
		// диапазон.
		//
		dataRows.get(1).getCell(NUMBER_ALIAS).setColSpan(2);
		dataRows.get(1).getCell(NUMBER_ALIAS).setRowSpan(2);

		FormDataUtils.setValueOwners(dataRows);

		// Проверяем, сбросились ли спаны для внутреннего (не используемого)
		// диапазона
		assertEquals(1, dataRows.get(1).getCell(NUMBER_ALIAS).getColSpan());
		assertEquals(1, dataRows.get(1).getCell(NUMBER_ALIAS).getRowSpan());

		assertEquals(testValue, dataRows.get(1).get(STRING_ALIAS));
		assertNull(dataRows.get(1).get(NUMBER_ALIAS));
		assertTrue(dataRows.get(1).getCell(NUMBER_ALIAS).hasValueOwner());
		assertNull(FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).get(STRING_ALIAS));
		assertNull(FormDataUtils.getDataRowByAlias(dataRows, ROW_ALIAS).get(NUMBER_ALIAS));

		assertNotEquals(testValueChange, dataRows.get(1).get(DATE_ALIAS));
		
		FormDataUtils.cleanValueOwners(dataRows);
		
		assertNotEquals(testValueChange, dataRows.get(1).get(NUMBER_ALIAS));
		assertFalse(dataRows.get(1).getCell(NUMBER_ALIAS).hasValueOwner());
	}
	
}
