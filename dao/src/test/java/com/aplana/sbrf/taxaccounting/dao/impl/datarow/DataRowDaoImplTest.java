package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DataRowDaoImplTest.xml" })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataRowDaoImplTest {

	@Autowired
	FormDataDao formDataDao;

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
		Assert.assertEquals(5, dataRowDao.getSize(fd, null));
		dataRowDao.removeRows(fd, 2, 2);
		Assert.assertEquals(4, dataRowDao.getSize(fd, null));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(4, dataRowDao.getSize(fd, null));
		dataRowDao.removeRows(fd);
		Assert.assertEquals(0, dataRowDao.getSize(fd, null));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(0, dataRowDao.getSize(fd, null));
	}

	@Test
	public void getSavedSizeSuccess() {
		FormData fd = formDataDao.get(1, false);
		Assert.assertEquals(5, dataRowDao.getSavedSize(fd, null));
		dataRowDao.removeRows(fd, 2, 2);
		Assert.assertEquals(5, dataRowDao.getSavedSize(fd, null));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(4, dataRowDao.getSavedSize(fd, null));
		dataRowDao.removeRows(fd);
		Assert.assertEquals(4, dataRowDao.getSavedSize(fd, null));
		dataRowDao.commit(fd.getId());
		Assert.assertEquals(0, dataRowDao.getSavedSize(fd, null));
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
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test(expected=DaoException.class)
	public void updateRowsErrorDataRowId() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
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

	@Test(expected=IllegalArgumentException.class)
	public void maodifyAndSaveErrorDublicat() {

		FormData fd = formDataDao.get(1, false);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);

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
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
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
				dataRowDao.getRows(fd, null, new DataRowRange(1, 1)).get(0),
				dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 11, 12, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
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

		DataRow<Cell> drAfter = dataRowDao.getRows(fd, null, new DataRowRange(1, 1)).get(0);
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
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
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
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void removeRowsByIndexes3Success() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd, 1, 5);
		Assert.assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void removeRowsAll() {
		FormData fd = formDataDao.get(1, false);
		dataRowDao.removeRows(fd);
		Assert.assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
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
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void getSavedRowsSuccess() {
		FormData fd = formDataDao.get(1, false);
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null,
						null)));
	}

	@Test
	public void repackORDSuccessFirst() {
		FormData fd = formDataDao.get(1, false);
        int sizeBefore = dataRowDao.getSize(fd,null);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		for (int i = 0; i < DataRowDaoImplUtils.DEFAULT_ORDER_STEP; i++) {
			DataRow<Cell> dr = fd.createDataRow();
			dataRows.add(dr);
		}
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getRows(fd, null, null);
		dataRowDao.insertRows(fd, 1, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getRows(fd, null, null);
        Assert.assertNotEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        Assert.assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
		Assert.assertEquals(sizeBefore + DataRowDaoImplUtils.DEFAULT_ORDER_STEP, addedDataRowsAfter.size());
        //проверка сдвига, id записи должны быть равны, все сдвинулись
        for (int i  =0; i< sizeBefore; i++){
            Assert.assertEquals(addedDataRowsBefore.get(i).getId(), addedDataRowsAfter.get((int) (DataRowDaoImplUtils.DEFAULT_ORDER_STEP + i)).getId());
        }
	}

	@Test
	public void repackORDSuccessCenter() {
		FormData fd = formDataDao.get(1, false);
        //int sizeBefore = dataRowDao.getSize(fd,null);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		for (int i = 0; i < DataRowDaoImplUtils.DEFAULT_ORDER_STEP; i++) {
			DataRow<Cell> dr = fd.createDataRow();
			dataRows.add(dr);
		}
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getRows(fd, null, null);
		dataRowDao.insertRows(fd, 5, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getRows(fd, null, null);
        Assert.assertEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        Assert.assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
        //проверка сдвига, id записи должны быть равны(т.е. со сдвигом в данном случае на 100000)
        Assert.assertEquals(addedDataRowsBefore.get(4).getId(), addedDataRowsAfter.get((int) (DataRowDaoImplUtils.DEFAULT_ORDER_STEP + 4)).getId());
	}

	@Test
	public void repackORDSuccessLast() {
		FormData fd = formDataDao.get(1, false);
        int sizeBefore = dataRowDao.getSize(fd,null);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		for (int i = 0; i < DataRowDaoImplUtils.DEFAULT_ORDER_STEP; i++) {
			DataRow<Cell> dr = fd.createDataRow();
			dataRows.add(dr);
		}
        List<DataRow<Cell>> addedDataRowsBefore = dataRowDao.getRows(fd, null, null);
		dataRowDao.insertRows(fd, 6, dataRows);
        List<DataRow<Cell>> addedDataRowsAfter = dataRowDao.getRows(fd, null, null);

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
        List<DataRow<Cell>> rowsBefore = dataRowDao.getRows(fd,null,null);
        List<Long> rowIdsBefore = new ArrayList<Long>();
        for (DataRow<Cell> row : rowsBefore){
            rowIdsBefore.add(row.getId());
        }
        int sizeBefore = rowsBefore.size();
        dataRowDao.removeRows(fd,3,3);
        Assert.assertEquals(sizeBefore - 1, dataRowDao.getSize(fd,null));

        //Execute
        int sizeAfterRem = dataRowDao.getSize(fd,null);
        List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

        for (int i = 0; i < DataRowDaoImplUtils.DEFAULT_ORDER_STEP; i++) {
            DataRow<Cell> dr = fd.createDataRow();
            dataRows.add(dr);
        }
        dataRowDao.insertRows(fd, 1, dataRows);
        Assert.assertEquals(DataRowDaoImplUtils.DEFAULT_ORDER_STEP + sizeAfterRem, dataRowDao.getRows(fd, null, null).size());

        //We check after rollback that second element become first(it could be second)
        dataRowDao.rollback(fd.getId());
        Assert.assertEquals(sizeBefore, dataRowDao.getSize(fd,null));
        List<Long> rowIdsAfterRollback = new ArrayList<Long>();
        for (DataRow<Cell> row : dataRowDao.getRows(fd,null,null)) {
            rowIdsAfterRollback.add(row.getId());
        }
        //Must following with same order
        Assert.assertArrayEquals(rowIdsBefore.toArray(), rowIdsAfterRollback.toArray());
    }

}
