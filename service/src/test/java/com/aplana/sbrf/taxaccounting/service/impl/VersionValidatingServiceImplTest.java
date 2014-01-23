package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.SegmentIntersection;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.VersionOperatingService;
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


    @Before
    public void init(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);
        FormTemplate formTemplate1 = new FormTemplate();
        formTemplate1.setId(FORM_TEMPLATE_ID_F);
        formTemplate1.setCode("code");
        formTemplate1.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate1.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate1.setEdition(1);

        calendar.set(2013, Calendar.JANUARY, 1);
        FormTemplate formTemplate2 = new FormTemplate();
        formTemplate2.setId(FORM_TEMPLATE_ID_S);
        formTemplate2.setCode("code");
        formTemplate2.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate2.setStatus(VersionedObjectStatus.FAKE);
        formTemplate2.setEdition(1);

        when(formTemplateService.get(FORM_TEMPLATE_ID_F)).thenReturn(formTemplate1);

        //Настройка пересечений
        List<SegmentIntersection> segmentIntersections = new ArrayList<SegmentIntersection>();
        calendar.set(2012, Calendar.JANUARY, 1);
        SegmentIntersection segmentIntersection1 = new SegmentIntersection();
        segmentIntersection1.setTemplateId(FORM_TEMPLATE_ID_F);
        segmentIntersection1.setBeginDate(calendar.getTime());
        segmentIntersection1.setStatus(VersionedObjectStatus.NORMAL);
        calendar.clear();
        SegmentIntersection segmentIntersection2 = new SegmentIntersection();
        calendar.set(2013, Calendar.JANUARY, 1);
        segmentIntersection2.setTemplateId(FORM_TEMPLATE_ID_S);
        segmentIntersection2.setBeginDate(calendar.getTime());
        segmentIntersection2.setStatus(VersionedObjectStatus.FAKE);
        calendar.clear();
        segmentIntersections.add(segmentIntersection1);
        segmentIntersections.add(segmentIntersection2);

        calendar.set(2013, Calendar.FEBRUARY, 1);
        actualEndVersion = calendar.getTime();
        when(formTemplateService.findFTVersionIntersections(formTemplate1, actualEndVersion)).thenReturn(segmentIntersections);
        calendar.clear();
        when(formTemplateService.get(FORM_TEMPLATE_ID_F)).thenReturn(formTemplate1);
        when(formTemplateService.get(FORM_TEMPLATE_ID_S)).thenReturn(formTemplate2);

        /*when(formTemplateService.getNearestFTRight(formTemplate2,
                VersionedObjectStatus.FAKE, VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL)).thenReturn(formTemplate1);
        when(formTemplateService.getNearestFTRight(formTemplate2)).thenReturn(formTemplate1);*/
    }

    @Test
    public void testIsIntersectionVersion(){
        FormTemplate formTemplate = formTemplateService.get(1);
        Logger logger = new Logger();

        versionOperatingService.isIntersectionVersion(formTemplate, actualEndVersion, logger);
    }
}
