package com.aplana.sbrf.taxaccounting.service.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.service.impl.print.FormDataXlsxReportBuilder;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormDataXlsxReportBuilderTestMock {
	
	private List<Column> columns = new ArrayList<Column>();
	private FormData formData;
	
	@Before
	public void init(){
		
		Column colNum = new NumericColumn();
		Column colNum1 = new NumericColumn();
		Column colNum2 = new NumericColumn();
		
		Column colStr = new StringColumn();
		Column colStr1 = new StringColumn();
		Column colStr2 = new StringColumn();
		
		Column colDate = new DateColumn();
		Column colDate1 = new DateColumn();
		Column colDate2 = new DateColumn();
		
		colNum.setGroupName("Нумерованные");
		colNum1.setGroupName("Нумерованные");
		colNum2.setGroupName("Нумерованные");
		colStr.setGroupName("Строковые");
		colStr1.setGroupName("Строковые");
		colStr2.setGroupName("Строковые");
		colDate.setGroupName("Датовые");
		colDate1.setGroupName("Датовые");
		colDate2.setGroupName("Датовые");
		
		colNum.setAlias("Number");
		colNum1.setName("Номер");
		colNum2.setAlias("Number1");
		colNum.setName("Номер");
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
		
		formData = mock(FormData.class);
		when(formData.getFormColumns()).thenReturn(columns);
		
		FormType formType = new FormType();
		formType.setName("Fkfd");
		when(formData.getFormType()).thenReturn(formType);
		
		when(formData.getKind()).thenReturn(FormDataKind.CONSOLIDATED);
		when(formData.getDepartmentId()).thenReturn(1);
	}
	
	@Test
	public void testReport(){
		FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(formData);
		try {
			builder.createReport();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
