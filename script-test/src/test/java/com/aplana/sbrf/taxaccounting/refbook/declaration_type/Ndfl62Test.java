package com.aplana.sbrf.taxaccounting.refbook.declaration_type;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.util.RefBookScriptTestBase;
import com.aplana.sbrf.taxaccounting.util.mock.ScriptTestMockHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тест для загрузки НДФЛ 6 и НДФЛ 2
 */
public class Ndfl62Test extends RefBookScriptTestBase {

    @Override
    protected ScriptTestMockHelper getMockHelper() {
        return getDefaultScriptTestMockHelper(Ndfl62Test.class);
    }

    private final static Long DECLARATION_DATA_ID = 100500L;
    private final static int DECLARATION_TEMPLATE_ID = 100501;
    private final static Long DECLARATION_FORM_TYPE_ID = 100502L;
    private final static String NDFL2_1 = "2 НДФЛ (1)";
    private final static Long NOT_CORRECT_1_ID = 100503L;
    private final static Long NOT_CORRECT_2_ID = 100504L;
    private final static Long ATTACH_FILE_TYPE_ID = 100505L;
    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private final static Long DOC_STATE_REQUIRED_ID = 100506L;
    private final static Long DOC_STATE_SUCCESS_ID = 100507L;

    @Before
    public void initMock() {
        Mockito.reset(
                testHelper.getDeclarationService(),
                testHelper.getRefBookFactory()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNdfl2Prot() throws ParseException {
        String fileName = "PROT_NO_NDFL2_9979_9979_7707083893775001001_20160406_2FCC177D-2C02-59A5-E054-00144F6713DE.txt";
        InputStream inputStream = Ndfl62Test.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/refbook/declaration_type/" + fileName);

        testHelper.setImportInputStream(inputStream);
        testHelper.setUploadFileName(fileName);
        testHelper.setDataFile(new File(fileName));

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(DECLARATION_TEMPLATE_ID);
        declarationTemplate.setDeclarationFormTypeId(DECLARATION_FORM_TYPE_ID);

        when(testHelper.getDeclarationService().findDeclarationDataByFileNameAndFileType(eq("NO_NDFL2_9979_9979_7707083893775001001_20160406_2FCC177D-2C02-59A5-E054-00144F6713DE.XML"), anyLong()))
                .thenReturn(Arrays.asList(declarationData));
        when(testHelper.getDeclarationService().findDeclarationDataByFileNameAndFileType(eq(fileName), anyLong()))
                .thenReturn(new ArrayList<DeclarationData>());
        when(testHelper.getDeclarationService().getDeclarationIds(any(DeclarationDataFilter.class), any(DeclarationDataSearchOrdering.class), anyBoolean()))
                .thenReturn(new ArrayList<Long>());
        when(testHelper.getDeclarationService().getTemplate(eq(DECLARATION_TEMPLATE_ID)))
                .thenReturn(declarationTemplate);

        RefBookDataProvider attachFileType = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.ATTACH_FILE_TYPE.getId()))).thenReturn(attachFileType);
        when(attachFileType.getUniqueRecordIds(any(Date.class), eq("CODE = 2"))).thenReturn(Arrays.asList(ATTACH_FILE_TYPE_ID));

        FormType formType = new FormType();
        formType.setId(DECLARATION_FORM_TYPE_ID.intValue());
        formType.setCode(NDFL2_1);
        when(testHelper.getFormTypeService().get(DECLARATION_FORM_TYPE_ID.intValue())).thenReturn(formType);

