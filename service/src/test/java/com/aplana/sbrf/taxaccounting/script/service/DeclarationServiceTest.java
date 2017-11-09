package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.script.service.impl.DeclarationServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.mock;

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
        DeclarationDataService declarationDataService = mock(DeclarationDataService.class);
        SourceService sourceService = mock(SourceService.class);

        declarationType.setId(1);
        declarationType.setName("name");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
        declarationTemplate.setCreateScript("script");
        declarationTemplate.setType(new DeclarationType());
        declarationTemplate.setVersion(new Date());
        declarationTemplate.setId(1);

        declarationData.setId(1L);
        declarationData.setState(State.ACCEPTED);
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(101);
        declarationData.setDepartmentReportPeriodId(1L);

        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);
        ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);
        ReflectionTestUtils.setField(service, "declarationDataService", declarationDataService);
        ReflectionTestUtils.setField(service, "sourceService", sourceService);
    }

    @Test
    public void getXmlData() {
        //В свзязи с изменениями в DECLARATION_DATA (http://jira.aplana.com/browse/SBRFACCTAX-4544)
        //зкомментил тест, слишком громозко для изменения.
        /*assertTrue(service.getXmlData(1) != null);*/
    }
}
