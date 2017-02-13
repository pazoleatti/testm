package com.aplana.sbrf.taxaccounting.form_template.fond.primary_1151111.v2016;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.util.DeclarationScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.DeclarationTestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Тестирование разбора xml-файла Расчета по страховым взносам
 */
public class RaschsvCheckTest extends DeclarationScriptTestBase {

    private RefBookFactory refBookFactory;

    private static final String FILE_NAME = "NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml";

    private static final long REF_BOOK_FOND_ID = RefBook.Id.FOND.getId();
    private static final long REF_BOOK_FOND_DETAIL_ID = RefBook.Id.FOND_DETAIL.getId();
    private static final long REF_BOOK_OKVED_ID = RefBook.Id.OKVED.getId();
    private static final long REF_BOOK_REORGANIZATION_ID = RefBook.Id.REORGANIZATION.getId();
    private static final long REF_BOOK_PRESENT_PLACE_ID = RefBook.Id.PRESENT_PLACE.getId();
    private static final long REF_BOOK_CONFIGURATION_PARAM_ID = RefBook.Id.CONFIGURATION_PARAM.getId();
    private static final long REF_BOOK_OKTMO_ID = RefBook.Id.OKTMO.getId();
    private static final long REF_BOOK_TARIFF_PAYER_ID = RefBook.Id.TARIFF_PAYER.getId();
    private static final long REF_BOOK_FILL_BASE_ID = RefBook.Id.FILL_BASE.getId();
    private static final long REF_BOOK_HARD_WORK_ID = RefBook.Id.HARD_WORK.getId();
    private static final long REF_BOOK_PERSON_CATEGORY_ID = RefBook.Id.PERSON_CATEGORY.getId();

    private static final int DEPARTMENT_ID = 1;
    private static final int DECLARATION_TEMPLATE_ID = 1022;
    private static final int REPORT_PERIOD_ID = 3489;
    private static final int DEPARTMENT_PERIOD_ID = 1;
    private static final long ASNU_ID = 1000;
    private static final String KPP = "123456789";
    private static final String OKTMO = "12345678";
    private static final String CODE_ORG = "1234";

