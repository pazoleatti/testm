package com.aplana.sbrf.taxaccounting.util.mock;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.script.impl.FormDataServiceImpl;
import com.aplana.sbrf.taxaccounting.service.script.impl.ImportServiceImpl;
import com.aplana.sbrf.taxaccounting.service.script.impl.ReportPeriodServiceImpl;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.*;
import com.aplana.sbrf.taxaccounting.util.DataRowHelperStub;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

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

    @Override
    public FormDataService mockFormDataService() {
        // Mock имплементации из-за обращения к реальным методам (addRow())
        FormDataService formDataService = mock(FormDataServiceImpl.class);
        // DataRowHelper
        when(formDataService.getDataRowHelper(any(FormData.class))).thenReturn(currentDataRowHelper);

        // Работа со справочниками
        when(formDataService.getRefBookRecordIdImport(anyLong(), anyMap(), anyMap(), anyString(), anyString(),
                any(Date.class), anyInt(), anyInt(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                String alias = (String) invocation.getArguments()[3];
                String value = (String) invocation.getArguments()[4];
                Map<String, RefBookValue> map = getRecord(refBookId, alias, value);
                if (map == null) {
                    return null;
                }
                Number number = map.get(RefBook.RECORD_ID_ALIAS).getNumberValue();
                if (number == null) {
                    throw new ServiceException("Wrong reference book " + refBookId + " format!");
                }
                return number.longValue();
            }
        });
        when(formDataService.getRefBookRecordIdImport(anyLong(), anyMap(), anyMap(), anyString(), anyString(), anyString(),
                any(Date.class), anyInt(), anyInt(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                String alias = (String) invocation.getArguments()[3];
                String value = (String) invocation.getArguments()[4];
                Map<String, RefBookValue> map = getRecord(refBookId, alias, value);
                if (map == null) {
                    return null;
                }
                Number number = map.get(RefBook.RECORD_ID_ALIAS).getNumberValue();
                if (number == null) {
                    throw new ServiceException("Wrong reference book " + refBookId + " format!");
                }
                return number.longValue();
            }
        });
        when(formDataService.getRefBookRecordImport(anyLong(), anyMap(), anyMap(), anyMap(), anyString(), anyString(),
                any(Date.class), anyInt(), anyInt(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Map<String, RefBookValue> >() {
            @Override
            public Map<String, RefBookValue>  answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                String alias = (String) invocation.getArguments()[4];
                String value = (String) invocation.getArguments()[5];
                return getRecord(refBookId, alias, value);
            }
        });
        when(formDataService.getRefBookRecordImport(anyLong(), anyMap(), anyMap(), anyMap(), anyString(), anyString(), anyString(),
                any(Date.class), anyInt(), anyInt(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Map<String, RefBookValue> >() {
            @Override
            public Map<String, RefBookValue>  answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                String alias = (String) invocation.getArguments()[4];
                String value = (String) invocation.getArguments()[5];
                return getRecord(refBookId, alias, value);
            }
        });
        when(formDataService.getRefBookRecordId(anyLong(), anyMap(), anyMap(), anyString(), anyString(),
                any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
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
        when(formDataService.getRefBookRecordId(anyLong(), anyMap(), anyMap(), anyString(), anyString(), any(String.class),
                any(Date.class), anyInt(), anyString(), any(Logger.class), anyBoolean())).thenAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
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
        when(formDataService.getRefBookValue(anyLong(), anyLong(), anyMap())).thenAnswer(new Answer<Map<String, RefBookValue>>() {
            @Override
            public Map<String, RefBookValue> answer(InvocationOnMock invocation) throws Throwable {
                Long refBookId = (Long) invocation.getArguments()[0];
                Long recordId = (Long) invocation.getArguments()[1];
                Map<Long, Map<String, RefBookValue>> map = refBookMap.get(refBookId);
                if (map == null) {
                    return null;
                }
                return map.get(recordId);
            }
        });

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
    public BookerStatementService mockBookerStatementService() {
        return mock(BookerStatementService.class);
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
    public DepartmentFormTypeService mockDepartmentFormTypeService() {
        return mock(DepartmentFormTypeService.class);
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
    public FormTypeService mockFormTypeService() {
        return mock(FormTypeService.class);
    }

    @Override
    public DeclarationService mockDeclarationService(){
        return mock(DeclarationService.class);
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

    // "Персонифицированные сведения о застрахованных лицах"
    @Override
    public RaschsvPersSvStrahLicService mockRaschsvPersSvStrahLicService() {
        RaschsvPersSvStrahLicService raschsvPersSvStrahLicService = mock(RaschsvPersSvStrahLicService.class);
        when(raschsvPersSvStrahLicService.insertPersSvStrahLic(any(List.class))).thenReturn(1);
        return raschsvPersSvStrahLicService;
    }

    // "Сводные данные об обязательствах плательщика страховых взносов"
    @Override
    public RaschsvObyazPlatSvService mockRaschsvObyazPlatSvService() {
        RaschsvObyazPlatSvService raschsvObyazPlatSvService = mock(RaschsvObyazPlatSvService.class);
        when(raschsvObyazPlatSvService.insertObyazPlatSv(any(RaschsvObyazPlatSv.class))).thenReturn(1L);
        return raschsvObyazPlatSvService;
    }

    // "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
    @Override
    public RaschsvUplPerService mockRaschsvUplPerService() {
        RaschsvUplPerService raschsvObyazPlatSvService = mock(RaschsvUplPerService.class);
        when(raschsvObyazPlatSvService.insertUplPer(any(List.class))).thenReturn(1);
        return raschsvObyazPlatSvService;
    }

    // "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    @Override
    public RaschsvUplPrevOssService mockRaschsvUplPrevOssService() {
        RaschsvUplPrevOssService raschsvUplPrevOssService = mock(RaschsvUplPrevOssService.class);
        when(raschsvUplPrevOssService.insertUplPrevOss(any(RaschsvUplPrevOss.class))).thenReturn(1L);
        return raschsvUplPrevOssService;
    }

    // "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
    @Override
    public RaschsvSvOpsOmsService mockRaschsvSvOpsOmsService() {
        RaschsvSvOpsOmsService raschsvSvOpsOmsService = mock(RaschsvSvOpsOmsService.class);
        when(raschsvSvOpsOmsService.insertRaschsvSvOpsOms(any(List.class))).thenReturn(1);
        return raschsvSvOpsOmsService;
    }

    // "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
    @Override
    public RaschsvOssVnmService mockRaschsvOssVnmService() {
        RaschsvOssVnmService raschsvOssVnmService = mock(RaschsvOssVnmService.class);
        when(raschsvOssVnmService.insertRaschsvOssVnm(any(RaschsvOssVnm.class))).thenReturn(1L);
        return raschsvOssVnmService;
    }

    // "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
    @Override
    public RaschsvRashOssZakService mockRaschsvRashOssZakService() {
        RaschsvRashOssZakService raschsvRashOssZakService = mock(RaschsvRashOssZakService.class);
        when(raschsvRashOssZakService.insertRaschsvRashOssZak(any(RaschsvRashOssZak.class))).thenReturn(1L);
        return raschsvRashOssZakService;
    }

    // "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
    @Override
    public RaschsvVyplFinFbService mockRaschsvVyplFinFbService() {
        RaschsvVyplFinFbService raschsvVyplFinFbService = mock(RaschsvVyplFinFbService.class);
        when(raschsvVyplFinFbService.insertRaschsvVyplFinFb(any(RaschsvVyplFinFb.class))).thenReturn(1L);
        return raschsvVyplFinFbService;
    }

    // "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427"
    @Override
    public RaschsvPravTarif31427Service mockRaschsvPravTarif31427Service() {
        RaschsvPravTarif31427Service raschsvPravTarif31427Service = mock(RaschsvPravTarif31427Service.class);
        when(raschsvPravTarif31427Service.insertRaschsvPravTarif31427(any(RaschsvPravTarif31427.class))).thenReturn(1L);
        return raschsvPravTarif31427Service;
    }

    // "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427"
    @Override
    public RaschsvPravTarif51427Service mockRaschsvPravTarif51427Service() {
        RaschsvPravTarif51427Service raschsvPravTarif51427Service = mock(RaschsvPravTarif51427Service.class);
        when(raschsvPravTarif51427Service.insertRaschsvPravTarif51427(any(RaschsvPravTarif51427.class))).thenReturn(1L);
        return raschsvPravTarif51427Service;
    }

    // "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427"
    @Override
    public RaschsvPravTarif71427Service mockRaschsvPravTarif71427Service() {
        RaschsvPravTarif71427Service raschsvPravTarif71427Service = mock(RaschsvPravTarif71427Service.class);
        when(raschsvPravTarif71427Service.insertRaschsvPravTarif71427(any(RaschsvPravTarif71427.class))).thenReturn(1L);
        return raschsvPravTarif71427Service;
    }

    // "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
    @Override
    public RaschsvSvPrimTarif91427Service mockRaschsvSvPrimTarif91427Service() {
        RaschsvSvPrimTarif91427Service raschsvSvPrimTarif91427Service = mock(RaschsvSvPrimTarif91427Service.class);
        when(raschsvSvPrimTarif91427Service.insertRaschsvSvPrimTarif91427(any(RaschsvSvPrimTarif91427.class))).thenReturn(1L);
        return raschsvSvPrimTarif91427Service;
    }

    // "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426)"
    @Override
    public RaschsvSvPrimTarif22425Service mockRaschsvSvPrimTarif22425Service() {
        RaschsvSvPrimTarif22425Service raschsvSvPrimTarif22425Service = mock(RaschsvSvPrimTarif22425Service.class);
        when(raschsvSvPrimTarif22425Service.insertRaschsvSvPrimTarif22425(any(RaschsvSvPrimTarif22425.class))).thenReturn(1L);
        return raschsvSvPrimTarif22425Service;
    }

    // "Сведения об обучающихся, необходимые для применения положений подпункта 1 пункта 3 статьи 422"
    @Override
    public RaschsvSvPrimTarif13422Service mockRaschsvSvPrimTarif13422Service() {
        RaschsvSvPrimTarif13422Service raschsvSvPrimTarif13422Service = mock(RaschsvSvPrimTarif13422Service.class);
        when(raschsvSvPrimTarif13422Service.insertRaschsvSvPrimTarif13422(any(RaschsvSvPrimTarif13422.class))).thenReturn(1L);
        return raschsvSvPrimTarif13422Service;
    }

    // "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
    public RaschsvSvnpPodpisantService mockRaschsvSvnpPodpisantService() {
        RaschsvSvnpPodpisantService raschsvSvnpPodpisantService = mock(RaschsvSvnpPodpisantService.class);
        when(raschsvSvnpPodpisantService.insertRaschsvSvnpPodpisant(any(RaschsvSvnpPodpisant.class))).thenReturn(1L);
        return raschsvSvnpPodpisantService;
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
        when(pagingResultItem.get(any())).thenReturn(new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        pagingResult.add(pagingResultItem);
        when(refBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(pagingResult);
        return refBookDataProvider;
    }


    @Override
    public DataRowHelper getDataRowHelper() {
        return currentDataRowHelper;
    }

    @Override
    public RefBookDataProvider getRefBookDataProvider() {
        return refBookDataProvider;
    }

    @Override
    public DeclarationService getDeclarationService() {
        return mock(DeclarationService.class);
    }

    @Override
    public NdflPersonService getNdflPersonService() {
        return mock(NdflPersonService.class);
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
