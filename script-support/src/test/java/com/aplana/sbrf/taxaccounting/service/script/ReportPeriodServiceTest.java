package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.script.impl.ReportPeriodServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportPeriodServiceTest {

	ReportPeriodService service = new ReportPeriodServiceImpl();
	
	@Before
	public void init(){
		
        // Mock для taxPeriodDao
        TaxPeriodDao taxPeriodDao = mock(TaxPeriodDao.class);
        // 1 налоговый период
        TaxPeriod taxPeriod1 = new TaxPeriod();
        taxPeriod1.setId(1);
        taxPeriod1.setTaxType(TaxType.TRANSPORT);
        // set time
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 1, 1);
        taxPeriod1.setStartDate(cl.getTime());
        // 2 налоговый период
        TaxPeriod taxPeriod2 = new TaxPeriod();
        taxPeriod2.setId(2);
        taxPeriod2.setTaxType(TaxType.TRANSPORT);
        // 3 налоговый период
        TaxPeriod taxPeriod3 = new TaxPeriod();
        taxPeriod3.setId(3);
        taxPeriod3.setTaxType(TaxType.INCOME);
        // set time
        taxPeriod3.setStartDate(cl.getTime());
		
		// Mock для reportPeriodDao
		ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
		
		// подготовим модели reportPeriod
		ReportPeriod reportPeriod2 = getReportPeriod(2, taxPeriod1, 2, 2);
		// подготовим модель reportPeriod для 1 отчетного периода в 2 налоговом периоде
        ReportPeriod reportPeriod5 = getReportPeriod(5, taxPeriod2, 2, 1);
        // отчетный период для 1 налогового периода по транспотру
        ReportPeriod reportPeriod1 = getReportPeriod(4, taxPeriod1, 4, 4);

		// перехват вызова функции получения отчетного периода по налоговому и возвращение нашего reportPeriod
		when(reportPeriodDao.get(2)).thenReturn(reportPeriod2);
        when(reportPeriodDao.get(5)).thenReturn(reportPeriod5);
        when(reportPeriodDao.get(8)).thenReturn(getReportPeriod(8, taxPeriod3, 2, 2));
        when(reportPeriodDao.get(4)).thenReturn(reportPeriod1);

		// подготовка списка отчетных периодов для 1 налогового периода 
		List<ReportPeriod> reportPeriodListBy1Period= new ArrayList<ReportPeriod>();
		reportPeriodListBy1Period.add(getReportPeriod(4, taxPeriod1, 4, 4));
        reportPeriodListBy1Period.add(getReportPeriod(3, taxPeriod1, 3, 3));
        reportPeriodListBy1Period.add(getReportPeriod(2, taxPeriod1, 2, 2));
        reportPeriodListBy1Period.add(getReportPeriod(1, taxPeriod1, 1, 1));
        // подготовка списка отчетных периодов для 2 налогового периода
        List<ReportPeriod> reportPeriodListBy2Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy2Period.add(getReportPeriod(6, taxPeriod2, 2, 2));
        reportPeriodListBy2Period.add(getReportPeriod(5, taxPeriod2, 1, 1));
        // подготовка списка отчетных периодов для 3 налогового периода
        List<ReportPeriod> reportPeriodListBy3Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy3Period.add(getReportPeriod(8, taxPeriod3, 2, 2));
        reportPeriodListBy3Period.add(getReportPeriod(7, taxPeriod3, 1, 1));

		
		when(reportPeriodDao.listByTaxPeriod(1)).thenReturn(reportPeriodListBy1Period);
        when(reportPeriodDao.listByTaxPeriod(2)).thenReturn(reportPeriodListBy2Period);
        when(reportPeriodDao.listByTaxPeriod(3)).thenReturn(reportPeriodListBy3Period);

        ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);
        

        



        when(taxPeriodDao.get(1)).thenReturn(taxPeriod1);
        when(taxPeriodDao.get(2)).thenReturn(taxPeriod2);
        when(taxPeriodDao.get(3)).thenReturn(taxPeriod3);

        List<TaxPeriod> taxPeriodList = new ArrayList<TaxPeriod>();
        taxPeriodList.add(taxPeriod1);
        taxPeriodList.add(taxPeriod2);

        when(taxPeriodDao.listByTaxType(TaxType.TRANSPORT)).thenReturn(taxPeriodList);
		
        ReflectionTestUtils.setField(service, "taxPeriodDao", taxPeriodDao);
        
	}
	
	/**
	 * Фабричный метод для создания отченого периода 
	 */
	public ReportPeriod getReportPeriod(int id, TaxPeriod taxPeriod, int months, int order){
		ReportPeriod reportPeriod = new ReportPeriod();
		reportPeriod.setId(id);
		reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setMonths(months);
        reportPeriod.setOrder(order);
		
		return reportPeriod;
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
		assertEquals(service.getPrevReportPeriod(5).getId().intValue(), 4);
	}


    @Test
    public void getStartDate(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 2, 1);

        assertEquals(service.getStartDate(2).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getEndDate(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 4, 1);

        assertEquals(service.getEndDate(2).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getStartDateIncome(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 1, 1);

        assertEquals("Wait ", service.getStartDate(8).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getEndDateIncome(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 2, 1);

        assertEquals(service.getEndDate(8).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }

    @Test
    public void getForthTransportPeriod(){
        Calendar cl = Calendar.getInstance();
        cl.set(2012, 1, 1);

        assertEquals(service.getStartDate(4).get(Calendar.MONTH), cl.get(Calendar.MONTH));
    }
}
