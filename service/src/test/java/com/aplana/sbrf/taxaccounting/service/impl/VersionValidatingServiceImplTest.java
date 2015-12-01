package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.templateversion.VersionOperatingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.when;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("VersionValidatingServiceImplTest.xml")
public class VersionValidatingServiceImplTest {

    @Autowired
    FormTemplateService formTemplateService;
    @Autowired
    FormTypeService formTypeService;

    @Qualifier("versionFTValidatingService")
    @Autowired
    VersionOperatingService versionFTOperatingService;

    @Autowired
    SourceService sourceService;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    PeriodService periodService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    FormDataDao formDataDao;
    @Autowired
    FormDataService formDataService;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private static int FORM_TEMPLATE_ID_F = 1;
    private static int FORM_TEMPLATE_ID_S = 2;
    private static int FORM_TEMPLATE_ID_TH = 3;
    private static int FORM_TEMPLATE_ID_FORTH = 4;

    private static int FORM_TYPE_ID_F = 1;

    private Date actualEndVersion;
    private Calendar calendar = Calendar.getInstance();


    @Before
    public void init(){
        calendar.set(2012, Calendar.JANUARY, 1);
        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setId(FORM_TEMPLATE_ID_F);
        formTemplate1.setHeader("header");
        formTemplate1.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate1.setStatus(VersionedObjectStatus.NORMAL);
        FormType type1 = new FormType();
        type1.setId(FORM_TYPE_ID_F);
        formTemplate1.setType(type1);

        calendar.set(2013, Calendar.JANUARY, 1);
        FormTemplate formTemplate4 = new FormTemplate();
        formTemplate4.setId(FORM_TEMPLATE_ID_FORTH);
        formTemplate4.setHeader("header");
        formTemplate4.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate4.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate4.setType(type1);

        calendar.set(2013, Calendar.DECEMBER, 31);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setId(FORM_TEMPLATE_ID_S);
        formTemplate2.setHeader("header");
        formTemplate2.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate2.setStatus(VersionedObjectStatus.FAKE);

        calendar.set(2014, Calendar.JANUARY, 1);
        FormTemplate formTemplate3 = new FormTemplate();
        formTemplate3.setId(FORM_TEMPLATE_ID_TH);
        formTemplate3.setHeader("header");
        formTemplate3.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate3.setStatus(VersionedObjectStatus.DRAFT);

        //Настройка пересечений
        VersionSegment segmentIntersection1 = new VersionSegment();
        segmentIntersection1.setTemplateId(formTemplate1.getId());
        segmentIntersection1.setBeginDate(formTemplate1.getVersion());
        segmentIntersection1.setStatus(formTemplate1.getStatus());
        segmentIntersection1.setEndDate(formTemplate4.getVersion());

        VersionSegment segmentIntersection2 = new VersionSegment();

        segmentIntersection2.setTemplateId(formTemplate2.getId());
        segmentIntersection2.setBeginDate(formTemplate2.getVersion());
        segmentIntersection2.setStatus(formTemplate2.getStatus());
        segmentIntersection2.setEndDate(formTemplate3.getVersion());

        VersionSegment segmentIntersection3 = new VersionSegment();
        segmentIntersection3.setTemplateId(formTemplate3.getId());
        segmentIntersection3.setBeginDate(formTemplate3.getVersion());
        segmentIntersection3.setStatus(formTemplate3.getStatus());

        VersionSegment segmentIntersection4 = new VersionSegment();
        segmentIntersection4.setTemplateId(formTemplate4.getId());
        segmentIntersection4.setBeginDate(formTemplate4.getVersion());
        segmentIntersection4.setStatus(formTemplate4.getStatus());
        segmentIntersection4.setEndDate(formTemplate2.getVersion());

        calendar.set(2014, Calendar.JULY, 1);
        actualEndVersion = calendar.getTime();
        when(formTemplateService.findFTVersionIntersections(formTemplate1.getId(), formTemplate1.getType().getId(),
                formTemplate1.getVersion(), actualEndVersion)).thenReturn(Arrays.asList(segmentIntersection2, segmentIntersection3));
        calendar.set(2013, Calendar.JANUARY, 1);
        when(formTemplateService.findFTVersionIntersections(formTemplate1.getId(), formTemplate1.getType().getId(),
                formTemplate1.getVersion(), calendar.getTime())).thenReturn(Arrays.asList(segmentIntersection2));
        calendar.set(2014, Calendar.JANUARY, 1);
        when(formTemplateService.findFTVersionIntersections(formTemplate4.getId(), formTemplate4.getType().getId(),
                formTemplate4.getVersion(), calendar.getTime())).thenReturn(Arrays.asList(segmentIntersection4));
        when(formTemplateService.findFTVersionIntersections(formTemplate4.getId(), formTemplate4.getType().getId(),
                formTemplate4.getVersion(), null)).thenReturn(Arrays.asList(segmentIntersection4));
        calendar.set(2013, Calendar.DECEMBER, 31);
        when(formTemplateService.findFTVersionIntersections(formTemplate4.getId(), formTemplate4.getType().getId(),
                formTemplate4.getVersion(), calendar.getTime())).thenReturn(Arrays.asList(segmentIntersection4));

        calendar.clear();
        when(formTemplateService.get(FORM_TEMPLATE_ID_F)).thenReturn(formTemplate1);
        when(formTemplateService.get(FORM_TEMPLATE_ID_S)).thenReturn(formTemplate2);
        when(formTemplateService.get(FORM_TEMPLATE_ID_TH)).thenReturn(formTemplate3);
        when(formTemplateService.get(FORM_TEMPLATE_ID_FORTH)).thenReturn(formTemplate4);
        when(formTemplateService.getNearestFTRight(FORM_TEMPLATE_ID_F, VersionedObjectStatus.FAKE)).thenReturn(formTemplate2);
        when(formTypeService.get(FORM_TYPE_ID_F)).thenReturn(type1);

        when(sourceService.getDFTByFormType(FORM_TYPE_ID_F)).thenReturn(new ArrayList<DepartmentFormType>(0));

        /*when(formTemplateService.getNearestFTRight(formTemplate2,
                VersionedObjectStatus.FAKE, VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL)).thenReturn(formTemplate1);
        when(formTemplateService.getNearestFTRight(formTemplate2)).thenReturn(formTemplate1);*/
    }

