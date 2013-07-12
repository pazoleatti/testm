package com.aplana.sbrf.taxaccounting.model;

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

}
