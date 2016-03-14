package com.aplana.sbrf.taxaccounting.service.print.formdata;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataCSVReportBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class FormDataCSVReportBuilderTest {

    private final String HEADERROWSS_TEMPLATE = ClassUtils
            .classPackageAsResourcePath(getClass())
            + "/headerrows.xml";

    private final String DATAROWS_TEMPLATE = ClassUtils
            .classPackageAsResourcePath(getClass())
            + "/datarows.xml";

    private List<Column> columns = new ArrayList<Column>();
    private FormDataReport data = new FormDataReport();
    private List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
    private RefBookValue refBookValue = new RefBookValue(RefBookAttributeType.STRING, "34");

    private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();

    @Before
    public void init() throws IOException {

        Column colNum = new NumericColumn();
        Column colNum1 = new StringColumn();
        Column colNum2 = new StringColumn();
        Column colNum3 = new StringColumn();
        Column colNum4 = new StringColumn();
        Column colNum5 = new NumericColumn();
        Column colNum6 = new NumericColumn();
        Column colNum7 = new StringColumn();
        Column colNum8 = new DateColumn();

        Column colStr9 = new StringColumn();
        Column colStr10 = new NumericColumn();
        Column colStr11 = new NumericColumn();
        Column colDate12 = new StringColumn();
        Column colDate13 = new StringColumn();
        Column colDate14 = new StringColumn();
        Column colDate15 = new StringColumn();
        Column colDate16 = new StringColumn();
        Column colDate17 = new StringColumn();
        Column colDate18 = new StringColumn();
        Column colDate19 = new StringColumn();


        //setting alias
        colNum.setAlias("number");
        colNum1.setAlias("contract");
        colNum2.setAlias("contractDate");
        colNum3.setAlias("amountOfTheGuarantee");
        colNum4.setAlias("dateOfTransaction");
        colNum5.setAlias("rateOfTheBankOfRussia");
        colNum6.setAlias("interestRate");
        colNum7.setAlias("baseForCalculation");
        colNum8.setAlias("accrualAccountingStartDate");

        colStr9.setAlias("accrualAccountingEndDate");
        colStr10.setAlias("preAccrualsStartDate");
        colStr11.setAlias("preAccrualsEndDate");
        colDate12.setAlias("incomeCurrency");
        colDate13.setAlias("incomeRuble");
        colDate14.setAlias("accountingCurrency");
        colDate15.setAlias("accountingRuble");
        colDate16.setAlias("preChargeCurrency");
        colDate17.setAlias("preChargeRuble");
        colDate18.setAlias("taxPeriodCurrency");
        colDate19.setAlias("taxPeriodRuble");


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
        colNum.setWidth(0);
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
        colDate13.setWidth(10);
        colDate14.setWidth(10);
        colDate15.setWidth(10);
        colDate16.setWidth(10);
        colDate17.setWidth(10);
        colDate18.setWidth(10);
        colDate19.setWidth(0);

        ((DateColumn)colNum8).setFormatId(Formats.NONE.getId());


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
        columns.add(colDate13);
        columns.add(colDate14);
        columns.add(colDate15);
        columns.add(colDate16);
        columns.add(colDate17);
        columns.add(colDate18);
        columns.add(colDate19);

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
        TaxPeriod taxPeriod = new TaxPeriod();
        FormDataPerformer formDataperformer = new FormDataPerformer();
        List<FormDataSigner> formDataSigners = new ArrayList<FormDataSigner>();
        FormDataSigner formDataSigner1 = new FormDataSigner();
        FormDataSigner formDataSigner2 = new FormDataSigner();

        taxPeriod.setYear(Calendar.getInstance().get(Calendar.YEAR));
        formData = new FormData();
        formTemplate = new FormTemplate();
        formTemplate.setId(328);
        reportPeriod = new ReportPeriod();
        reportPeriod.setName("1 квартал");
        reportPeriod.setTaxPeriod(taxPeriod);
        formTemplate.getStyles().addAll(formStyles);
        formTemplate.setHeader("Таблица 1\\2\\3 | Приложение 1 | Приложение 2");
        formTemplate.getColumns().addAll(columns);
        formTemplate.setFullName("Печатная форма");
        formData.initFormTemplateParams(formTemplate);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(HEADERROWSS_TEMPLATE)));
        String s;
        StringBuilder builder = new StringBuilder();
        while ((s = reader.readLine())!=null)
            builder.append(s);
        List<DataRow<HeaderCell>> headerCells =
                xmlSerializationUtils.deserialize(builder.toString(), formTemplate, HeaderCell.class);
        formTemplate.getHeaders().addAll(headerCells);

        builder = new StringBuilder();
        reader = new BufferedReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(DATAROWS_TEMPLATE)));
        while ((s = reader.readLine())!=null)
            builder.append(s);
        dataRows.addAll(
                xmlSerializationUtils.deserialize(builder.toString(), formTemplate, Cell.class)
        );

        for (int i = 0; i < 100; i++)
            dataRows.add(dataRows.get(0));

        department = new Department();
        department.setId(1);
        department.setName("Тестовое");

        FormType formType = new FormType();
        formType.setName("Fkfd");
        formType.setTaxType(TaxType.TRANSPORT);
        formData.setFormType(formType);

        formDataperformer.setName("performer");
        formDataperformer.setPhone("777");
        formDataperformer.setReportDepartmentName("Деп.");
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
        data.setFormTemplate(formTemplate);
        data.setAcceptanceDate(null);
        data.setCreationDate(new Date(324234));
    }

    @Test
    public void test() throws IOException {
        FormDataCSVReportBuilder builder = new FormDataCSVReportBuilder(data,true, dataRows, refBookValue);
        String reportPath = null;
        try {
            reportPath = builder.createReport();
        } finally {
            assert reportPath != null;
            File file = new File(reportPath);
            file.delete();
        }
    }
}
