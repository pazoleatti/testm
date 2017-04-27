package com.aplana.sbrf.taxaccounting.form_template.fond.primary_1151111.v2016;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Тестирование разбора xml-файла Расчета по страховым взносам
 */
//TODO 06.03.2017 отклчил на время

public class RaschsvParseTest extends ScriptTestBase {

    /**
     * Тип формы НДФЛ
     */
    private static final int TYPE_ID = 814;
    /**
     * Подразделение
     */
    private static final int DEPARTMENT_ID = 1;

    /**
     * Идентификатор отчетного периода
     */
    private static final int REPORT_PERIOD_ID = 1;

    private static final int DEPARTMENT_PERIOD_ID = 1;

    /**
     * Тип формы первичная
     */
    private static final FormDataKind KIND = FormDataKind.PRIMARY;

    private static final Long DECLARATION_DATA_ID = 1L;
    private static final String FILE_NAME = "NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml";

    @Override
    protected FormData getFormData() {
        FormData formData = new FormData();
        FormType formType = new FormType();
        formData.setId(TestScriptHelper.CURRENT_FORM_DATA_ID);
        formData.setFormType(formType);
        formData.setFormTemplateId(TYPE_ID);
        formData.setKind(KIND);
        formData.setDepartmentId(DEPARTMENT_ID);
        formData.setDepartmentReportPeriodId(DEPARTMENT_PERIOD_ID);
        formData.setReportPeriodId(REPORT_PERIOD_ID);
        formData.setPeriodOrder(1);
        return formData;
    }

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(RaschsvParseTest.class);
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
            testHelper = new TestScriptHelper(path, getFormData(), getMockHelper()) {
            };
        }
        testHelper.reset();
    }

    public void initMock() {
        initReferenceMock();
        initIdentificatePersonMock();
    }

    public void initReferenceMock() {
        Mockito.reset(testHelper.getRaschsvItogVyplService());

        RefBookDataProvider okvedDataRovider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.OKVED.getId()))).thenReturn(okvedDataRovider);
        when(okvedDataRovider.getRecordsCount(any(Date.class), eq("CODE = '1234'"))).thenReturn(1);
        when(okvedDataRovider.getRecordsCount(any(Date.class), eq("CODE = '0000'"))).thenReturn(0);

        //when(testHelper.getFiasRefBookService().findAddress(anyString(), anyString(),anyString(), anyString(), anyString())).thenReturn(new ArrayList<AddressObject>());
        //when(testHelper.getFiasRefBookService().findAddress(eq("590"), eq("590"), eq("590"), eq("590"), eq("590"))).thenReturn(Arrays.asList(new AddressObject()));

        RefBookDataProvider oksmDataRovider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.COUNTRY.getId()))).thenReturn(oksmDataRovider);
        when(oksmDataRovider.getRecordsCount(any(Date.class), eq("CODE = '643'"))).thenReturn(1);
        when(oksmDataRovider.getRecordsCount(any(Date.class), eq("CODE = '644'"))).thenReturn(0);
        PagingResult<Map<String, RefBookValue>> countryPagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> countryPagingResultItem = new HashMap<String, RefBookValue>();
        countryPagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 262254399L));
        countryPagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "643"));
        countryPagingResult.add(countryPagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(RefBook.Id.COUNTRY.getId())).thenReturn(oksmDataRovider);
        when(oksmDataRovider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(countryPagingResult);

        RefBookDataProvider documentCodesProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DOCUMENT_CODES.getId()))).thenReturn(documentCodesProvider);
        when(documentCodesProvider.getRecordsCount(any(Date.class), eq("CODE = '21'"))).thenReturn(1);
        when(documentCodesProvider.getRecordsCount(any(Date.class), eq("CODE = '20'"))).thenReturn(0);
        PagingResult<Map<String, RefBookValue>> documentTypePagingResult = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> documentTypePagingResultItem = new HashMap<String, RefBookValue>();
        documentTypePagingResultItem.put("id", new RefBookValue(RefBookAttributeType.NUMBER, 266135799L));
        documentTypePagingResultItem.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "21"));
        documentTypePagingResult.add(documentTypePagingResultItem);
        when(testHelper.getRefBookFactory().getDataProvider(RefBook.Id.DOCUMENT_CODES.getId())).thenReturn(documentCodesProvider);
        when(documentCodesProvider.getRecords(any(Date.class), any(PagingParams.class), anyString(), any(RefBookAttribute.class))).thenReturn(documentTypePagingResult);

        RefBookDataProvider oktmoProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.OKTMO.getId()))).thenReturn(oktmoProvider);
        when(oktmoProvider.getRecordsCount(any(Date.class), eq("CODE = '57701000'"))).thenReturn(1);
        when(oktmoProvider.getRecordsCount(any(Date.class), eq("CODE = '57701001'"))).thenReturn(0);

        RefBookDataProvider kbkProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.KBK.getId()))).thenReturn(kbkProvider);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000110'"))).thenReturn(1);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000111'"))).thenReturn(0);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000112'"))).thenReturn(0);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000113'"))).thenReturn(0);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000114'"))).thenReturn(0);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000115'"))).thenReturn(0);
        when(kbkProvider.getRecordsCount(any(Date.class), eq("CODE = '10302330010000116'"))).thenReturn(0);

        DeclarationService declarationService = mock(DeclarationService.class);
        Mockito.doThrow(new IllegalArgumentException()).when(declarationService).validateDeclaration(any(DeclarationData.class), any(TAUserInfo.class), any(Logger.class), any(File.class), any(String.class));
    }

    public void initIdentificatePersonMock() {
        final int raschsvPersSvStrahLicSize = 5;

        final Map<Long, RaschsvPersSvStrahLic> raschsvPersSvStrahLicMap = mockFindRaschsvPersSvStrahLic(raschsvPersSvStrahLicSize);

        when(testHelper.getRefBookPersonService().identificatePerson(any(PersonData.class), anyList(), anyInt(), any(Logger.class))).thenReturn(null).thenReturn(null).thenReturn(null);
                //thenReturn(1L).thenReturn(2L).thenReturn(3L);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return null;
            }
        }).when(testHelper.getRefBookDataProvider()).updateRecordVersionWithoutLock(any(Logger.class), anyLong(), any(Date.class), any(Date.class), anyMap());

        when(testHelper.getRefBookDataProvider().createRecordVersionWithoutLock(any(Logger.class), any(Date.class), any(Date.class), anyList())).thenAnswer(new Answer<List<Long>>() {
            @Override
            public List<Long> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return new ArrayList<Long>(raschsvPersSvStrahLicMap.keySet());
            }
        });

        when(testHelper.getRefBookDataProvider().getRecordData(anyList())).thenReturn(createRefBook());
    }

    private Map<Long, RaschsvPersSvStrahLic> mockFindRaschsvPersSvStrahLic(int size) {
        Map<Long, RaschsvPersSvStrahLic> result = new HashMap<Long, RaschsvPersSvStrahLic>();
        for (int i = 0; i < size; i++) {
            result.put(Long.valueOf(i), createGoodRaschsvPersSvStrahLic(Long.valueOf(i)));
        }
        return result;
    }

    private Map<Long, Map<String, RefBookValue>> createRefBook() {
        Map<Long, Map<String, RefBookValue>> map = new HashMap<Long, Map<String, RefBookValue>>();
        for (int i = 0; i < 5; i++) {
            map.put(Long.valueOf(i), createRefBookMock(i));
        }
        return map;
    }

    private Map<String, RefBookValue> createRefBookMock(long id) {
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, id));
        result.put("CODE", new RefBookValue(RefBookAttributeType.STRING, "foo"));
        result.put("ADDRESS", new RefBookValue(RefBookAttributeType.REFERENCE, Long.valueOf(new Random().nextInt(1000))));
        return result;
    }

    private RaschsvPersSvStrahLic createGoodRaschsvPersSvStrahLic(Long id) {
        RaschsvPersSvStrahLic person = new RaschsvPersSvStrahLic();
        person.setId(id);
        person.setDeclarationDataId(1L);
        person.setInnfl("000-000-000-00");
        person.setSnils("123-321-111-11");
        person.setFamilia("Иванов");
        person.setImya("Иван");
        person.setOtchestvo("Иванович");
        person.setDataRozd(new Date());
        person.setGrazd("643");

        person.setKodVidDoc("11");
        person.setSerNomDoc("2002 123456");

        return person;
    }

    @Ignore
    @Test
    public void checkRaschsvValid() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);
        File dataFile = null;
        params.put("dataFile", dataFile);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        checkLoggerErrorOrWarn();
    }

    @Ignore
    @Test
    public void checkRaschsvInvalid() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Invalid.xml");

        Map<String, Object> params = new HashedMap<String, Object>();

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        params.put("declarationData", declarationData);
        File dataFile = null;
        params.put("dataFile", dataFile);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        Assert.assertTrue(containLog("код НО в имени не совпадает с кодом НО внутри файла"));
        Assert.assertTrue(containLog("ИНН в имени не совпадает с ИНН внутри файла"));
        Assert.assertTrue(containLog("КПП в имени не совпадает с КПП внутри файла"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПЮП.ИННЮЛ = \"7723643860\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как форма реорганизации = \"2\""));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как форма реорганизации = \"2\""));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ = \"\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.ОКВЭД = \"0000\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике ОКВЭД"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПИП.ИННФЛ = \"500100732250\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПФЛ.СвНПФЛ.АдрМЖРФ = \"'590'/'591'/'592'/'593'/'594'\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике"));
        Assert.assertTrue(containLog("Файл.Документ.Подписант.ФИО = \"''/'z'\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, если признак лица, подписавшего документ = 2 или 1 и плательщиком является ЮЛ"));
        Assert.assertTrue(containLog("Файл.Документ.Подписант.СвПред.НаимДок = \"\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как признак лица, подписавшего документ = 2"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПИП.СвНПФЛ.Гражд = \"644\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике ОКСМ"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПФЛ.СвНПФЛ.УдЛичнФЛ.КодВидДок = \"20\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Коды документов, удостоверяющих личность\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как код места = \"350\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.OKTMO = \"57701001\" транспортного файла \"" + FILE_NAME + "\" не найден в справочнике"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как код места = \"350\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.OKTMO = \"57701001\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Общероссийский классификатор территорий муниципальных образований (ОКТМО)\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.КБК = \"10302330010000111\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС.КБК = \"10302330010000112\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОМС.КБК = \"10302330010000113\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПСДоп.КБК = \"10302330010000114\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерДСО.КБК = \"10302330010000115\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.КБК = \"10302330010000116\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не заполнен, элемент является обязательным, так как не заполнен Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.ИННФЛ = \"500100732250\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.СНИЛС = \"112-233-445 90\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.Гражд = \"640\" иностранного гражданина \"Surname Name\" не найден в справочнике ОКСМ"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц = \"'21'/'2016'\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не входит в отчетный период формы"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ = \"500100732250\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС = \"112-233-445 90\" в транспортном файле \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" некорректный"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок = \"20\" получателя доходов с СНИЛС \"112-233-445 90\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике \"Коды документов, удостоверяющих личность\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд = \"640\" ФЛ с СНИЛС \"112-233-445 90\" транспортного файла \"NO_RASCHSV_123_1234_7723643863123456789_20170802_guid1.xml\" не найден в справочнике ОКСМ"));
    }

    /**
     * Тестирование разбора xml
     * @throws IOException
     */
    @Ignore
    @Test
    public void importDataTest() throws IOException {
        initMock();
        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_PARSE.xml");

        Map<String, Object> params = new HashedMap<String, Object>();
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        declarationData.setDepartmentReportPeriodId(1010);
        params.put("declarationData", declarationData);

        File dataFile = null;
        params.put("dataFile", dataFile);

        // Проверка соответствия числа узлов по их имени
        Map<String, Integer> countNodes = new HashedMap<String, Integer>();
        countNodes.put("ПерсСвСтрахЛиц", 10);
        countNodes.put("УплПерОПС", 1);
        countNodes.put("УплПерОМС", 1);
        countNodes.put("УплПерОПСДоп", 10);
        countNodes.put("УплПерДСО", 10);
        countNodes.put("УплПерОСС", 1);
        countNodes.put("ПревРасхОСС", 1);
        countNodes.put("РасчСВ_ОПС_ОМС", 10);
        countNodes.put("РасчСВ_ОСС.ВНМ", 1);
        countNodes.put("РасхОССЗак", 1);
        countNodes.put("ВыплФинФБ", 1);

        countNodes.put("ПравТариф3.1.427", 1);
        countNodes.put("ПравТариф5.1.427", 1);
        countNodes.put("ПравТариф7.1.427", 1);
        countNodes.put("СвПримТариф9.1.427", 1);
        countNodes.put("СвПримТариф2.2.425", 1);
        countNodes.put("СвПримТариф1.3.422", 1);

        countNodes.put("СведПатент", 10);
        countNodes.put("СвИноГражд", 10);
        countNodes.put("СведОбуч", 10);
        params.put("countNodes", countNodes);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        checkLogger();
    }

    private static RaschsvItogVypl findByMonthAndCode(Collection<RaschsvItogVypl> list, String month, String code) {
        for (RaschsvItogVypl raschsvItogVypl : list) {
            if (month.equals(raschsvItogVypl.getMesyac()) && code.equals(raschsvItogVypl.getKodKatLic())) {
                return raschsvItogVypl;
            }
        }

        return null;
    }

    private static RaschsvItogVyplDop findByMonthAndTarif(Collection<RaschsvItogVyplDop> list, String month, String tarif) {
        for (RaschsvItogVyplDop raschsvItogVyplDop : list) {
            if (month.equals(raschsvItogVyplDop.getMesyac()) && tarif.equals(raschsvItogVyplDop.getTarif())) {
                return raschsvItogVyplDop;
            }
        }

        return null;
    }

    @Ignore
    @Test
    public void checkRaschsvItog() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);
        File dataFile = null;
        params.put("dataFile", dataFile);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        ArgumentCaptor<RaschsvItogStrahLic> argumentLic = ArgumentCaptor.forClass(RaschsvItogStrahLic.class);
        ArgumentCaptor<Collection> argumentVupl = ArgumentCaptor.forClass(Collection.class);

        verify(testHelper.getRaschsvItogVyplService(), Mockito.times(1)).insertItogStrahLic(argumentLic.capture());
        verify(testHelper.getRaschsvItogVyplService(), Mockito.times(1)).insertItogVypl(argumentVupl.capture());

        RaschsvItogStrahLic raschsvItogStrahLic = argumentLic.getValue();
        Assert.assertEquals(DECLARATION_DATA_ID, raschsvItogStrahLic.getDeclarationDataId());

        Collection<RaschsvItogVypl> vyplCollection = argumentVupl.getValue();
        Assert.assertEquals(3, vyplCollection.size());

        RaschsvItogVypl xo = findByMonthAndCode(vyplCollection, "01", "ХО");
        Assert.assertNotNull(xo);
        Assert.assertEquals(new BigDecimal(100), xo.getSumVypl());
        Assert.assertEquals(new BigDecimal(0), xo.getVyplOps());
        Assert.assertEquals(new BigDecimal(0), xo.getVyplOpsDog());
        Assert.assertEquals(new BigDecimal(0), xo.getSumNachisl());

        RaschsvItogVypl asb01 = findByMonthAndCode(vyplCollection, "01", "АСБ");
        Assert.assertNotNull(asb01);
        Assert.assertEquals(new BigDecimal(100), asb01.getSumVypl());
        Assert.assertEquals(new BigDecimal(1000), asb01.getVyplOps());
        Assert.assertEquals(new BigDecimal(10000), asb01.getVyplOpsDog());
        Assert.assertEquals(new BigDecimal(100000), asb01.getSumNachisl());

        RaschsvItogVypl asb02 = findByMonthAndCode(vyplCollection, "02", "АСБ");
        Assert.assertNotNull(asb02);
        Assert.assertEquals(new BigDecimal(200), asb02.getSumVypl());
        Assert.assertEquals(new BigDecimal(2000), asb02.getVyplOps());
        Assert.assertEquals(new BigDecimal(20000), asb02.getVyplOpsDog());
        Assert.assertEquals(new BigDecimal(200000), asb02.getSumNachisl());

        checkLoggerErrorOrWarn();
    }

    @Ignore
    @Test
    public void checkRaschsvItogDop() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);
        File dataFile = null;
        params.put("dataFile", dataFile);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        ArgumentCaptor<RaschsvItogStrahLic> argumentLic = ArgumentCaptor.forClass(RaschsvItogStrahLic.class);
        ArgumentCaptor<Collection> argumentVupl = ArgumentCaptor.forClass(Collection.class);

        verify(testHelper.getRaschsvItogVyplService(), Mockito.times(1)).insertItogStrahLic(argumentLic.capture());
        verify(testHelper.getRaschsvItogVyplService(), Mockito.times(1)).insertItogVyplDop(argumentVupl.capture());

        RaschsvItogStrahLic raschsvItogStrahLic = argumentLic.getValue();
        Assert.assertEquals(DECLARATION_DATA_ID, raschsvItogStrahLic.getDeclarationDataId());

        Collection<RaschsvItogVyplDop> vyplCollection = argumentVupl.getValue();
        Assert.assertEquals(3, vyplCollection.size());

        RaschsvItogVyplDop xo = findByMonthAndTarif(vyplCollection, "01", "01");
        Assert.assertNotNull(xo);
        Assert.assertEquals(new BigDecimal(100), xo.getSumVypl());
        Assert.assertEquals(new BigDecimal(0), xo.getSumNachisl());

        RaschsvItogVyplDop asb01 = findByMonthAndTarif(vyplCollection, "01", "02");
        Assert.assertNotNull(asb01);
        Assert.assertEquals(new BigDecimal(100), asb01.getSumVypl());
        Assert.assertEquals(new BigDecimal(1000), asb01.getSumNachisl());

        RaschsvItogVyplDop asb02 = findByMonthAndTarif(vyplCollection, "02", "01");
        Assert.assertNotNull(asb02);
        Assert.assertEquals(new BigDecimal(200), asb02.getSumVypl());
        Assert.assertEquals(new BigDecimal(2000), asb02.getSumNachisl());

        checkLoggerErrorOrWarn();
    }

    @Ignore
    @Test
    public void deleteEventTest() {
        initMock();

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(-1L);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);

        testHelper.execute(FormDataEvent.DELETE, params);

        verify(testHelper.getRaschsvObyazPlatSvService(), Mockito.times(1)).deleteFromLinkedTable(-1L);
    }
}
