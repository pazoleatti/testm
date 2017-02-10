package com.aplana.sbrf.taxaccounting.form_template.fond.primary_1151111.v2016;

import com.aplana.sbrf.taxaccounting.form_template.ndfl.report_6ndfl.v2016.NdflReport6ScriptTest;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
public class Primary1151111ScriptTest extends DeclarationScriptTestBase {

    private RefBookFactory refBookFactory;

    private static final long REF_BOOK_FOND_ID = RefBook.Id.FOND.getId();
    private static final long REF_BOOK_FOND_DETAIL_ID = RefBook.Id.FOND_DETAIL.getId();
    private static final long REF_BOOK_OKVED_ID = RefBook.Id.OKVED.getId();
    private static final long REF_BOOK_REORGANIZATION_ID = RefBook.Id.REORGANIZATION.getId();
    private static final long REF_BOOK_PRESENT_PLACE_ID = RefBook.Id.PRESENT_PLACE.getId();
    private static final long REF_BOOK_CONFIGURATION_PARAM_ID = RefBook.Id.CONFIGURATION_PARAM.getId();

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
        return getDefaultScriptTestMockHelper(Primary1151111ScriptTest.class);
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
    }

    /**
     * Тестирование проверок загруженных данных
     */
    @Test
    public void checkTest() {

        initRefBook();
        initFondRefBook();
        initFondDetailRefBook();

        when(testHelper.getDeclarationService().getXmlStream(any(Long.class))).thenAnswer(new Answer<ZipInputStream>() {
            @Override
            public ZipInputStream answer(InvocationOnMock invocation) throws Throwable {
                InputStream inputStream = Primary1151111ScriptTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");
//                return new ZipInputStream(inputStream);
//                BufferedReader bufferedReader = new  BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//                String content = bufferedReader.readLine();
//                while(content != null) {
//                    System.out.println(content);
//                    content = bufferedReader.readLine();
//                }
                return new ZipInputStream(inputStream);
            }
        });

        testHelper.execute(FormDataEvent.CHECK);
    }

    /**
     * Тестирование разбора xml
     * @throws IOException
     */
//    @Test
    public void parseTest() throws IOException {

        InputStream inputStream = Primary1151111ScriptTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/schema.xml");

//        Map<String, Object> params = new HashedMap<String, Object>();
//
//        // Проверка соответствия числа узлов по их имени
//        Map<String, Integer> countNodes = new HashedMap<String, Integer>();
//        countNodes.put("ПерсСвСтрахЛиц", 10);
//        countNodes.put("УплПерОПС", 1);
//        countNodes.put("УплПерОМС", 1);
//        countNodes.put("УплПерОПСДоп", 10);
//        countNodes.put("УплПерДСО", 10);
//        countNodes.put("УплПерОСС", 1);
//        countNodes.put("ПревРасхОСС", 1);
//        countNodes.put("РасчСВ_ОПС_ОМС", 10);
//        countNodes.put("РасчСВ_ОСС.ВНМ", 1);
//        countNodes.put("РасхОССЗак", 1);
//        countNodes.put("ВыплФинФБ", 1);
//
//        countNodes.put("ПравТариф3.1.427", 1);
//        countNodes.put("ПравТариф5.1.427", 1);
//        countNodes.put("ПравТариф7.1.427", 1);
//        countNodes.put("СвПримТариф9.1.427", 1);
//        countNodes.put("СвПримТариф2.2.425", 1);
//        countNodes.put("СвПримТариф1.3.422", 1);
//
//        countNodes.put("СведПатент", 10);
//        countNodes.put("СвИноГражд", 10);
//        countNodes.put("СведОбуч", 10);
//
//        DeclarationData declarationData = new DeclarationData();
//        declarationData.setId(1L);
//
//        params.put("declarationData", getDeclarationData());
//        params.put("countNodes", countNodes);

        testHelper.setImportFileInputStream(inputStream);
//        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        checkLogger();
    }
}
