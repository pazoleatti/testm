package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.script.impl.DeclarationServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

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
        DepartmentParamDao departmentParamDao = mock(DepartmentParamDao.class);
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
        when(departmentParamDao.getDepartmentParam(1)).thenReturn(new DepartmentParam());
        when(declarationTemplateDao.get(1)).thenReturn(declarationTemplate);
        when(departmentFormTypeDao.getDeclarationSources(1, 1)).thenReturn(sourcesInfo);

        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);
        ReflectionTestUtils.setField(service, "departmentParamDao", departmentParamDao);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
        ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);
    }

    @Test
    public void find() {
        assertTrue(service.find(2, 1, 101) != null);
    }

    @Test
    public void generateXmlFileId() {
        assertTrue(service.generateXmlFileId(2, 1) != null);
    }

    @Test
    public void getAcceptedFormDataSources() {
        assertTrue(service.getAcceptedFormDataSources(declarationData) != null);
    }

    @Test
    public void getXmlData() {
        assertTrue(service.getXmlData(1) != null);
    }
}
