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
		dr.put("stringColumn", "String 1");
		dr.put("numericColumn", 1.01);
		Date date = getDate(2012, 11, 31);
		dr.put("dateColumn", date);
		dataRows.add(dr);

		dr = fd.createDataRow();
		dr.setAlias("newAlias0");
		dr.put("stringColumn", "String 2");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		
		dr = fd.createDataRow();
		dr.setAlias("newAlias1");
		dr.put("stringColumn", "String 3");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		
		dr = fd.createDataRow();
		dr.setAlias("newAlias2");
		dr.put("stringColumn", "String 4");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		
		dr = fd.createDataRow();
		dr.setAlias("newAlias3");
		dr.put("stringColumn", "String 5");
		dr.put("numericColumn", 2.02);
		date = getDate(2013, 0, 1);
		dr.put("dateColumn", date);
		dataRows.add(dr);
		
		dataRowDao.saveRows(fd, dataRows);
		dataRowDao.commit(fd);

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
	public void deleteDataRow(){
		FormData fd = formDataDao.get(1);
		dataRowDao.removeRows(fd, 1, 1);
		
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertEquals(1, dataRows.size());
		Assert.assertEquals(1, dataRowDao.getSize(fd, null));
		
		dataRows = dataRowDao.getSavedRows(fd, null, null);
		Assert.assertEquals(2, dataRows.size());
		Assert.assertEquals(2, dataRowDao.getSavedSize(fd, null));
		
		dataRowDao.rollback(fd);
		
		dataRows = dataRowDao.getRows(fd, null, null);
		Assert.assertEquals(2, dataRows.size());
		Assert.assertEquals(2, dataRowDao.getSize(fd, null));
		
		dataRows = dataRowDao.getRows(fd, null, null);
		System.out.println(dataRows.size());
	}
	
	

}
