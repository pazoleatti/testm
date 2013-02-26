package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Тест для строки данных налоговой формы
 * @author dsultanbekov
 */
public class DataRowTest {
	private DataRow createRow() {
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
		
		DataRow row = new DataRow(formColumns, formStyles);
		return row;
	}
	
	@Test
	public void testGetCell() {
		DataRow row = createRow();
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
}
