package com.aplana.sbrf.taxaccounting.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataPrintingServiceContext.xml","/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class FormDataPrintingServiceTest {

	@Autowired
	FormDataPrintingService formDataPrintingService;
	
	@Test
	public void formDataPrintingServiceTest(){
		formDataPrintingService.generateExcel(3, 10182);
	}
}
