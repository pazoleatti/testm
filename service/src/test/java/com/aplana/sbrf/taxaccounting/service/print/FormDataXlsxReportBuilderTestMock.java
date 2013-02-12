package com.aplana.sbrf.taxaccounting.service.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
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
	private FormTemplate formTemplate;
	
	@Before
	public void init(){
		
		Column colNum = new NumericColumn();
		Column colNum1 = new NumericColumn();
		Column colNum2 = new NumericColumn();
		Column colNum3 = new NumericColumn();
		Column colNum4 = new NumericColumn();
		Column colNum5 = new NumericColumn();
		Column colNum6 = new NumericColumn();
		Column colNum7 = new NumericColumn();
		Column colNum8 = new NumericColumn();
		
		Column colStr9 = new StringColumn();
		Column colStr10 = new StringColumn();
		Column colStr11 = new StringColumn();
		Column colStr12 = new StringColumn();
		
		Column colDate13 = new DateColumn();
		Column colDate14 = new DateColumn();
		Column colDate15 = new DateColumn();
		
		colNum.setGroupName(null);
		colNum1.setGroupName(null);
		colNum2.setGroupName(null);
		colNum3.setGroupName(null);
		colNum4.setGroupName(null);
		colNum5.setGroupName("РНУ-7 (графа 12)");
		colNum6.setGroupName("РНУ-7 (графа 12)");
		colNum7.setGroupName("РНУ-5 (графа 5)");
		colNum8.setGroupName("РНУ-5 (графа 5)");
		colStr9.setGroupName(null);
		colStr10.setGroupName("Сумма символа ОПУ. Налоговый учёт");
		colStr11.setGroupName("Сумма символа ОПУ. Налоговый учёт");
		colStr12.setGroupName("Сумма символа ОПУ. Налоговый учёт");
		colDate13.setGroupName("Сумма символа ОПУ. Налоговый учёт");
		colDate14.setGroupName(null);
		colDate15.setGroupName(null);
		
		colNum.setAlias("Number");
		colNum1.setAlias("Number2");
		colNum2.setAlias("Number1");
		colNum3.setAlias("Number3");
		colNum4.setAlias("Number4");
		colNum5.setAlias("Number5");
		colNum6.setAlias("Number6");
		colNum7.setAlias("Number7");
		colNum8.setAlias("Number8");
		
		colStr9.setAlias("String");
		colStr10.setAlias("String2");
		colStr11.setAlias("String1");
		colStr12.setAlias("String3");
		
		colDate13.setAlias("Date");
		colDate14.setAlias("Date2");
		colDate15.setAlias("Date1");
		
		colNum.setName("Код вида расхода");
		colNum1.setName("Группа расходов");
		colNum2.setName("Вид расхода по операции");
		colNum3.setName("Номер счёта учёта расходов, при их получении");
		colNum4.setName("РНУ-7 (графа 10). Сумма");
		colNum5.setName("Номер счёта учёта расходов, при их получении");
		colNum6.setName("Номер счёта учёта расходов, при их получении");
		colNum7.setName("Номер счёта учёта расходов, при их получении");
		colNum8.setName("Номер счёта учёта расходов, при их получении");
		
		colStr9.setName("Логическая проверка");
		colStr10.setName("по Приложению №3");
		colStr11.setName("по Таблице Р");
		colStr12.setName("всего");
		
		colDate13.setName("по ОПУ");
		colDate14.setName("Расхождение");
		colDate15.setName("Расхождение");
		
		colNum.setChecking(false);
		colNum1.setChecking(false);
		colNum2.setChecking(false);
		colNum3.setChecking(false);
		colNum4.setChecking(false);
		colNum5.setChecking(false);
		colNum6.setChecking(false);
		colNum7.setChecking(false);
		colNum8.setChecking(false);
		
		colStr9.setChecking(true);
		colStr10.setChecking(true);
		colStr11.setChecking(true);
		colStr12.setChecking(true);
		
		colDate13.setChecking(true);
		colDate14.setChecking(true);
		colDate15.setChecking(true);
		
		columns.add(colNum);
		columns.add(colNum1);
		columns.add(colNum2);
		columns.add(colNum3);
		columns.add(colNum4);
		columns.add(colNum5);
		columns.add(colNum6);
		columns.add(colNum7);
		columns.add(colNum8);
		columns.add(colStr9);
		columns.add(colStr10);
		columns.add(colStr11);
		columns.add(colStr12);
		columns.add(colDate13);
		columns.add(colDate14);
		columns.add(colDate15);
		
		
		
		DataRow dataRow = mock(DataRow.class);
		Cell cell = mock(Cell.class);
		FormStyle formStyle = new FormStyle();
		formStyle.setBackColor(Color.BLUE);
		formStyle.setFontColor(Color.GREEN);
		
		when(dataRow.getCell("Number")).thenReturn(cell);
		when(dataRow.getCell("Number1")).thenReturn(cell);
		when(dataRow.getCell("Number2")).thenReturn(cell);
		when(dataRow.getCell("Number3")).thenReturn(cell);
		when(dataRow.getCell("Number4")).thenReturn(cell);
		when(dataRow.getCell("Number5")).thenReturn(cell);
		when(dataRow.getCell("Number6")).thenReturn(cell);
		when(dataRow.getCell("Number7")).thenReturn(cell);
		when(dataRow.getCell("Number8")).thenReturn(cell);
		when(dataRow.getCell("String")).thenReturn(cell);
		when(dataRow.getCell("String1")).thenReturn(cell);
		when(dataRow.getCell("String2")).thenReturn(cell);
		when(dataRow.getCell("String3")).thenReturn(cell);
		when(dataRow.getCell("Date")).thenReturn(cell);
		when(dataRow.getCell("Date1")).thenReturn(cell);
		when(dataRow.getCell("Date2")).thenReturn(cell);
		when(cell.getStyle()).thenReturn(formStyle);
		when(dataRow.get("Number")).thenReturn("777");
		when(dataRow.get("Number1")).thenReturn("777");
		when(dataRow.get("Number2")).thenReturn("777");
		
		formData = mock(FormData.class);
		formTemplate = new FormTemplate();
		formTemplate.setNumberedColumns(true);
		when(formData.getFormColumns()).thenReturn(columns);
		
		FormType formType = new FormType();
		formType.setName("Fkfd");
		when(formData.getFormType()).thenReturn(formType);
		
		List<DataRow> dataRows = new ArrayList<DataRow>();
		dataRows.add(dataRow);
		dataRows.add(dataRow);
		dataRows.add(dataRow);
		when(formData.getDataRows()).thenReturn(dataRows);
		when(formData.getKind()).thenReturn(FormDataKind.CONSOLIDATED);
		when(formData.getDepartmentId()).thenReturn(1);
	}
	
	@Test
	public void testReport(){
		FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(formData,formTemplate,false);
		try {
			builder.createReport();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
