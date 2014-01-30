package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.SegmentIntersection;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
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

    @Qualifier("versionValidatingService")
    @Autowired
    VersionOperatingService<FormTemplate> versionOperatingService;

    private static int FORM_TEMPLATE_ID_F = 1;
    private static int FORM_TEMPLATE_ID_S = 2;
    private static int FORM_TEMPLATE_ID_TH = 3;

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
        List<SegmentIntersection> segmentIntersections = new ArrayList<SegmentIntersection>();

        SegmentIntersection segmentIntersection1 = new SegmentIntersection();
        segmentIntersection1.setTemplateId(formTemplate1.getId());
        segmentIntersection1.setBeginDate(formTemplate1.getVersion());
        segmentIntersection1.setStatus(formTemplate1.getStatus());
        segmentIntersection1.setEndDate(formTemplate2.getVersion());

        SegmentIntersection segmentIntersection2 = new SegmentIntersection();

        segmentIntersection2.setTemplateId(formTemplate2.getId());
        segmentIntersection2.setBeginDate(formTemplate2.getVersion());
        segmentIntersection2.setStatus(formTemplate2.getStatus());
        segmentIntersection2.setEndDate(formTemplate3.getVersion());

        SegmentIntersection segmentIntersection3 = new SegmentIntersection();
        segmentIntersection3.setTemplateId(formTemplate3.getId());
        segmentIntersection3.setBeginDate(formTemplate3.getVersion());
        segmentIntersection3.setStatus(formTemplate3.getStatus());

        segmentIntersections.add(segmentIntersection2);
        segmentIntersections.add(segmentIntersection3);

        calendar.set(2014, Calendar.JULY, 1);
        actualEndVersion = calendar.getTime();
        when(formTemplateService.findFTVersionIntersections(formTemplate1, actualEndVersion)).thenReturn(segmentIntersections);
        calendar.set(2013, Calendar.JANUARY, 1);
        when(formTemplateService.findFTVersionIntersections(formTemplate1, calendar.getTime())).thenReturn(segmentIntersections);

        calendar.clear();
        when(formTemplateService.get(FORM_TEMPLATE_ID_F)).thenReturn(formTemplate1);
        when(formTemplateService.get(FORM_TEMPLATE_ID_S)).thenReturn(formTemplate2);
        when(formTemplateService.get(FORM_TEMPLATE_ID_TH)).thenReturn(formTemplate3);
        when(formTemplateService.getNearestFTRight(FORM_TEMPLATE_ID_F, VersionedObjectStatus.FAKE)).thenReturn(formTemplate2);

        /*when(formTemplateService.getNearestFTRight(formTemplate2,
                VersionedObjectStatus.FAKE, VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL)).thenReturn(formTemplate1);
        when(formTemplateService.getNearestFTRight(formTemplate2)).thenReturn(formTemplate1);*/
    }

    @Test
    public void testIsIntersectionVersion(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_F);
        Logger logger = new Logger();

        versionOperatingService.isIntersectionVersion(formTemplate, actualEndVersion, logger);
    }

    //Пересечеиние нет, но есть шаблон FAKE
    @Test
    public void testIsIntersectionVersionNo(){
        FormTemplate formTemplate = formTemplateService.get(FORM_TEMPLATE_ID_F);
        calendar.set(2013, Calendar.JANUARY, 1);
        Logger logger = new Logger();

        versionOperatingService.isIntersectionVersion(formTemplate, calendar.getTime(), logger);
    }

}
