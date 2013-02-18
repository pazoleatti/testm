package com.aplana.sbrf.taxaccounting.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.service.impl.FormDataPrintingServiceImpl;

public class FormDataPrintingServiceTestMock {
	
	private static final int TB1_CONTROL_USER_ID = 1;
	private static final int BANK_CONTROL_USER_ID = 3;
	
	private static final long TB2_CREATED_FORMDATA_ID = 4;
	private static final long TB2_APPROVED_FORMDATA_ID = 5;
	private static final int TB2_ACCEPTED_FORMTEMPLATE_ID = 6;
	
	static FormDataPrintingService formDataPrintingService = new FormDataPrintingServiceImpl();
	
	private static FormData formData;
	
	@BeforeClass
	public static void init() {
		List<Column> columns = new ArrayList<Column>();
		Column colNum = new NumericColumn();
		Column colNum1 = new NumericColumn();
		Column colNum2 = new NumericColumn();
		Column colNum3 = new NumericColumn();
		
		Column colStr = new StringColumn();
		Column colStr1 = new StringColumn();
		Column colStr2 = new StringColumn();
		
		Column colDate = new DateColumn();
		Column colDate1 = new DateColumn();
		Column colDate2 = new DateColumn();
		
		colNum.setGroupName("Нумерованные");
		colNum1.setGroupName("Нумерованные");
		colNum2.setGroupName("Нумерованные");
		colNum3.setGroupName(null);
		colStr.setGroupName("Строковые");
		colStr1.setGroupName("Строковые");
		colStr2.setGroupName("Строковые");
		colDate.setGroupName("Датовые");
		colDate1.setGroupName("Датовые");
		colDate2.setGroupName("Датовые");
		
		colNum.setAlias("Number");
		colNum1.setName("Номер");
		colNum2.setAlias("Number1");
		colNum3.setAlias("Number3");
		colNum.setName("Номер");
		colNum3.setName("Номер 3");
		colNum1.setAlias("Number2");
		colNum2.setName("Номер");
		colStr.setAlias("String");
		colStr1.setName("Строка");
		colStr2.setAlias("String1");
		colStr.setName("Строка");
		colStr1.setAlias("String2");
		colStr2.setName("Строка");
		colDate.setAlias("Date");
		colDate1.setName("Дата");
		colDate2.setAlias("Date1");
		colDate.setName("Дата");
		colDate1.setAlias("Date2");
		colDate2.setName("Дата");
		
		columns.add(colDate);
		columns.add(colDate1);
		columns.add(colDate2);
		columns.add(colStr);
		columns.add(colStr1);
		columns.add(colStr2);
		columns.add(colNum);
		columns.add(colNum1);
		columns.add(colNum2);
		columns.add(colNum3);
		
		formData = mock(FormData.class);
		when(formData.getFormColumns()).thenReturn(columns);
		
		FormType formType = new FormType();
		formType.setName("Fkfd");
		when(formData.getFormType()).thenReturn(formType);
		when(formData.getFormTemplateId()).thenReturn(TB2_ACCEPTED_FORMTEMPLATE_ID);
		when(formData.getKind()).thenReturn(FormDataKind.CONSOLIDATED);
		when(formData.getDepartmentId()).thenReturn(1);
		
		FormDataDao formDataDao = mock(FormDataDao.class);
		FormTemplateDao formTemplateDao = mock(FormTemplateDao.class);
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		FormDataAccessService formDataAccessService = mock(FormDataAccessService.class);

		// TODO: заменить логгер или вообще использовать дао класс
		when(formDataDao.get(TB2_APPROVED_FORMDATA_ID)).thenReturn(formData);
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.setNumberedColumns(true);
		//when(formTemplate.isNumberedColumns()).thenReturn(true);
		when(formTemplateDao.get(TB2_ACCEPTED_FORMTEMPLATE_ID)).thenReturn(formTemplate);
		when(formDataAccessService.canRead(TB1_CONTROL_USER_ID, TB2_APPROVED_FORMDATA_ID)).thenReturn(true);
		ReflectionTestUtils.setField(formDataPrintingService, "formDataDao", formDataDao);
		ReflectionTestUtils.setField(formDataPrintingService, "formTemplateDao", formTemplateDao);
		ReflectionTestUtils.setField(formDataPrintingService, "formDataAccessService", formDataAccessService);
		ReflectionTestUtils.setField(formDataPrintingService, "departmentDao", departmentDao);
	}
	
	@Test
	public void testReportPrintService(){
		formDataPrintingService.generateExcel(TB1_CONTROL_USER_ID,TB2_APPROVED_FORMDATA_ID, true);
	}

}
