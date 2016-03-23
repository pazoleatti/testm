package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.util.*;

/**
 * User: avanteev
 */
public class JAXBTest {

    private CurrencyRates currencyRates;

    @Before
    public void init() throws DatatypeConfigurationException {
        currencyRates = new CurrencyRates();
        SimpleTimeZone zone = new SimpleTimeZone(4*60*60*1000, TimeZone.getTimeZone("GMT+4").getID());
        GregorianCalendar calendar = new GregorianCalendar(zone);
        calendar.setTime(new Date());
        currencyRates.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED));
        CurrencyRate currencyRate = new CurrencyRate();
        CurrencyRate currencyRate1 = new CurrencyRate();
        currencyRate.setCode("036");
        currencyRate.setIsoCode("AUD");
        currencyRate.setLotSize(1);
        currencyRate.setRate(BigDecimal.valueOf(30.2329));
        currencyRate1.setCode("826");
        currencyRate1.setIsoCode("GBP");
        currencyRate1.setLotSize(1);
        currencyRate1.setRate(BigDecimal.valueOf(49.9139));
        List<CurrencyRate> rates = new ArrayList<CurrencyRate>();
        rates.add(currencyRate);
        rates.add(currencyRate1);
        currencyRates.getCurrencyRate().add(currencyRate);
        currencyRates.getCurrencyRate().add(currencyRate1);
    }

    @Test
    public void test() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CurrencyRates.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
        marshaller.marshal(currencyRates, System.out);
    }

    @Test
    public void testFormTemplateContent() throws JAXBException {
        Column column1 = new DateColumn();
        Column column2 = new NumericColumn();
        Column column3 = new StringColumn();
        column1.setId(1);
        column2.setId(2);
        column3.setId(3);
        column1.setAlias("contract");
        column2.setAlias("contractDate");
        column3.setAlias("amountOfTheGuarantee");
        List<Column> columns = new ArrayList<Column>();
        columns.add(column1);
        columns.add(column2);
        columns.add(column3);

        List<FormStyle> formStyles = new ArrayList<FormStyle>();
        FormStyle formStyle1 = new FormStyle();
        formStyle1.setAlias("Редактируемая");
        formStyle1.setBackColor(Color.LIGHT_BLUE);
        formStyle1.setFontColor(Color.LIGHT_BROWN);
        formStyles.add(formStyle1);

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Fkfd");
        formType.setTaxType(TaxType.TRANSPORT);

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(1);
        formTemplate.setFixedRows(false);
        formTemplate.setVersion(new Date());
        formTemplate.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate.setName("name_3");
        formTemplate.setFullName("fullname_3");
        formTemplate.setHeader("header_3");
        formTemplate.setScript("test_script");
        formTemplate.getColumns().addAll(columns);
        DataRow<Cell> rows = new DataRow<Cell>(FormDataUtils.createCells(formTemplate));
        formTemplate.getRows().add(rows);
        DataRow<HeaderCell> headers1 = new DataRow<HeaderCell>(FormDataUtils.createHeaderCells(formTemplate.getColumns()));
        formTemplate.getHeaders().add(headers1);
        formTemplate.getStyles().addAll(formStyles);
        formTemplate.setType(formType);

        FormTemplateContent ftc = new FormTemplateContent();
        ftc.fillFormTemplateContent(formTemplate);

        JAXBContext context = JAXBContext.newInstance(FormTemplateContent.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(ftc, System.out);
    }

}
