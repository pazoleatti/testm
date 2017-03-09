package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("PeriodServiceImplTest.xml")
public class PeriodServiceImplTest {
    @Autowired
    PeriodService periodService;
    @Autowired
    ReportPeriodDao reportPeriodDao;
    @Autowired
    TaxPeriodDao taxPeriodDao;
    @Autowired
    RefBookDataProvider provider;
    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    DepartmentService departmentService;

    private static final Long PERIOD_CODE_REFBOOK = RefBook.Id.PERIOD_CODE.getId();
    private final static String LOCAL_IP = "127.0.0.1";
    private static final int CONTROL_USER_ID = 2;
    private TAUserInfo userInfo = new TAUserInfo();

    @Before
    public void init(){
        userInfo.setIp(LOCAL_IP);
        userInfo.setUser(mockUser(CONTROL_USER_ID, 1, TARole.N_ROLE_CONTROL_NS));

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
		taxPeriod2.setYear(2013);
        taxPeriod2.setTaxType(TaxType.TRANSPORT);

        TaxPeriod taxPeriod3 = new TaxPeriod();
        taxPeriod3.setId(3);
		taxPeriod3.setYear(2012);
        taxPeriod3.setTaxType(TaxType.INCOME);

        // список налоговых периодов по ТН
        List<TaxPeriod> taxPeriodList = new ArrayList<TaxPeriod>();
        taxPeriodList.add(taxPeriod1);
        taxPeriodList.add(taxPeriod2);

        // Mock для taxPeriodDao
        when(taxPeriodDao.get(1)).thenReturn(taxPeriod1);
        when(taxPeriodDao.get(2)).thenReturn(taxPeriod2);
        when(taxPeriodDao.get(3)).thenReturn(taxPeriod3);
        when(taxPeriodDao.listByTaxType(TaxType.TRANSPORT)).thenReturn(taxPeriodList);
        when(taxPeriodDao.getByTaxTypeAndYear(TaxType.TRANSPORT, 2012)).thenReturn(taxPeriod1);
        /**
         * подготовим отчетные периоды для 1 налогового
         */
        ReportPeriod reportPeriod11 = new ReportPeriod();
        reportPeriod11.setId(1);
        reportPeriod11.setTaxPeriod(taxPeriod1);
        reportPeriod11.setOrder(1);
        reportPeriod11.setName("Первый отчетный период в 1 налоговом периоде");
		reportPeriod11.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
		reportPeriod11.setEndDate(new GregorianCalendar(2012, Calendar.MARCH, 31).getTime());
		reportPeriod11.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());

