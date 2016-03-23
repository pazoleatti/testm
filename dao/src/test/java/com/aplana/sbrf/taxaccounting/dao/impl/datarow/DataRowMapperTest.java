package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.model.AutoNumerationColumn;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.ColumnType;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 02.02.2016 13:17
 */

public class DataRowMapperTest {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final double EPS = 1e-6;

	private static final FormStyle testStyle = new FormStyle() {{
		setFontColor(Color.BLACK);
		setBackColor(Color.RED);
		setBold(true);
	}};

	private Cell getTestCell() {
		List<FormStyle> styles = new ArrayList<FormStyle>();
		FormStyle style = new FormStyle();
		style.setAlias("style");
		styles.add(style);
		return new Cell(null, styles);
	}

	@Test
	public void parseCellStyleTest() {
		Cell cell = getTestCell();

		DataRowMapper.parseCellStyle(cell, "");
		assertEquals(FormStyle.DEFAULT_STYLE, cell.getStyle());
		assertFalse(cell.isEditable());
		assertEquals(1, cell.getColSpan());
		assertEquals(1, cell.getRowSpan());

		DataRowMapper.parseCellStyle(cell, null);
		assertEquals(FormStyle.DEFAULT_STYLE, cell.getStyle());
		assertFalse(cell.isEditable());
		assertEquals(1, cell.getColSpan());
		assertEquals(1, cell.getRowSpan());

		DataRowMapper.parseCellStyle(cell, "e");
		assertEquals(FormStyle.DEFAULT_STYLE, cell.getStyle());
		assertTrue(cell.isEditable());
		assertEquals(1, cell.getColSpan());
		assertEquals(1, cell.getRowSpan());

		DataRowMapper.parseCellStyle(cell, "s0-10b;e;r623;c932");
		assertEquals(Color.BLACK, cell.getStyle().getFontColor());
		assertEquals(Color.RED, cell.getStyle().getBackColor());
		assertTrue(cell.getStyle().isBold());
		assertFalse(cell.getStyle().isItalic());
		assertTrue(cell.isEditable());
		assertEquals(932, cell.getColSpan());
		assertEquals(623, cell.getRowSpan());

		DataRowMapper.parseCellStyle(cell, "c34;r2");
		assertEquals(FormStyle.DEFAULT_STYLE, cell.getStyle());
		assertFalse(cell.isEditable());
		assertEquals(34, cell.getColSpan());
		assertEquals(2, cell.getRowSpan());

		DataRowMapper.parseCellStyle(cell, "s0-10b");
		assertEquals(Color.BLACK, cell.getStyle().getFontColor());
		assertEquals(Color.RED, cell.getStyle().getBackColor());
		assertTrue(cell.getStyle().isBold());
		assertFalse(cell.getStyle().isItalic());
		assertFalse(cell.isEditable());
		assertEquals(1, cell.getColSpan());
		assertEquals(1, cell.getRowSpan());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCellStyleTest2() {
		Cell cell = getTestCell();
		DataRowMapper.parseCellStyle(cell, "s2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCellStyleTest3() {
		Cell cell = getTestCell();
		DataRowMapper.parseCellStyle(cell, "e3");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCellStyleTest4() {
		Cell cell = getTestCell();
		DataRowMapper.parseCellStyle(cell, "s0-1;r");
	}

	@Test(expected = NumberFormatException.class)
	public void parseCellStyleTest5() {
		Cell cell = getTestCell();
		DataRowMapper.parseCellStyle(cell, "r 4;s0-1;e ; c7");
	}

	@Test
	public void formatCellStyleTest() {
		Cell cell = getTestCell();
		assertNull(DataRowMapper.formatCellStyle(cell));
		cell.setStyle(testStyle);
		assertEquals("s0-10b", DataRowMapper.formatCellStyle(cell));
		cell.setEditable(true);
		assertEquals("s0-10b;e", DataRowMapper.formatCellStyle(cell));
		cell.setRowSpan(2);
		assertEquals("s0-10b;e;r2", DataRowMapper.formatCellStyle(cell));
		cell.setColSpan(56);
		assertEquals("s0-10b;e;c56;r2", DataRowMapper.formatCellStyle(cell));
		cell.setColSpan(1);
		assertEquals("s0-10b;e;r2", DataRowMapper.formatCellStyle(cell));
		cell.setEditable(false);
		assertEquals("s0-10b;r2", DataRowMapper.formatCellStyle(cell));
	}

	@Test
	public void getColumnNamesTest() {
		Column column1 = new StringColumn();
		column1.setId(22);
		column1.setDataOrder(17);

		Column column2 = new StringColumn();
		column2.setId(31);
		column2.setDataOrder(3);

		FormTemplate template = new FormTemplate();
		template.setId(982);
		template.addColumn(column1);
		template.addColumn(column2);

		FormData formData = new FormData();
		formData.setId(20500L);
		formData.initFormTemplateParams(template);

		Map<Integer, String[]> columnNames = DataRowMapper.getColumnNames(formData);
		String[] cn1 = columnNames.get(column1.getId());
		assertEquals("c17", cn1[0]);
		assertEquals("c17_style", cn1[1]);
		String[] cn2 = columnNames.get(column2.getId());
		assertEquals("c3", cn2[0]);
		assertEquals("c3_style", cn2[1]);
	}

	@Test
	public void parseCellValueTest() {
        DataRowMapper dataRowMapper = new DataRowMapper(new FormData());
		assertNull(dataRowMapper.parseCellValue(ColumnType.STRING, null));
		assertEquals("31.12.2016 12:21:59", SDF.format((Date) dataRowMapper.parseCellValue(ColumnType.DATE, "31.12.2016 12:21:59")));
		assertEquals("All 7 tests passed ", dataRowMapper.parseCellValue(ColumnType.STRING, "All 7 tests passed "));
		assertEquals("", dataRowMapper.parseCellValue(ColumnType.STRING, ""));
		assertEquals(567L, dataRowMapper.parseCellValue(ColumnType.REFBOOK, "567"));
		assertNull(dataRowMapper.parseCellValue(ColumnType.REFBOOK, null));
		assertNull(dataRowMapper.parseCellValue(ColumnType.REFERENCE, "asd"));
		assertNull(dataRowMapper.parseCellValue(ColumnType.AUTO, "asd"));

		assertNumberEquals(2.1, dataRowMapper.parseCellValue(ColumnType.NUMBER, "2.1"));
		assertNumberEquals(2.1, dataRowMapper.parseCellValue(ColumnType.NUMBER, "2,1"));
		assertNumberEquals(0.1123, dataRowMapper.parseCellValue(ColumnType.NUMBER, "0.1123"));
		assertNumberEquals(1234567890123456789.1234567890123456789, dataRowMapper.parseCellValue(ColumnType.NUMBER, "1234567890123456789.1234567890123456789"));
		assertNumberEquals(-1234567890123456789.1234567890123456789, dataRowMapper.parseCellValue(ColumnType.NUMBER, "-1234567890123456789.1234567890123456789"));
		assertNumberEquals(4564523.3, dataRowMapper.parseCellValue(ColumnType.NUMBER, "4564523,3"));
		assertNumberEquals(924, dataRowMapper.parseCellValue(ColumnType.NUMBER, "924"));
		assertNumberEquals(.123, dataRowMapper.parseCellValue(ColumnType.NUMBER, ".123"));
		assertNumberEquals(0.123, dataRowMapper.parseCellValue(ColumnType.NUMBER, "0.123"));
		assertNumberEquals(0, dataRowMapper.parseCellValue(ColumnType.NUMBER, "0"));
		assertNumberEquals(0, dataRowMapper.parseCellValue(ColumnType.NUMBER, ".0"));
		assertNumberEquals(0, dataRowMapper.parseCellValue(ColumnType.NUMBER, "-.0"));
		assertNumberEquals(-23243466.423, dataRowMapper.parseCellValue(ColumnType.NUMBER, "-23243466.423"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCellValueTest2() {
        new DataRowMapper(new FormData()).parseCellValue(ColumnType.DATE, "asd");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCellValueTest3() {
        new DataRowMapper(new FormData()).parseCellValue(ColumnType.REFBOOK, "567.23");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseCellValueTest4() {
        new DataRowMapper(new FormData()).parseCellValue(ColumnType.NUMBER, "jrdfvSfd");
	}

	private void assertNumberEquals(double expected, Object value) {
		assertNotNull(value);
		assertTrue(value instanceof BigDecimal);
		assertEquals(expected, ((BigDecimal) value).doubleValue(), EPS);
	}

	@Test
	public void formatCellValueTest() throws ParseException {
        DataRowMapper dataRowMapper = new DataRowMapper(new FormData());
		Cell cell = new Cell();

		Column column = new AutoNumerationColumn();
		cell.setColumn(column);
		cell.setValue(null, null);
        assertNull(dataRowMapper.formatCellValue(cell));

		column = new StringColumn();
		cell.setColumn(column);
		cell.setValue(null, null);
		assertNull(dataRowMapper.formatCellValue(cell));

		cell.setValue(" qww rt ds", null);
		assertEquals(" qww rt ds", dataRowMapper.formatCellValue(cell));

		column = new DateColumn();
		cell.setColumn(column);
		cell.setValue(SDF.parse("31.12.2016 12:21:59"), null);
		assertEquals("31.12.2016 12:21:59", dataRowMapper.formatCellValue(cell));

		column = new RefBookColumn();
		cell.setColumn(column);
		cell.setValue(472L, null);
		assertEquals("472", dataRowMapper.formatCellValue(cell));

		column = new NumericColumn();
		((NumericColumn) column).setPrecision(5);
		cell.setColumn(column);
		cell.setValue(BigDecimal.valueOf(34523.12366), null);
		assertEquals("34523.12366", dataRowMapper.formatCellValue(cell));

		((NumericColumn) column).setPrecision(2);
		cell.setValue(BigDecimal.valueOf(34523.120), null);
		assertEquals("34523.12", dataRowMapper.formatCellValue(cell));
	}
}