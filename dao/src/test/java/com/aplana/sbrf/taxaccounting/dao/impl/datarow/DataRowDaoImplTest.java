package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRowType;
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

import static com.aplana.sbrf.taxaccounting.model.DataRowType.AUTO;

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

		assertEquals(1, dataRowDao.getRows(formData, new DataRowRange(2, 1)).size());
		assertEquals(1, dataRowDao.getRows(formData, new DataRowRange(2, 5)).size());
		assertEquals(2, dataRowDao.getRows(formData, new DataRowRange(1, 5)).size());

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
		assertEquals(1, dataRowDao.getAutoNumerationRowCount(formData));

		formData = formDataDao.get(3291, false);
		dataRowDao.restoreCheckPoint(formData);
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
		assertEquals("a1", rows.get(2).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(3).get("numericColumn")).doubleValue(), 1e-2);

		dataRowDao.insertRows(formData, 6, insertRows);
		assertEquals(8, dataRowDao.getRowCount(formData));
		rows = dataRowDao.getRows(formData, null);
		assertEquals("a1", rows.get(5).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(6).get("numericColumn")).doubleValue(), 1e-2);
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

	@Test(expected = IllegalArgumentException.class)
	public void insertRowsNull() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.insertRows(formData, 1, null);
	}

	@Test
	public void isDataRowsCountChanged() {
		FormData formData = formDataDao.get(329, false);
		assertTrue(dataRowDao.isDataRowsCountChanged(formData));
		formData = formDataDao.get(3292, false);
		assertFalse(dataRowDao.isDataRowsCountChanged(formData));
		formData = formDataDao.get(3291, false);
		assertFalse(dataRowDao.isDataRowsCountChanged(formData));
	}

	@Test
	public void removeRows() {
		FormData formData = formDataDao.get(329, false);
		// удаляем строки
		dataRowDao.removeRows(formData);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertTrue(rows.isEmpty());

		formData.setManual(true);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(1, rows.size());
		assertEquals(1000.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);

		// проверяем удаление диапазона
		formData = formDataDao.get(3291, false);
		dataRowDao.restoreCheckPoint(formData);
		dataRowDao.removeRows(formData, new DataRowRange(2, 1));
		rows = dataRowDao.getRows(formData, null);
		// проверяем размернсть и правильные значения ORD
		assertEquals(2, rows.size());
		assertEquals(1, rows.get(0).getIndex().intValue());
		assertEquals(2, rows.get(1).getIndex().intValue());
		assertEquals(666.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);
		assertEquals(50.0, ((BigDecimal) rows.get(1).get("numericColumn")).doubleValue(), 1e-2);

		// удаляем указанные строки
		rows.remove(0);
		dataRowDao.removeRows(formData, rows);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(1, rows.size());
		assertEquals(666.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);
	}

	@Test
	public void removeAllManualRows() {
		FormData formData = formDataDao.get(329, true);
		dataRowDao.removeAllManualRows(formData);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertTrue(rows.isEmpty());
	}

	@Test
	public void removeRowsSelectedAndReorderRows() {
		//удаление по списку строк
		FormData formData = formDataDao.get(329, false);
		dataRowDao.restoreCheckPoint(formData);

		List<DataRow<Cell>> deleteRows = new ArrayList<DataRow<Cell>>();
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		deleteRows.add(rows.get(1));
		dataRowDao.removeRows(formData, deleteRows);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals(666.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);
		assertEquals("total", rows.get(1).getAlias());
		assertEquals(1, rows.get(0).getIndex().intValue());
		assertEquals(2, rows.get(1).getIndex().intValue());
	}

	@Test
	public void restoreCheckPoint() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.restoreCheckPoint(formData);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(3, rows.size());
		assertEquals(0, getTempRowCount(329, AUTO));

        // проверям, что изменения не затронули другие НФ
		formData = formDataDao.get(3291, false);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(0, rows.size());
		assertEquals(3, getTempRowCount(3291, AUTO));
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
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals("a1", rows.get(0).getAlias());
		assertEquals(431.0, ((BigDecimal) rows.get(1).get("numericColumn")).doubleValue(), 1e-2);
	}

	@Test
	public void searchByKey() {
		//TODO
	}

	@Test
	public void createManual() {
		FormData formData = formDataDao.get(329, true);
		dataRowDao.createManual(formData);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertEquals(636.0, ((BigDecimal) rows.get(0).get("numericColumn")).doubleValue(), 1e-2);
		assertEquals("number", rows.get(0).get("stringColumn"));
		assertEquals("some string", rows.get(1).get("stringColumn"));

		dataRowDao.removeAllManualRows(formData);
		rows = dataRowDao.getRows(formData, null);
		assertTrue(rows.isEmpty());
	}


	@Test
	public void refreshRefBookLinks() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> insertRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> row = formData.createDataRow();
		row.setAlias("a1");
		row.put("refBookColumn", 182632);
		insertRows.add(row);
		dataRowDao.insertRows(formData, 1, insertRows);
		dataRowDao.refreshRefBookLinks(formData);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("ref_book_id", 38);

		List<Integer> recordIds = jdbc.queryForList("SELECT record_id FROM form_data_ref_book WHERE " +
				"form_data_id = :form_data_id AND ref_book_id = :ref_book_id", params, Integer.class);
		assertEquals(1, recordIds.size());
		assertEquals(Integer.valueOf(182632), recordIds.get(0));
	}

	@Test
	public void refreshRefBookLinks2() {
		FormData formData = formDataDao.get(330, false);
		List<DataRow<Cell>> insertRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> row = formData.createDataRow();
		row.setAlias("a1");
		row.put("refBookColumn1", 182632);
		insertRows.add(row);
		row = formData.createDataRow();
		row.setAlias("a1");
		row.put("refBookColumn2", 182633);
		insertRows.add(row);
		dataRowDao.insertRows(formData, 1, insertRows);
		dataRowDao.refreshRefBookLinks(formData);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("ref_book_id", 38);

		List<Integer> recordIds = jdbc.queryForList("SELECT record_id FROM form_data_ref_book WHERE " +
				"form_data_id = :form_data_id AND ref_book_id = :ref_book_id ORDER BY record_id", params, Integer.class);
		assertEquals(2, recordIds.size());
		assertEquals(Integer.valueOf(182632), recordIds.get(0));
		assertEquals(Integer.valueOf(182633), recordIds.get(1));
	}

	@Test
	public void updateRows() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		rows.get(1).getCell("stringColumn").setStringValue("new value");
		List<DataRow<Cell>> updateRows = new ArrayList<DataRow<Cell>>();
		updateRows.add(rows.get(1));
		dataRowDao.updateRows(formData, updateRows);
		rows = dataRowDao.getRows(formData, null);
		assertEquals("new value", rows.get(1).getCell("stringColumn").getStringValue());
	}

}