    @Override
    protected DeclarationData getDeclarationData() {
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DeclarationTestScriptHelper.CURRENT_DECLARATION_DATA_ID);
        declarationData.setDepartmentId(DEPARTMENT_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);
        declarationData.setReportPeriodId(REPORT_PERIOD_ID);
        declarationData.setState(State.ACCEPTED);
        declarationData.setAsnuId(ASNU_ID);
        declarationData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        declarationData.setKpp(KPP);
        declarationData.setOktmo(OKTMO);
        declarationData.setTaxOrganCode(CODE_ORG);
        return declarationData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(RaschsvCheckTest.class);
    }

    @After
    public void resetMock() {
        reset(testHelper.getRefBookFactory());
    }

    @Before
    public void mockService() {
        //mock сервисов для получения тестовых наборов данных
    }

    /**
     * Инициализация перед каждым отдельным тестом
     */
    @Before
    public void init() {
        // Хэлпер хранится статично для оптимизации, чтобы он был один для всех тестов отдельного скрипта
        if (testHelper == null) {
            String path = getFolderPath();
            if (path == null) {
                throw new ServiceException("Test folder path is null!");
            }
            testHelper = new DeclarationTestScriptHelper(path, getDeclarationData(), getMockHelper()) {};
        }
        testHelper.reset();
    }

    /**
     * Параметры подразделения по сборам, взносам
     */
    private void initFondRefBook() {
        RefBookDataProvider ndflRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> ndflPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> ndflPagingResultItem = new HashMap<String, RefBookValue>();
        when(ndflRefBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(ndflPagingResult);
        ndflPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        ndflPagingResult.add(ndflPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_FOND_ID)).thenReturn(ndflRefBookDataProvider);
    }

    /**
     * Тестовые значение для "Параметры подразделения по сборам, взносам (таблица)"
     */
    private void initFondDetailRefBook() {
        RefBookDataProvider ndflRefBookDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> ndflPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> ndflPagingResultItem = new HashMap<String, RefBookValue>();
        when(ndflRefBookDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(ndflPagingResult);
        ndflPagingResultItem.put("OKVED", new RefBookValue(RefBookAttributeType.REFERENCE, 266988699L));
        ndflPagingResultItem.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "123456789"));
        ndflPagingResultItem.put("REORG_FORM_CODE", new RefBookValue(RefBookAttributeType.REFERENCE, 266990799L));
        ndflPagingResultItem.put("REORG_INN", new RefBookValue(RefBookAttributeType.STRING, "7723643863"));
        ndflPagingResultItem.put("REORG_KPP", new RefBookValue(RefBookAttributeType.STRING, "123456789"));
        ndflPagingResultItem.put("PRESENT_PLACE", new RefBookValue(RefBookAttributeType.REFERENCE, 266969499L));
        ndflPagingResultItem.put("OKTMO", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
        ndflPagingResult.add(ndflPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_FOND_DETAIL_ID)).thenReturn(ndflRefBookDataProvider);
    }

    /**
     * Тестовые значение для Справочников
     */
    private void initRefBook() {
        // Коды места представления расчета
        RefBookDataProvider refBookPresentPlaceDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> presentPlacePagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> presentPlacePagingResultItem = new HashMap<String, RefBookValue>();
        presentPlacePagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 266969499L));
        presentPlacePagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "350"));
        presentPlacePagingResultItem.put("FOR_FOND", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        presentPlacePagingResult.add(presentPlacePagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_PRESENT_PLACE_ID)).thenReturn(refBookPresentPlaceDataProvider);
        when(refBookPresentPlaceDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(presentPlacePagingResult);

        // ОКВЭД
        RefBookDataProvider refBookOkvedDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> okvedPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> okvedPagingResultItem = new HashMap<String, RefBookValue>();
        okvedPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 266988699L));
        okvedPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "74.5"));
        okvedPagingResult.add(okvedPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_OKVED_ID)).thenReturn(refBookOkvedDataProvider);
        when(refBookOkvedDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(okvedPagingResult);

        // Форма реорганизации
        RefBookDataProvider refBookReorgDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> reorgPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> reorgPagingResultItem = new HashMap<String, RefBookValue>();
        reorgPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 266990799L));
        reorgPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "2"));
        reorgPagingResult.add(reorgPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_REORGANIZATION_ID)).thenReturn(refBookReorgDataProvider);
        when(refBookReorgDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(reorgPagingResult);

        // Общие настройки
        RefBookDataProvider refBookConfigurationParamDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> сonfigurationParamPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> сonfigurationParamPagingResultItem = new HashMap<String, RefBookValue>();
        сonfigurationParamPagingResultItem.put("VALUE", new RefBookValue(RefBookAttributeType.STRING, "7707083893"));
        сonfigurationParamPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "SBERBANK_INN"));
        сonfigurationParamPagingResult.add(сonfigurationParamPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_CONFIGURATION_PARAM_ID)).thenReturn(refBookConfigurationParamDataProvider);
        when(refBookConfigurationParamDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(сonfigurationParamPagingResult);

        // ОКТМО
        RefBookDataProvider refBookOktmoDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> oktmoPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> oktmoPagingResultItem = new HashMap<String, RefBookValue>();
        oktmoPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 1L));
        oktmoPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "14000000"));
        oktmoPagingResult.add(oktmoPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_OKTMO_ID)).thenReturn(refBookOktmoDataProvider);
        when(refBookOktmoDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(oktmoPagingResult);

        // Тариф плательщика
        RefBookDataProvider refBookTariffPayeerDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> tariffPayeerPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> tariffPayeerPagingResultItem = new HashMap<String, RefBookValue>();
        tariffPayeerPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 268585199L));
        tariffPayeerPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "01"));
        tariffPayeerPagingResultItem.put("FOR_OPS_OMS", new RefBookValue(RefBookAttributeType.NUMBER, 1));
        tariffPayeerPagingResult.add(tariffPayeerPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_TARIFF_PAYER_ID)).thenReturn(refBookTariffPayeerDataProvider);
        when(refBookTariffPayeerDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(tariffPayeerPagingResult);

        // Основания заполнения сумм страховых взносов
        RefBookDataProvider refBookFillBaseDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> fillBasePagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> fillBasePagingResultItem = new HashMap<String, RefBookValue>();
        fillBasePagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 268584899L));
        fillBasePagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "2"));
        fillBasePagingResult.add(fillBasePagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_FILL_BASE_ID)).thenReturn(refBookFillBaseDataProvider);
        when(refBookFillBaseDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(fillBasePagingResult);

        // Классы условий труда
        RefBookDataProvider refBookHardWorkDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> hardWorkPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> hardWorkPagingResultItem = new HashMap<String, RefBookValue>();
        hardWorkPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 268587699L));
        hardWorkPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "1"));
        hardWorkPagingResult.add(hardWorkPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_HARD_WORK_ID)).thenReturn(refBookHardWorkDataProvider);
        when(refBookHardWorkDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(hardWorkPagingResult);

        // Категории застрахованных лиц
        RefBookDataProvider refBookPersonCategoryDataProvider = mock(RefBookDataProvider.class);
        PagingResult<Map<String, RefBookValue>> personCategoryPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> personCategoryPagingResultItem = new HashMap<String, RefBookValue>();
        personCategoryPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "ПНЭД"));
        personCategoryPagingResult.add(personCategoryPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(REF_BOOK_PERSON_CATEGORY_ID)).thenReturn(refBookPersonCategoryDataProvider);
        when(refBookPersonCategoryDataProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(personCategoryPagingResult);
    }

    /**
     * Тестирование проверок загруженных данных
     */
    @Test
    public void checkDataTest() {

        initRefBook();
        initFondRefBook();
        initFondDetailRefBook();

        when(testHelper.getDeclarationService().getXmlStream(any(Long.class))).thenAnswer(new Answer<ZipInputStream>() {
            @Override
            public ZipInputStream answer(InvocationOnMock invocation) throws Throwable {
                InputStream inputStream = RaschsvCheckTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_CHECK.xml");
                return new ZipInputStream(inputStream);
            }
        });
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.CHECK);
    }
}
