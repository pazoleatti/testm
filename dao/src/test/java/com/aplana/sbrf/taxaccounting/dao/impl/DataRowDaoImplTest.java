package com.aplana.sbrf.taxaccounting.dao.impl;

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
		//List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		//dataRowDao.removeRows(fd, dataRows);
		dataRowDao.removeRows(fd, 1, 2);
		dataRowDao.save(fd);
		
		// Проверяем, пуста ли форма для дальнейшего тестирования
		//dataRows = dataRowDao.getRows(fd, null, null);
		//Assert.assertEquals(0, dataRows.size());
		//dataRows = dataRowDao.getSavedRows(fd, null, null);
		//Assert.assertEquals(0, dataRows.size());

	}
	
	@Test
	public void deleteDataRow(){
		FormData fd = formDataDao.get(1);
		List<DataRow<Cell>> dataRows = dataRowDao.getRows(fd, null, null);
		System.out.println(dataRows.size());
	}
	
	

}
