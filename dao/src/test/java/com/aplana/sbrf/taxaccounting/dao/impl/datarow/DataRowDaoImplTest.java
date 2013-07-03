package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DataRowDaoImplTest.xml"})
@Transactional
public class DataRowDaoImplTest {
	
	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	FormDataDao formDataDao;
	
	@Autowired
	DataRowDao dataRowDao;
	
	@Before
	public void cleanDataRow(){
		
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		
		DataRow<Cell> dr = fd.createDataRow();
		dr.put("stringColumn", "1");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "2");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		
		dr = fd.createDataRow();
		dr.setAlias("newAlias1");
		dr.put("stringColumn", "3");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		
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
		dataRowDao.commit(fd);

	}
	
	private int[] dataRowsToStringColumnValues(List<DataRow<Cell>> dataRows) {
		int[] result = new int[dataRows.size()];
		int i = 0;
		for (DataRow<Cell> dataRow : dataRows) {
			 Object v = dataRow.get("stringColumn");;
			 result[i] = Integer.valueOf(v!=null ? String.valueOf(v) : "0");
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
	public void getSizeSuccess(){
		FormData fd = formDataDao.get(1);
		Assert.assertEquals(5, dataRowDao.getSize(fd, null));
		dataRowDao.removeRows(fd, 2, 2);
		Assert.assertEquals(4, dataRowDao.getSize(fd, null));
		dataRowDao.commit(fd);
		Assert.assertEquals(4, dataRowDao.getSize(fd, null));
		dataRowDao.removeRows(fd);
		Assert.assertEquals(0, dataRowDao.getSize(fd, null));
		dataRowDao.commit(fd);
		Assert.assertEquals(0, dataRowDao.getSize(fd, null));
	}
	
	@Test
	public void getSavedSizeSuccess(){
		FormData fd = formDataDao.get(1);
		Assert.assertEquals(5, dataRowDao.getSavedSize(fd, null));
		dataRowDao.removeRows(fd, 2, 2);
		Assert.assertEquals(5, dataRowDao.getSavedSize(fd, null));
		dataRowDao.commit(fd);
		Assert.assertEquals(4, dataRowDao.getSavedSize(fd, null));
		dataRowDao.removeRows(fd);
		Assert.assertEquals(4, dataRowDao.getSavedSize(fd, null));
		dataRowDao.commit(fd);
		Assert.assertEquals(0, dataRowDao.getSavedSize(fd, null));
	}
	
	@Test
	public void saveRowsSuccess(){
		
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
		Assert.assertArrayEquals(new int[]{100, 0}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void insertRowsByIndexCenterSuccess(){
		
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
		Assert.assertArrayEquals(new int[]{1, 2, 21, 22, 3, 4, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void insertRowsByIndexLastSuccess(){
		
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
		Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5, 51, 52}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void insertRowsByIndexFirstSuccess(){
		
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
		Assert.assertArrayEquals(new int[]{-2, -1, 1, 2, 3, 4, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void insertRowsAfterFirstSuccess(){	
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
		
		dataRowDao.insertRows(fd, dataRowDao.getRows(fd, null, new DataRowRange(1, 1)).get(0), dataRows); 
		Assert.assertArrayEquals(new int[]{1, 11, 12, 2, 3, 4, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void insertRowsAfterLastSuccess(){	
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
		
		dataRowDao.insertRows(fd, dataRowDao.getRows(fd, null, new DataRowRange(5, 1)).get(0), dataRows); 
		Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5, 51, 52}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void insertRowsAfterEnptySuccess(){	
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		dataRowDao.insertRows(fd, dataRowDao.getRows(fd, null, new DataRowRange(5, 1)).get(0), dataRows); 
		Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void removeRowsByIndexes1Success(){
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 2, 4);
		Assert.assertArrayEquals(new int[]{1, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void removeRowsByIndexes2Success(){
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 2, 5);
		Assert.assertArrayEquals(new int[]{1}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void removeRowsByIndexes3Success(){
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 1, 5);
		Assert.assertArrayEquals(new int[]{}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void removeRowsAll(){
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd);
		Assert.assertArrayEquals(new int[]{}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void removeRowsByDataRows(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, range);
		dataRowDao.removeRows(fd, dataRows);
		Assert.assertArrayEquals(new int[]{1, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void getRowsSuccess(){
		FormData fd = formDataDao.get(1);
		Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, null)));
	}
	
	@Test
	public void getRowsRange1Success(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		Assert.assertArrayEquals(new int[]{2, 3, 4}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, range)));
	}
	
	@Test
	public void getRowsRange2Success(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(1, 2);
		Assert.assertArrayEquals(new int[]{1, 2}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, range)));
	}
	
	@Test
	public void getRowsRange3Success(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(5, 1);
		Assert.assertArrayEquals(new int[]{5}, dataRowsToStringColumnValues(dataRowDao.getRows(fd, null, range)));
	}
	
	@Test
	public void getSavedRowsSuccess(){
		FormData fd = formDataDao.get(1);
		Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5}, dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null, null)));
	}
	
	@Test
	public void getSavedRowsRange1Success(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(2, 3);
		Assert.assertArrayEquals(new int[]{2, 3, 4}, dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null, range)));
	}
	
	@Test
	public void getSavedRowsRange2Success(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(1, 2);
		Assert.assertArrayEquals(new int[]{1, 2}, dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null, range)));
	}
	
	@Test
	public void getSavedRowsRange3Success(){
		FormData fd = formDataDao.get(1);
		DataRowRange range = new DataRowRange(5, 1);
		Assert.assertArrayEquals(new int[]{5}, dataRowsToStringColumnValues(dataRowDao.getSavedRows(fd, null, range)));
	}
	
	

}
