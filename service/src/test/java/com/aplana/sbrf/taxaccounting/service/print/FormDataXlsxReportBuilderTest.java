package com.aplana.sbrf.taxaccounting.service.print;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.impl.print.FormDataXlsxReportBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataXlsxReportBuilderContext.xml","/applicationContext.xml", "classpath:com/aplana/sbrf/taxaccounting/dao.xml"})
public class FormDataXlsxReportBuilderTest {
	
	@Autowired
	FormDataService formDataService;
	
	FormData formData;
	
	@Before
	public void init(){
		formData = formDataService.getFormData(3, 3);
		/*FormTemplate formTemplate = new FormTemplate();
		formTemplate.setId(1);
		formTemplate.setType(new FormType());
		formData.initFormTemplateParams(formTemplate);
		
		formData.setDepartmentId(111);
		formData.setKind(FormDataKind.ADDITIONAL);*/
	}
	
	@Test
	public void createReportTest(){
		try {
			FormDataXlsxReportBuilder test = new FormDataXlsxReportBuilder(formData);
			test.createReport();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
