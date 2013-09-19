package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.RefBookIncome101;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

import java.io.*;

/**
 * Тест для импорта бух отчетности из xml файла смотри com.aplana.sbrf.taxaccounting.service.BookerStatementsService
 * User: ekuvshinov
 *
 * @see com.aplana.sbrf.taxaccounting.service.BookerStatementsService
 */
public class BookerStatementsServiceImplTest {
    private static final long REFBOOK_INCOME_101 = 50L;
    private static final long REFBOOK_INCOME_102 = 52L;
    private static final Integer TYPE_INCOME_101 = 0;
    private static final Integer TYPE_INCOME_102 = 1;
    private static final Integer TYPE_INVALID = 2;
    private static final Integer REPORT_PERIOD_ID_OPEN = 1;
    private static final Integer REPORT_PERIOD_ID_CLOSED = 2;
    private static final Integer REPORT_PERIOD_ID_INVALID = 3;
    private static final Integer DEPARTMENT_ID = 1;
    private static final Integer DEPARTMENT_INVALID = 2;

    private static BookerStatementsService service;

    @BeforeClass
    public static void setUp() throws FileNotFoundException {
        service = new BookerStatementsServiceImpl();

        // По мокаем зависимости сервиса
        PeriodService periodService = mock(PeriodService.class);
        ReflectionTestUtils.setField(service, "reportPeriodService", periodService);
        when(periodService.isActivePeriod(REPORT_PERIOD_ID_OPEN, DEPARTMENT_ID)).thenReturn(true);
        when(periodService.isActivePeriod(REPORT_PERIOD_ID_CLOSED, DEPARTMENT_ID)).thenReturn(false);

        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        RefBookDataProvider provider101 = mock(RefBookDataProvider.class);
        when(refBookFactory.getDataProvider(REFBOOK_INCOME_101)).thenReturn(provider101);
        RefBookDataProvider provider102 = mock(RefBookDataProvider.class);
        when(refBookFactory.getDataProvider(REFBOOK_INCOME_102)).thenReturn(provider102);
        ReflectionTestUtils.setField(service, "rbFactory", refBookFactory);
    }

    /**
     * FIXME Пустой фаил в текущей реализации должен импортироваться успешно, возможно в будущем http://jira.aplana.com/browse/SBRFACCTAX-4483 поведение измениться
     */
    @Test
    public void importEmptyXml() {
        service.importXML("test.xls", getEmptyStream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_ID);
        service.importXML("test.xls", getEmptyStream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_102, DEPARTMENT_ID);
    }

    /**
     * FIXME метод должен выкидывать Исключения (но почему то не выкидывает) http://jira.aplana.com/browse/SBRFACCTAX-4485
     */
    public void importInvalidXml() {
        service.importXML("test.xls", getInvalidStream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_ID);
        service.importXML("test.xls", getInvalidStream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_102, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void nullReportPeriod() {
        service.importXML("test.xls", get101Stream(), null, TYPE_INCOME_101, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void nullStream() {
        service.importXML("test.xls", null, REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void nullName() {
        service.importXML(null, get101Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void invalidPeriod() {
        service.importXML("test.xls", get101Stream(), REPORT_PERIOD_ID_INVALID, TYPE_INCOME_101, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void closedPeriod() {
        service.importXML("test.xls", get101Stream(), REPORT_PERIOD_ID_CLOSED, TYPE_INCOME_101, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void invalidType() {
        service.importXML("test.xls", get101Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INVALID, DEPARTMENT_ID);
    }

    @Test(expected = ServiceException.class)
    public void invalidDepartment() {
        service.importXML("test.xls", get101Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_INVALID);
    }

    /**
     * Успешные параметры и должно всё успешно выполняться
     */
    @Test
    public void importValidXml() {
        service.importXML("test.xls", get101Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_101, DEPARTMENT_ID);
        service.importXML("test.xls", get102Stream(), REPORT_PERIOD_ID_OPEN, TYPE_INCOME_102, DEPARTMENT_ID);
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
