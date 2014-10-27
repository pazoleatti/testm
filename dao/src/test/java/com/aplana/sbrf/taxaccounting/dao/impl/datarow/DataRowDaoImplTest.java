package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.ColumnType;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.test.BDUtilsMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DataRowDaoImplTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataRowDaoImplTest extends Assert {

	static final long DEFAULT_ORDER_STEP_TEST = DataRowDaoImplUtils.DEFAULT_ORDER_STEP / 100;

	@Autowired
	FormDataDao formDataDao;

	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	DataRowDao dataRowDao;

	@Before
	public void cleanDataRow() {
        ReflectionTestUtils.setField(dataRowDao, "dbUtils", BDUtilsMock.getBDUtils());

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "1");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		// Editable
		dr.getCell("stringColumn").setEditable(true);
		dr.getCell("numericColumn").setEditable(false);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "2");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		// Span Info
		dr.getCell("stringColumn").setColSpan(2);
		dr.getCell("numericColumn").setRowSpan(2);

		dr = fd.createDataRow();
		dr.setAlias("newAlias1");
		dr.put("stringColumn", "3");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		// Style
		dr.getCell("stringColumn").setStyleAlias(
				fd.getFormStyles().get(0).getAlias());

		dr = fd.createDataRow();
		dr.setAlias("newAlias2");
		dr.put("stringColumn", "4");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias3");
		dr.put("stringColumn", "5");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.saveRows(fd, dataRows);
		dataRowDao.commit(fd.getId());

	}

	private int[] dataRowsToStringColumnValues(List<DataRow<Cell>> dataRows) {
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
		FormData fd = formDataDao.get(1, false);
		Assert.assertEquals(5, dataRowDao.getSize(fd));
		dataRowDao.removeRows(fd, 2, 2);
		Assert.assertEquals(4, dataRowDao.getSize(fd));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(4, dataRowDao.getSize(fd));
		dataRowDao.removeRows(fd);
		Assert.assertEquals(0, dataRowDao.getSize(fd));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(0, dataRowDao.getSize(fd));
	}

	@Test
	public void getSavedSizeSuccess() {
		FormData fd = formDataDao.get(1, false);
		Assert.assertEquals(5, dataRowDao.getSavedSize(fd));
		dataRowDao.removeRows(fd, 2, 2);
		Assert.assertEquals(5, dataRowDao.getSavedSize(fd));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(4, dataRowDao.getSavedSize(fd));
		dataRowDao.removeRows(fd);
		Assert.assertEquals(4, dataRowDao.getSavedSize(fd));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(0, dataRowDao.getSavedSize(fd));
	}

	@Test
	public void saveRowsSuccess() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "100");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.saveRows(fd, dataRows);
		Assert.assertArrayEquals(
				new int[] { 100, 0 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}

	@Test(expected=DaoException.class)
	public void updateRowsErrorDataRowId() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null);
		List<DataRow<Cell>> dataRowsForUpdate = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.setId(null);
		
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);
		

		dataRowDao.updateRows(fd, dataRowsForUpdate);
	}

	/**
	 * Добавляем к существующим строкам еще несколько и проверяем итоговое количество
	 */
	@Test
	public void updateRowsTest() {
		final int count = 10;
		FormData fd = formDataDao.get(1000, false);
		List<DataRow<Cell>> dataRowsOld = dataRowDao.getRows(fd, null);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		for (int i = 0; i < count; i++) {
			DataRow<Cell> dataRow = fd.createDataRow();
			dataRows.add(dataRow);
		}
		dataRowDao.insertRows(fd, 1, dataRows);
		dataRowDao.updateRows(fd, dataRows);
		dataRowDao.commit(fd.getId());
		dataRows = dataRowDao.getRows(fd, null);
		Assert.assertEquals(dataRowsOld.size() + count, dataRows.size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void maodifyAndSaveErrorDublicat() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null);

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "21");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);

		dataRows.add(1, dr);
		dataRows.add(2, dr);
		dataRowDao.saveRows(fd, dataRows);
	}

	@Test
	public void insertRowsByIndexFirstSuccess() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "-2");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "-1");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(fd, 1, dataRows);
		Assert.assertArrayEquals(
				new int[] { -2, -1, 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void insertRowsByIndexError() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dataRows.add(dr);

		dataRowDao.insertRows(fd, 7, dataRows);
	}

	@Test
	public void insertRowsAfterFirstSuccess() {
		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "12");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(fd,
				dataRowDao.getRows(fd, new DataRowRange(1, 1)).get(0),
				dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 11, 12, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}
	
	@Test(expected=DaoException.class)
	public void insertRowsAfterErrorRowId() {
		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		DataRow<Cell> drAfter = dataRowDao.getRows(fd, new DataRowRange(1, 1)).get(0);
		drAfter.setId(null);
		dataRowDao.insertRows(fd, drAfter, dataRows);
	}

/*
	@Test
	public void insertRowsAfterLastSuccess() {
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "51");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "52");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(fd,
				dataRowDao.getRows(fd, null, new DataRowRange(5, 1)).get(0),
				dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5, 51, 52 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
*/

/*
	@Test
	public void insertRowsAfterEmptySuccess() {
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		dataRowDao.insertRows(fd,
				dataRowDao.getRows(fd, null, new DataRowRange(5, 1)).get(0),
				dataRows);
		dataRows = dataRowDao.getRows(fd, null, null);
		checkIndexCorrect(dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
		
	}
*/

	@Test
	public void removeRowsByIndexes1Success() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd, 2, 4);
		Assert.assertArrayEquals(
				new int[] { 1, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void removeRowsByIndexesError1() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd, 0, 4);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void removeRowsByIndexes1Error2() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd, 1, 6);
	}

	@Test
	public void removeRowsByIndexes2Success() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd, 2, 5);
		Assert.assertArrayEquals(
				new int[] { 1 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}

	@Test
	public void removeRowsByIndexes3Success() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd, 1, 5);
		Assert.assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}

	@Test
	public void removeRowsAll() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd);
		Assert.assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}

