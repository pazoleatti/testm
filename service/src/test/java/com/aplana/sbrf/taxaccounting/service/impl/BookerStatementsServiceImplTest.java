package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.HashMap;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тест для импорта бух отчетности из xml файла смотри com.aplana.sbrf.taxaccounting.service.BookerStatementsService
 * User: ekuvshinov
 *
 * @see com.aplana.sbrf.taxaccounting.service.BookerStatementsService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("BookerStatementsSeviceImplTest.xml")
public class BookerStatementsServiceImplTest {
    private static final Integer TYPE_INCOME_101 = 0;
    private static final Integer TYPE_INCOME_102 = 1;
    private static final Integer REPORT_PERIOD_ID_OPEN = 1;
    private static final Integer REPORT_PERIOD_ID_INVALID = null;
    private static final Integer DEPARTMENT_ID = 1;
    private static final Integer DEPARTMENT_INVALID = null;

    @Autowired
    private BookerStatementsService bookerStatementsService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookDataProvider refBookIncome101;
    @Autowired
    private RefBookDataProvider refBookIncome102;

    @Test(expected = ServiceException.class)
    public void nullReportPeriod() {
        bookerStatementsService.importData("test.xls", get101Stream(), null, TYPE_INCOME_101, DEPARTMENT_ID, new TAUserInfo());
    }

    @Test(expected = ServiceException.class)
    public void nullStream() {
        bookerStatementsService.importData("test.xls", null, REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_ID, new TAUserInfo());
    }

    @Test(expected = ServiceException.class)
    public void invalidPeriod() {
        bookerStatementsService.importData("test.xls", get101Stream(), REPORT_PERIOD_ID_INVALID, TYPE_INCOME_101, DEPARTMENT_ID, new TAUserInfo());
    }

    @Test(expected = ServiceException.class)
    public void invalidDepartment() {
        bookerStatementsService.importData("test.xls", get101Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_INVALID, new TAUserInfo());
    }

    /**
     * Успешные параметры и должно всё успешно выполняться
     */
    @Test
    public void importValidData() {
        final Long referenceId  = 243035699l;
        when(refBookFactory.getDataProvider(107l)).thenReturn(refBookIncome101);
        when(refBookFactory.getDataProvider(106l)).thenReturn(refBookIncome102);
        when(refBookIncome101.getRecordData(REPORT_PERIOD_ID_OPEN.longValue()))
                .thenReturn(new HashMap<String, RefBookValue>() {{
                    put("YEAR", new RefBookValue(RefBookAttributeType.NUMBER, 2014));
                    put("ACCOUNT_PERIOD_ID", new RefBookValue(RefBookAttributeType.REFERENCE, referenceId));
                }});
        when(refBookIncome102.getRecordData(referenceId))
                .thenReturn(new HashMap<String, RefBookValue>() {{
                    put("NAME", new RefBookValue(RefBookAttributeType.STRING, "год"));
                }});

        Department department = new Department();
        department.setId(131);
        department.setName("Цетнральный");
        when(departmentService.getDepartment(department.getId())).thenReturn(department);

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setYear(2015);
        reportPeriod.setName("первый квартал");
        reportPeriod.setTaxPeriod(taxPeriod);
        when(periodService.getReportPeriod(reportPeriod.getId())).thenReturn(reportPeriod);

        String note = "Импорт бухгалтерской отчётности: test.xls";

        bookerStatementsService.importData("test.xls", get101Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, department.getId(), new TAUserInfo());
        verify(auditService, Mockito.atLeastOnce()).add(eq(FormDataEvent.IMPORT), any(TAUserInfo.class), eq("2014 год"), eq(department.getId()), isNull(String.class), isNull(String.class), isNull(Integer.class), eq(note), eq(AuditFormType.INCOME101), isNull(String.class));

        bookerStatementsService.importData("test.xls", get102Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_102, department.getId(), new TAUserInfo());
        verify(auditService, Mockito.atLeastOnce()).add(eq(FormDataEvent.IMPORT), any(TAUserInfo.class),  eq("2014 год"), eq(department.getId()), isNull(String.class), isNull(String.class), isNull(Integer.class), eq(note), eq(AuditFormType.INCOME102), isNull(String.class));
    }

    private static InputStream getEmptyStream() {
        return BookerStatementsServiceImplTest.class.getClassLoader().getResourceAsStream("com/aplana/sbrf/taxaccounting/service/impl/BookerStatementsServiceImplTestEmpty.xls");
    }

    private static InputStream get101Stream() {
        return BookerStatementsServiceImplTest.class.getClassLoader().getResourceAsStream("com/aplana/sbrf/taxaccounting/service/impl/BookerStatementsServiceImplTest101.xls");
    }

    private static InputStream get102Stream() {
        return BookerStatementsServiceImplTest.class.getClassLoader().getResourceAsStream("com/aplana/sbrf/taxaccounting/service/impl/BookerStatementsServiceImplTest102.xls");
    }
    private static InputStream getInvalidStream() {
        return BookerStatementsServiceImplTest.class.getClassLoader().getResourceAsStream("com/aplana/sbrf/taxaccounting/service/impl/BookerStatementsServiceImplTestInvalid.xls");
    }
}
