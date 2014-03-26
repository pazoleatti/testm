package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PeriodServiceImplTest {
    PeriodService service = new PeriodServiceImpl();
    private static final Long PERIOD_CODE_REFBOOK = 8L;
    private final static String LOCAL_IP = "127.0.0.1";
    private static final int CONTROL_USER_ID = 2;
    ReportPeriodDao reportPeriodDao;

    @Before
    public void init(){
        /**
         * Налоговые периоды 1,2 - транспорт.
         * 3 - по прибыли
         */
        TaxPeriod taxPeriod1 = new TaxPeriod();
        taxPeriod1.setId(1);
		taxPeriod1.setYear(2012);
        taxPeriod1.setTaxType(TaxType.TRANSPORT);

        TaxPeriod taxPeriod2 = new TaxPeriod();
        taxPeriod2.setId(2);
		taxPeriod2.setYear(2012);
        taxPeriod2.setTaxType(TaxType.TRANSPORT);

        TaxPeriod taxPeriod3 = new TaxPeriod();
        taxPeriod3.setId(3);
		taxPeriod3.setYear(2012);
        taxPeriod3.setTaxType(TaxType.INCOME);

        // список налоговых периодов по ТН
        List<TaxPeriod> taxPeriodList = new ArrayList<TaxPeriod>();
        taxPeriodList.add(taxPeriod1);
        taxPeriodList.add(taxPeriod2);

        List<TaxPeriod> taxPeriodList1 = new ArrayList<TaxPeriod>();
        taxPeriodList1.add(taxPeriod1);

        // Mock для taxPeriodDao
        TaxPeriodDao taxPeriodDao = mock(TaxPeriodDao.class);
        when(taxPeriodDao.get(1)).thenReturn(taxPeriod1);
        when(taxPeriodDao.get(2)).thenReturn(taxPeriod2);
        when(taxPeriodDao.get(3)).thenReturn(taxPeriod3);
        when(taxPeriodDao.listByTaxType(TaxType.TRANSPORT)).thenReturn(taxPeriodList);
        when(taxPeriodDao.listByTaxTypeAndYear(TaxType.TRANSPORT, 2012)).thenReturn(taxPeriodList1);

        ReflectionTestUtils.setField(service, "taxPeriodDao", taxPeriodDao);

        /**
         * подготовим отчетные периоды для 1 налогового
         */
        ReportPeriod reportPeriod11 = new ReportPeriod();
        reportPeriod11.setId(1);
        reportPeriod11.setTaxPeriod(taxPeriod1);
        reportPeriod11.setName("Первый отчетный период в 1 налоговом периоде");
        reportPeriod11.setOrder(1);
		reportPeriod11.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
		reportPeriod11.setEndDate(new GregorianCalendar(2012, Calendar.MARCH, 31).getTime());
		reportPeriod11.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());

        ReportPeriod reportPeriod12 = new ReportPeriod();
        reportPeriod12.setId(2);
        reportPeriod12.setTaxPeriod(taxPeriod1);
        reportPeriod12.setName("Второй отчетный период в 1 налоговом периоде");
        reportPeriod12.setOrder(2);
		reportPeriod12.setStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());
		reportPeriod12.setEndDate(new GregorianCalendar(2012, Calendar.JUNE, 30).getTime());
		reportPeriod12.setCalendarStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());

        ReportPeriod reportPeriod13 = new ReportPeriod();
        reportPeriod13.setId(3);
        reportPeriod13.setTaxPeriod(taxPeriod1);
        reportPeriod13.setName("Третий отчетный период в 1 налоговом периоде");
        reportPeriod13.setOrder(3);
		reportPeriod13.setStartDate(new GregorianCalendar(2012, Calendar.JULY, 1).getTime());
		reportPeriod13.setEndDate(new GregorianCalendar(2012, Calendar.SEPTEMBER, 30).getTime());
		reportPeriod13.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JULY, 1).getTime());

        ReportPeriod reportPeriod14 = new ReportPeriod();
        reportPeriod14.setId(4);
        reportPeriod14.setTaxPeriod(taxPeriod1);
        reportPeriod14.setName("Четвертый отчетный период в 1 налоговом периоде");
        reportPeriod14.setOrder(4);
		reportPeriod14.setStartDate(new GregorianCalendar(2012, Calendar.OCTOBER, 1).getTime());
		reportPeriod14.setEndDate(new GregorianCalendar(2012, Calendar.DECEMBER, 31).getTime());
		reportPeriod14.setCalendarStartDate(new GregorianCalendar(2012, Calendar.OCTOBER, 1).getTime());

        /**
         * подготовим отчетные периоды для 2 налогового
         */
        ReportPeriod reportPeriod21 = new ReportPeriod();
        reportPeriod21.setId(5);
        reportPeriod21.setTaxPeriod(taxPeriod2);
        reportPeriod21.setName("Первый отчетный период в 2 налоговом периоде");
        reportPeriod21.setOrder(1);
		reportPeriod21.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
		reportPeriod21.setEndDate(new GregorianCalendar(2012, Calendar.MARCH, 31).getTime());
		reportPeriod21.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());

        ReportPeriod reportPeriod22 = new ReportPeriod();
        reportPeriod22.setId(6);
        reportPeriod22.setTaxPeriod(taxPeriod2);
        reportPeriod22.setName("Второй отчетный период в 2 налоговом периоде");
        reportPeriod22.setOrder(2);
		reportPeriod22.setStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());
		reportPeriod22.setEndDate(new GregorianCalendar(2012, Calendar.JUNE, 30).getTime());
		reportPeriod22.setCalendarStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());

        /**
         * подготовим отчетные периоды для 3 налогового
         */
        ReportPeriod reportPeriod31 = new ReportPeriod();
        reportPeriod31.setId(7);
        reportPeriod31.setTaxPeriod(taxPeriod3);
        reportPeriod31.setName("Первый отчетный период в 3 налоговом периоде");
        reportPeriod31.setOrder(1);
		reportPeriod31.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
		reportPeriod31.setEndDate(new GregorianCalendar(2012, Calendar.MARCH, 31).getTime());
		reportPeriod31.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());

        ReportPeriod reportPeriod32 = new ReportPeriod();
        reportPeriod32.setId(8);
        reportPeriod32.setTaxPeriod(taxPeriod3);
        reportPeriod32.setName("Второй отчетный период в 3 налоговом периоде");
        reportPeriod32.setOrder(2);
		reportPeriod32.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
		reportPeriod32.setEndDate(new GregorianCalendar(2012, Calendar.JUNE, 30).getTime());
		reportPeriod32.setCalendarStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());

        // списки отчетных периодов для налоговых
        List<ReportPeriod> reportPeriodListBy1Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy1Period.add(reportPeriod11);
        reportPeriodListBy1Period.add(reportPeriod12);
        reportPeriodListBy1Period.add(reportPeriod13);
        reportPeriodListBy1Period.add(reportPeriod14);

        List<ReportPeriod> reportPeriodListBy2Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy2Period.add(reportPeriod21);
        reportPeriodListBy2Period.add(reportPeriod22);

        List<ReportPeriod> reportPeriodListBy3Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy3Period.add(reportPeriod31);
        reportPeriodListBy3Period.add(reportPeriod32);


        // Mock для reportPeriodDao
        reportPeriodDao = mock(ReportPeriodDao.class);

        // перехват вызова функции получения отчетного периода по налоговому и возвращение нашего reportPeriod
        when(reportPeriodDao.get(1)).thenReturn(reportPeriod11);
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod12);
		when(reportPeriodDao.get(4)).thenReturn(reportPeriod14);
		when(reportPeriodDao.get(5)).thenReturn(reportPeriod21);
        when(reportPeriodDao.get(8)).thenReturn(reportPeriod32);

        // переотпределение вызовов метода listByTaxPeriod
        when(reportPeriodDao.listByTaxPeriod(1)).thenReturn(reportPeriodListBy1Period);
        when(reportPeriodDao.listByTaxPeriod(2)).thenReturn(reportPeriodListBy2Period);
        when(reportPeriodDao.listByTaxPeriod(3)).thenReturn(reportPeriodListBy3Period);

        ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);

        RefBookFactory rbFactory = mock(RefBookFactory.class);
        RefBook refBook = new RefBook(){{
            setId(PERIOD_CODE_REFBOOK);
            setName("REFBOOK_NAME");
        }};
        when(rbFactory.get(PERIOD_CODE_REFBOOK)).thenReturn(refBook);

        RefBookDataProvider provider = mock(RefBookDataProvider.class);
        when(rbFactory.getDataProvider(PERIOD_CODE_REFBOOK)).thenReturn(provider);
        Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
        record.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        record.put("ORD", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        record.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, new Date(101, 1, 1)));
        record.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, new Date(101, 1, 28)));
        record.put("CALENDAR_START_DATE", new RefBookValue(RefBookAttributeType.DATE, new Date(101, 1, 1)));
        when(provider.getRecordData(1L)).thenReturn(record);
        ReflectionTestUtils.setField(service, "rbFactory", rbFactory);
    }

	private void checkDates(int year, int month, int day, Date date) {
		Calendar cl = Calendar.getInstance();
		cl.clear();
		cl.set(year, month, day);

		Calendar cl2 = new GregorianCalendar();
		cl2.setTime(date);

		assertEquals("DATE", cl.get(Calendar.DATE), cl2.get(Calendar.DATE));
		assertEquals("MONTH", cl.get(Calendar.MONTH), cl2.get(Calendar.MONTH));
		assertEquals("YEAR", cl.get(Calendar.YEAR), cl2.get(Calendar.YEAR));
	}

    @Test
    public void getStartDate(){
		checkDates(2012, Calendar.APRIL, 1, service.getReportPeriod(2).getStartDate());
    }

    @Test
    public void getEndDate(){
		checkDates(2012, Calendar.JUNE, 30, service.getReportPeriod(2).getEndDate());
    }

    @Test
    public void getReportDate(){
		checkDates(2012, Calendar.JULY, 1, service.getReportDate(2).getTime());
    }

    @Test
    public void getStartDateIncome(){
		checkDates(2012, Calendar.JANUARY, 1, service.getReportPeriod(8).getStartDate());
    }

	@Test
	public void getCalendarStartDateIncome(){
		checkDates(2012, Calendar.APRIL, 1, service.getReportPeriod(8).getCalendarStartDate());
	}

    @Test
    public void getEndDateIncome(){
		checkDates(2012, Calendar.JUNE, 30, service.getReportPeriod(8).getEndDate());
    }

    @Test
    public void getForthTransportPeriod(){
		checkDates(2012, Calendar.OCTOBER, 1, service.getReportPeriod(4).getStartDate());
    }

    @Test
    public void getMonthStartDate() {
		checkDates(2012, Calendar.MAY, 1, service.getMonthStartDate(8, 5).getTime());
    }

    @Test
    public void getMonthEndDate() {
		checkDates(2012, Calendar.MAY, 31, service.getMonthEndDate(8, 5).getTime());
    }

    @Test
    public void getMonthReportDate() {
		checkDates(2012, Calendar.JUNE, 1, service.getMonthReportDate(8, 5).getTime());
    }

    /*
     * Тест получения из текущего налогового периода
     */
    @Test
    public void getPrevReportPeriodInSide(){
        assertNotNull(service.getPrevReportPeriod(2));
        assertTrue(service.getPrevReportPeriod(2).getId() == 1);
    }

    /*
     * Тест получения из предыдущего налогового периода
     */
    @Test
    public void getPrevReportPeriodOutSide(){
        service.getPrevReportPeriod(5);
        assertEquals(service.getPrevReportPeriod(5).getId().intValue(), 4);
    }

    @Test
    public void open() {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setIp(LOCAL_IP);
        userInfo.setUser(mockUser(CONTROL_USER_ID, 1, TARole.ROLE_CONTROL));
        service.open(2012, 1, TaxType.TRANSPORT, userInfo, 1, new ArrayList<LogEntry>(), false, new Date(), false);

        ArgumentCaptor<ReportPeriod> argument = ArgumentCaptor.forClass(ReportPeriod.class);
        verify(reportPeriodDao, times(1)).save(argument.capture());

        checkDates(2012, Calendar.FEBRUARY, 1, argument.getAllValues().get(0).getStartDate());
        checkDates(2012, Calendar.FEBRUARY, 29, argument.getAllValues().get(0).getEndDate());
        checkDates(2012, Calendar.FEBRUARY, 1, argument.getAllValues().get(0).getCalendarStartDate());
    }
}
