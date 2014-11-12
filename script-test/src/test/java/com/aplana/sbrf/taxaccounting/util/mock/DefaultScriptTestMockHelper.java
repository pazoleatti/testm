package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.impl.FormDataServiceImpl;
import com.aplana.sbrf.taxaccounting.service.script.impl.ImportServiceImpl;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Реализация хэлпера с заглушками сервисов по-умолчанию
 *
 * @author Levykin
 */
public class DefaultScriptTestMockHelper implements ScriptTestMockHelper {
    // DataRowHelper для тестируемой НФ
    private DataRowHelper currentDataRowHelper = new DataRowHelperStub();
    private Map<Long, Map<Long, Map<String, RefBookValue>>> refBookMap;
    public static Calendar PERIOD_START_DATE = Calendar.getInstance();
    public static Calendar PERIOD_END_DATE = Calendar.getInstance();

    // По-умолчанию НФ в периоде 01.01.2014 — 21.12.2014
    static {
        PERIOD_START_DATE.setTime(new GregorianCalendar(2014, 0, 1).getTime());
        PERIOD_END_DATE.setTime(new GregorianCalendar(2014, 11, 31).getTime());
    }

    /**
     * Реализация хэлпера с заглушками сервисов по-умолчанию
     */
    public DefaultScriptTestMockHelper(Map<Long, Map<Long, Map<String, RefBookValue>>> map) {
        this.refBookMap = map;
    }

    @Override
    public FormDataService mockFormDataService() {
        // Mock имплементации из-за обращения к реальным методам (addRow())
        FormDataService formDataService = mock(FormDataServiceImpl.class);
        // DataRowHelper
        when(formDataService.getDataRowHelper(any(FormData.class))).thenReturn(currentDataRowHelper);

        // Работа со справочниками
        when(formDataService.getRefBookRecordIdImport(anyLong(), anyMap(), anyMap(), anyString(), anyString(),
                any(Date.class), anyInt(), anyInt(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                String alias = (String) invocation.getArguments()[3];
                String value = (String) invocation.getArguments()[4];
                Map<String, RefBookValue> map = getRecord(refBookId, alias, value);
                if (map == null) {
                    return null;
                }
                return map.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
            }
        });
        when(formDataService.getRefBookRecordImport(anyLong(), anyMap(), anyMap(), anyMap(), anyString(), anyString(),
                any(Date.class), anyInt(), anyInt(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                String alias = (String) invocation.getArguments()[4];
                String value = (String) invocation.getArguments()[5];
                return getRecord(refBookId, alias, value);
            }
        });
        RefBookDataProvider refBookDataProvider = mockRefBookDataProvider();
        when(formDataService.getRefBookProvider(any(RefBookFactory.class), anyLong(), anyMap())).thenReturn(refBookDataProvider);
        // Работа со строками НФ
        when(formDataService.addRow(any(FormData.class), any(DataRow.class), anyList(), anyList())).thenCallRealMethod();
        return formDataService;
    }

    /**
     * Получение записи справочника по значению атрибута
     */
    private Map<String, RefBookValue> getRecord(long refBookId, String alias, String value) {
        Map<Long, Map<String, RefBookValue>> map = refBookMap.get(refBookId);
        if (map == null) {
            throw new ServiceException("Not found reference book with id = %d!", refBookId);
        }

        for (Map.Entry<Long, Map<String, RefBookValue>> entry : map.entrySet()) {
            String refBookValue = entry.getValue().get(alias).getStringValue();
            if (entry.getValue().get(alias).getAttributeType() == RefBookAttributeType.NUMBER) {
                refBookValue = String.valueOf(entry.getValue().get(alias).getNumberValue().longValue());
            }
            if (refBookValue.equals(value)) {
                return entry.getValue();
            }
        }
        // Запись не найдена
        return null;
    }

    @Override
    public ReportPeriodService mockReportPeriodService() {
        ReportPeriodService reportPeriodService = mock(ReportPeriodService.class);
        when(reportPeriodService.getStartDate(anyInt())).thenReturn(PERIOD_START_DATE);
        when(reportPeriodService.getEndDate(anyInt())).thenReturn(PERIOD_END_DATE);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setTaxType(TaxType.INCOME);
        taxPeriod.setId(1);
        taxPeriod.setYear(2014);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setTaxPeriod(taxPeriod);
        when(reportPeriodService.get(anyInt())).thenReturn(reportPeriod);
        return reportPeriodService;
    }

    @Override
    public ImportService mockImportService() {
        ImportService importService = new ImportServiceImpl();
        return importService;
    }

    @Override
    public RefBookService mockRefBookService() {
        RefBookService refBookService = mock(RefBookService.class);
        // Разыменование записей
        when(refBookService.getDateValue(anyLong(), anyLong(), anyString())).thenAnswer(new Answer<Date>() {
            @Override
            public Date answer(InvocationOnMock invocation) throws Throwable {
                return getRefBookValue(invocation).getDateValue();
            }
        });
        when(refBookService.getNumberValue(anyLong(), anyLong(), anyString())).thenAnswer(new Answer<Number>() {
            @Override
            public Number answer(InvocationOnMock invocation) throws Throwable {
                return getRefBookValue(invocation).getNumberValue();
            }
        });
        when(refBookService.getStringValue(anyLong(), anyLong(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return getRefBookValue(invocation).getStringValue();
            }
        });
        when(refBookService.getRecordData(anyLong(), anyLong())).thenAnswer(new Answer<Map<String, RefBookValue>>() {
            @Override
            public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                return getRecordData((Long) invocation.getArguments()[0], (Long) invocation.getArguments()[1]);
            }
        });
        return refBookService;
    }

    @Override
    public DepartmentFormTypeService mockDepartmentFormTypeService() {
        DepartmentFormTypeService departmentFormTypeService = mock(DepartmentFormTypeService.class);
        return departmentFormTypeService;
    }

    @Override
    public RefBookFactory mockRefBookFactory() {
        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        return refBookFactory;
    }

    /**
     * Получение всех значений записи справочника по Id
     */
    private Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        Map<Long, Map<String, RefBookValue>> recordMap = refBookMap.get(refBookId);
        if (recordMap == null) {
            return null;
        }
        return recordMap.get(recordId);
    }

    /**
     * Получение значения записи справочника по Id
     */
    private RefBookValue getRefBookValue(InvocationOnMock invocation) {
        Map<String, RefBookValue> valueMap = getRecordData((Long) invocation.getArguments()[0],
                (Long) invocation.getArguments()[1]);
        if (valueMap == null) {
            return null;
        }
        return valueMap.get(invocation.getArguments()[2]);
    }

    private RefBookDataProvider mockRefBookDataProvider() {
        RefBookDataProvider refBookDataProvider = mock(RefBookDataProvider.class);
        return  refBookDataProvider;
    }

    @Override
    public DataRowHelper getDataRowHelper() {
        return currentDataRowHelper;
    }
}
