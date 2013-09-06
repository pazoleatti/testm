package com.aplana.sbrf.taxaccounting.service.script;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.script.impl.QuarterServiceImpl;

/**
 * @author auldanov
 * Date: 03.06.13
 * Time: 14:12
 */
public class QuarterServiceTest {
    QuarterService service = new QuarterServiceImpl();

    @Before
    public void init(){
    	
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
        ReportPeriod reportPeriod2 = getReportPeriod(2, taxPeriod1);
        // подготовим модель reportPeriod для 1 отчетного периода в 2 налоговом периоде
        ReportPeriod reportPeriod5 = getReportPeriod(5, taxPeriod2);

        // перехват вызова функции получения отчетного периода по налоговому и возвращение нашего reportPeriod
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod2);
        when(reportPeriodDao.get(5)).thenReturn(reportPeriod5);
        when(reportPeriodDao.get(8)).thenReturn(getReportPeriod(8, taxPeriod3));

        // подготовка списка отчетных периодов для 1 налогового периода
        List<ReportPeriod> reportPeriodListBy1Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy1Period.add(getReportPeriod(1, taxPeriod1));
        reportPeriodListBy1Period.add(getReportPeriod(2, taxPeriod1));
        reportPeriodListBy1Period.add(getReportPeriod(3, taxPeriod1));
        reportPeriodListBy1Period.add(getReportPeriod(4, taxPeriod1));
        // подготовка списка отчетных периодов для 2 налогового периода
        List<ReportPeriod> reportPeriodListBy2Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy2Period.add(getReportPeriod(5, taxPeriod2));
        reportPeriodListBy2Period.add(getReportPeriod(6, taxPeriod2));
        // подготовка списка отчетных периодов для 3 налогового периода
        List<ReportPeriod> reportPeriodListBy3Period= new ArrayList<ReportPeriod>();
        reportPeriodListBy2Period.add(getReportPeriod(7, taxPeriod3));
        reportPeriodListBy2Period.add(getReportPeriod(8, taxPeriod3));


        when(reportPeriodDao.listByTaxPeriod(1)).thenReturn(reportPeriodListBy1Period);
        when(reportPeriodDao.listByTaxPeriod(2)).thenReturn(reportPeriodListBy2Period);
        when(reportPeriodDao.listByTaxPeriod(3)).thenReturn(reportPeriodListBy3Period);

        ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);

    }

    /**
     * Фабричный метод для создания отченого периода
     */
    public ReportPeriod getReportPeriod(int id, TaxPeriod taxPeriod){
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(id);
        reportPeriod.setTaxPeriod(taxPeriod);
        return reportPeriod;
    }

    /*
     * Тест получения из текущего налогового периода
     */
    @Test
    public void getPrevReportPeriodInSide(){
        assertNull(service.getPrevReportPeriod(5));
        assertTrue(service.getPrevReportPeriod(2).getId() == 1);
    }
}
