package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRowType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.DataRowType.AUTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DataRowDaoImplTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataRowDaoImplTest extends Assert {

	private static final Log LOG = LogFactory.getLog(DataRowDaoImplTest.class);

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

		return jdbc.queryForObject("SELECT COUNT(*) FROM form_data_row WHERE temporary = :temporary AND " +
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

		formData = formDataDao.get(3291, false);
		dataRowDao.restoreCheckPoint(formData);
		rows = dataRowDao.getRows(formData, null);
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

		formData = formDataDao.get(331, false);
		dataRowDao.refreshRefBookLinks(formData);
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
	public void refreshRefBookLinks3() {
		FormData formData = formDataDao.get(330, false);
		dataRowDao.refreshRefBookLinks(formData);
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

	@Test
	public void saveTempRows() {
		FormData formData = formDataDao.get(330, false);
		List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
		dataRowDao.saveTempRows(formData, rows);

		formData = formDataDao.get(329, false);
		rows = dataRowDao.getRows(formData, null);
		dataRowDao.saveTempRows(formData, rows);
		List<DataRow<Cell>> rowsB = dataRowDao.getRows(formData, null);
		List<DataRow<Cell>> rowsT = dataRowDao.getTempRows(formData, null);
		assertEquals(2, rowsB.size());
		assertEquals(1, rowsB.get(0).getIndex().intValue());
		assertEquals(2, rowsB.get(1).getIndex().intValue());
		assertEquals(2, rowsT.size());
		assertEquals(1, rowsT.get(0).getIndex().intValue());
		assertEquals(2, rowsT.get(1).getIndex().intValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void copyRows2() {
		dataRowDao.copyRows(329, 330);
	}

	@Test
	public void reorderRows() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		List<DataRow<Cell>> newRows = new ArrayList<DataRow<Cell>>();
		newRows.add(rows.get(1));
		newRows.add(rows.get(0));
		dataRowDao.reorderRows(formData, newRows);

		rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertNull(rows.get(0).getAlias());
		assertEquals("row_alias №1", rows.get(1).getAlias());

		List<DataRow<Cell>> emptyRows = new ArrayList<DataRow<Cell>>();
		dataRowDao.reorderRows(formData, emptyRows);
		rows = dataRowDao.getRows(formData, null);
		assertEquals(2, rows.size());
		assertNull(rows.get(0).getAlias());
		assertEquals("row_alias №1", rows.get(1).getAlias());
	}

	@Test(expected = IllegalArgumentException.class)
	public void createManual2() {
		FormData formData = formDataDao.get(329, false);
		dataRowDao.createManual(formData);
	}

	@Test
	public void getTempRows() {
		FormData formData = formDataDao.get(329, false);
		List<DataRow<Cell>> rows = dataRowDao.getTempRows(formData, null);
		assertEquals(3, dataRowDao.getTempRowCount(formData));
		assertEquals(3, rows.size());
		assertEquals("total", rows.get(2).getAlias());
	}

	@Test
	public void getColumnByDataOrdTest() {
		FormData formData = formDataDao.get(329, false);
		List<Column> columns = formData.getFormColumns();
		Column column = DataRowDaoImpl.getColumnByDataOrd(columns, 0);
		assertEquals("stringColumn", column.getAlias());
		column = DataRowDaoImpl.getColumnByDataOrd(columns, 3);
		assertEquals("autoNumerationColumn", column.getAlias());
	}

	@Test(expected = IllegalArgumentException.class)
	public void getColumnByDataOrdTest2() {
		FormData formData = formDataDao.get(329, false);
		List<Column> columns = formData.getFormColumns();
		DataRowDaoImpl.getColumnByDataOrd(columns, 99);
	}

	@Test
	public void updateChildRowTest() throws SQLException {
		FormData formData = formDataDao.get(329, false);
		DataRowMapper dataRowMapper = new DataRowMapper(formData);
		// имитация первой строки результата пейджинга
		DataRow<Cell> row = formData.createDataRow();
		row.setIndex(5);

		Connection conn = ((JdbcTemplate) jdbc.getJdbcOperations()).getDataSource().getConnection();
		Statement stmt = conn.createStatement();
		// выполняем основную операцию
		try {
			ResultSet rs = stmt.executeQuery(
				"SELECT 3 AS row_id, 1 AS x, 3 AS y, 2 AS colspan, 5 AS rowspan, '31.49' AS cell_value, 'b5-6' AS cell_style FROM DUAL UNION " +
				"SELECT 4 AS row_id, 0 AS x, 4 AS y, 1 AS colspan, 2 AS rowspan, 'string_value' AS cell_value, '3-10' AS cell_style FROM DUAL UNION " +
				"SELECT 4 AS row_id, 3 AS x, 4 AS y, 1 AS colspan, 2 AS rowspan, '52' AS cell_value, 'i0-4' AS cell_style FROM DUAL");
			while (rs.next()) {
				DataRowDaoImpl.updateChildCell(dataRowMapper, rs, row, null);
			}
		} finally {
			stmt.close();
		}
		assertEquals(1, row.getCell("stringColumn").getColSpan());
		assertEquals(1, row.getCell("stringColumn").getRowSpan());
		assertEquals("string_value", row.get("stringColumn"));
		assertEquals(2, row.getCell("numericColumn").getColSpan());
		assertEquals(3, row.getCell("numericColumn").getRowSpan());
		assertEquals(BigDecimal.valueOf(31.49), row.get("numericColumn"));
		assertEquals(1, row.getCell("dateColumn").getColSpan());
		assertEquals(1, row.getCell("dateColumn").getRowSpan());
		assertNull(row.get("dateColumn"));
		assertEquals(1, row.getCell("autoNumerationColumn").getColSpan());
		assertEquals(1, row.getCell("autoNumerationColumn").getRowSpan());
		assertNull(row.get("autoNumerationColumn"));
		assertEquals(1, row.getCell("refBookColumn").getColSpan());
		assertEquals(1, row.getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void setSpanInfo() {
		FormData formData = formDataDao.get(3293, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(8, rows.size());
		assertEquals(1, rows.get(0).getCell("stringColumn").getColSpan());
		assertEquals(1, rows.get(0).getCell("stringColumn").getRowSpan());
		assertEquals(1, rows.get(1).getCell("stringColumn").getColSpan());
		assertEquals(1, rows.get(1).getCell("stringColumn").getRowSpan());
		assertEquals(1, rows.get(2).getCell("stringColumn").getColSpan());
		assertEquals(1, rows.get(2).getCell("stringColumn").getRowSpan());
		assertEquals(1, rows.get(5).getCell("stringColumn").getColSpan());
		assertEquals(1, rows.get(5).getCell("stringColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("stringColumn").getColSpan());
		assertEquals(1, rows.get(6).getCell("stringColumn").getRowSpan());
		assertEquals(1, rows.get(7).getCell("stringColumn").getColSpan());
		assertEquals(1, rows.get(7).getCell("stringColumn").getRowSpan());

		assertEquals(1, rows.get(0).getCell("numericColumn").getColSpan());
		assertEquals(2, rows.get(0).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(1).getCell("numericColumn").getColSpan());
		assertEquals(1, rows.get(1).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(7).getCell("numericColumn").getColSpan());
		assertEquals(1, rows.get(7).getCell("numericColumn").getRowSpan());

		assertEquals(1, rows.get(0).getCell("dateColumn").getColSpan());
		assertEquals(1, rows.get(0).getCell("dateColumn").getRowSpan());
		assertEquals(1, rows.get(1).getCell("dateColumn").getColSpan());
		assertEquals(1, rows.get(1).getCell("dateColumn").getRowSpan());
		assertEquals(1, rows.get(7).getCell("dateColumn").getColSpan());
		assertEquals(1, rows.get(7).getCell("dateColumn").getRowSpan());

		assertEquals(1, rows.get(3).getCell("stringColumn").getColSpan());
		assertEquals(2, rows.get(3).getCell("stringColumn").getRowSpan());
		assertEquals(2, rows.get(2).getCell("numericColumn").getColSpan());
		assertEquals(5, rows.get(2).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(3).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(3).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(6).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("refBookColumn").getColSpan());
		assertEquals(2, rows.get(6).getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void updateRowsSpan() throws SQLException {
		FormData formData = formDataDao.get(3293, false);
		DataRowMapper dataRowMapper = new DataRowMapper(formData);

		List<DataRow<Cell>> beforeRows = dataRowDao.getRows(formData, new DataRowRange(5, 3));
		List<DataRow<Cell>> updRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> row = beforeRows.get(0);
		Cell cell = row.getCell("numericColumn");
		cell.setValue(999, null);
		cell.setColSpan(2);
		cell.setRowSpan(3);
		row.getCell("stringColumn").setValue("ss5", null);
		updRows.add(row);

		DataRowDaoImpl.DataRowCallbackHandler handler = new DataRowDaoImpl.DataRowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				//do nothing
			}
		};
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", dataRowMapper.getFormData().getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", dataRowMapper.getFormData().isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());
		params.put("row_ord", row.getIndex());
		handler.addParams(params);

		Connection conn = ((JdbcTemplate) jdbc.getJdbcOperations()).getDataSource().getConnection();
		Statement stmt = conn.createStatement();
		// выполняем основную операцию
		try {
			ResultSet rs = stmt.executeQuery(
					"SELECT 3 AS row_id, 1 AS x, 3 AS y, 2 AS colspan, 5 AS rowspan, '31.49' AS cell_value, 'b5-6' AS cell_style FROM DUAL UNION " +
							"SELECT 4 AS row_id, 0 AS x, 4 AS y, 1 AS colspan, 2 AS rowspan, 'string_value' AS cell_value, '3-10' AS cell_style FROM DUAL UNION " +
							"SELECT 4 AS row_id, 3 AS x, 4 AS y, 1 AS colspan, 2 AS rowspan, '52' AS cell_value, 'i0-4' AS cell_style FROM DUAL");
			while (rs.next()) {
				((DataRowDaoImpl) dataRowDao).updateParentCell(dataRowMapper, rs, row, handler);
			}
		} finally {
			stmt.close();
		}
		List<DataRow<Cell>> afterRows = dataRowDao.getRows(formData, null);
		assertEquals(BigDecimal.valueOf(999).setScale(2), afterRows.get(2).getCell("numericColumn").getNumericValue());
		assertEquals("ss5", afterRows.get(3).getCell("stringColumn").getStringValue());
	}

	@Test
	public void removeSpan() {
		FormData formData = formDataDao.get(3293, false);
		List<DataRow<Cell>> allRows = dataRowDao.getRows(formData, null);
		List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
		rows.add(allRows.get(2));
		rows.add(allRows.get(6));
		((DataRowDaoImpl) dataRowDao).removeSpan(rows);
		allRows = dataRowDao.getRows(formData, null);
		assertEquals(1, allRows.get(2).getCell("numericColumn").getColSpan());
		assertEquals(1, allRows.get(2).getCell("numericColumn").getRowSpan());
		assertEquals(1, allRows.get(6).getCell("autoNumerationColumn").getColSpan());
		assertEquals(1, allRows.get(6).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, allRows.get(6).getCell("refBookColumn").getColSpan());
		assertEquals(1, allRows.get(6).getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void createManualSpan() {
		FormData formData = formDataDao.get(3293, true);
		dataRowDao.createManual(formData);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);

		assertEquals(1, rows.get(3).getCell("stringColumn").getColSpan());
		assertEquals(2, rows.get(3).getCell("stringColumn").getRowSpan());
		assertEquals(2, rows.get(2).getCell("numericColumn").getColSpan());
		assertEquals(5, rows.get(2).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(3).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(3).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(6).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("refBookColumn").getColSpan());
		assertEquals(2, rows.get(6).getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void removeSpanAll() {
		FormData formData = formDataDao.get(3293, false);
		((DataRowDaoImpl) dataRowDao).removeSpanAll(formData, DataRowType.SAVED);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		assertEquals(1, rows.get(2).getCell("numericColumn").getColSpan());
		assertEquals(1, rows.get(2).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("autoNumerationColumn").getColSpan());
		assertEquals(1, rows.get(6).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("refBookColumn").getColSpan());
		assertEquals(1, rows.get(6).getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void reorderRows2() {
		FormData formData = formDataDao.get(3293, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		List<DataRow<Cell>> newRows = new ArrayList<DataRow<Cell>>();
		newRows.add(rows.get(3));
		newRows.add(rows.get(2));
		newRows.add(rows.get(7));
		newRows.add(rows.get(1));
		newRows.add(rows.get(4));
		newRows.add(rows.get(5));
		newRows.add(rows.get(0));
		newRows.add(rows.get(6));
		dataRowDao.reorderRows(formData, newRows);

		rows = dataRowDao.getRows(formData, null);
		assertEquals(1, rows.get(0).getCell("stringColumn").getColSpan());
		assertEquals(2, rows.get(0).getCell("stringColumn").getRowSpan());
		assertEquals(2, rows.get(1).getCell("numericColumn").getColSpan());
		assertEquals(5, rows.get(1).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(0).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(0).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(7).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(7).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(7).getCell("refBookColumn").getColSpan());
		assertEquals(2, rows.get(7).getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void shiftSpan() {
		FormData formData = formDataDao.get(3293, false);
		((DataRowDaoImpl) dataRowDao).shiftSpan(formData, new DataRowRange(5, 2), DataRowType.SAVED);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);

		assertEquals(1, rows.get(3).getCell("stringColumn").getColSpan());
		assertEquals(4, rows.get(3).getCell("stringColumn").getRowSpan());
		assertEquals(2, rows.get(2).getCell("numericColumn").getColSpan());
		assertEquals(7, rows.get(2).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(3).getCell("autoNumerationColumn").getColSpan());
		assertEquals(4, rows.get(3).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("autoNumerationColumn").getColSpan());
		assertEquals(2, rows.get(6).getCell("autoNumerationColumn").getRowSpan());
		assertEquals(1, rows.get(6).getCell("refBookColumn").getColSpan());
		assertEquals(2, rows.get(6).getCell("refBookColumn").getRowSpan());
	}

	@Test
	public void moveSpan() {
		FormData formData = formDataDao.get(3293, false);
		((DataRowDaoImpl) dataRowDao).moveSpan(formData, DataRowType.SAVED, DataRowType.TEMP);

		int savedCount = jdbc.queryForObject(String.format("SELECT COUNT(*) FROM form_data_row_span WHERE form_data_id = %s AND temporary = %s AND manual = %s",
				formData.getId(), DataRowType.SAVED.getCode(), DataRowType.AUTO.getCode()), new HashMap(), Integer.class);
		assertEquals(0, savedCount);
		int tempCount = jdbc.queryForObject(String.format("SELECT COUNT(*) FROM form_data_row_span WHERE form_data_id = %s AND temporary = %s AND manual = %s",
				formData.getId(), DataRowType.TEMP.getCode(), DataRowType.AUTO.getCode()), new HashMap(), Integer.class);
		assertEquals(6, tempCount);

	}

	@Test
	public void reorderSpan() {
		FormData formData = formDataDao.get(3293, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);

		dataRowDao.removeRows(formData, Arrays.asList(rows.get(3)));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		// проверяем значение индекса после сортировки
		assertEquals(6, jdbc.queryForObject("SELECT ord FROM form_data_row WHERE id = 17", new HashMap<String, Object>(), Integer.class).intValue());
		// проверяем, что индексы расположились по порядку
		rows = dataRowDao.getRows(formData, null);
		assertEquals(7, rows.size());
		for (int i = 0; i < 7; i++) {
			assertEquals(i + 1, rows.get(i).getIndex().intValue());
		}
		// проверяем результат
		List<Integer> ords = jdbc.queryForList("SELECT ord FROM form_data_row_span WHERE form_data_id = :form_data_id ORDER BY row_id, data_ord", params, Integer.class);
		assertEquals(4, ords.size());
		assertEquals(1, ords.get(0).intValue()); //11
		assertEquals(3, ords.get(1).intValue()); //13
		assertEquals(6, ords.get(2).intValue()); //17
		assertEquals(6, ords.get(3).intValue()); //17
	}

	@Test
	public void reorderSpan2() {
		FormData formData = formDataDao.get(3293, false);
		List<DataRow<Cell>> rows = dataRowDao.getRows(formData, null);
		List<DataRow<Cell>> delRows = new ArrayList<DataRow<Cell>>();
		delRows.add(rows.get(3));
		delRows.add(rows.get(6));
		showSpan(formData);
		dataRowDao.removeRows(formData, delRows);
		showSpan(formData);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		// проверяем результат
		List<Integer> ords = jdbc.queryForList("SELECT ord FROM form_data_row_span WHERE form_data_id = :form_data_id ORDER BY row_id, data_ord", params, Integer.class);
		assertEquals(2, ords.size());
		assertEquals(1, ords.get(0).intValue()); //11
		assertEquals(3, ords.get(1).intValue()); //13

		rows = dataRowDao.getRows(formData, null);
		for (int i = 0; i < 6; i++) {
			assertEquals(1, rows.get(i).getCell("stringColumn").getColSpan());
			assertEquals(1, rows.get(i).getCell("stringColumn").getRowSpan());
			assertEquals(1, rows.get(i).getCell("autoNumerationColumn").getColSpan());
			assertEquals(1, rows.get(i).getCell("autoNumerationColumn").getRowSpan());
			assertEquals(1, rows.get(i).getCell("refBookColumn").getColSpan());
			assertEquals(1, rows.get(i).getCell("refBookColumn").getRowSpan());
		}

		assertEquals(1, rows.get(0).getCell("numericColumn").getColSpan());
		assertEquals(2, rows.get(0).getCell("numericColumn").getRowSpan());
		assertEquals(1, rows.get(0).getCell("dateColumn").getColSpan());
		assertEquals(1, rows.get(0).getCell("dateColumn").getRowSpan());
		assertEquals(1, rows.get(1).getCell("dateColumn").getColSpan());
		assertEquals(1, rows.get(1).getCell("dateColumn").getRowSpan());

		assertEquals(2, rows.get(2).getCell("numericColumn").getColSpan());
		assertEquals(3, rows.get(2).getCell("numericColumn").getRowSpan());
	}

	private void showSpan(FormData formData) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("form_data_id", formData.getId());
		params.put("temporary", DataRowType.SAVED.getCode());
		params.put("manual", formData.isManual() ? DataRowType.MANUAL.getCode() : DataRowType.AUTO.getCode());

		LOG.info("span: fd.id = " + formData.getId());
		jdbc.query("SELECT row_id, data_ord, ord, colspan, rowspan FROM form_data_row_span WHERE " +
				"form_data_id = :form_data_id AND temporary = :temporary AND manual = :manual " +
				"ORDER BY row_id, data_ord", params, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				LOG.info(rs.getInt(1) + " (" + rs.getInt(2) + "; " + rs.getInt(3) + ") " + rs.getInt(4) + "-" + rs.getInt(5));
			}
		});
	}

}