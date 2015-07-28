package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRowType;
import static com.aplana.sbrf.taxaccounting.model.DataRowType.*;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DataRowDaoImplTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataRowDaoImplTest extends Assert {

	@Autowired
	FormDataDao formDataDao;

	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	DataRowDao dataRowDao;
	
	@Autowired
	NamedParameterJdbcTemplate jdbc;

	/**
	 * Вспомогательный метод для получения количества строк НФ в точке восстановления
	 */
	private int getTempRowCount(int formDataId, DataRowType manual) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formDataId);
		params.put("temporary", DataRowType.TEMP.getCode());
		params.put("manual", manual.getCode());

		return jdbc.queryForObject("SELECT COUNT(*) FROM form_data_329 WHERE temporary = :temporary AND " +
				"form_data_id = :form_data_id AND manual = :manual", params, Integer.class);
	}

	@Test
	public void removeCheckPoint() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.removeCheckPoint(formData);
		assertEquals(0, getTempRowCount(329, AUTO));
		assertEquals(2, dataRowDao.getRowCount(formData));
		assertEquals(3, getTempRowCount(3291, AUTO));
	}

	@Test
	public void copyRows() {
		dataRowDao.copyRows(329, 3291);
		FormData formData = formDataDao.get(3291, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals("some string", rows.get(1).get("stringColumn"));
	}

	@Test
	public void createCheckPoint() {
		FormData formData = formDataDao.get(329, false);
		// создаем точку восстановления
		dataRowDao.createCheckPoint(formData);
		assertEquals(2, getTempRowCount(329, AUTO));
		// удаляем строки
		dataRowDao.removeRows(formData);
		assertEquals(0, dataRowDao.getRowCount(formData));
		assertEquals(2, getTempRowCount(329, AUTO));
		// в других НФ ничего не поменялось
		formData = formDataDao.get(3291, false);
		assertEquals(0, dataRowDao.getRowCount(formData));
		assertEquals(3, getTempRowCount(3291, AUTO));
	}

	@Test
	public void getRows() {
		FormData formData = formDataDao.get(329, false);

		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals(636.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);
		assertEquals("number", rows.get(0).get("stringColumn"));
		assertEquals("some string", rows.get(1).get("stringColumn"));

		formData.setManual(true);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(1, rows.size());
		assertEquals(1000.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);

		formData = formDataDao.get(3291, false);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(0, rows.size());
	}

	@Test
	public void getRowCount() {
		FormData formData = formDataDao.get(329, false);
		assertEquals(2, dataRowDao.getRowCount(formData));
		formData.setManual(true);
		assertEquals(1, dataRowDao.getRowCount(formData));

		formData = formDataDao.get(3291, false);
		assertEquals(0, dataRowDao.getRowCount(formData));
		formData.setManual(true);
		assertEquals(0, dataRowDao.getRowCount(formData));
	}

	@Test
	public void getAutoNumerationRowCount() {
		FormData formData = formDataDao.get(329, false);
		assertEquals(1, dataRowDao.getAutoNumerationRowCount(formData));
		formData.setManual(true);
		assertEquals(0, dataRowDao.getAutoNumerationRowCount(formData));

		formData = formDataDao.get(3291, false);
		assertEquals(2, dataRowDao.getAutoNumerationRowCount(formData));
		formData.setManual(true);
		assertEquals(0, dataRowDao.getAutoNumerationRowCount(formData));
	}

	@Test
	public void insertRows() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> insertRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> row = formData.createDataRow();
		row.setAlias("a1");
		row.put("stringColumn", "s1");
		row.put("dateColumn", new Date());
		insertRows.add(row);
		row = formData.createDataRow();
		row.put("stringColumn", "s2");
		row.put("numericColumn", 431);
		insertRows.add(row);
		dataRowDao.insertRows(formData, 1, insertRows);
		assertEquals(4, dataRowDao.getRowCount(formData));
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals("a1", rows.get(0).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(1).get("numericColumn")).doubleValue(), 1e-2);
		assertEquals(3, getTempRowCount(329, AUTO));

		dataRowDao.insertRows(formData, 3, insertRows);
		assertEquals(3, insertRows.get(0).getIndex().intValue());
		assertEquals(4, insertRows.get(1).getIndex().intValue());
		assertEquals(6, dataRowDao.getRowCount(formData));
		rows = dataRowDao.getRows(formData, null);
		assertEquals("a1", rows.get(3).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(4).get("numericColumn")).doubleValue(), 1e-2);

		dataRowDao.insertRows(formData, 6, insertRows);
		assertEquals(8, dataRowDao.getRowCount(formData));
		rows = dataRowDao.getRows(formData, null);
		assertEquals("a1", rows.get(7).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(8).get("numericColumn")).doubleValue(), 1e-2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void insertRows0() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.insertRows(formData, 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void insertRowsMax() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.insertRows(formData, 1000000, null);
	}

	@Test
	public void isDataRowsCountChanged() {
		FormData formData = formDataDao.get(329, false);
		assertTrue(dataRowDao.isDataRowsCountChanged(formData));
		dataRowDao.createCheckPoint(formData);
		assertFalse(dataRowDao.isDataRowsCountChanged(formData));
	}

	@Test
	public void removeRows() {
		FormData formData = formDataDao.get(329, false);
		// удаляем строки из временного среза
		dataRowDao.removeRows(formData);
		List<DataRow<Cell>> rows = dataRowDao.getTempRows(formData, null);
		assertTrue(rows.isEmpty());
		// проверяем, что другие срезы не пострадали в ходе удаления
		rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());

		formData.setManual(true);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(1, rows.size());
		assertEquals(1000.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);

		dataRowDao.removeAllManualRows(formData);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(0, rows.size());
		// проверяем удаление диапазона
		formData = formDataDao.get(3291, false);
		dataRowDao.removeRows(formData, new DataRowRange(2, 1));
		rows = dataRowDao.getTempRows(formData, null);
		// проверяем размернсть и правильные значения ORD
		assertEquals(2, rows.size());
		assertEquals(1, rows.get(0).getIndex().intValue());
		assertEquals(2, rows.get(1).getIndex().intValue());
		// сохраняем временный срез в постоянный
		dataRowDao.removeCheckPoint(formData);
		rows = dataRowDao.getTempRows(formData, null);
		assertTrue(rows.isEmpty());
		rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
	}

	@Test
	public void removeRowsSelectedAndReorderRows() {
		//удаление по списку строк
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> deleteRows = new ArrayList<DataRow<Cell>>();
		List<DataRow<Cell>> rows = dataRowDao.getTempRows(formData, null);
		deleteRows.add(rows.get(1));
		dataRowDao.removeRows(formData, deleteRows);
		rows = dataRowDao.getTempRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals(666.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);
		assertEquals("total", rows.get(1).getAlias());
		assertEquals(1, rows.get(0).getIndex().intValue());
		assertEquals(2, rows.get(1).getIndex().intValue());
	}

	@Test
	public void rollback() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.restoreCheckPoint(formData);
		List<DataRow<Cell>> rows = dataRowDao.getTempRows(formData, null);
		assertTrue(rows.isEmpty());
		rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		// проверям, что изменения не затронули другие НФ
		formData = formDataDao.get(3291, false);
		rows = dataRowDao.getTempRows(formData, null);
		assertEquals(3, rows.size());
	}

	@Test
	public void saveRows() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> insertRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> row = formData.createDataRow();
		row.setAlias("a1");
		row.put("stringColumn", "s1");
		row.put("dateColumn", new Date());
		insertRows.add(row);
		row = formData.createDataRow();
		row.put("stringColumn", "s2");
		row.put("numericColumn", 431);
		insertRows.add(row);
		dataRowDao.saveRows(formData, insertRows);
		List<DataRow<Cell>> rows = dataRowDao.getTempRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals("a1", rows.get(0).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(1).get("numericColumn")).doubleValue(), 1e-2);
	}

	@Test
	public void searchByKey() {
		//TODO
	}

	@Test
	public void updateRows() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> rows = dataRowDao.getTempRows(formData, null);
		rows.get(1).getCell("stringColumn").setStringValue("new value");
		List<DataRow<Cell>> updateRows = new ArrayList<DataRow<Cell>>();
		updateRows.add(rows.get(1));
		dataRowDao.updateRows(formData, updateRows);
		rows = dataRowDao.getTempRows(formData, null);
		assertEquals("new value", rows.get(1).getCell("stringColumn").getStringValue());
	}

	/*@Before
	public void cleanDataRow() {
        ReflectionTestUtils.setField(dataRowDao, "dbUtils", BDUtilsMock.getBDUtils());

		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "1");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		// Editable
		dr.getCell("stringColumn").setEditable(true);
		dr.getCell("numericColumn").setEditable(false);

		dr = formData.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "2");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		// Span Info
		dr.getCell("stringColumn").setColSpan(2);
		dr.getCell("numericColumn").setRowSpan(2);

		dr = formData.createDataRow();
		dr.setAlias("newAlias1");
		dr.put("stringColumn", "3");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		// Style
		dr.getCell("stringColumn").setStyleAlias(
				formData.getFormStyles().get(0).getAlias());

		dr = formData.createDataRow();
		dr.setAlias("newAlias2");
		dr.put("stringColumn", "4");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = formData.createDataRow();
		dr.setAlias("newAlias3");
		dr.put("stringColumn", "5");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.saveRows(formData, dataRows);
		dataRowDao.removeCheckPoint(formData);
	}*/

	/*private int[] dataRowsToStringColumnValues(List<DataRow<Cell>> dataRows) {
		int[] result = new int[dataRows.size()];
		int i = 0;
		for (DataRow<Cell> dataRow : dataRows) {
			Object v = dataRow.get("stringColumn");
			result[i] = Integer.valueOf(v != null ? String.valueOf(v) : "0");
			i++;
		}
		return result;
	}

	private Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Test
	public void getSizeSuccess() {
		FormData formData = formDataDao.get(1, false);
		assertEquals(5, dataRowDao.getTempSize(formData));
		dataRowDao.removeRows(formData, 2, 2);
		assertEquals(4, dataRowDao.getTempSize(formData));
		dataRowDao.removeCheckPoint(formData);
		assertEquals(4, dataRowDao.getTempSize(formData));
		dataRowDao.removeRows(formData);
		assertEquals(0, dataRowDao.getTempSize(formData));
		dataRowDao.removeCheckPoint(formData);
		assertEquals(0, dataRowDao.getTempSize(formData));
	}

	@Test
	public void getSavedSizeSuccess() {
		FormData formData = formDataDao.get(1, false);
		assertEquals(5, dataRowDao.getRowCount(formData));
		dataRowDao.removeRows(formData, 2, 2);
		assertEquals(5, dataRowDao.getRowCount(formData));
		dataRowDao.removeCheckPoint(formData);
		assertEquals(4, dataRowDao.getRowCount(formData));
		dataRowDao.removeRows(formData);
		assertEquals(4, dataRowDao.getRowCount(formData));
		dataRowDao.removeCheckPoint(formData));
		assertEquals(0, dataRowDao.getRowCount(formData));
	}

	@Test
	public void saveRowsSuccess() {

		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "100");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = formData.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.saveRows(formData, dataRows);
		assertArrayEquals(
				new int[] { 100, 0 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test(expected=DaoException.class)
	public void updateRowsErrorDataRowId() {

		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = dataRowDao.getTempRows(formData, null);
		List<DataRow<Cell>> dataRowsForUpdate = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.setId(null);

		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);


		dataRowDao.updateRows(formData, dataRowsForUpdate);
	}

	*//**
	 * Добавляем к существующим строкам еще несколько и проверяем итоговое количество
	 *//*
	@Test
	public void updateRowsTest() {
		final int count = 10;
		FormData formData = formDataDao.get(1000, false);
		List<DataRow<Cell>> dataRowsOld = dataRowDao.getTempRows(formData, null);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		for (int i = 0; i < count; i++) {
			DataRow<Cell> dataRow = formData.createDataRow();
			dataRows.add(dataRow);
		}
		dataRowDao.insertRows(formData, 1, dataRows);
		dataRowDao.updateRows(formData, dataRows);
		dataRowDao.removeCheckPoint(formData);
		dataRows = dataRowDao.getTempRows(formData, null);
		assertEquals(dataRowsOld.size() + count, dataRows.size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void maodifyAndSaveErrorDublicat() {

		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = dataRowDao.getTempRows(formData, null);

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "21");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);

		dataRows.add(1, dr);
		dataRows.add(2, dr);
		dataRowDao.saveRows(formData, dataRows);
	}

	@Test
	public void insertRowsByIndexFirstSuccess() {

		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "-2");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = formData.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "-1");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(formData, 1, dataRows);
		assertArrayEquals(
				new int[] { -2, -1, 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void insertRowsByIndexError() {

		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dataRows.add(dr);

		dataRowDao.insertRows(formData, 7, dataRows);
	}

	@Test
	public void insertRowsAfterFirstSuccess() {
		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = formData.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "12");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(formData,
				dataRowDao.getTempRows(formData, new DataRowRange(1, 1)).get(0),
				dataRows);
		assertArrayEquals(
				new int[] { 1, 11, 12, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test(expected=DaoException.class)
	public void insertRowsAfterErrorRowId() {
		FormData formData = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		DataRow<Cell> drAfter = dataRowDao.getTempRows(formData, new DataRowRange(1, 1)).get(0);
		drAfter.setId(null);
		dataRowDao.insertRows(formData, drAfter, dataRows);
	}*/

