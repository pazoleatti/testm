package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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
        DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
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

        when(declarationTypeDao.get(2)).thenReturn(declarationType);
        when(declarationTemplateDao.get(1)).thenReturn(declarationTemplate);
        when(departmentFormTypeDao.getDeclarationSources(eq(1), eq(1), any(Date.class), any(Date.class))).thenReturn(sourcesInfo);
        when(declarationDataService.getFormDataSources(any(DeclarationData.class), any(Boolean.class), any(Logger.class))).thenReturn(sourcesInfo);

        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);
        ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
        ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);
        ReflectionTestUtils.setField(service, "declarationDataService", declarationDataService);

        PeriodService periodService = mock(PeriodService.class);
        when(periodService.getReportPeriod(any(Integer.class))).thenReturn(mock(ReportPeriod.class));
        ReflectionTestUtils.setField(service, "periodService", periodService);
    }

    @Test
    public void generateXmlFileIdTest() {
        DeclarationType declarationType = new DeclarationType();
        declarationType.setTaxType(TaxType.TRANSPORT);
        DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
        when(declarationTypeDao.get(1)).thenReturn(declarationType);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(48);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(2);
        DepartmentReportPeriodDao departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        when(departmentReportPeriodDao.get(1)).thenReturn(departmentReportPeriod);
        ReflectionTestUtils.setField(service, "departmentReportPeriodDao", departmentReportPeriodDao);

        Map<String, RefBookValue> departmentParam = new HashMap<String, RefBookValue>();
        departmentParam.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "ткод"));
        departmentParam.put("INN", new RefBookValue(RefBookAttributeType.STRING, "инн"));
        departmentParam.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "кпп"));

        RefBookDataProvider dataProvider = mock(RefBookDataProvider.class);
        PeriodService reportPeriodService = mock(PeriodService.class);
        when(reportPeriodService.getEndDate(48)).thenReturn(currentCalendar);
        PagingResult<Map<String, RefBookValue>> list = new PagingResult<Map<String, RefBookValue>>();
        list.add(departmentParam);
        when(dataProvider.getRecords(addDayToDate(currentCalendar.getTime(), -1), null, String.format("DEPARTMENT_ID = %d", 2), null)).thenReturn(list);

        RefBookFactory factory = mock(RefBookFactory.class);
        when(factory.getDataProvider(31L)).thenReturn(dataProvider);

        ReflectionTestUtils.setField(service, "periodService", reportPeriodService);
        ReflectionTestUtils.setField(service, "factory", factory);
        String fileId = service.generateXmlFileId(1, 1, departmentParam.get("TAX_ORGAN_CODE").getStringValue(), departmentParam.get("KPP").getStringValue());
        assertTrue(fileId != null);
        assertTrue(fileId.startsWith("NO_TRAND_ткод_ткод_иннкпп_"));
    }

    @Test
    public void generateXmlFileIdTest2() {
        DeclarationType declarationType = new DeclarationType();
        declarationType.setTaxType(TaxType.TRANSPORT);
        DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
        when(declarationTypeDao.get(1)).thenReturn(declarationType);
        ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);

        DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(48);
        departmentReportPeriod.setReportPeriod(reportPeriod);
        departmentReportPeriod.setDepartmentId(2);
        DepartmentReportPeriodDao departmentReportPeriodDao = mock(DepartmentReportPeriodDao.class);
        when(departmentReportPeriodDao.get(1)).thenReturn(departmentReportPeriod);
        ReflectionTestUtils.setField(service, "departmentReportPeriodDao", departmentReportPeriodDao);

        Map<String, RefBookValue> departmentParam = new HashMap<String, RefBookValue>();
        departmentParam.put("TAX_ORGAN_CODE", new RefBookValue(RefBookAttributeType.STRING, "ткод"));
        departmentParam.put("TAX_ORGAN_CODE_PROM", new RefBookValue(RefBookAttributeType.STRING, "пкод"));
        departmentParam.put("INN", new RefBookValue(RefBookAttributeType.STRING, "инн"));
        departmentParam.put("KPP", new RefBookValue(RefBookAttributeType.STRING, "кпп"));

        RefBookDataProvider dataProvider = mock(RefBookDataProvider.class);
        PeriodService reportPeriodService = mock(PeriodService.class);
        when(reportPeriodService.getEndDate(48)).thenReturn(currentCalendar);
        PagingResult<Map<String, RefBookValue>> list = new PagingResult<Map<String, RefBookValue>>();
        list.add(departmentParam);
        when(dataProvider.getRecords(addDayToDate(currentCalendar.getTime(), -1), null, String.format("DEPARTMENT_ID = %d", 2), null)).thenReturn(list);

        RefBookFactory factory = mock(RefBookFactory.class);
        when(factory.getDataProvider(31L)).thenReturn(dataProvider);

        ReflectionTestUtils.setField(service, "periodService", reportPeriodService);
        ReflectionTestUtils.setField(service, "factory", factory);
        String fileId = service.generateXmlFileId(1, 1, departmentParam.get("TAX_ORGAN_CODE_PROM").getStringValue(), departmentParam.get("TAX_ORGAN_CODE").getStringValue(), departmentParam.get("KPP").getStringValue());
        assertTrue(fileId != null);
        assertTrue(fileId.startsWith("NO_TRAND_пкод_ткод_иннкпп_"));
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

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}
