package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
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
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(FORM_TEMPLATE_ID_F);
        formTemplate.setCode("code");
        formTemplate.setVersion(calendar.getTime());
        calendar.clear();
        formTemplate.setStatus(VersionedObjectStatus.NORMAL);
        formTemplate.setEdition(1);

        calendar.set(2013, Calendar.JANUARY, 1);
        FormTemplate formTemplateIntersection = new FormTemplate();
        formTemplateIntersection.setId(FORM_TEMPLATE_ID_S);
        formTemplateIntersection.setCode("code");
        formTemplateIntersection.setVersion(calendar.getTime());
        calendar.clear();
        formTemplateIntersection.setStatus(VersionedObjectStatus.FAKE);
        formTemplateIntersection.setEdition(1);

        when(formTemplateService.get(FORM_TEMPLATE_ID_F)).thenReturn(formTemplate);

        List<Integer> listIntersectionIds = new ArrayList<Integer>();
        listIntersectionIds.add(2);
        listIntersectionIds.add(3);

        calendar.set(2012, Calendar.JANUARY, 1);
        actualEndVersion = calendar.getTime();
        when(formTemplateService.findFTVersionIntersections(formTemplate, actualEndVersion)).thenReturn(listIntersectionIds);
        calendar.clear();
        when(formTemplateService.get(listIntersectionIds.get(0))).thenReturn(formTemplateIntersection);

        when(formTemplateService.getNearestFTRight(formTemplateIntersection,
                VersionedObjectStatus.FAKE, VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL)).thenReturn(formTemplate);
        when(formTemplateService.getNearestFTRight(formTemplateIntersection)).thenReturn(formTemplate);
    }

    @Test
    public void testIsIntersectionVersion(){
        FormTemplate formTemplate = formTemplateService.get(1);
        Logger logger = new Logger();

        versionOperatingService.isIntersectionVersion(formTemplate, actualEndVersion, logger);
    }
}