/*	@Test
	public void removeRowsByDataRows() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, range);
		dataRowDao.removeRows(fd, dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}*/

	@Test
	public void getRowsSuccess() {
		FormData fd = formDataDao.get(1, false);
		Assert.assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null)));
	}

	@Test
	public void getSavedRowsSuccess() {
		FormData fd = formDataDao.get(1, false);
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null)));
	}

	@Test
	public void repackORDSuccessFirst() {
		FormData fd = formDataDao.get(1, false);
        int sizeBefore = dataRowDao.getSize(fd);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		for (int i = 0; i < DEFAULT_ORDER_STEP_TEST; i++) {
			DataRow<Cell> dr = fd.createDataRow();
			dataRows.add(dr);
		}
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getRows(fd, null);
		dataRowDao.insertRows(fd, 1, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getRows(fd, null);
        Assert.assertNotEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        Assert.assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
		Assert.assertEquals(sizeBefore + DEFAULT_ORDER_STEP_TEST, addedDataRowsAfter.size());
        //проверка сдвига, id записи должны быть равны, все сдвинулись
        for (int i  =0; i< sizeBefore; i++){
            Assert.assertEquals(addedDataRowsBefore.get(i).getId(), addedDataRowsAfter.get((int) (DEFAULT_ORDER_STEP_TEST + i)).getId());
        }
	}

	private List<DataRow<Cell>> createDataRows(FormData formData, long count) {
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
		FormData fd = formDataDao.get(1, false);
        //int sizeBefore = dataRowDao.getSize(fd,null);
		List<DataRow<Cell>> dataRows = createDataRows(fd, DEFAULT_ORDER_STEP_TEST);
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getRows(fd, null);
		dataRowDao.insertRows(fd, 5, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getRows(fd, null);
        Assert.assertEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        Assert.assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
        //проверка сдвига, id записи должны быть равны(т.е. со сдвигом в данном случае на 100000)
        Assert.assertEquals(addedDataRowsBefore.get(4).getId(), addedDataRowsAfter.get((int) (DEFAULT_ORDER_STEP_TEST + 4)).getId());
	}

	@Test
	public void repackORDSuccessLast() {
		FormData fd = formDataDao.get(1, false);
        int sizeBefore = dataRowDao.getSize(fd);
		List<DataRow<Cell>> dataRows = createDataRows(fd, DEFAULT_ORDER_STEP_TEST);
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getRows(fd, null);
		dataRowDao.insertRows(fd, 6, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getRows(fd, null);

        //проверка сдвига, id записи должны быть равны(т.е. без сдвига в данном случае)
        for (int i  =0; i< sizeBefore; i++){
            Assert.assertEquals(addedDataRowsBefore.get(i).getId(), addedDataRowsAfter.get(i).getId());
        }
        Assert.assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
	}

    @Test
    public void repackORDWithRemoveRows(){
        //Prepare
        FormData fd = formDataDao.get(1, false);
        List<DataRow<Cell>> rowsBefore = dataRowDao.getRows(fd, null);
        List<Long> rowIdsBefore = new ArrayList<Long>();
        for (DataRow<Cell> row : rowsBefore){
            rowIdsBefore.add(row.getId());
        }
        int sizeBefore = rowsBefore.size();
        dataRowDao.removeRows(fd,3,3);
        Assert.assertEquals(sizeBefore - 1, dataRowDao.getSize(fd));

        //Execute
        int sizeAfterRem = dataRowDao.getSize(fd);
		List<DataRow<Cell>> dataRows = createDataRows(fd, DEFAULT_ORDER_STEP_TEST);
        dataRowDao.insertRows(fd, 1, dataRows);
        Assert.assertEquals(DEFAULT_ORDER_STEP_TEST + sizeAfterRem, dataRowDao.getRows(fd, null).size());

        //We check after rollback that second element become first(it could be second)
        dataRowDao.rollback(fd.getId());
        Assert.assertEquals(sizeBefore, dataRowDao.getSize(fd));
        List<Long> rowIdsAfterRollback = new ArrayList<Long>();
        for (DataRow<Cell> row : dataRowDao.getRows(fd,null)) {
            rowIdsAfterRollback.add(row.getId());
        }
        //Must following with same order
        Assert.assertArrayEquals(rowIdsBefore.toArray(), rowIdsAfterRollback.toArray());
    }

    /**
     * Система для текущего экземпляра НФ выполняет сравнение количества строк в табличной части до и после
     * редактирования. Количество строк в табличной части не изменено.
     */
    @Test
    public void testIsDataRowsCountChangedNotChanged() {
        FormData fd = formDataDao.get(1000L, false);

        dataRowDao.removeRows(fd, 1, 1);

        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        DataRow<Cell> dataRow = fd.createDataRow();
        rows.add(dataRow);
        dataRowDao.insertRows(fd, 1, rows);

        boolean isCountChanged = dataRowDao.isDataRowsCountChanged(1000L);
        assertFalse("Количество строк не должно было измениться", isCountChanged);
    }

    /**
     * Система для текущего экземпляра НФ выполняет сравнение количества строк в табличной части до и после
     * редактирования. Количество строк в табличной части изменено.
     */
    @Test
    public void testIsDataRowsCountChangedChanged() {
        FormData fd = formDataDao.get(1000L, false);

        dataRowDao.removeRows(fd, 1, 1);

        List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
        for (int i = 0; i < 5; i++) {
            DataRow<Cell> dataRow = fd.createDataRow();
            rows.add(dataRow);
        }
        dataRowDao.insertRows(fd, 1, rows);

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

        dataRows = dataRowDao.getRows(formData, null);
        Assert.assertEquals(2, dataRows.size());

        // Пустое удаление
        dataRowDao.cleanValue(null);
        dataRowDao.cleanValue(new ArrayList<Integer>(0));
        dataRows = dataRowDao.getRows(formData, null);
        Assert.assertEquals(2, dataRows.size());
        Assert.assertNotNull(dataRows.get(0).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(0).get("numericColumn"));
        Assert.assertNotNull(dataRows.get(0).get("dateColumn"));
        Assert.assertNotNull(dataRows.get(1).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(1).get("numericColumn"));
        Assert.assertNotNull(dataRows.get(1).get("dateColumn"));
        // Удаление несуществующих ID
        dataRowDao.cleanValue(Arrays.asList(-1, 0));
        dataRows = dataRowDao.getRows(formData, null);
        Assert.assertEquals(2, dataRows.size());
        Assert.assertNotNull(dataRows.get(0).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(0).get("numericColumn"));
        Assert.assertNotNull(dataRows.get(0).get("dateColumn"));
        Assert.assertNotNull(dataRows.get(1).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(1).get("numericColumn"));
        Assert.assertNotNull(dataRows.get(1).get("dateColumn"));
        // Удаление одного значения
        dataRowDao.cleanValue(Arrays.asList(1));
        dataRows = dataRowDao.getRows(formData, null);
        Assert.assertEquals(2, dataRows.size());
        Assert.assertNull(dataRows.get(0).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(0).get("numericColumn"));
        Assert.assertNotNull(dataRows.get(0).get("dateColumn"));
        Assert.assertNull(dataRows.get(1).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(1).get("numericColumn"));
        Assert.assertNotNull(dataRows.get(1).get("dateColumn"));
        // Удаление еще одного значения
        dataRowDao.cleanValue(Arrays.asList(1, 3));
        dataRows = dataRowDao.getRows(formData, null);
        Assert.assertEquals(2, dataRows.size());
        Assert.assertNull(dataRows.get(0).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(0).get("numericColumn"));
        Assert.assertNull(dataRows.get(0).get("dateColumn"));
        Assert.assertNull(dataRows.get(1).get("stringColumn"));
        Assert.assertNotNull(dataRows.get(1).get("numericColumn"));
        Assert.assertNull(dataRows.get(1).get("dateColumn"));
    }

    /**
     * Проверить генерацию автонумеруемых граф для последовательной нумерации
     */
    @Test
    public void getRowsWhenSerialAutoNumeration() {
        FormData fd = formDataDao.get(1000, false);
        List<DataRow<Cell>> rows = dataRowDao.getRows(fd, null);
        Assert.assertTrue(Integer.valueOf(rows.get(0).get("autoNumerationColumn").toString()).equals(1));
        Assert.assertNull(rows.get(1).get("autoNumerationColumn"));
        Assert.assertTrue(Integer.valueOf(rows.get(2).get("autoNumerationColumn").toString()).equals(2));
        Assert.assertTrue(Integer.valueOf(rows.get(3).get("autoNumerationColumn").toString()).equals(3));
        Assert.assertNull(rows.get(4).get("autoNumerationColumn"));
    }

    /**
     * Проверить генерацию автонумеруемых граф для сквозной нумерации
     */
    @Test
    public void getRowsWhenCrossAutoNumeration() {
        FormData fd = formDataDao.get(1000, false);
        fd.setPreviousRowNumber(5);
        List<DataRow<Cell>> rows = dataRowDao.getRows(fd, null);
        Assert.assertTrue(Integer.valueOf(rows.get(0).get("autoNumerationColumn").toString()).equals(6));
        Assert.assertNull(rows.get(1).get("autoNumerationColumn"));
        Assert.assertTrue(Integer.valueOf(rows.get(2).get("autoNumerationColumn").toString()).equals(7));
        Assert.assertTrue(Integer.valueOf(rows.get(3).get("autoNumerationColumn").toString()).equals(8));
        Assert.assertNull(rows.get(4).get("autoNumerationColumn"));
    }

    @Test
    public void copyRowsTest() {
        FormData fd1 = formDataDao.get(11, false);
        FormData fd2 = formDataDao.get(12, false);

        List<DataRow<Cell>> rows1s = dataRowDao.getSavedRows(fd1, null);
        List<DataRow<Cell>> rows1t = dataRowDao.getRows(fd1, null);
        List<DataRow<Cell>> rows2s = dataRowDao.getSavedRows(fd2, null);
        List<DataRow<Cell>> rows2t = dataRowDao.getRows(fd2, null);

        // Изначально временные срезы и постоянные срезы НФ должны быть пустыми
        Assert.assertEquals(0, rows1s.size());
        Assert.assertEquals(0, rows1t.size());
        Assert.assertEquals(0, rows2s.size());
        Assert.assertEquals(0, rows2t.size());

        // Заполнение постоянного среза ф1
        List<DataRow<Cell>> rows = new LinkedList<DataRow<Cell>>();
        DataRow<Cell> row = fd1.createDataRow();

        row.getCell("stringColumn").setStringValue("str");
        row.getCell("numericColumn").setNumericValue(BigDecimal.valueOf(1.33d));
        rows.add(row);

        dataRowDao.saveRows(fd1, rows);
        dataRowDao.commit(fd1.getId());

        // Копирование
        dataRowDao.copyRows(fd1.getId(), fd2.getId());

        rows1s = dataRowDao.getSavedRows(fd1, null);
        rows1t = dataRowDao.getRows(fd1, null);
        rows2s = dataRowDao.getSavedRows(fd2, null);
        rows2t = dataRowDao.getRows(fd2, null);

        Assert.assertEquals(1, rows1s.size());
        Assert.assertEquals(1, rows1t.size());
        Assert.assertEquals(0, rows2s.size());
        Assert.assertEquals(1, rows2t.size());

        Assert.assertEquals("str", rows2t.get(0).getCell("stringColumn").getValue());
        Assert.assertEquals(BigDecimal.valueOf(1.33d), rows2t.get(0).getCell("numericColumn").getValue());

        // Перенос в постоянный срез
        dataRowDao.commit(fd2.getId());

        // Копирование
        dataRowDao.copyRows(fd1.getId(), fd2.getId());

        rows1s = dataRowDao.getSavedRows(fd1, null);
        rows1t = dataRowDao.getRows(fd1, null);
        rows2s = dataRowDao.getSavedRows(fd2, null);
        rows2t = dataRowDao.getRows(fd2, null);

        Assert.assertEquals(1, rows1s.size());
        Assert.assertEquals(1, rows1t.size());
        Assert.assertEquals(1, rows2s.size());
        Assert.assertEquals(1, rows2t.size());

        Assert.assertEquals("str", rows2t.get(0).getCell("stringColumn").getValue());
        Assert.assertEquals(BigDecimal.valueOf(1.33d), rows2t.get(0).getCell("numericColumn").getValue());
    }
}