/*
	@Test
	public void insertRowsAfterLastSuccess() {
		FormData formData = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = formData.createDataRow();
		dr.put("stringColumn", "51");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = formData.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "52");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(formData,
				dataRowDao.getTempRows(formData, null, new DataRowRange(5, 1)).get(0),
				dataRows);
		assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5, 51, 52 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null, null)));
	}
*/

/*
	@Test
	public void insertRowsAfterEmptySuccess() {
		FormData formData = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		dataRowDao.insertRows(formData,
				dataRowDao.getTempRows(formData, null, new DataRowRange(5, 1)).get(0),
				dataRows);
		dataRows = dataRowDao.getTempRows(formData, null, null);
		checkIndexCorrect(dataRows);
		assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null, null)));

	}
*/

	/*@Test
	public void removeRowsByIndexes1Success() {
		FormData formData = formDataDao.get(1, false);
		dataRowDao.removeRows(formData, 2, 4);
		assertArrayEquals(
				new int[] { 1, 5 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test(expected=IllegalArgumentException.class)
	public void removeRowsByIndexesError1() {
		FormData formData = formDataDao.get(1, false);
		dataRowDao.removeRows(formData, 0, 4);
	}

	@Test(expected=IllegalArgumentException.class)
	public void removeRowsByIndexes1Error2() {
		FormData formData = formDataDao.get(1, false);
		dataRowDao.removeRows(formData, 1, 6);
	}

	@Test
	public void removeRowsByIndexes2Success() {
		FormData formData = formDataDao.get(1, false);
		dataRowDao.removeRows(formData, 2, 5);
		assertArrayEquals(
				new int[] { 1 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test
	public void removeRowsByIndexes3Success() {
		FormData formData = formDataDao.get(1, false);
		dataRowDao.removeRows(formData, 1, 5);
		assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test
	public void removeRowsAll() {
		FormData formData = formDataDao.get(1, false);
		dataRowDao.removeRows(formData);
		assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

*//*	@Test
	public void removeRowsByDataRows() {
		FormData formData = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		List<DataRow<Cell>> dataRows = dataRowDao.getTempRows(formData, null, range);
		dataRowDao.removeRows(formData, dataRows);
		assertArrayEquals(
				new int[] { 1, 5 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null, null)));
	}*//*

	@Test
	public void getRowsSuccess() {
		FormData formData = formDataDao.get(1, false);
		assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getTempRows(formData, null)));
	}

	@Test
	public void getSavedRowsSuccess() {
		FormData formData = formDataDao.get(1, false);
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(formData, null)));
	}

	@Test
	public void repackORDSuccessFirst() {
		FormData formData = formDataDao.get(1, false);
        int sizeBefore = dataRowDao.getTempSize(formData);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		for (int i = 0; i < DEFAULT_ORDER_STEP_TEST; i++) {
			DataRow<Cell> dr = formData.createDataRow();
			dataRows.add(dr);
		}
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getTempRows(formData, null);
		dataRowDao.insertRows(formData, 1, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getTempRows(formData, null);
        assertNotEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
		assertEquals(sizeBefore + DEFAULT_ORDER_STEP_TEST, addedDataRowsAfter.size());
        //проверка сдвига, id записи должны быть равны, все сдвинулись
        for (int i  =0; i< sizeBefore; i++){
            assertEquals(addedDataRowsBefore.get(i).getId(), addedDataRowsAfter.get((int) (DEFAULT_ORDER_STEP_TEST + i)).getId());
        }
	}*/

	/*private List<DataRow<Cell>> createDataRows(FormData formData, long count) {
		FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());


		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		for (int i = 0; i < count; i++) {
			DataRow<Cell> dataRow = formData.createDataRow();
			for (Column column : formTemplate.getColumns()) {
				Object value = null;
				switch (column.getColumnType()) {
					case STRING: value = Long.valueOf(Math.round(Math.random() * 100000)).toString();
						break;
					case DATE: value = new Date(new Date().getTime() - 50000000 + Math.round(Math.random() * 100000000));
						break;
					case NUMBER: value = Math.round(Math.random() * 100000);
						break;
				}
				dataRow.put(column.getAlias(), value);
			}
			dataRows.add(dataRow);
		}
		return dataRows;
	}

	@Test
	public void repackORDSuccessCenter() {
		FormData formData = formDataDao.get(1, false);
        //int sizeBefore = dataRowDao.getTempSize(formData,null);
		List<DataRow<Cell>> dataRows = createDataRows(formData, DEFAULT_ORDER_STEP_TEST);
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getTempRows(formData, null);
		dataRowDao.insertRows(formData, 5, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getTempRows(formData, null);
        assertEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
        //проверка сдвига, id записи должны быть равны(т.е. со сдвигом в данном случае на 100000)
        assertEquals(addedDataRowsBefore.get(4).getId(), addedDataRowsAfter.get((int) (DEFAULT_ORDER_STEP_TEST + 4)).getId());
	}

	@Test
	public void repackORDSuccessLast() {
		FormData formData = formDataDao.get(1, false);
        int sizeBefore = dataRowDao.getTempSize(formData);
		List<DataRow<Cell>> dataRows = createDataRows(formData, DEFAULT_ORDER_STEP_TEST);
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getTempRows(formData, null);
		dataRowDao.insertRows(formData, 6, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getTempRows(formData, null);

        //проверка сдвига, id записи должны быть равны(т.е. без сдвига в данном случае)
        for (int i  =0; i< sizeBefore; i++){
            assertEquals(addedDataRowsBefore.get(i).getId(), addedDataRowsAfter.get(i).getId());
        }
        assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
	}

    @Test
    public void repackORDWithRemoveRows(){
        //Prepare
        FormData formData = formDataDao.get(1, false);
        List<DataRow<Cell>> rowsBefore = dataRowDao.getTempRows(formData, null);
        List<Long> rowIdsBefore = new ArrayList<Long>();
        for (DataRow<Cell> row : rowsBefore){
            rowIdsBefore.add(row.getId());
        }
        int sizeBefore = rowsBefore.size();
        dataRowDao.removeRows(formData,3,3);
        assertEquals(sizeBefore - 1, dataRowDao.getTempSize(formData));

        //Execute
        int sizeAfterRem = dataRowDao.getTempSize(formData);
		List<DataRow<Cell>> dataRows = createDataRows(formData, DEFAULT_ORDER_STEP_TEST);
        dataRowDao.insertRows(formData, 1, dataRows);
        assertEquals(DEFAULT_ORDER_STEP_TEST + sizeAfterRem, dataRowDao.getTempRows(formData, null).size());

        //We check after restoreCheckPoint that second element become first(it could be second)
        dataRowDao.restoreCheckPoint(formData);
        assertEquals(sizeBefore, dataRowDao.getTempSize(formData));
        List<Long> rowIdsAfterRollback = new ArrayList<Long>();
        for (DataRow<Cell> row : dataRowDao.getTempRows(formData,null)) {
            rowIdsAfterRollback.add(row.getId());
        }
        //Must following with same order
        assertArrayEquals(rowIdsBefore.toArray(), rowIdsAfterRollback.toArray());
    }

    *//**
     * Система для текущего экземпляра НФ выполняет сравнение количества строк в табличной части до и после
     * редактирования. Количество строк в табличной части не изменено.
     *//*
    @Test
    public void testIsDataRowsCountChangedNotChanged() {
        FormData formData = formDataDao.get(1000L, false);

        dataRowDao.removeRows(formData, 1, 1);

        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> dataRow = formData.createDataRow();
        rows.add(dataRow);
        dataRowDao.insertRows(formData, 1, rows);

        boolean isCountChanged = dataRowDao.isDataRowsCountChanged(1000L);
        assertFalse("Количество строк не должно было измениться", isCountChanged);
    }

    *//**
     * Система для текущего экземпляра НФ выполняет сравнение количества строк в табличной части до и после
     * редактирования. Количество строк в табличной части изменено.
     *//*
    @Test
    public void testIsDataRowsCountChangedChanged() {
        FormData formData = formDataDao.get(1000L, false);

        dataRowDao.removeRows(formData, 1, 1);

        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        for (int i = 0; i < 5; i++) {
            DataRow<Cell> dataRow = formData.createDataRow();
            rows.add(dataRow);
        }
        dataRowDao.insertRows(formData, 1, rows);

        boolean isCountChanged = dataRowDao.isDataRowsCountChanged(1000L);
        assertTrue("Количество строк должно было измениться", isCountChanged);
    }

    @Test
    public void cleanValueTest() {
        FormData formData = formDataDao.get(1000L, false);

        // Исходные данные
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>(2);
        DataRow<Cell> row1 = formData.createDataRow();
        DataRow<Cell> row2 = formData.createDataRow();
        dataRows.add(row1);
        dataRows.add(row2);

        row1.put("stringColumn", "1");
        row1.put("numericColumn", 1);
        row1.put("dateColumn", new Date());

        row2.put("stringColumn", "2");
        row2.put("numericColumn", 2);
        row2.put("dateColumn", new Date());

        dataRowDao.saveRows(formData, dataRows);

        dataRows = dataRowDao.getTempRows(formData, null);
        assertEquals(2, dataRows.size());

        // Пустое удаление
        dataRowDao.cleanValue(null);
        dataRowDao.cleanValue(new ArrayList<Integer>(0));
        dataRows = dataRowDao.getTempRows(formData, null);
        assertEquals(2, dataRows.size());
        assertNotNull(dataRows.get(0).get("stringColumn"));
        assertNotNull(dataRows.get(0).get("numericColumn"));
        assertNotNull(dataRows.get(0).get("dateColumn"));
        assertNotNull(dataRows.get(1).get("stringColumn"));
        assertNotNull(dataRows.get(1).get("numericColumn"));
        assertNotNull(dataRows.get(1).get("dateColumn"));
        // Удаление несуществующих ID
        dataRowDao.cleanValue(Arrays.asList(-1, 0));
        dataRows = dataRowDao.getTempRows(formData, null);
        assertEquals(2, dataRows.size());
        assertNotNull(dataRows.get(0).get("stringColumn"));
        assertNotNull(dataRows.get(0).get("numericColumn"));
        assertNotNull(dataRows.get(0).get("dateColumn"));
        assertNotNull(dataRows.get(1).get("stringColumn"));
        assertNotNull(dataRows.get(1).get("numericColumn"));
        assertNotNull(dataRows.get(1).get("dateColumn"));
        // Удаление одного значения
        dataRowDao.cleanValue(Arrays.asList(1));
        dataRows = dataRowDao.getTempRows(formData, null);
        assertEquals(2, dataRows.size());
        assertNull(dataRows.get(0).get("stringColumn"));
        assertNotNull(dataRows.get(0).get("numericColumn"));
        assertNotNull(dataRows.get(0).get("dateColumn"));
        assertNull(dataRows.get(1).get("stringColumn"));
        assertNotNull(dataRows.get(1).get("numericColumn"));
        assertNotNull(dataRows.get(1).get("dateColumn"));
        // Удаление еще одного значения
        dataRowDao.cleanValue(Arrays.asList(1, 3));
        dataRows = dataRowDao.getTempRows(formData, null);
        assertEquals(2, dataRows.size());
        assertNull(dataRows.get(0).get("stringColumn"));
        assertNotNull(dataRows.get(0).get("numericColumn"));
        assertNull(dataRows.get(0).get("dateColumn"));
        assertNull(dataRows.get(1).get("stringColumn"));
        assertNotNull(dataRows.get(1).get("numericColumn"));
        assertNull(dataRows.get(1).get("dateColumn"));
    }

    @Test
    public void copyRowsTest() {
        FormData fd1 = formDataDao.get(11, false);
        FormData fd2 = formDataDao.get(12, false);

        List<DataRow<Cell>> rows1s = dataRowDao.getRows(fd1, null);
        List<DataRow<Cell>> rows1t = dataRowDao.getTempRows(fd1, null);
        List<DataRow<Cell>> rows2s = dataRowDao.getRows(fd2, null);
        List<DataRow<Cell>> rows2t = dataRowDao.getTempRows(fd2, null);

        // Изначально временные срезы и постоянные срезы НФ должны быть пустыми
        assertEquals(0, rows1s.size());
        assertEquals(0, rows1t.size());
        assertEquals(0, rows2s.size());
        assertEquals(0, rows2t.size());

        // Заполнение постоянного среза ф1
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        DataRow<Cell> row = fd1.createDataRow();

        row.getCell("stringColumn").setStringValue("str");
        row.getCell("numericColumn").setNumericValue(BigDecimal.valueOf(1.33d));
        rows.add(row);

        dataRowDao.saveRows(fd1, rows);
        dataRowDao.removeCheckPoint(fd1);

        // Копирование
        dataRowDao.copyRows(fd1.getId(), fd2.getId());

        rows1s = dataRowDao.getRows(fd1, null);
        rows1t = dataRowDao.getTempRows(fd1, null);
        rows2s = dataRowDao.getRows(fd2, null);
        rows2t = dataRowDao.getTempRows(fd2, null);

        assertEquals(1, rows1s.size());
        assertEquals(1, rows1t.size());
        assertEquals(0, rows2s.size());
        assertEquals(1, rows2t.size());

        assertEquals("str", rows2t.get(0).getCell("stringColumn").getValue());
        assertEquals(BigDecimal.valueOf(1.33d), rows2t.get(0).getCell("numericColumn").getValue());

        // Перенос в постоянный срез
        dataRowDao.removeCheckPoint(fd2);

        // Копирование
        dataRowDao.copyRows(fd1.getId(), fd2.getId());

        rows1s = dataRowDao.getRows(fd1, null);
        rows1t = dataRowDao.getTempRows(fd1, null);
        rows2s = dataRowDao.getRows(fd2, null);
        rows2t = dataRowDao.getTempRows(fd2, null);

        assertEquals(1, rows1s.size());
        assertEquals(1, rows1t.size());
        assertEquals(1, rows2s.size());
        assertEquals(1, rows2t.size());

        assertEquals("str", rows2t.get(0).getCell("stringColumn").getValue());
        assertEquals(BigDecimal.valueOf(1.33d), rows2t.get(0).getCell("numericColumn").getValue());
    }

    @Test
    public void saveSortRowsWithTmpInsertRows() {
        FormData formData = formDataDao.get(1000, false);
        // получить 5 строк из временного среза
        List<DataRow<Cell>> dataRows1 = dataRowDao.getTempRows(formData, null);

        // удаленные
        List<DataRow<Cell>> deleteRows = new ArrayList<DataRow<Cell>>();
        deleteRows.add(dataRows1.get(0));
        deleteRows.add(dataRows1.get(1));
        dataRowDao.removeRows(formData, deleteRows);

        // добавленные
        List<DataRow<Cell>> insertRows = new ArrayList<DataRow<Cell>>();
        insertRows.add(formData.createDataRow());
        insertRows.add(formData.createDataRow());
        dataRowDao.insertRows(formData, 3, insertRows);

        // получить временный срез после изменении
        dataRows1 = dataRowDao.getTempRows(formData, null);

        // изменить сортировку
        Collections.sort(dataRows1, new Comparator<DataRow<Cell>>() {
            @Override
            public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                return o2.getId().compareTo(o1.getId());
            }
        });

        // сохранить сортировку
        dataRowDao.reorderRows(formData, dataRows1);

        // получить строки из временного среза
        List<DataRow<Cell>> dataRows2 = dataRowDao.getTempRows(formData, null);

        // проверка - после удаления стало на одну строку меньше
        assertEquals(dataRows1.size(), dataRows2.size());

        // проверить что оставшиеся строки отсортированы в обратном порядке
        if (dataRows2.size() == dataRows1.size()) {
            for (int i = 0; i < dataRows2.size(); i++) {
                int id1 = dataRows1.get(i).getId().intValue();
                int id2 = dataRows2.get(i).getId().intValue();
                assertEquals(id1, id2);
            }
        }
    }*/

}
