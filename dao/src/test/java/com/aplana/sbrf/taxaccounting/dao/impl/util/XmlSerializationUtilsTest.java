package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

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
	private FormTemplate formTemplate;
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

		formTemplate = new FormTemplate();
		formTemplate.getColumns().addAll(columns);
	}

	@Test
	public void testSimple() {
		assert true;
	}

	@Test
	@Transactional
	public void testSerializationCell() {
		// Prepare
		List<DataRow<Cell>> data = prepareDataCell();
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

		FormTemplate formTemplate = new FormTemplate();
		formTemplate.getStyles().addAll(styles);
		formTemplate.getColumns().addAll(columns);
		
		List<DataRow<Cell>> deserializedData = xmlSerializationUtils.deserialize(string, formTemplate, Cell.class);
		assertNotNull("The result of deserialization is null.", deserializedData);
		assertFalse("The result of deserialization is empty.", deserializedData.isEmpty());

		// Check equals
		assertTrue(
				"The result of deserialization doesn't equals the initial data.",
				equals(data, deserializedData));
	}
	
	@Test
	@Transactional
	public void testSerializationHeaderCell() {
		// Prepare
		List<DataRow<HeaderCell>> data = prepareDataHeaderCell();
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

		FormTemplate formTemplate = new FormTemplate();
		formTemplate.getStyles().addAll(styles);
		formTemplate.getColumns().addAll(columns);
		
		List<DataRow<HeaderCell>> deserializedData = xmlSerializationUtils.deserialize(string, formTemplate, HeaderCell.class);
		assertNotNull("The result of deserialization is null.", deserializedData);
		assertFalse("The result of deserialization is empty.", deserializedData.isEmpty());

		// Check equals
		assertTrue("The result of deserialization doesn't equals the initial data.", equals(data, deserializedData));
	}

	public List<DataRow<Cell>> prepareDataCell() {
		List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
		List<FormStyle> styles = new ArrayList<FormStyle>();
		FormStyle fs = new FormStyle();
		fs.setAlias("sa");
		styles.add(fs);
		fs = new FormStyle();
		fs.setAlias("sa1");
		styles.add(fs);

		FormTemplate formTemplate = new FormTemplate();
		formTemplate.getColumns().addAll(columns);
		formTemplate.getStyles().addAll(styles);
		// Empty row
		rows.add(new DataRow<Cell>(FormDataUtils.createCells(formTemplate)));
		// Row with alias and order parameter
		DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(formTemplate));
		row.setAlias("alias");
		rows.add(row);

		row = new DataRow<Cell>("withColumns", FormDataUtils.createCells(formTemplate));
		row.put("stringColumn", "test тест");
		row.put("numericColumn", new BigDecimal(1234.56));
		row.put("dateColumn", new Date());
		rows.add(row);

		row.getCell("stringColumn").setColSpan(2);
		row.getCell("stringColumn").setRowSpan(3);
		row.getCell("stringColumn").setStyle(formTemplate.getStyle("sa"));
		row.getCell("stringColumn").setEditable(true);

		return rows;
	}
	
	public List<DataRow<HeaderCell>> prepareDataHeaderCell() {
		List<DataRow<HeaderCell>> rows = new ArrayList<DataRow<HeaderCell>>();
		List<FormStyle> styles = new ArrayList<FormStyle>();
		FormStyle fs = new FormStyle();
		fs.setAlias("sa");
		styles.add(fs);
		fs = new FormStyle();
		fs.setAlias("sa1");
		styles.add(fs);

		// Empty row
		rows.add(new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(columns)));

		// Row with alias and order parameter
		DataRow<HeaderCell> row = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(columns));
		row.setAlias("alias");
		rows.add(row);
		
		row = new DataRow<HeaderCell>("withColumns", FormDataUtils.createHeaderCells(columns));
		row.put("stringColumn", "test тест");
		row.put("numericColumn", "заголовок1");
		row.put("dateColumn", "заголовок2");
		rows.add(row);

		row.getCell("stringColumn").setColSpan(2);
		row.getCell("stringColumn").setRowSpan(3);

		return rows;
	}

	private <T extends AbstractCell> boolean equals(List<DataRow<T>> list1, List<DataRow<T>> list2) {
		if (list1 == null && list2 == null) {
			return true;
		}

		if (list1 == null || list2 == null) {
			return false;
		}

		if (list1.size() != list2.size()) {
			return false;
		}

		Iterator<DataRow<T>> i1 = list1.iterator();
		Iterator<DataRow<T>> i2 = list2.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			assertEqualsDataRow(i1.next(), i2.next());
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
	private <T extends AbstractCell> void assertEqualsCell(T cell1, T cell2) {
		
		assertEquals(cell1.getClass(), cell2.getClass());
		assertEquals(cell1.getColSpan(), cell2.getColSpan());
		assertEquals(cell1.getRowSpan(), cell2.getRowSpan());
			
		if (Cell.class.equals(cell1)){
			Cell ccell1 = (Cell) cell1;
			Cell ccell2 = (Cell) cell2;
			assertEquals(ccell1.isEditable(), ccell2.isEditable());
	
			if (FormStyle.DEFAULT_STYLE.equals(ccell1.getStyle()) && !FormStyle.DEFAULT_STYLE.equals(ccell2.getStyle())){
					fail();
			}
			if (!FormStyle.DEFAULT_STYLE.equals(ccell1.getStyle()) && FormStyle.DEFAULT_STYLE.equals(ccell2.getStyle())){
				fail();
			}
			assertEquals(ccell1.getStyleAlias(), ccell2.getStyleAlias());
		}
	}

	private <T extends AbstractCell> void assertEqualsDataRow(DataRow<T> row1, DataRow<T> row2) {


		if (row1 == null || row2 == null) {
			fail();
		}

		assertEquals(row1.getAlias(), row2.getAlias());
		assertEquals(row1.size(), row2.size());


		for (String key : row1.keySet()) {
			T cell1 = row1.getCell(key);
			T cell2 = row2.getCell(key);
			Object val1 = cell1.getValue();
			Object val2 = cell2.getValue();

			if (val1 != null && val2 != null) {
				assertEquals(val1.getClass(), val2.getClass());
	
				if (val1 instanceof Date) {
					Calendar c1 = Calendar.getInstance();
					Calendar c2 = Calendar.getInstance();

					c1.setTime((Date) val1);
					c2.setTime((Date) val2);

					if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR)) {
						fail();
					}

					if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH)) {
						fail();
					}

					if (c1.get(Calendar.DATE) != c2.get(Calendar.DATE)) {
						fail();
					}
				} else {
					assertEquals(val1, val2);
				}

				assertEqualsCell(cell1, cell2);
				

			} else if (val1 != null && val2 == null) {
				fail(String.valueOf(val1));
			} else if (val1 == null && val2 != null){
				fail(String.valueOf(val2));
			}
		}

	}
}