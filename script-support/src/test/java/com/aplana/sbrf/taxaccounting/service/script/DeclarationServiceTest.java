package com.aplana.sbrf.taxaccounting.service.script;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.script.impl.DeclarationServiceImpl;

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

    @Test
    public void generateXmlFileId() {
        DeclarationType declarationType = new DeclarationType();
        declarationType.setTaxType(TaxType.TRANSPORT);
        DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
        when(declarationTypeDao.get(1)).thenReturn(declarationType);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);

        Map<String, RefBookValue> departmentParam = new HashMap<String, RefBookValue>();
        departmentParam.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "тест"));
        departmentParam.put("INN", new RefBookValue(RefBookAttributeType.STRING, "тест"));
        departmentParam.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "тест"));

        RefBookDataProvider dataProvider = mock(RefBookDataProvider.class);
        PeriodService reportPeriodService = mock(PeriodService.class);
        when(reportPeriodService.getStartDate(48)).thenReturn(Calendar.getInstance());
        PagingResult<Map<String, RefBookValue>> list = new PagingResult<Map<String, RefBookValue>>();
        list.add(departmentParam);
        when(dataProvider.getRecords(new Date(), null, String.format("DEPARTMENT_ID = %d", 2), null)).thenReturn(list);

        RefBookFactory factory = mock(RefBookFactory.class);
        when(factory.getDataProvider(31L)).thenReturn(dataProvider);

        ReflectionTestUtils.setField(service, "periodService", reportPeriodService);
        ReflectionTestUtils.setField(service, "factory", factory);
        String fileId = service.generateXmlFileId(1, 2, 48);
        assertTrue(fileId != null);
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
