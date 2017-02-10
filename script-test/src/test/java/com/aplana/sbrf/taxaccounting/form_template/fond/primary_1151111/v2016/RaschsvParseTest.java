package com.aplana.sbrf.taxaccounting.form_template.fond.primary_1151111.v2016;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
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

    /**
     * Тестирование числа узлов, которые были перебраны
     * @throws IOException
     */
//    @Test
    public void create() throws IOException {

        InputStream inputStream = RaschsvParseTest.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/form_template/fond/primary_1151111/v2016/schema.xml");

        Map<String, Object> params = new HashedMap<String, Object>();

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

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(1L);

        params.put("declarationData", declarationData);
        params.put("countNodes", countNodes);

        testHelper.setImportFileInputStream(inputStream);
        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE, params);

        checkLogger();
    }

    public void initMock() {
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

        checkLogger();
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
        //TODO Файл.Документ.СвНП.НПЮП.КПП
        Assert.assertTrue(containLog("Не заполнен Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        Assert.assertTrue(containLog("Не заполнен Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.ИННЮЛ = \"\" реорганизованной организации для организации плательщика страховых взносов с ИНН 7723643860"));
        //TODO Файл.Документ.СвНП.НПЮЛ.СвРеоргЮР.КПП
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПИП.ИННФЛ = \"500100732250\" индивидуального предпринимателя - плательщика страховых взносов в транспортном файле \""+ FILE_NAME + "\""));
        Assert.assertTrue(containLog("Некорректный Файл.Документ.СвНП.НПФЛ.ИННФЛ = \"500100732250\" физического лица - плательщика страховых взносов в транспортном файле \"" + FILE_NAME + "\""));
        Assert.assertTrue(containLog("В справочнике отсутствует Файл.Документ.СвНП.НПФЛ.СвНПФЛ.АдрМЖРФ = \"'590'/'591'/'592'/'593'/'594'\" для ФЛ с ИНН \"7723643860\""));
        Assert.assertTrue(containLog("В справочнике отсутствует Файл.Документ.СвНП.НПФЛ.СвНПФЛ.АдрМЖРФ = \"'590'/'591'/'592'/'593'/'594'\" для ФЛ с ИНН \"7723643860\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.Подписант.ФИО=\"''/'z'\" в транспортном файле \""+ FILE_NAME + "\""));
        Assert.assertTrue(containLog("Не заполнено Файл.Документ.Подписант.СвПред.НаимДок = \"\" в транспортном файле \""+ FILE_NAME + "\""));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПИП.СвНПФЛ.Гражд = \"644\" ФЛ с ИНН \"500100732250\" не найден в справочнике ОКСМ"));
        Assert.assertTrue(containLog("Файл.Документ.СвНП.НПФЛ.СвНПФЛ.УдЛичнФЛ.КодВидДок = \"20\" ФЛ с ИНН 500100732250 не найден в справочнике \"Коды документов, удостоверяющих личность\""));
        Assert.assertTrue(containLog("Не заполнены Файл.Документ.РасчетСВ.ОбязПлатСВ в транспортном файле \""+ FILE_NAME + "\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.OKTMO = \"57701001\" транспортного файла \""+ FILE_NAME + "\" не найден в справочнике"));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС = \"10302330010000111\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПС = \"10302330010000112\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОМС = \"10302330010000113\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерОПСДоп = \"10302330010000114\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПерДСО = \"10302330010000115\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
        Assert.assertTrue(containLog("Файл.Документ.РасчетСВ.ОбязПлатСВ.УплПревОСС = \"10302330010000116\" не найден в справочнике \"Классификатор кодов классификации доходов бюджетов Российской Федерации\""));
    }
}
