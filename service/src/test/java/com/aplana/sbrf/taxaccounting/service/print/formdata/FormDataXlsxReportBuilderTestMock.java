package com.aplana.sbrf.taxaccounting.service.print.formdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataReport;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsxReportBuilder;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormDataXlsxReportBuilderTestMock {
	
	private List<Column> columns = new ArrayList<Column>();
	private FormDataReport data = new FormDataReport();
	
	
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
		
		((NumericColumn)colNum).setPrecision(0);
		((NumericColumn)colNum1).setPrecision(1);
		((NumericColumn)colNum2).setPrecision(3);
		
		//setting alias
		colNum.setAlias("Number");
		colNum1.setAlias("Number1");
		colNum2.setAlias("Number2");
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
		colDate14.setAlias("Date1");
		colDate15.setAlias("Date2");
		
		//setting name
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
		
		//setting check
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
	
		colDate13.setChecking(false);
		colDate14.setChecking(false);
		colDate15.setChecking(false);
		
		//setting format
		((DateColumn)colDate13).setFormatId(1);
		((DateColumn)colDate14).setFormatId(2);
		((DateColumn)colDate15).setFormatId(2);
		
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
		
		
		
		DataRow<Cell> dataRow = mock(DataRow.class);
		Cell cell = mock(Cell.class);
		FormStyle formStyle = new FormStyle();
		formStyle.setBackColor(Color.LIGHT_BLUE);
		formStyle.setFontColor(Color.LIGHT_BROWN);
		
		when(dataRow.getCell(colNum.getAlias())).thenReturn(cell);
		when(dataRow.getCell(colNum.getAlias()).getColumn()).thenReturn(colNum);
		when(dataRow.getCell(colNum1.getAlias())).thenReturn(cell);
		when(dataRow.getCell(colNum1.getAlias()).getColumn()).thenReturn(colNum1);
		when(dataRow.getCell("Number2")).thenReturn(cell);
		when(dataRow.getCell("Number2").getColumn()).thenReturn(colNum2);
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
		when(dataRow.getCell(colDate13.getAlias())).thenReturn(cell);
		when(dataRow.getCell(colDate13.getAlias()).getColumn()).thenReturn(colDate13);
		when(dataRow.getCell("Date1")).thenReturn(cell);
		when(dataRow.getCell("Date1").getColumn()).thenReturn(colDate14);
		when(dataRow.getCell("Date2")).thenReturn(cell);
		when(dataRow.getCell("Date2").getColumn()).thenReturn(colDate15);
		when(cell.getStyle()).thenReturn(formStyle);
		BigDecimal bd = new BigDecimal(1234567891234567.011, new MathContext(19));
		bd.setScale(3,RoundingMode.HALF_UP);
		when(dataRow.get("Number")).thenReturn(bd);
		when(dataRow.get("Number1")).thenReturn(bd);
		when(dataRow.get("Number2")).thenReturn(bd);
		when(dataRow.get("Date")).thenReturn(new Date());
		when(dataRow.get("Date1")).thenReturn(new Date());
		when(dataRow.get("Date2")).thenReturn(new Date());
		
		FormData formData;
		FormTemplate formTemplate;
		Department department;
		ReportPeriod reportPeriod;
		FormDataPerformer formDataperformer = new FormDataPerformer();
		List<FormDataSigner> formDataSigners = new ArrayList<FormDataSigner>();
		FormDataSigner formDataSigner1 = new FormDataSigner();
		FormDataSigner formDataSigner2 = new FormDataSigner();
		
		formData = mock(FormData.class);
		formTemplate = new FormTemplate();
		reportPeriod = new ReportPeriod();
		reportPeriod.setName("1 квартал");
		formTemplate.setNumberedColumns(true);
		formTemplate.setCode("Таблица 1\\2\\3 | Приложение 1 | Приложение 2");
		when(formData.getFormColumns()).thenReturn(columns);
		
		department = new Department();
		department.setName("Тестовое");
		
		FormType formType = new FormType();
		formType.setName("Fkfd");
		formType.setTaxType(TaxType.TRANSPORT);
		when(formData.getFormType()).thenReturn(formType);
		
		formDataperformer.setName("performer");
		formDataperformer.setPhone("777");
		formDataSigner1.setName("Карл Петрович");
		formDataSigner1.setPosition("Топ");
		formDataSigner2.setPosition("Гл. бухгалтер");
		formDataSigner2.setName("Нина Васильевна");
		formDataSigners.add(formDataSigner1);
		formDataSigners.add(formDataSigner2);
		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		dataRows.add(dataRow);
		dataRows.add(dataRow);
		dataRows.add(dataRow);
		when(formData.getDataRows()).thenReturn(dataRows);
		when(formData.getKind()).thenReturn(FormDataKind.CONSOLIDATED);
		when(formData.getDepartmentId()).thenReturn(1);
		when(formData.getState()).thenReturn(WorkflowState.ACCEPTED);
		when(formData.getPerformer()).thenReturn(formDataperformer);
		when(formData.getSigners()).thenReturn(formDataSigners);

		data.setData(formData);
		data.setDepartment(department);
		data.setReportPeriod(reportPeriod);
		data.setFormTemplate(formTemplate);
	}
	
	@Test
	public void testReport() throws IOException{
		FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(data,false);
		try {
			builder.createReport();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
