package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "DataRowDaoImplTest.xml" })
@Transactional
public class DataRowDaoImplTest {

	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	FormDataDao formDataDao;

	@Autowired
	DataRowDao dataRowDao;

	@Before
	public void cleanDataRow() {

		FormData fd = formDataDao.get(1);
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
			;
			result[i] = Integer.valueOf(v != null ? String.valueOf(v) : "0");
			i++;
		}
		return result;
	}

	private void checkIndexCorrect(List<DataRow<Cell>> dataRows) {
		for (int i = 0; i < dataRows.size(); i++) {
			Assert.assertEquals(Integer.valueOf(i + 1), dataRows.get(i).getIndex());	
		}
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
		FormData fd = formDataDao.get(1);
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
		FormData fd = formDataDao.get(1);
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

		FormData fd = formDataDao.get(1);
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

	@Test
	public void updateRowsSuccess() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		List<DataRow<Cell>> dataRowsForUpdate = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dr = dataRows.get(3);
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "41");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);
		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 11, 2, 3, 41, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);
	}
	
	@Test(expected=DaoException.class)
	public void updateRowsErrorDataRowId() {

		FormData fd = formDataDao.get(1);
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
	
	

	@Test
	public void updateRowsRepeatSuccess() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		List<DataRow<Cell>> dataRowsForUpdate = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dr = dataRows.get(3);
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "41");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);
		dataRowsForUpdate.clear();
		dataRows = dataRowDao.getRows(fd, null, null);

		dr = dataRows.get(0);
		dr.put("stringColumn", "111");
		dr.put("numericColumn", 1.01);
		date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);

		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 111, 2, 3, 41, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);
	}
	
	
	

	@Test
	public void updateRowsRepeat2Success() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		Set<DataRow<Cell>> dataRowsForUpdate = new HashSet<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dr = dataRows.get(3);
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "41");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);

		dr = dataRows.get(0);
		dr.put("stringColumn", "111");
		dr.put("numericColumn", 1.01);
		date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);

		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 111, 2, 3, 41, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);
	}

	@Test
	public void insertRowsByIndexCenterSuccess() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "21");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "22");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(fd, 3, dataRows);
		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 1, 2, 21, 22, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void maodifyAndSaveSuccess() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		checkIndexCorrect(dataRows);

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "21");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);

		dataRowDao.insertRows(fd, 1, Arrays.asList(dr));
		dataRows.add(1, dr);
		dataRowDao.saveRows(fd, dataRows);
		checkIndexCorrect(dataRowDao.getRows(fd, null, null));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void maodifyAndSaveErrorDublicat() {

		FormData fd = formDataDao.get(1);
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
	public void insertRowsRepeatedlySuccess() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "21");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "22");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dataRowDao.insertRows(fd, 3, dataRows);
		dataRowDao.insertRows(fd, 3, dataRows);
        for (DataRow dataRow : dataRows){
            System.out.println("Test: " + dataRow);
        }
        dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 1, 2, 21, 22, 21, 22, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);
	}

	@Test
	public void insertRowsByIndexLastSuccess() {

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

		dataRowDao.insertRows(fd, 6, dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5, 51, 52 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
		checkIndexCorrect(dataRowDao.getRows(fd, null, null));
	}

	@Test
	public void insertRowsByIndexFirstSuccess() {

		FormData fd = formDataDao.get(1);
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

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		DataRow<Cell> dr = fd.createDataRow();
		dataRows.add(dr);

		dataRowDao.insertRows(fd, 7, dataRows);
	}

	@Test
	public void insertRowsAfterFirstSuccess() {
		FormData fd = formDataDao.get(1);
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
		FormData fd = formDataDao.get(1);
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

	@Test
	public void removeRowsByIndexes1Success() {
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 2, 4);
		Assert.assertArrayEquals(
				new int[] { 1, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void removeRowsByIndexesError1() {
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 0, 4);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void removeRowsByIndexes1Error2() {
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 1, 6);
	}

	@Test
	public void removeRowsByIndexes2Success() {
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 2, 5);
		Assert.assertArrayEquals(
				new int[] { 1 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void removeRowsByIndexes3Success() {
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 1, 5);
		Assert.assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void removeRowsAll() {
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd);
		Assert.assertArrayEquals(
				new int[] {},
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void removeRowsByDataRows() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, range);
		dataRowDao.removeRows(fd, dataRows);
		Assert.assertArrayEquals(
				new int[] { 1, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void getRowsSuccess() {
		FormData fd = formDataDao.get(1);
		Assert.assertArrayEquals(
				new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}

	@Test
	public void getRowsRange1Success() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		Assert.assertArrayEquals(new int[] { 2, 3, 4 },
				dataRowsToStringColumnValues(dataRowDao
						.getRows(fd, null, range)));
	}

	@Test
	public void getRowsRange2Success() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(1, 2);
		Assert.assertArrayEquals(new int[] { 1, 2 },
				dataRowsToStringColumnValues(dataRowDao
						.getRows(fd, null, range)));
	}

	@Test
	public void getRowsRange3Success() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(5, 1);
		Assert.assertArrayEquals(new int[] { 5 },
				dataRowsToStringColumnValues(dataRowDao
						.getRows(fd, null, range)));
	}

	@Test
	public void getSavedRowsSuccess() {
		FormData fd = formDataDao.get(1);
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null,
						null)));
	}

	@Test
	public void getSavedRowsRange1Success() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		Assert.assertArrayEquals(new int[] { 2, 3, 4 },
				dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null,
						range)));
	}

	@Test
	public void getSavedRowsRange2Success() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(1, 2);
		Assert.assertArrayEquals(new int[] { 1, 2 },
				dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null,
						range)));
	}

	@Test
	public void getSavedRowsRange3Success() {
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(5, 1);
		Assert.assertArrayEquals(new int[] { 5 },
				dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null,
						range)));
	}

	@Test
	public void commitSuccess() {
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		Set<DataRow<Cell>> dataRowsForUpdate = new HashSet<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dr = dataRows.get(3);
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "41");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);

		dr = dataRows.get(0);
		dr.put("stringColumn", "111");
		dr.put("numericColumn", 1.01);
		date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);
		dataRowDao.removeRows(fd, 4, 5);
		dataRowDao.insertRows(fd, 4, dataRows);
		dataRowDao.commit(fd.getId());
		dataRowDao.rollback(fd.getId());
		dataRowDao.commit(fd.getId());
		dataRowDao.rollback(fd.getId());
		dataRowDao.commit(fd.getId());
		dataRowDao.rollback(fd.getId());

		

		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 111, 2, 3, 111, 2, 3, 41, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);

		dataRows = dataRowDao.getSavedRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 111, 2, 3, 111, 2, 3, 41, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);

	}

	@Test
	public void rollbackSuccess() {
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		Set<DataRow<Cell>> dataRowsForUpdate = new HashSet<DataRow<Cell>>();

		DataRow<Cell> dr = dataRows.get(0);
		dr.put("stringColumn", "11");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dr = dataRows.get(3);
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "41");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);

		dr = dataRows.get(0);
		dr.put("stringColumn", "111");
		dr.put("numericColumn", 1.01);
		date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRowsForUpdate.add(dr);

		dataRowDao.updateRows(fd, dataRowsForUpdate);
		dataRowDao.removeRows(fd, 4, 5);
		dataRowDao.insertRows(fd, 4, dataRows);
		dataRowDao.rollback(fd.getId());
		dataRowDao.rollback(fd.getId());
		dataRowDao.rollback(fd.getId());
		dataRowDao.commit(fd.getId());
		dataRowDao.commit(fd.getId());
		dataRowDao.commit(fd.getId());

		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);

		dataRows = dataRowDao.getSavedRows(fd, null, null);
		Assert.assertArrayEquals(new int[] { 1, 2, 3, 4, 5 },
				dataRowsToStringColumnValues(dataRows));
		checkIndexCorrect(dataRows);

	}
	
	@Test
	public void performance() {

		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

		for (int i = 0; i < 5000; i++) {
			DataRow<Cell> dr = fd.createDataRow();
			dr.put("stringColumn", String.valueOf(i + 1));
			dr.put("numericColumn", 1.01);
			Date date = getDate(2012, 11, 31);
			dr.put("dateColumn", date);
			dataRows.add(dr);
		}

		dataRowDao.removeRows(fd);
		dataRowDao.insertRows(fd, 1, dataRows);
		dataRowDao.updateRows(fd, dataRows);
		//dataRowDao.rollback(fd);
		dataRowDao.commit(fd.getId());
		//dataRowDao.rollback(fd);
		dataRows = dataRowDao.getRows(fd, null, null);
		checkIndexCorrect(dataRows);
		Assert.assertEquals(5000, dataRows.size());
	}

	@Test
	public void repackORDSuccessFirst() {
		FormData fd = formDataDao.get(1);
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
        Assert.assertEquals(addedDataRowsBefore.get(0).getId(), addedDataRowsAfter.get(100000).getId());//проверка сдвига, id записи должны быть равны
        Assert.assertEquals(DataRowDaoImplUtils.DEFAULT_ORDER_STEP + sizeBefore, dataRowDao.getSize(fd,null));
	}

	@Test
	public void repackORDSuccessCenter() {
		FormData fd = formDataDao.get(1);
        int sizeBefore = dataRowDao.getSize(fd,null);
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
        Assert.assertEquals(addedDataRowsBefore.get(4).getId(), addedDataRowsAfter.get(100004).getId());

        Assert.assertEquals(DataRowDaoImplUtils.DEFAULT_ORDER_STEP + sizeBefore, dataRowDao.getSize(fd,null));
	}

	@Test
	public void repackORDSuccessLast() {
		FormData fd = formDataDao.get(1);
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
        Assert.assertEquals(addedDataRowsAfter.get(0).getId(), addedDataRowsBefore.get(0).getId());
        Assert.assertEquals(addedDataRowsBefore.get(4).getId(), addedDataRowsAfter.get(4).getId());

        Assert.assertNotEquals(addedDataRowsAfter.size(), addedDataRowsBefore.size());
        Assert.assertEquals(DataRowDaoImplUtils.DEFAULT_ORDER_STEP + sizeBefore, dataRowDao.getSize(fd,null));
	}

}