        RefBookDataProvider declarationDataTypeRefProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DECLARATION_DATA_TYPE_REF_BOOK.getId()))).thenReturn(declarationDataTypeRefProvider);
        RefBookValue codeTypeRefBookValue = new RefBookValue(RefBookAttributeType.STRING, "2 НДФЛ (2)");
        Map<String, RefBookValue> resultTypeRef = new HashMap<String, RefBookValue>();
        resultTypeRef.put("CODE", codeTypeRefBookValue);
        when(declarationDataTypeRefProvider.getRecordData(DECLARATION_FORM_TYPE_ID)).thenReturn(resultTypeRef);

        RefBookDataProvider ndflRefProvider = mock(RefBookDataProvider.class);

        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0076322"))).thenReturn(Arrays.asList(0L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0099710"))).thenReturn(Arrays.asList(1L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0102256"))).thenReturn(Arrays.asList(2L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0108799"))).thenReturn(Arrays.asList(3L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0124793"))).thenReturn(Arrays.asList(4L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0136806"))).thenReturn(Arrays.asList(5L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0137344"))).thenReturn(Arrays.asList(6L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0215860"))).thenReturn(Arrays.asList(7L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0229666"))).thenReturn(Arrays.asList(8L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0237452"))).thenReturn(Arrays.asList(9L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0261125"))).thenReturn(Arrays.asList(10L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0272432"))).thenReturn(Arrays.asList(11L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0143796"))).thenReturn(Arrays.asList(12L));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0269856"))).thenReturn(Arrays.asList(13L));

        for (long i = 0; i < 14; i++) {
            Map<String, RefBookValue> correct = new HashMap<String, RefBookValue>();
            correct.put("ERRTEXT", new RefBookValue(RefBookAttributeType.STRING, null));
            when(ndflRefProvider.getRecordData(eq(i))).thenReturn(correct);
        }

        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.NDFL_REFERENCES.getId()))).thenReturn(ndflRefProvider);
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0913378"))).thenReturn(Arrays.asList(NOT_CORRECT_1_ID));
        when(ndflRefProvider.getUniqueRecordIds(any(Date.class), eq("DECLARATION_DATA_ID = " + DECLARATION_DATA_ID + " AND NUM = 0913386"))).thenReturn(Arrays.asList(NOT_CORRECT_2_ID));

        Map<String, RefBookValue> notCorrect_1 = new HashMap<String, RefBookValue>();
        notCorrect_1.put("ERRTEXT", new RefBookValue(RefBookAttributeType.STRING, null));
        when(ndflRefProvider.getRecordData(eq(NOT_CORRECT_1_ID))).thenReturn(notCorrect_1);

        Map<String, RefBookValue> notCorrect_2 = new HashMap<String, RefBookValue>();
        notCorrect_2.put("ERRTEXT", new RefBookValue(RefBookAttributeType.STRING, null));
        when(ndflRefProvider.getRecordData(eq(NOT_CORRECT_2_ID))).thenReturn(notCorrect_2);

        when(testHelper.getBlobDataService().create(any(File.class), anyString(), any(Date.class))).thenReturn("123-123-123");

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setName("uzer");
        userInfo.setUser(user);
        userInfo.setUser(user);
        when(testHelper.getDeclarationService().getSystemUserInfo()).thenReturn(userInfo);

        when(testHelper.getDepartmentService().getParentsHierarchyShortNames(anyInt())).thenReturn("dep/dep1");

        DeclarationDataFile maxWeightDeclData = new DeclarationDataFile();
        maxWeightDeclData.setFileName("PROT_NO_NDFL2_9979_9979_7707083893775001001_20160406_2FCC177D-2C02-59A5-E054-00144F6713DE.txt");
        maxWeightDeclData.setDate(sdf.parse("01.01.2099"));
        when(testHelper.getDeclarationService().findFileWithMaxWeight(anyLong())).thenReturn(maxWeightDeclData);

        RefBookDataProvider docStateProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DOC_STATE.getId()))).thenReturn(docStateProvider);
        when(docStateProvider.getUniqueRecordIds(any(Date.class), eq("KND = '1166009'"))).thenReturn(Arrays.asList(DOC_STATE_REQUIRED_ID));

        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        ArgumentCaptor<Map> argumentUpdate = ArgumentCaptor.forClass(Map.class);
        verify(ndflRefProvider, Mockito.times(16)).updateRecordVersion(any(Logger.class), anyLong(), any(Date.class), any(Date.class), argumentUpdate.capture());
        List<Map> allValues = argumentUpdate.getAllValues();

        Map<String, RefBookValue> notCorrect1 = (Map<String, RefBookValue>)allValues.get(0);
        Assert.assertEquals("Путь к реквизиту: \"ОбщСвИ/НомСпр\"; Значение элемента: \"0913378\"; Текст ошибки: \"Дубликат справки\"", notCorrect1.get("ERRTEXT").getStringValue());

        Map<String, RefBookValue> notCorrect2 = (Map<String, RefBookValue>)allValues.get(1);
        Assert.assertEquals("Путь к реквизиту: \"ОбщСвИ/НомСпр\"; Значение элемента: \"0913386\"; Текст ошибки: \"Дубликат справки\"", notCorrect2.get("ERRTEXT").getStringValue());

        Map<String, RefBookValue> correct3 = (Map<String, RefBookValue>)allValues.get(2);
        Assert.assertEquals("Текст ошибки от ФНС: \",629830,89,,ГУБКИНСКИЙ Г,,9-Й МКР,35,,26\" ДО исправления; (\",629830,89,,Губкинский г,,9 мкр,35,,26\" ПОСЛЕ исправления)", correct3.get("ERRTEXT").getStringValue());

        Map<String, RefBookValue> correct15 = (Map<String, RefBookValue>)allValues.get(15);
        Assert.assertEquals("Текст ошибки от ФНС: \",141000,50,МЫТИЩИНСКИЙ Р-Н,МЫТИЩИ Г,,,ВЧ151,,\"; (Адрес признан верным (ИФНСМЖ - 5029))", correct15.get("ERRTEXT").getStringValue());

        Assert.assertTrue(testHelper.getLogger().getEntries().isEmpty());

        ArgumentCaptor<DeclarationDataFile> argumentSaveFile = ArgumentCaptor.forClass(DeclarationDataFile.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).saveFile(argumentSaveFile.capture());

        DeclarationDataFile resultFile = argumentSaveFile.getValue();
        Assert.assertEquals(DECLARATION_DATA_ID.longValue(), resultFile.getDeclarationDataId());
        Assert.assertNotNull(resultFile.getUuid());
        Assert.assertEquals("uzer", resultFile.getUserName());
        Assert.assertEquals("dep/dep1", resultFile.getUserDepartmentName());
        Assert.assertEquals(ATTACH_FILE_TYPE_ID.longValue(), resultFile.getFileTypeId());
        Assert.assertEquals(sdf.parse("20.04.2016"), resultFile.getDate());

        ArgumentCaptor<Long> declarationDataId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> docStateId = ArgumentCaptor.forClass(Long.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).setDocStateId(declarationDataId.capture(), docStateId.capture());
        Assert.assertEquals(DECLARATION_DATA_ID, declarationDataId.getValue());
        Assert.assertEquals(DOC_STATE_REQUIRED_ID, docStateId.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNdfl2Register() throws ParseException {
        String fileName = "reestr_NO_NDFL2_9979_9979_7707083893997950001_20160602_344B2B8C-3DC6-7097-E054-00144F6713DE.txt";
        InputStream inputStream = Ndfl62Test.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/refbook/declaration_type/" + fileName);

        testHelper.setImportInputStream(inputStream);
        testHelper.setUploadFileName(fileName);
        testHelper.setDataFile(new File(fileName));

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(DECLARATION_TEMPLATE_ID);
        declarationTemplate.setDeclarationFormTypeId(DECLARATION_FORM_TYPE_ID);

        when(testHelper.getDeclarationService().findDeclarationDataByFileNameAndFileType(eq("NO_NDFL2_9979_9979_7707083893997950001_20160602_344B2B8C-3DC6-7097-E054-00144F6713DE.XML"), anyLong()))
                .thenReturn(Arrays.asList(declarationData));
        when(testHelper.getDeclarationService().findDeclarationDataByFileNameAndFileType(eq(fileName), anyLong()))
                .thenReturn(new ArrayList<DeclarationData>());
        when(testHelper.getDeclarationService().getDeclarationIds(any(DeclarationDataFilter.class), any(DeclarationDataSearchOrdering.class), anyBoolean()))
                .thenReturn(new ArrayList<Long>());
        when(testHelper.getDeclarationService().getTemplate(eq(DECLARATION_TEMPLATE_ID)))
                .thenReturn(declarationTemplate);

        RefBookDataProvider attachFileType = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.ATTACH_FILE_TYPE.getId()))).thenReturn(attachFileType);
        when(attachFileType.getUniqueRecordIds(any(Date.class), eq("CODE = 2"))).thenReturn(Arrays.asList(ATTACH_FILE_TYPE_ID));

        RefBookDataProvider declarationDataTypeRefProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DECLARATION_DATA_TYPE_REF_BOOK.getId()))).thenReturn(declarationDataTypeRefProvider);
        RefBookValue codeTypeRefBookValue = new RefBookValue(RefBookAttributeType.STRING, "2 НДФЛ (2)");
        Map<String, RefBookValue> resultTypeRef = new HashMap<String, RefBookValue>();
        resultTypeRef.put("CODE", codeTypeRefBookValue);
        when(declarationDataTypeRefProvider.getRecordData(DECLARATION_FORM_TYPE_ID)).thenReturn(resultTypeRef);

        when(testHelper.getBlobDataService().create(any(File.class), anyString(), any(Date.class))).thenReturn("123-123-123");

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setName("uzer");
        userInfo.setUser(user);
        userInfo.setUser(user);
        when(testHelper.getDeclarationService().getSystemUserInfo()).thenReturn(userInfo);

        when(testHelper.getDepartmentService().getParentsHierarchyShortNames(anyInt())).thenReturn("dep/dep1");

        RefBookDataProvider docStateProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DOC_STATE.getId()))).thenReturn(docStateProvider);
        when(docStateProvider.getUniqueRecordIds(any(Date.class), eq("KND = '1166009'"))).thenReturn(Arrays.asList(DOC_STATE_REQUIRED_ID));

        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        ArgumentCaptor<DeclarationDataFile> argumentSaveFile = ArgumentCaptor.forClass(DeclarationDataFile.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).saveFile(argumentSaveFile.capture());

        DeclarationDataFile resultFile = argumentSaveFile.getValue();
        Assert.assertEquals(DECLARATION_DATA_ID.longValue(), resultFile.getDeclarationDataId());
        Assert.assertNotNull(resultFile.getUuid());
        Assert.assertEquals("uzer", resultFile.getUserName());
        Assert.assertEquals("dep/dep1", resultFile.getUserDepartmentName());
        Assert.assertEquals(ATTACH_FILE_TYPE_ID.longValue(), resultFile.getFileTypeId());
        Assert.assertEquals(sdf.parse("10.06.2016"), resultFile.getDate());

        verify(testHelper.getDeclarationService(), Mockito.times(0)).setDocStateId(anyLong(), anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNdfl6IV() throws ParseException {
        String fileName = "IV_1_7707083893997950001_7707083893997950001_7707_20161230_704AD75C-9327-4472-9663-54F7FE5589C9.xml";
        InputStream inputStream = Ndfl62Test.class.getResourceAsStream("/com/aplana/sbrf/taxaccounting/refbook/declaration_type/" + fileName);

        testHelper.setImportInputStream(inputStream);
        testHelper.setUploadFileName(fileName);
        testHelper.setDataFile(new File(Ndfl62Test.class.getResource("/com/aplana/sbrf/taxaccounting/refbook/declaration_type/" + fileName).getPath()));

        DeclarationData declarationData = new DeclarationData();
        declarationData.setId(DECLARATION_DATA_ID);
        declarationData.setDeclarationTemplateId(DECLARATION_TEMPLATE_ID);

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setId(DECLARATION_TEMPLATE_ID);
        declarationTemplate.setDeclarationFormTypeId(DECLARATION_FORM_TYPE_ID);
        DeclarationTemplateFile xsdFile = new DeclarationTemplateFile();
        xsdFile.setFileName("IV_.xsd");
        declarationTemplate.setDeclarationTemplateFiles(Arrays.asList(xsdFile));

        when(testHelper.getDeclarationService().findDeclarationDataByFileNameAndFileType(eq("NO_NDFL6_9979_9979_7707083893775001001_20160406_2FCC177D-2C02-59A5-E054-00144F6713DE"), anyLong()))
                .thenReturn(Arrays.asList(declarationData));
        when(testHelper.getDeclarationService().findDeclarationDataByFileNameAndFileType(eq(fileName), anyLong()))
                .thenReturn(new ArrayList<DeclarationData>());
        when(testHelper.getDeclarationService().getDeclarationIds(any(DeclarationDataFilter.class), any(DeclarationDataSearchOrdering.class), anyBoolean()))
                .thenReturn(new ArrayList<Long>());
        when(testHelper.getDeclarationService().getTemplate(eq(DECLARATION_TEMPLATE_ID)))
                .thenReturn(declarationTemplate);

        RefBookDataProvider attachFileType = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.ATTACH_FILE_TYPE.getId()))).thenReturn(attachFileType);
        when(attachFileType.getUniqueRecordIds(any(Date.class), eq("CODE = 2"))).thenReturn(Arrays.asList(ATTACH_FILE_TYPE_ID));

        RefBookDataProvider declarationDataTypeRefProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DECLARATION_DATA_TYPE_REF_BOOK.getId()))).thenReturn(declarationDataTypeRefProvider);
        RefBookValue codeTypeRefBookValue = new RefBookValue(RefBookAttributeType.STRING, "6 НДФЛ");
        Map<String, RefBookValue> resultTypeRef = new HashMap<String, RefBookValue>();
        resultTypeRef.put("CODE", codeTypeRefBookValue);
        when(declarationDataTypeRefProvider.getRecordData(DECLARATION_FORM_TYPE_ID)).thenReturn(resultTypeRef);

        when(testHelper.getBlobDataService().create(any(File.class), anyString(), any(Date.class))).thenReturn("123-123-123");

        TAUserInfo userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setName("uzer");
        userInfo.setUser(user);
        userInfo.setUser(user);
        when(testHelper.getDeclarationService().getSystemUserInfo()).thenReturn(userInfo);

        when(testHelper.getDepartmentService().getParentsHierarchyShortNames(anyInt())).thenReturn("dep/dep1");

        RefBookDataProvider docStateProvider = mock(RefBookDataProvider.class);
        when(testHelper.getRefBookFactory().getDataProvider(eq(RefBook.Id.DOC_STATE.getId()))).thenReturn(docStateProvider);
        when(docStateProvider.getUniqueRecordIds(any(Date.class), eq("KND = '1166007'"))).thenReturn(Arrays.asList(DOC_STATE_SUCCESS_ID));

        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        ArgumentCaptor<DeclarationDataFile> argumentSaveFile = ArgumentCaptor.forClass(DeclarationDataFile.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).saveFile(argumentSaveFile.capture());
        verify(testHelper.getDeclarationService(), Mockito.times(1)).validateDeclaration(any(Logger.class), any(File.class), anyString());

        DeclarationDataFile resultFile = argumentSaveFile.getValue();
        Assert.assertEquals(DECLARATION_DATA_ID.longValue(), resultFile.getDeclarationDataId());
        Assert.assertNotNull(resultFile.getUuid());
        Assert.assertEquals("uzer", resultFile.getUserName());
        Assert.assertEquals("dep/dep1", resultFile.getUserDepartmentName());
        Assert.assertEquals(ATTACH_FILE_TYPE_ID.longValue(), resultFile.getFileTypeId());
        Assert.assertEquals(sdf.parse("30.12.2016"), resultFile.getDate());

        ArgumentCaptor<Long> declarationDataId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> docStateId = ArgumentCaptor.forClass(Long.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).setDocStateId(declarationDataId.capture(), docStateId.capture());
        Assert.assertEquals(DECLARATION_DATA_ID, declarationDataId.getValue());
        Assert.assertEquals(DOC_STATE_SUCCESS_ID, docStateId.getValue());
    }
}
