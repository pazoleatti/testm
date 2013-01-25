package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.*;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Тестируем "сериализацию" строк данных.
 * 
 * @author Vitalii Samolovskikh
 */
public class XmlSerializationUtilsTest {

	private List<Column> columns;
	private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils
			.getInstance();

	public XmlSerializationUtilsTest() {
		// Columns
		columns = new ArrayList<Column>();

		Column stringColumn = new StringColumn();
		stringColumn.setAlias("stringColumn");
		columns.add(stringColumn);

		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setAlias("numericColumn");
		numericColumn.setPrecision(2);
		columns.add(numericColumn);

		Column dateColumn = new DateColumn();
		dateColumn.setAlias("dateColumn");
		columns.add(dateColumn);
	}

	@Test
	public void testSimple() {
		assert true;
	}

	@Test
	public void testSerialization() {
		// Prepare
		List<DataRow> data = prepareData();
		assertNotNull("The initial data is null.", data);
		assertFalse("The initial data is empty.", data.isEmpty());

		// Serialize
		String string = xmlSerializationUtils.serialize(data);
		assertNotNull("The result of serialization is null.", string);
		assertFalse("The result of serialization is empty.", string.isEmpty());

		System.out.println(string);

		// Deserialize
		List<FormStyle> styles = new ArrayList<FormStyle>();
		FormStyle fs = new FormStyle();
		fs.setAlias("sa");
		styles.add(fs);
		fs = new FormStyle();
		fs.setAlias("sa1");
		styles.add(fs);
		
		List<DataRow> deserializedData = xmlSerializationUtils.deserialize(
				string, columns, styles);
		assertNotNull("The result of deserialization is null.",
				deserializedData);
		assertFalse("The result of deserialization is empty.",
				deserializedData.isEmpty());

		// Check equals
		assertTrue(
				"The result of deserialization doesn't equals the initial data.",
				equals(data, deserializedData));
		
		styles = new ArrayList<FormStyle>();
		fs = new FormStyle();
		fs.setAlias("saa");
		styles.add(fs);
		fs = new FormStyle();
		fs.setAlias("sa1");
		styles.add(fs);
		
		try{
			xmlSerializationUtils.deserialize(
					string, columns, styles);
		} catch (IllegalStateException e){
			return;
		}
		Assert.fail("Должно всплыть исключение о том что стиль не найден в шаблоне");
	}

	public List<DataRow> prepareData() {
		List<DataRow> rows = new ArrayList<DataRow>();
		List<FormStyle> styles = new ArrayList<FormStyle>();
		FormStyle fs = new FormStyle();
		fs.setAlias("sa");
		styles.add(fs);
		fs = new FormStyle();
		fs.setAlias("sa1");
		styles.add(fs);

		// Empty row
		rows.add(new DataRow(columns, styles));

		// Row with alias and order parameter
		DataRow row = new DataRow(columns, styles);
		row.setAlias("alias");
		row.setOrder(1001);
		rows.add(row);
		row.setManagedByScripts(true);

		row = new DataRow("withColumns", columns, styles);
		row.put("stringColumn", "test тест");
		row.put("numericColumn", new BigDecimal(1234.56));
		row.put("dateColumn", new Date());
		rows.add(row);

		row.getCell("stringColumn").setColSpan(2);
		row.getCell("stringColumn").setRowSpan(3);
		row.getCell("stringColumn").setStyleAlias("sa");

		return rows;
	}

	private boolean equals(List<DataRow> list1, List<DataRow> list2) {
		if (list1 == null && list2 == null) {
			return true;
		}

		if (list1 == null || list2 == null) {
			return false;
		}

		if (list1.size() != list2.size()) {
			return false;
		}

		Iterator<DataRow> i1 = list1.iterator();
		Iterator<DataRow> i2 = list2.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			if (!equals(i1.next(), i2.next())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Сравнение некоторых полей Cell
	 * 
	 * @param cell1
	 * @param cell2
	 * @return
	 */
	private boolean equals(Cell cell1, Cell cell2) {

		if (cell1 == cell2)
			return true;

		Cell other = (Cell) cell2;
		if (cell1.getColSpan() != other.getColSpan())
			return false;
		if (cell1.getRowSpan() != other.getRowSpan())
			return false;
		if (cell1.getStyle() == null) {
			if (other.getStyle() != null)
				return false;
		} else if (!cell1.getStyle().getAlias().equals(other.getStyle().getAlias()))
			return false;

		return true;

	}

	private boolean equals(DataRow row1, DataRow row2) {
		if (row1 == row2) {
			return true;
		}

		if (row1 == null || row2 == null) {
			return false;
		}

		if (row1.getAlias() != null ? !row1.getAlias().equals(row2.getAlias())
				: row2.getAlias() != null) {
			return false;
		}

		if (row1.getOrder() != row2.getOrder()) {
			return false;
		}

		if (row1.isManagedByScripts() != row2.isManagedByScripts()) {
			return false;
		}

		if (row1.size() != row2.size()) {
			return false;
		}

		for (String key : row1.keySet()) {
			Cell cell1 = row1.getCell(key);
			Cell cell2 = row2.getCell(key);
			Object val1 = cell1.getValue();
			Object val2 = cell2.getValue();

			if (val1 != null && val2 != null) {
				if (val1.getClass() != val2.getClass()) {
					return false;
				}

				if (val1 instanceof Date) {
					Calendar c1 = Calendar.getInstance();
					Calendar c2 = Calendar.getInstance();

					c1.setTime((Date) val1);
					c2.setTime((Date) val2);

					if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
						return false;
					}

					if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH)) {
						return false;
					}

					if (c1.get(Calendar.DATE) != c2.get(Calendar.DATE)) {
						return false;
					}
				} else {
					if (!val1.equals(val2)) {
						return false;
					}
				}

				if (!equals(cell1, cell2)) {
					return false;
				}

			} else if (val1 != null || val2 != null) {
				return false;
			}
		}

		return true;
	}
}
