package com.aplana.sbrf.taxaccounting.service.print.formdata;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsxReportBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormDataXlsxReportBuilderTestMock {
	
	private List<Column> columns = new ArrayList<Column>();
	private FormDataReport data = new FormDataReport();
    private List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();

    private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();
    private final String HEADERROWSS_TEMPLATE = ClassUtils
            .classPackageAsResourcePath(getClass())
            + "/headerrows.xml";

    private final String DATAROWS_TEMPLATE = ClassUtils
            .classPackageAsResourcePath(getClass())
            + "/datarows.xml";
	
	
	@Before
	public void init() throws IOException {
		
		Column colNum = new NumericColumn();
		Column colNum1 = new StringColumn();
		Column colNum2 = new StringColumn();
		Column colNum3 = new StringColumn();
		Column colNum4 = new StringColumn();
		Column colNum5 = new NumericColumn();
		Column colNum6 = new StringColumn();
		Column colNum7 = new StringColumn();
		Column colNum8 = new StringColumn();
		
		Column colStr9 = new StringColumn();
		Column colStr10 = new NumericColumn();
		Column colStr11 = new NumericColumn();
        Column colDate12 = new DateColumn();
		
		//setting alias
		colNum.setAlias("number");
		colNum1.setAlias("securitiesType");
		colNum2.setAlias("ofz");
		colNum3.setAlias("municipalBonds");
		colNum4.setAlias("governmentBonds");
		colNum5.setAlias("mortgageBonds");
		colNum6.setAlias("municipalBondsBefore");
		colNum7.setAlias("rtgageBondsBefore");
		colNum8.setAlias("ovgvz");
		
		colStr9.setAlias("eurobondsRF");
		colStr10.setAlias("itherEurobonds");
		colStr11.setAlias("corporateBonds");
        colDate12.setAlias("dateInfo");
		
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
        colDate12.setChecking(true);

        //set width
        colNum.setWidth(10);
        colNum1.setWidth(0);
        colNum2.setWidth(10);
        colNum3.setWidth(10);
        colNum4.setWidth(10);
        colNum5.setWidth(10);
        colNum6.setWidth(10);
        colNum7.setWidth(0);
        colNum8.setWidth(10);
        colStr9.setWidth(10);
        colStr10.setWidth(10);
        colStr11.setWidth(10);
        colDate12.setWidth(10);
		
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
        columns.add(colDate12);

        List<FormStyle> formStyles = new ArrayList<FormStyle>();
		FormStyle formStyle1 = new FormStyle();
        formStyle1.setAlias("Редактируемая");
        formStyle1.setBackColor(Color.LIGHT_BLUE);
        formStyle1.setFontColor(Color.LIGHT_BROWN);
        formStyles.add(formStyle1);

		FormData formData;
		FormTemplate formTemplate;
		Department department;
		ReportPeriod reportPeriod;
		FormDataPerformer formDataperformer = new FormDataPerformer();
		List<FormDataSigner> formDataSigners = new ArrayList<FormDataSigner>();
		FormDataSigner formDataSigner1 = new FormDataSigner();
		FormDataSigner formDataSigner2 = new FormDataSigner();
		
		formData = new FormData();
		formTemplate = new FormTemplate();
        formTemplate.setId(328);
		reportPeriod = new ReportPeriod();
		reportPeriod.setName("1 квартал");
        formTemplate.getStyles().addAll(formStyles);
		formTemplate.setNumberedColumns(true);
		formTemplate.setCode("Таблица 1\\2\\3 | Приложение 1 | Приложение 2");
        formTemplate.getColumns().addAll(columns);
        formData.initFormTemplateParams(formTemplate);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(HEADERROWSS_TEMPLATE)));
        String s;
        StringBuilder builder = new StringBuilder();
        while ((s = reader.readLine())!=null)
            builder.append(s);
        List<DataRow<HeaderCell>> headerCells =
                xmlSerializationUtils.deserialize(builder.toString(), formTemplate.getColumns(), formTemplate.getStyles(), HeaderCell.class);
        formTemplate.getHeaders().addAll(headerCells);

        builder = new StringBuilder();
        reader = new BufferedReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(DATAROWS_TEMPLATE)));
        while ((s = reader.readLine())!=null)
            builder.append(s);
        dataRows.addAll(
                xmlSerializationUtils.deserialize(builder.toString(), formTemplate.getColumns(), formTemplate.getStyles(), Cell.class)
        );
		
		department = new Department();
        department.setId(1);
		department.setName("Тестовое");
		
		FormType formType = new FormType();
		formType.setName("Fkfd");
		formType.setTaxType(TaxType.TRANSPORT);
        formData.setFormType(formType);
		
		formDataperformer.setName("performer");
		formDataperformer.setPhone("777");
		formDataSigner1.setName("Карл Петрович");
		formDataSigner1.setPosition("Топ");
		formDataSigner2.setPosition("Гл. бухгалтер");
		formDataSigner2.setName("Нина Васильевна");
		formDataSigners.add(formDataSigner1);
		formDataSigners.add(formDataSigner2);

        formData.setKind(FormDataKind.CONSOLIDATED);
        formData.setDepartmentId(1);
        formData.setState(WorkflowState.CREATED);
        formData.setPerformer(formDataperformer);
        formData.setSigners(formDataSigners);

		data.setData(formData);
		data.setDepartment(department);
		data.setReportPeriod(reportPeriod);
		data.setFormTemplate(formTemplate);
        data.setAcceptanceDate(null);
        data.setCreationDate(new Date(324234));
	}

	@Test
	public void testReport() throws IOException{
		FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(data,true, dataRows);
        builder.createReport();
	}
}
