package com.aplana.sbrf.taxaccounting.form_template.fond.primary_1151111.v2016;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.util.ScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.TestScriptHelper;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тестирование разбора xml-файла Расчета по страховым взносам
 */
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
        formType.setId(TYPE_ID);
        formData.setId(TestScriptHelper.CURRENT_FORM_DATA_ID);
        formData.setFormType(formType);
        formData.setFormTemplateId(TYPE_ID);
        formData.setKind(KIND);
        formData.setState(WorkflowState.CREATED);
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
                @Override
                protected void initFormTemplate() {
                    //do nothing...
                }

                @Override
                public void initRowData() {
                    //do nothing...
                }
            };
        }
        testHelper.reset();
    }

    public void initMock() {
        Mockito.reset(testHelper.getRaschsvItogVyplService());

        RefBookDataProvider okvedDataRovider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.OKVED.getId()))).thenReturn(okvedDataRovider);
        when(okvedDataRovider.getRecordsCount(any(Date.class), eq("CODE = '1234'"))).thenReturn(1);
        when(okvedDataRovider.getRecordsCount(any(Date.class), eq("CODE = '0000'"))).thenReturn(0);

        when(testHelper.getFiasRefBookService().findAddress(anyString(), anyString(),anyString(), anyString(), anyString())).thenReturn(new ArrayList<AddressObject>());
        when(testHelper.getFiasRefBookService().findAddress(eq("590"), eq("590"), eq("590"), eq("590"), eq("590"))).thenReturn(Arrays.asList(new AddressObject()));

        RefBookDataProvider oksmDataRovider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.COUNTRY.getId()))).thenReturn(oksmDataRovider);
        when(oksmDataRovider.getRecordsCount(any(Date.class), eq("CODE = '643'"))).thenReturn(1);
        when(oksmDataRovider.getRecordsCount(any(Date.class), eq("CODE = '644'"))).thenReturn(0);

        RefBookDataProvider documentCodesProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DOCUMENT_CODES.getId()))).thenReturn(documentCodesProvider);
        when(documentCodesProvider.getRecordsCount(any(Date.class), eq("CODE = '21'"))).thenReturn(1);
        when(documentCodesProvider.getRecordsCount(any(Date.class), eq("CODE = '20'"))).thenReturn(0);

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
    }

    @Test
    public void checkRaschsvValid() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        checkLoggerErrorOrWarn();
    }

    @Test
    public void checkRaschsvInvalid() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Invalid.xml");

        Map<String, Object> params = new HashedMap<String, Object>();

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        params.put("declarationData", declarationData);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.setImportFileName(FILE_NAME);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        Assert.assertTrue(containLog("код НО в имени не совпадает с кодом НО внутри файла"));
        Assert.assertTrue(containLog("ИНН в имени не совпадает с ИНН внутри файла"));
        Assert.assertTrue(containLog("КПП в имени не совпадает с КПП внутри файла"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.ОКВЭД = \"0000\" не найден в справочнике ОКВЭД"));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПЮП.ИННЮЛ = \"7723643860\" для организации - плательщика страховых взносов в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПЮП.КПП = \"1234567890\" для организации - плательщика страховых взносов с ИНН \"7723643860\""));
        Assert.assertTrue(containLog("Не заполнен Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        Assert.assertTrue(containLog("Не заполнен Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ = \"\" реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП = \"1234567891\" реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПИП.ИННФЛ = \"500100732250\" индивидуального предпринимателя - плательщика страховых взносов в транспортном файле \""+ FILE_NAME + "\""));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПФЛ.ИННФЛ = \"500100732250\" физического лица - плательщика страховых взносов в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("В справочнике отсутствует Файл.Документ.СвНП.НПФЛ.СвНПФЛ.АдрМЖРФ = \"'590'/'591'/'592'/'593'/'594'\" для ФЛ с ИНН \"500100732250\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.Подписант.ФИО=\"''/'z'\" в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.Подписант.СвПред.НаимДок = \"\" в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПИП.СвНПФЛ.Гражд = \"644\" ФЛ с ИНН \"500100732250\" не найден в справочнике ОКСМ"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПФЛ.СвНПФЛ.УдЛичнФЛ.КодВидДок = \"20\" ФЛ с ИНН 500100732250 не найден в справочнике \"Коды документов, удостоверяющих личность\""));
        Assert.assertTrue(containLog("Не заполнены Файл.Документ.РасчетСВ.ОбязПлатСВ в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.OKTMO = \"57701001\" транспортного файла \"" + FILE_NAME + "\" не найден в справочнике"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС = \"10302330010000111\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС = \"10302330010000112\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОМС = \"10302330010000113\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПСДоп = \"10302330010000114\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерДСО = \"10302330010000115\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС = \"10302330010000116\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУплПер в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл1М в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл2М в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.УплПерОСС.СумСВУпл3М в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВПер в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ1М в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ2М в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС.ПревРасхОСС.ПревРасхСВ3М в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.ИННФЛ = \"500100732250\" иностранного гражданина, необходимый для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.СНИЛС = \"112-233-445 90\" иностранного гражданина с ИНН \"500100732250\" не найден в справочнике ОКСМ"));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.РасчетСВ.ОбязПлатСВ.СВПримТариф2.2.425.СвИноГражд.Гражд = \"640\" иностранного гражданина, необходимый для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.ИННФЛ = \"500100732250\" для получателя доходов в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.СНИЛС = \"112-233-445 90\" для получателя доходов"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.КодВидДок = \"20\" получателя доходов с СНИЛС \"112-233-445 90\" не найден в справочнике \"Коды документов, удостоверяющих личность\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц.ДанФЛПолуч.Гражд = \"640\" получателя доходов с СНИЛС  \"112-233-445 90\" не найден в справочнике ОКСМ"));
        Assert.assertTrue(containLog("Период расчетных сведений Файл.Документ.РасчетСВ.ПерсСвСтрахЛиц = \"'21'/'2016'\" в транспортном файле \"" + FILE_NAME + "\" не входит в отчетный период формы"));
    }

    /**
     * Тестирование разбора xml
     * @throws IOException
     */
    @Test
    public void parseTest() throws IOException {
        initMock();
        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_PARSE.xml");

        Map<String, Object> params = new HashedMap<String, Object>();
        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);
        params.put("declarationData", declarationData);

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

    @Test
    public void checkRaschsvItog() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);

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

    @Test
    public void checkRaschsvItogDop() {
        initMock();

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/NO_RASCHSV_Valid.xml");

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);

        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("declarationData", declarationData);

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
}
