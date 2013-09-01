package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.impl.DeclarationServiceImpl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тест для сервиса работы с декларациями.
 *
 * @author rtimerbaev
 */
public class DeclarationServiceTest {

    private static DeclarationService service = new DeclarationServiceImpl();
    private static DeclarationType declarationType = new DeclarationType();
    private static DeclarationData declarationData = new DeclarationData();

    @BeforeClass
    public static void tearUp() {
        DeclarationDataDao declarationDataDao = mock(DeclarationDataDao.class);
        DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
        DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);

        declarationType.setId(1);
        declarationType.setName("name");
        declarationType.setTaxType(TaxType.INCOME);

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setActive(true);
        declarationTemplate.setCreateScript("script");
        declarationTemplate.setDeclarationType(new DeclarationType());
        declarationTemplate.setEdition(1);
        declarationTemplate.setVersion("1");
        declarationTemplate.setId(1);

        declarationData.setId(1L);
        declarationData.setAccepted(true);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(101);

        List<DepartmentFormType> sourcesInfo = new ArrayList<DepartmentFormType>();
        sourcesInfo.add(new DepartmentFormType());
        sourcesInfo.add(new DepartmentFormType());

        when(declarationDataDao.find(2, 1, 101)).thenReturn(new DeclarationData());
        when(declarationDataDao.getXmlData(1)).thenReturn("result");
        when(declarationTypeDao.get(2)).thenReturn(declarationType);
        when(declarationTemplateDao.get(1)).thenReturn(declarationTemplate);
        when(departmentFormTypeDao.getDeclarationSources(1, 1)).thenReturn(sourcesInfo);

        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
        ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);
    }

    @Test
    public void find() {
        assertTrue(service.find(2, 1, 101) != null);
    }

    // TODO (Aydar Kadyrgulov) Доделать тест
    /*@Test
    public void generateXmlFileId() {
        DeclarationType declarationType = new DeclarationType();
        declarationType.setTaxType(TaxType.TRANSPORT);
        DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
        when(declarationTypeDao.get(1)).thenReturn(declarationType);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);

        Map<String, RefBookValue> departmentParam = new HashMap<String, RefBookValue>();
        departmentParam.put();

        RefBookDataProvider dataProvider = mock(RefBookDataProvider.class);
        when(dataProvider.getRecords()).thenReturn(new List< Map<String, RefBookValue>>(){{add(departmentParam);}});

        RefBookFactory factory = mock(RefBookFactory.class);
        when(factory.getDataProvider(31L)).thenReturn(dataProvider);

        String fileId = service.generateXmlFileId(1, 2);
        assertTrue(fileId != null);
    }*/

    @Test
    public void getAcceptedFormDataSources() {
        assertTrue(service.getAcceptedFormDataSources(declarationData) != null);
    }

    @Test
    public void getXmlData() {
        assertTrue(service.getXmlData(1) != null);
    }
}
