package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.script.service.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataServiceImpl;
import com.aplana.sbrf.taxaccounting.script.service.impl.DeclarationServiceImpl;
import com.aplana.sbrf.taxaccounting.script.service.impl.ImportServiceImpl;
import com.aplana.sbrf.taxaccounting.script.service.impl.ReportPeriodServiceImpl;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import net.sf.jasperreports.engine.JasperPrint;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Реализация хэлпера с заглушками сервисов по-умолчанию
 *
 * @author Levykin
 */
public class DefaultScriptTestMockHelper implements ScriptTestMockHelper {
    private RefBookDataProvider refBookDataProvider = mockRefBookDataProvider();
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

    /**
     * Получение записи справочника по значению атрибута
     */
    private Map<String, RefBookValue> getRecord(long refBookId, String alias, String value) {
        Map<Long, Map<String, RefBookValue>> map = refBookMap.get(refBookId);
        if (map == null) {
            throw new ServiceException("Not found reference book with id = %d!", refBookId);
        }

        // Поиск по значению
        for (Map.Entry<Long, Map<String, RefBookValue>> entry : map.entrySet()) {
            RefBookValue refBookValue = entry.getValue().get(alias);
            if (refBookValue == null) {
                throw new ServiceException("Not found %s = \"%s\" in reference book with id = %d!", alias, value,
                        refBookId);
            }
            // Поиск производится только по числовому и строковому значению, другой поиск не требуется
            String refBookStringValue = refBookValue.getStringValue();
            String tmpValue = value;
            if (entry.getValue().get(alias).getAttributeType() == RefBookAttributeType.NUMBER) {
                refBookStringValue = String.valueOf(entry.getValue().get(alias).getNumberValue().longValue());
                tmpValue = (value.endsWith(".0") ? value.replaceAll(".0", "") : value);
            }

            if (refBookStringValue != null && refBookStringValue.equals(tmpValue)) {
                return entry.getValue();
            }
        }
        // Запись не найдена
        return null;
    }

    @Override
    public ReportPeriodService mockReportPeriodService() {
        ReportPeriodService reportPeriodService = mock(ReportPeriodServiceImpl.class);
        when(reportPeriodService.getStartDate(anyInt())).thenReturn(PERIOD_START_DATE);
        when(reportPeriodService.getEndDate(anyInt())).thenReturn(PERIOD_END_DATE);
        when(reportPeriodService.getReportDate(anyInt())).thenReturn(PERIOD_END_DATE);
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setTaxType(TaxType.INCOME);
        taxPeriod.setId(1);
        taxPeriod.setYear(2014);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setOrder(1);
        reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setName("test period name");
        when(reportPeriodService.get(anyInt())).thenReturn(reportPeriod);
        when(reportPeriodService.getCalendarStartDate(anyInt())).thenReturn(PERIOD_START_DATE);
        return reportPeriodService;
    }

    @Override
    public DepartmentService mockDepartmentService() {
        return mock(DepartmentService.class);
    }

    @Override
    public ImportService mockImportService() {
        return new ImportServiceImpl();
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
    public RefBookPersonService mockRefBookPersonService() {
        return mock(RefBookPersonService.class);
    }

    @Override
    public FiasRefBookService mockFiasRefBookService() {
        return mock(FiasRefBookService.class);
    }

    @Override
    public RefBookFactory mockRefBookFactory() {
        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        when(refBookFactory.getDataProvider(anyLong())).thenReturn(refBookDataProvider);
        return refBookFactory;
    }

    @Override
    public DepartmentReportPeriodService mockDepartmentReportPeriodService() {
        return mock(DepartmentReportPeriodService.class);
    }

    @Override
    public DeclarationService mockDeclarationService() {

        DeclarationService mockDeclarationService = mock(DeclarationServiceImpl.class);

        //желательно вынести утилитные методы в отдельный сервис, чтобы не приходилось, переопределять их вне mock контекста
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                JasperPrint jasperPrint = (JasperPrint) args[0];
                OutputStream outputStream = (OutputStream) args[1];
                DeclarationDataService ds = new DeclarationDataServiceImpl();
                ds.exportXLSX(jasperPrint, outputStream);
                return null;
            }
        }).when(mockDeclarationService).exportXLSX(any(JasperPrint.class), any(OutputStream.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                JasperPrint jasperPrint = (JasperPrint) args[0];
                OutputStream outputStream = (OutputStream) args[1];
                DeclarationDataService ds = new DeclarationDataServiceImpl();
                ds.exportPDF(jasperPrint, outputStream);
                return null;
            }
        }).when(mockDeclarationService).exportPDF(any(JasperPrint.class), any(OutputStream.class));

        ConfigurationParamModel configurationParamModel = new ConfigurationParamModel();
        Map<Integer, List<String>> confShowTimingMap = new LinkedHashMap<Integer, List<String>>();
        List<String> timingList = new ArrayList<String>();
        timingList.add("0");
        confShowTimingMap.put(0, timingList);
        Map<Integer, List<String>> confLimitIdentMap = new LinkedHashMap<Integer, List<String>>();
        List<String> identList = new ArrayList<String>();
        identList.add("0.65");
        confLimitIdentMap.put(0, identList);
        configurationParamModel.put(ConfigurationParam.SHOW_TIMING, confShowTimingMap);
        configurationParamModel.put(ConfigurationParam.LIMIT_IDENT, confLimitIdentMap);
        when(mockDeclarationService.getAllConfig(any(TAUserInfo.class))).thenReturn(configurationParamModel);

        return mockDeclarationService;
    }

    @Override
    public TransactionHelper mockTransactionHelper() {
        return mock(TransactionHelper.class);
    }

    @Override
    public NdflPersonService mockNdflPersonService() {
        NdflPersonService ndflPersonService = mock(NdflPersonService.class);
        when(ndflPersonService.save(any(NdflPerson.class))).thenReturn(1243L);
        return ndflPersonService;
    }

    @Override
    public FiasRefBookService fiasRefBookService() {
        return mock(FiasRefBookService.class);
    }

    @Override
    public BlobDataService mockBlobDataService() {
        return mock(BlobDataService.class);
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
        PagingResult<Map<String, RefBookValue>> pagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> pagingResultItem = mock(Map.class);
        // Значения полей в таблице REF_BOOK_NDFL_DETAIL
        when(pagingResultItem.get(any())).thenReturn(new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        when(pagingResultItem.get("OKTMO")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, "12345678"));
        when(pagingResultItem.get("INN")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, "0123456789"));
        when(pagingResultItem.get("PHONE")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, null));
        when(pagingResultItem.get("SIGNATORY_LASTNAME")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, null));
        when(pagingResultItem.get("TAX_ORGAN_CODE")).thenReturn(new RefBookValue(RefBookAttributeType.STRING, "1234"));
        pagingResult.add(pagingResultItem);
        when(refBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(pagingResult);
        return refBookDataProvider;
    }

    @Override
    public RefBookDataProvider getRefBookDataProvider() {
        return refBookDataProvider;
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRefBookAllRecords(Long refBookId) {
        return refBookMap.get(refBookId);
    }

    @Override
    public ImportFiasDataService mockImportFiasDataService() {
        return mock(ImportFiasDataService.class);
    }
}
