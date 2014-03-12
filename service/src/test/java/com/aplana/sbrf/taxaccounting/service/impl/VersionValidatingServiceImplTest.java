package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.IntersectionSegment;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.templateversion.VersionOperatingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    @Qualifier("versionValidatingService")
    @Autowired
    VersionOperatingService versionOperatingService;

    private static int FORM_TEMPLATE_ID_F = 1;
    private static int FORM_TEMPLATE_ID_S = 2;
    private static int FORM_TEMPLATE_ID_TH = 3;

    private static int FORM_TYPE_ID_F = 1;

    private Date actualEndVersion;
    private Calendar calendar = Calendar.getInstance();


    @Before
    public void init(){
        calendar.set(2012, Calendar.JANUARY, 1);
        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setId(FORM_TEMPLATE_ID_F);
        formTemplate1.setCode("code");
        formTemplate1.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate1.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate1.setEdition(1);
        FormType type1 = new FormType();
        type1.setId(FORM_TYPE_ID_F);
        formTemplate1.setType(type1);

        calendar.set(2013, Calendar.DECEMBER, 1);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setId(FORM_TEMPLATE_ID_S);
        formTemplate2.setCode("code");
        formTemplate2.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate2.setStatus(VersionedObjectStatus.FAKE);
        formTemplate2.setEdition(1);

        calendar.set(2014, Calendar.JANUARY, 1);
        FormTemplate formTemplate3 = new FormTemplate();
        formTemplate3.setId(FORM_TEMPLATE_ID_TH);
        formTemplate3.setCode("code");
        formTemplate3.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate3.setStatus(VersionedObjectStatus.DRAFT);
        formTemplate3.setEdition(1);

        //Настройка пересечений
        List<IntersectionSegment> segmentIntersections = new ArrayList<IntersectionSegment>();

        IntersectionSegment segmentIntersection1 = new IntersectionSegment();
        segmentIntersection1.setTemplateId(formTemplate1.getId());
        segmentIntersection1.setBeginDate(formTemplate1.getVersion());
        segmentIntersection1.setStatus(formTemplate1.getStatus());
        segmentIntersection1.setEndDate(formTemplate2.getVersion());

        IntersectionSegment segmentIntersection2 = new IntersectionSegment();

        segmentIntersection2.setTemplateId(formTemplate2.getId());
        segmentIntersection2.setBeginDate(formTemplate2.getVersion());
        segmentIntersection2.setStatus(formTemplate2.getStatus());
        segmentIntersection2.setEndDate(formTemplate3.getVersion());

        IntersectionSegment segmentIntersection3 = new IntersectionSegment();
        segmentIntersection3.setTemplateId(formTemplate3.getId());
        segmentIntersection3.setBeginDate(formTemplate3.getVersion());
        segmentIntersection3.setStatus(formTemplate3.getStatus());

        segmentIntersections.add(segmentIntersection2);
        segmentIntersections.add(segmentIntersection3);

        calendar.set(2014, Calendar.JULY, 1);
        actualEndVersion = calendar.getTime();
        when(formTemplateService.findFTVersionIntersections(formTemplate1.getId(), formTemplate1.getType().getId(),
                formTemplate1.getVersion(), actualEndVersion)).thenReturn(segmentIntersections);
        calendar.set(2013, Calendar.JANUARY, 1);
        when(formTemplateService.findFTVersionIntersections(formTemplate1.getId(), formTemplate1.getType().getId(),
                formTemplate1.getVersion(), calendar.getTime())).thenReturn(segmentIntersections);

        calendar.clear();
        when(formTemplateService.get(FORM_TEMPLATE_ID_F)).thenReturn(formTemplate1);
        when(formTemplateService.get(FORM_TEMPLATE_ID_S)).thenReturn(formTemplate2);
        when(formTemplateService.get(FORM_TEMPLATE_ID_TH)).thenReturn(formTemplate3);
        when(formTemplateService.getNearestFTRight(FORM_TEMPLATE_ID_F, VersionedObjectStatus.FAKE)).thenReturn(formTemplate2);
        when(formTypeService.get(FORM_TYPE_ID_F)).thenReturn(type1);

        /*when(formTemplateService.getNearestFTRight(formTemplate2,
                VersionedObjectStatus.FAKE, VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL)).thenReturn(formTemplate1);
        when(formTemplateService.getNearestFTRight(formTemplate2)).thenReturn(formTemplate1);*/
    }

    @Test
    public void testIsIntersectionVersion(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_F);
        Logger logger = new Logger();

        versionOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(), formTemplate.getStatus(),
                formTemplate.getVersion(), actualEndVersion, logger);
    }

    //Пересечеиние нет, но есть шаблон FAKE
    @Test
    public void testIsIntersectionVersionNo(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_F);
        calendar.set(2013, Calendar.JANUARY, 1);
        Logger logger = new Logger();

        versionOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(), formTemplate.getStatus(),
                formTemplate.getVersion(), calendar.getTime(), logger);
    }

}
