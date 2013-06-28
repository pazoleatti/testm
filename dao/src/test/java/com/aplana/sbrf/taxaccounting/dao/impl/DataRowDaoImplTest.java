package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.FormData;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DataRowDaoImplTest.xml"})
public class DataRowDaoImplTest {
	
	@Autowired
	FormTemplateDao formTemplateDao;

	@Autowired
	FormDataDao formDataDao;
	
	@Autowired
	DataRowDao dataRowDao;
	
	@Test
	public void getRows(){
		
		FormData fd = formDataDao.get(1);
		
		dataRowDao.getRows(fd, null, null);
		
	}

}
