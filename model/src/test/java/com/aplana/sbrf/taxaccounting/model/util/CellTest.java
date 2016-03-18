package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 21.10.2015 14:14
 */

public class CellTest {

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
	private static final String STYLE_1_ALIAS = "my_style_1";
	private static final String STYLE_2_ALIAS = "my_style_2";
	private static final String STYLE_3_ALIAS = "my_style_3";

	private static final FormStyle STYLE_1 = new FormStyle(STYLE_1_ALIAS, Color.RED, Color.DARK_GREEN, false, true);
	private static final FormStyle STYLE_2 = new FormStyle(STYLE_2_ALIAS, Color.BLUE, Color.LIGHT_YELLOW, true, false);
	private static final FormStyle STYLE_3 = new FormStyle(STYLE_3_ALIAS, Color.BLACK, Color.LIGHT_BROWN, true, true);

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
		numColumn.setPrecision(2);
		numColumn.setMaxLength(3);
		Column dateColumn = new DateColumn();
		dateColumn.setName(DATE_NAME);
		dateColumn.setAlias(DATE_ALIAS);
		temp.getColumns().addAll(Arrays.asList(new Column[] {strColumn, numColumn, dateColumn}));

		List<FormStyle> styles = new ArrayList<FormStyle>();
		styles.add(STYLE_1);
		styles.add(STYLE_2);
		temp.getStyles().addAll(styles);

		FormData fd = new FormData(temp);

		DataRow<Cell> row1 = fd.createDataRow();
		row1.setAlias(ROW1_ALIAS);
		row1.getCell(STRING_ALIAS).setValue("книга", null);
		row1.getCell(NUMBER_ALIAS).setValue(1.04, null);
		row1.getCell(NUMBER_ALIAS).setStyle(STYLE_2);
		row1.getCell(DATE_ALIAS).setValue(DATE_CONST, null);

		DataRow<Cell> row2 = fd.createDataRow();
		row2.setAlias(ROW2_ALIAS);
		row2.getCell(STRING_ALIAS).setValue("карандаш", null);
		row2.getCell(STRING_ALIAS).setStyle(STYLE_3);
		row2.getCell(NUMBER_ALIAS).setValue(2.1, null);

		DataRow<Cell> row3 = fd.createDataRow();
		row3.setAlias(ROW3_ALIAS);
		row3.getCell(STRING_ALIAS).setValue("блокнот", null);
		row3.getCell(DATE_ALIAS).setValue(DATE_CONST, null);

		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		dataRows.add(row1);
		dataRows.add(row2);
		dataRows.add(row3);

		return new Pair<FormData, List<DataRow<Cell>>>(fd, dataRows);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setValue() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		int rowIndex = 1;
		rows.get(rowIndex - 1).getCell(NUMBER_ALIAS).setValue(22.111, rowIndex);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setValue2() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		int rowIndex = 1;
		rows.get(rowIndex - 1).getCell(NUMBER_ALIAS).setValue("asd", rowIndex);
	}

	@Test
	public void setValue3() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		int rowIndex = 1;
		DataRow<Cell> row = rows.get(rowIndex - 1);
		row.getCell(NUMBER_ALIAS).setValue(2.111, rowIndex);
		assertEquals(2.11, ((BigDecimal) row.get(NUMBER_ALIAS)).doubleValue(), 1e-4); //2.11, так как обрезается до нужной точности
		row.getCell(NUMBER_ALIAS).setValue(null, rowIndex);
		assertNull(row.get(NUMBER_ALIAS));
	}

	@Test
	public void setStyleTest() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		rows.get(2).getCell(NUMBER_ALIAS).setStyle(STYLE_3);
		assertEquals(Color.LIGHT_BROWN, rows.get(2).getCell(NUMBER_ALIAS).getStyle().getBackColor());
	}

	@Deprecated
	@Test
	public void setStyleAliasTest() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		rows.get(2).getCell(NUMBER_ALIAS).setStyleAlias(STYLE_2_ALIAS);
		assertEquals(Color.LIGHT_YELLOW, rows.get(2).getCell(NUMBER_ALIAS).getStyle().getBackColor());
	}

	@Deprecated
	@Test(expected = IllegalArgumentException.class)
	public void setStyleAliasTest2() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		rows.get(2).getCell(NUMBER_ALIAS).setStyleAlias(STYLE_3_ALIAS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setRefColumnValue() {
		Pair<FormData, List<DataRow<Cell>>> testData = getTestFormData();
		List<DataRow<Cell>> rows = testData.getSecond();
		Cell cell = rows.get(2).getCell(NUMBER_ALIAS);

		Column column = new ReferenceColumn();
		cell.setColumn(column);
		cell.setValue(5L, null); // нельзя выставлять значения в зависимую графу
	}
}