        ReportPeriod reportPeriod12 = new ReportPeriod();
        reportPeriod12.setId(2);
        reportPeriod12.setTaxPeriod(taxPeriod1);
        reportPeriod12.setOrder(2);
        reportPeriod12.setName("Второй отчетный период в 1 налоговом периоде");
		reportPeriod12.setStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());
		reportPeriod12.setEndDate(new GregorianCalendar(2012, Calendar.JUNE, 30).getTime());
		reportPeriod12.setCalendarStartDate(new GregorianCalendar(2012, Calendar.APRIL, 1).getTime());

        ReportPeriod reportPeriod13 = new ReportPeriod();
        reportPeriod13.setId(3);
        reportPeriod13.setTaxPeriod(taxPeriod1);
        reportPeriod13.setOrder(3);
        reportPeriod13.setName("Третий отчетный период в 1 налоговом периоде");
		reportPeriod13.setStartDate(new GregorianCalendar(2012, Calendar.JULY, 1).getTime());
		reportPeriod13.setEndDate(new GregorianCalendar(2012, Calendar.SEPTEMBER, 30).getTime());
		reportPeriod13.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JULY, 1).getTime());

        ReportPeriod reportPeriod14 = new ReportPeriod();
        reportPeriod14.setId(4);
        reportPeriod14.setTaxPeriod(taxPeriod1);
        reportPeriod14.setOrder(4);
        reportPeriod14.setName("Четвертый отчетный период в 1 налоговом периоде");
		reportPeriod14.setStartDate(new GregorianCalendar(2012, Calendar.OCTOBER, 1).getTime());
		reportPeriod14.setEndDate(new GregorianCalendar(2012, Calendar.DECEMBER, 31).getTime());
		reportPeriod14.setCalendarStartDate(new GregorianCalendar(2012, Calendar.OCTOBER, 1).getTime());

        /**
         * подготовим отчетные периоды для 2 налогового
         */
        ReportPeriod reportPeriod21 = new ReportPeriod();
        reportPeriod21.setId(5);
        reportPeriod21.setTaxPeriod(taxPeriod2);
        reportPeriod21.setOrder(1);
        reportPeriod21.setName("Первый отчетный период в 2 налоговом периоде");
		reportPeriod21.setStartDate(new GregorianCalendar(2013, Calendar.JANUARY, 1).getTime());
		reportPeriod21.setEndDate(new GregorianCalendar(2013, Calendar.MARCH, 31).getTime());
		reportPeriod21.setCalendarStartDate(new GregorianCalendar(2013, Calendar.JANUARY, 1).getTime());

        ReportPeriod reportPeriod22 = new ReportPeriod();
        reportPeriod22.setId(6);
        reportPeriod22.setTaxPeriod(taxPeriod2);
        reportPeriod22.setOrder(2);
        reportPeriod22.setName("Второй отчетный период в 2 налоговом периоде");
		reportPeriod22.setStartDate(new GregorianCalendar(2013, Calendar.APRIL, 1).getTime());
		reportPeriod22.setEndDate(new GregorianCalendar(2013, Calendar.JUNE, 30).getTime());
		reportPeriod22.setCalendarStartDate(new GregorianCalendar(2013, Calendar.APRIL, 1).getTime());

        /**
         * подготовим отчетные периоды для 3 налогового
         */
        ReportPeriod reportPeriod31 = new ReportPeriod();
        reportPeriod31.setId(7);
        reportPeriod31.setTaxPeriod(taxPeriod3);
        reportPeriod31.setOrder(1);
        reportPeriod31.setName("Первый отчетный период в 3 налоговом периоде");
		reportPeriod31.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
		reportPeriod31.setEndDate(new GregorianCalendar(2012, Calendar.MARCH, 31).getTime());
		reportPeriod31.setCalendarStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());

        ReportPeriod reportPeriod32 = new ReportPeriod();
        reportPeriod32.setId(8);
        reportPeriod32.setTaxPeriod(taxPeriod3);
        reportPeriod32.setOrder(2);
        reportPeriod32.setName("Второй отчетный период в 3 налоговом периоде");
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
        // перехват вызова функции получения отчетного периода по налоговому и возвращение нашего reportPeriod
        when(reportPeriodDao.get(1)).thenReturn(reportPeriod11);
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod12);
		when(reportPeriodDao.get(4)).thenReturn(reportPeriod14);
		when(reportPeriodDao.get(5)).thenReturn(reportPeriod21);
        when(reportPeriodDao.get(7)).thenReturn(reportPeriod31);
        when(reportPeriodDao.get(8)).thenReturn(reportPeriod32);

        // переотпределение вызовов метода listByTaxPeriod
        when(reportPeriodDao.listByTaxPeriod(1)).thenReturn(reportPeriodListBy1Period);
        when(reportPeriodDao.listByTaxPeriod(2)).thenReturn(reportPeriodListBy2Period);
        when(reportPeriodDao.listByTaxPeriod(3)).thenReturn(reportPeriodListBy3Period);

        /**
         * Подготовим отчетные периоды подразделений
         */
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        departmentReportPeriod.setId(1);
        departmentReportPeriod.setActive(true);
        departmentReportPeriod.setBalance(false);
        departmentReportPeriod.setCorrectionDate(null);
        departmentReportPeriod.setDepartmentId(1);
        departmentReportPeriod.setReportPeriod(reportPeriod11);
        when(departmentReportPeriodService.get(1)).thenReturn(departmentReportPeriod);

        RefBook refBook = new RefBook(){{
            setId(PERIOD_CODE_REFBOOK);
            setName("REFBOOK_NAME");
        }};
        when(rbFactory.get(PERIOD_CODE_REFBOOK)).thenReturn(refBook);

        when(rbFactory.getDataProvider(PERIOD_CODE_REFBOOK)).thenReturn(provider);
        Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
        record.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "NAME"));
        record.put("ORD", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        record.put("START_DATE", new RefBookValue(RefBookAttributeType.DATE, new Date(101, Calendar.FEBRUARY, 1)));
        record.put("END_DATE", new RefBookValue(RefBookAttributeType.DATE, new Date(101, Calendar.FEBRUARY, 28)));
        record.put("CALENDAR_START_DATE", new RefBookValue(RefBookAttributeType.DATE, new Date(101, Calendar.FEBRUARY, 1)));
        when(provider.getRecordData(1L)).thenReturn(record);
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
		checkDates(2012, Calendar.APRIL, 1, periodService.getReportPeriod(2).getStartDate());
    }

    @Test
    public void getEndDate(){
		checkDates(2012, Calendar.JUNE, 30, periodService.getReportPeriod(2).getEndDate());
    }

    @Test
    public void getReportDate(){
		checkDates(2012, Calendar.JULY, 1, periodService.getReportDate(2).getTime());
    }

    @Test
    public void getStartDateIncome(){
		checkDates(2012, Calendar.JANUARY, 1, periodService.getReportPeriod(8).getStartDate());
    }

	@Test
	public void getCalendarStartDateIncome(){
		checkDates(2012, Calendar.APRIL, 1, periodService.getReportPeriod(8).getCalendarStartDate());
	}

    @Test
    public void getEndDateIncome(){
		checkDates(2012, Calendar.JUNE, 30, periodService.getReportPeriod(8).getEndDate());
    }

    @Test
    public void getForthTransportPeriod(){
		checkDates(2012, Calendar.OCTOBER, 1, periodService.getReportPeriod(4).getStartDate());
    }

    @Test
    public void getMonthStartDate() {
		checkDates(2012, Calendar.MAY, 1, periodService.getMonthStartDate(8, 5).getTime());
    }

    @Test
    public void getMonthEndDate() {
		checkDates(2012, Calendar.MAY, 31, periodService.getMonthEndDate(8, 5).getTime());
    }

    @Test
    public void getMonthReportDate() {
		checkDates(2012, Calendar.JUNE, 1, periodService.getMonthReportDate(8, 5).getTime());
    }

    /*
     * Тест получения из текущего налогового периода
     */
    @Test
    public void getPrevReportPeriodInSide(){
        assertNotNull(periodService.getPrevReportPeriod(2));
        assertTrue(periodService.getPrevReportPeriod(2).getId() == 1);
    }

    /*
     * Тест получения из предыдущего налогового периода
     */
    @Test
    public void getPrevReportPeriodOutSide(){
        periodService.getPrevReportPeriod(5);
        assertEquals(periodService.getPrevReportPeriod(5).getId().intValue(), 4);
    }

    @Test
    public void open() {
        periodService.open(2012, 1, TaxType.TRANSPORT, userInfo, 1, new ArrayList<LogEntry>(), false, new Date());

        ArgumentCaptor<ReportPeriod> argument = ArgumentCaptor.forClass(ReportPeriod.class);
        verify(reportPeriodDao, times(1)).save(argument.capture());

        checkDates(2012, Calendar.FEBRUARY, 1, argument.getAllValues().get(0).getStartDate());
        checkDates(2012, Calendar.FEBRUARY, 29, argument.getAllValues().get(0).getEndDate());
        checkDates(2012, Calendar.FEBRUARY, 1, argument.getAllValues().get(0).getCalendarStartDate());
    }

    @Test
    public void isFirstPeriod() {
        Assert.assertTrue(periodService.isFirstPeriod(7));
        Assert.assertFalse(periodService.isFirstPeriod(8));
    }

    @Test
    public void close() {
        when(departmentService.getAllChildrenIds(1)).thenReturn(Arrays.asList(1,2,3));

        periodService.close(TaxType.TRANSPORT, 1, new ArrayList<LogEntry>(), userInfo);

        ArgumentCaptor<Integer> repPeriodId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> isActive = ArgumentCaptor.forClass(Boolean.class);
        verify(departmentReportPeriodService, times(1)).updateActive(anyListOf(Integer.class), repPeriodId.capture(), isActive.capture());

        assertEquals(1, repPeriodId.getValue().intValue());
        assertEquals(false, isActive.getValue());
    }

    @Test(expected = ServiceException.class)
    public void closeNonExistentPeriod() {
        periodService.close(TaxType.TRANSPORT, 999, new ArrayList<LogEntry>(), userInfo);
    }

    @Test(expected = ServiceException.class)
    public void removeNonExistentPeriod() {
        periodService.removeReportPeriod(TaxType.TRANSPORT, 999, new Logger(), userInfo);
    }
}
