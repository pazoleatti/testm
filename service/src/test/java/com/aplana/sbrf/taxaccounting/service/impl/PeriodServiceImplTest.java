package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PeriodServiceImplTest {
    PeriodService service = new PeriodServiceImpl();

    @Before
    public void init(){
        /**
         * Налоговые периоды 1,2 - транспорт.
         * 3 - по прибыли
         */
        TaxPeriod taxPeriod1 = new TaxPeriod();
        taxPeriod1.setId(1);
        taxPeriod1.setStartDate(new GregorianCalendar(2012, 1, 1).getTime());
        taxPeriod1.setEndDate(new GregorianCalendar(2012, 12, 31).getTime());
        taxPeriod1.setTaxType(TaxType.TRANSPORT);

        TaxPeriod taxPeriod2 = new TaxPeriod();
        taxPeriod2.setId(2);
        taxPeriod2.setStartDate(new GregorianCalendar(2012, 1, 1).getTime());
        taxPeriod2.setEndDate(new GregorianCalendar(2012, 12, 31).getTime());
        taxPeriod2.setTaxType(TaxType.TRANSPORT);

        TaxPeriod taxPeriod3 = new TaxPeriod();
        taxPeriod3.setId(3);
        taxPeriod3.setStartDate(new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime());
        taxPeriod3.setEndDate(new GregorianCalendar(2012, Calendar.DECEMBER, 31).getTime());
        taxPeriod3.setTaxType(TaxType.INCOME);

        // список налоговых периодов по ТН
        List<TaxPeriod> taxPeriodList = new ArrayList<TaxPeriod>();
        taxPeriodList.add(taxPeriod1);
        taxPeriodList.add(taxPeriod2);

        // Mock для taxPeriodDao
        TaxPeriodDao taxPeriodDao = mock(TaxPeriodDao.class);
        when(taxPeriodDao.get(1)).thenReturn(taxPeriod1);
        when(taxPeriodDao.get(2)).thenReturn(taxPeriod2);
        when(taxPeriodDao.get(3)).thenReturn(taxPeriod3);
        when(taxPeriodDao.listByTaxType(TaxType.TRANSPORT)).thenReturn(taxPeriodList);
        ReflectionTestUtils.setField(service, "taxPeriodDao", taxPeriodDao);

        /**
         * подготовим отчетные периоды для 1 налогового
         */
        ReportPeriod reportPeriod11 = new ReportPeriod();
        reportPeriod11.setId(1);
        reportPeriod11.setMonths(1);
        reportPeriod11.setTaxPeriod(taxPeriod1);
        reportPeriod11.setName("Первый отчетный период в 1 налоговом периоде");
        reportPeriod11.setOrder(1);

        ReportPeriod reportPeriod12 = new ReportPeriod();
        reportPeriod12.setId(2);
        reportPeriod12.setMonths(2);
        reportPeriod12.setTaxPeriod(taxPeriod1);
        reportPeriod12.setName("Второй отчетный период в 1 налоговом периоде");
        reportPeriod12.setOrder(2);

        ReportPeriod reportPeriod13 = new ReportPeriod();
        reportPeriod13.setId(3);
        reportPeriod13.setMonths(3);
        reportPeriod13.setTaxPeriod(taxPeriod1);
        reportPeriod13.setName("Третий отчетный период в 1 налоговом периоде");
        reportPeriod13.setOrder(3);

        ReportPeriod reportPeriod14 = new ReportPeriod();
        reportPeriod14.setId(4);
        reportPeriod14.setMonths(4);
        reportPeriod14.setTaxPeriod(taxPeriod1);
        reportPeriod14.setName("Четвертый отчетный период в 1 налоговом периоде");
        reportPeriod14.setOrder(4);


        /**
         * подготовим отчетные периоды для 2 налогового
         */
        ReportPeriod reportPeriod21 = new ReportPeriod();
        reportPeriod21.setId(1);
        reportPeriod21.setMonths(1);
        reportPeriod21.setTaxPeriod(taxPeriod2);
        reportPeriod21.setName("Первый отчетный период в 2 налоговом периоде");
        reportPeriod21.setOrder(1);

        ReportPeriod reportPeriod22 = new ReportPeriod();
        reportPeriod22.setId(2);
        reportPeriod22.setMonths(2);
        reportPeriod22.setTaxPeriod(taxPeriod2);
        reportPeriod22.setName("Второй отчетный период в 2 налоговом периоде");
        reportPeriod22.setOrder(2);

        /**
         * подготовим отчетные периоды для 3 налогового
         */
        ReportPeriod reportPeriod31 = new ReportPeriod();
        reportPeriod31.setId(1);
        reportPeriod31.setMonths(1);
        reportPeriod31.setTaxPeriod(taxPeriod3);
        reportPeriod31.setName("Первый отчетный период в 3 налоговом периоде");
        reportPeriod31.setOrder(1);

        ReportPeriod reportPeriod32 = new ReportPeriod();
        reportPeriod32.setId(2);
        reportPeriod32.setMonths(2);
        reportPeriod32.setTaxPeriod(taxPeriod3);
        reportPeriod32.setName("Второй отчетный период в 3 налоговом периоде");
        reportPeriod32.setOrder(2);

        // списки отчетных периодов для налоговых
        List<ReportPeriod> reportPeriodListBy1Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy1Period.add(reportPeriod11);
        reportPeriodListBy1Period.add(reportPeriod12);
        reportPeriodListBy1Period.add(reportPeriod13);
        reportPeriodListBy1Period.add(reportPeriod14);

        List<ReportPeriod> reportPeriodListBy2Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy1Period.add(reportPeriod21);
        reportPeriodListBy1Period.add(reportPeriod22);

        List<ReportPeriod> reportPeriodListBy3Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy1Period.add(reportPeriod31);
        reportPeriodListBy1Period.add(reportPeriod32);


        // Mock для reportPeriodDao
        ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);

        // перехват вызова функции получения отчетного периода по налоговому и возвращение нашего reportPeriod
        when(reportPeriodDao.get(1)).thenReturn(reportPeriod11);
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod12);
        when(reportPeriodDao.get(5)).thenReturn(reportPeriod21);
        when(reportPeriodDao.get(8)).thenReturn(reportPeriod32);
        when(reportPeriodDao.get(4)).thenReturn(reportPeriod14);

        // переотпределение вызовов метода listByTaxPeriod
        when(reportPeriodDao.listByTaxPeriod(1)).thenReturn(reportPeriodListBy1Period);
        when(reportPeriodDao.listByTaxPeriod(2)).thenReturn(reportPeriodListBy2Period);
        when(reportPeriodDao.listByTaxPeriod(3)).thenReturn(reportPeriodListBy3Period);

        ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);
    }

    @Test
    public void getStartDate(){
        Calendar cl = new GregorianCalendar();
        cl.clear();
        cl.set(2012, 1, 1);

        assertEquals(service.getStartDate(2), cl);
    }

    @Test
    public void getEndDate(){
        Calendar c2 = new GregorianCalendar();
        c2.clear();
        c2.set(2012, 2, 31);

        assertEquals(service.getEndDate(2), c2);

        Calendar cl = new GregorianCalendar();
        cl.clear();
        cl.set(2012, 0, 31);

        assertEquals(service.getEndDate(1), cl);
    }

    @Test
    public void getReportDate(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 3, 1);

        assertEquals(service.getReportDate(2).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getStartDateIncome(){
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.set(2012, 0, 1);

        assertEquals("Wait ", service.getStartDate(8).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getEndDateIncome(){
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.set(2012, 1, 1);

        assertEquals(service.getEndDate(8).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getForthTransportPeriod(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 1, 1);

        assertEquals(service.getStartDate(4).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getMonthFirstDate() {
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.set(2012, Calendar.FEBRUARY, 1);

        assertEquals(service.getMonthStartDate(8, 2), cl);
    }

    @Test
    public void getMonthEndDate() {
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.set(2012, Calendar.FEBRUARY, 29);

        assertEquals(service.getMonthEndDate(8, 2), cl);
    }

    @Test
    public void getMonthReportDate() {
        Calendar cl = Calendar.getInstance();
        cl.clear();
        cl.set(2012, Calendar.MARCH, 1);

        assertEquals(service.getMonthReportDate(8, 2), cl);
    }
}
