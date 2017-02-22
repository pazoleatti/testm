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
    private final static Long ATTACH_FILE_TYPE_SAVE_ID = 100508L;
    private final static Long DOC_STATE_ACCEPT_ID = 100508L;

    @Before
    public void initMock() {
        Mockito.reset(
                testHelper.getDeclarationService(),
                testHelper.getRefBookFactory()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNdfl6KV() throws ParseException {
        String fileName = "KV_1_7707083893997950001_7707083893997950001_7707_20161230_704AD75C-9327-4472-9663-54F7FE5589C9.xml";
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
        xsdFile.setFileName("KV_.xsd");
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
        when(attachFileType.getUniqueRecordIds(any(Date.class), eq("CODE = 3"))).thenReturn(Arrays.asList(ATTACH_FILE_TYPE_SAVE_ID));

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
        when(docStateProvider.getUniqueRecordIds(any(Date.class), eq("KND = '1166002'"))).thenReturn(Arrays.asList(DOC_STATE_ACCEPT_ID));

        testHelper.execute(FormDataEvent.IMPORT_TRANSPORT_FILE);

        ArgumentCaptor<DeclarationDataFile> argumentSaveFile = ArgumentCaptor.forClass(DeclarationDataFile.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).saveFile(argumentSaveFile.capture());
        verify(testHelper.getDeclarationService(), Mockito.times(1)).validateDeclaration(any(Logger.class), any(File.class), anyString());

        DeclarationDataFile resultFile = argumentSaveFile.getValue();
        Assert.assertEquals(DECLARATION_DATA_ID.longValue(), resultFile.getDeclarationDataId());
        Assert.assertNotNull(resultFile.getUuid());
        Assert.assertEquals("uzer", resultFile.getUserName());
        Assert.assertEquals("dep/dep1", resultFile.getUserDepartmentName());
        Assert.assertEquals(ATTACH_FILE_TYPE_SAVE_ID.longValue(), resultFile.getFileTypeId());
        Assert.assertEquals(sdf.parse("30.12.2016"), resultFile.getDate());

        ArgumentCaptor<Long> declarationDataId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> docStateId = ArgumentCaptor.forClass(Long.class);
        verify(testHelper.getDeclarationService(), Mockito.times(1)).setDocStateId(declarationDataId.capture(), docStateId.capture());
        Assert.assertEquals(DECLARATION_DATA_ID, declarationDataId.getValue());
        Assert.assertEquals(DOC_STATE_ACCEPT_ID, docStateId.getValue());
    }
}
