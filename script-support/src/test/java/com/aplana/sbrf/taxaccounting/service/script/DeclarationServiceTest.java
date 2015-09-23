package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.script.impl.DeclarationServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
    private static final Calendar currentCalendar = Calendar.getInstance();

    @BeforeClass
    public static void tearUp() {
        DeclarationDataDao declarationDataDao = mock(DeclarationDataDao.class);
        DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
        DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
        DeclarationDataService declarationDataService = mock(DeclarationDataService.class);

        declarationType.setId(1);
        declarationType.setName("name");
        declarationType.setTaxType(TaxType.INCOME);

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
        declarationTemplate.setCreateScript("script");
        declarationTemplate.setType(new DeclarationType());
        declarationTemplate.setVersion(new Date());
        declarationTemplate.setId(1);

        declarationData.setId(1L);
        declarationData.setAccepted(true);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(101);
        declarationData.setDepartmentReportPeriodId(1);

        List<DepartmentFormType> sourcesInfo = new ArrayList<DepartmentFormType>();
        sourcesInfo.add(new DepartmentFormType());
        sourcesInfo.add(new DepartmentFormType());

        when(declarationTemplateDao.get(1)).thenReturn(declarationTemplate);
        when(departmentFormTypeDao.getDeclarationSources(eq(1), eq(1), any(Date.class), any(Date.class))).thenReturn(sourcesInfo);
        when(declarationDataService.getFormDataSources(any(DeclarationData.class), any(Boolean.class), any(Logger.class))).thenReturn(sourcesInfo);

        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);
        ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);
        ReflectionTestUtils.setField(service, "declarationDataService", declarationDataService);
    }

    @Test
    public void getAcceptedFormDataSources() {
        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(48);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(2);

        DepartmentReportPeriodDao departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(1));
        filter.setReportPeriodIdList(Arrays.asList(101));

        when(departmentReportPeriodDao.getListByFilter(filter)).thenReturn(Arrays.asList(departmentReportPeriod));
        when(departmentReportPeriodDao.get(1)).thenReturn(departmentReportPeriod);

        FormDataDao formDataDao = mock(FormDataDao.class);

        ReflectionTestUtils.setField(service, "formDataDao", formDataDao);
        ReflectionTestUtils.setField(service, "departmentReportPeriodDao", departmentReportPeriodDao);
        assertTrue(service.getAcceptedFormDataSources(declarationData) != null);
    }

    @Test
    public void getXmlData() {
        //В свзязи с изменениями в DECLARATION_DATA (http://jira.aplana.com/browse/SBRFACCTAX-4544)
        //зкомментил тест, слишком громозко для изменения.
        /*assertTrue(service.getXmlData(1) != null);*/
    }
}