    @Test
    public void testIsIntersectionVersion(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_F);
        Logger logger = new Logger();

        versionFTOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(), formTemplate.getStatus(),
                formTemplate.getVersion(), actualEndVersion, logger);
    }

    //Пересечеиние нет, но есть шаблон FAKE
    @Test
    public void testIsIntersectionVersionNo(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_F);
        calendar.set(2013, Calendar.JANUARY, 1);
        Logger logger = new Logger();

        versionFTOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(), formTemplate.getStatus(),
                formTemplate.getVersion(), calendar.getTime(), logger);
    }

    @Test
    public void testIsIntersectionVersionEqualDates(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_FORTH);
        calendar.set(2013, Calendar.DECEMBER, 31);
        Logger logger = new Logger();

        versionFTOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(), formTemplate.getStatus(),
                formTemplate.getVersion(), calendar.getTime(), logger);
        Assert.assertTrue(logger.containsLevel(LogLevel.ERROR));
    }

    @Test
    public void isUsedVersionTest() throws ParseException {
        Logger logger = new Logger();

        FormType formType = new FormType();
        formType.setId(1);
        formType.setName("Тестовый тип НФ");

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setType(formType);
        formTemplate.setId(1);

        FormData formData = new FormData(formTemplate);
        formData.setState(WorkflowState.CREATED);
        formData.setKind(FormDataKind.SUMMARY);
        formData.setDepartmentId(1);
        formData.setReportPeriodId(1);
        formData.setId(1l);
        formData.setDepartmentReportPeriodId(1);
        formData.setComparativePeriodId(1);

        FormData formData1 = new FormData(formTemplate);
        formData1.setState(WorkflowState.CREATED);
        formData1.setKind(FormDataKind.SUMMARY);
        formData1.setDepartmentId(1);
        formData1.setReportPeriodId(2);
        formData1.setId(2l);
        formData1.setDepartmentReportPeriodId(2);
        formData1.setComparativePeriodId(2);

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        taxPeriod.setYear(2014);
        taxPeriod.setTaxType(TaxType.INCOME);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setName("Тестовый период");
        ReportPeriod reportPeriod1 = new ReportPeriod();
        reportPeriod1.setId(2);
        reportPeriod1.setTaxPeriod(taxPeriod);
        reportPeriod1.setName("Второй тестовый период");

        Department department = new Department();
        department.setName("Тестовое подразделение");

        List<Long> list = new ArrayList<Long>() {{
            add(1l);
            add(2l);
        }};

        DepartmentReportPeriod drp = new DepartmentReportPeriod();
        drp.setReportPeriod(reportPeriod);
        when(departmentReportPeriodService.get(formData.getDepartmentReportPeriodId())).thenReturn(drp);
        DepartmentReportPeriod drp1 = new DepartmentReportPeriod();
        drp1.setReportPeriod(reportPeriod1);
        drp1.setCorrectionDate(SIMPLE_DATE_FORMAT.parse("01.01.2014"));
        when(departmentReportPeriodService.get(formData1.getDepartmentReportPeriodId())).thenReturn(drp1);

        when(formDataDao.getWithoutRows(1)).thenReturn(formData);
        when(formDataDao.getWithoutRows(2)).thenReturn(formData1);

        when(periodService.getReportPeriod(1)).thenReturn(reportPeriod);
        when(periodService.getReportPeriod(2)).thenReturn(reportPeriod1);

        when(departmentService.getDepartment(1)).thenReturn(department);

        when(formDataService.getFormDataListInActualPeriodByTemplate(formTemplate.getId(), SIMPLE_DATE_FORMAT.parse("01.01.2014")))
        .thenReturn(list);

        versionFTOperatingService.isUsedVersion(
                formTemplate.getId(),
                1,
                VersionedObjectStatus.NORMAL,
                SIMPLE_DATE_FORMAT.parse("01.01.2014"),
                SIMPLE_DATE_FORMAT.parse("01.01.2014"),
                logger);

        Assert.assertEquals(
                "Существует экземпляр налоговые формы:  Тип: \"Сводная\", Вид: \"Тестовый тип НФ\", Подразделение: \"Тестовое подразделение\", Период: \"Тестовый период 2014\", Период сравнения: \"Тестовый период 2014\", Версия: \"Автоматическая\"",
                logger.getEntries().get(0).getMessage()
        );
        Assert.assertEquals(
                "Существует экземпляр налоговые формы:  Тип: \"Сводная\", Вид: \"Тестовый тип НФ\", Подразделение: \"Тестовое подразделение\", Период: \"Второй тестовый период 2014\", Период сравнения: \"Второй тестовый период 2014\", Дата сдачи корректировки: 01.01.2014, Версия: \"Абсолютные значения\"",
                logger.getEntries().get(1).getMessage()
        );
    }

}
