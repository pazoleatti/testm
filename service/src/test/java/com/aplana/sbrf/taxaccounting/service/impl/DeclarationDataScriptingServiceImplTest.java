package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeclarationDataScriptingServiceImplTest {
	private static DeclarationDataScriptingServiceImpl service;

	private static final int DEPARTMENT_ID = 1;
	private static final int REPORT_PERIOD_ID = 101;

	private static final int REPORT_TEMPLATE_ID1 = 51;
	private static final int REPORT_TEMPLATE_ID2 = 52;

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>";

	@BeforeClass
	public static void tearUp() throws IOException {
		service = new DeclarationDataScriptingServiceImpl();

		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansWithAnnotation(ScriptExposed.class)).thenReturn(new HashMap<String, Object>());
		service.setApplicationContext(ctx);

		DeclarationType declarationType = mock(DeclarationType.class);
		when(declarationType.getId()).thenReturn(1);

		// Этот шаблон генерирует демо-XML
		DeclarationTemplate template1 = new DeclarationTemplate();
		InputStream stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclaration.groovy");
		String createScript1 = IOUtils.toString(stream, "UTF-8");
        template1.setId(REPORT_TEMPLATE_ID1);
        template1.setType(declarationType);

		// Этот шаблон содержит ошибку в скрипте
		DeclarationTemplate template2 = new DeclarationTemplate();
		stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclarationException.groovy");
        String createScript2 = IOUtils.toString(stream, "UTF-8");
        template2.setId(REPORT_TEMPLATE_ID2);
        template2.setType(declarationType);

		DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
		when(declarationTemplateDao.get(REPORT_TEMPLATE_ID1)).thenReturn(template1);
		when(declarationTemplateDao.get(REPORT_TEMPLATE_ID2)).thenReturn(template2);
        when(declarationTemplateDao.getDeclarationTemplateScript(REPORT_TEMPLATE_ID1)).thenReturn(createScript1);
        when(declarationTemplateDao.getDeclarationTemplateScript(REPORT_TEMPLATE_ID2)).thenReturn(createScript2);
		ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao, DeclarationTemplateDao.class);

		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		List<DepartmentFormType> sourcesInfo = new ArrayList<DepartmentFormType>();
		DepartmentFormType dft = new DepartmentFormType();
		dft.setFormTypeId(1);
		dft.setKind(FormDataKind.SUMMARY);
		dft.setDepartmentId(DEPARTMENT_ID);
		sourcesInfo.add(dft);
		when(departmentFormTypeDao.getDeclarationSources(any(Integer.class), any(Integer.class), any(Date.class), any(Date.class))).thenReturn(sourcesInfo);
		//ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao, DepartmentFormTypeDao.class);

		FormDataDao formDataDao = mock(FormDataDao.class);
		FormData formData = new FormData();
		formData.setState(WorkflowState.ACCEPTED);
		when(formDataDao.find(1, FormDataKind.SUMMARY, DEPARTMENT_ID, REPORT_PERIOD_ID, null, false)).thenReturn(formData);
		//ReflectionTestUtils.setField(service, "formDataDao", formDataDao, FormDataDao.class);

        LogEntryService logEntryService = mock(LogEntryService.class);
        ReflectionTestUtils.setField(service, "logEntryService", logEntryService);

        TransactionHelper tx = new TransactionHelper() {
            @Override
            public <T> T executeInNewTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }

            @Override
            public <T> T executeInNewReadOnlyTransaction(TransactionLogic<T> logic) {
                return logic.execute();
            }
        };
        ReflectionTestUtils.setField(service, "tx", tx);
	}

	@Test
	public void executeScriptSuccess() throws IOException, SAXException {
		Logger logger = new Logger();
		DeclarationData declarationData = mockDeclarationData(1l, DEPARTMENT_ID, false, REPORT_TEMPLATE_ID1, REPORT_PERIOD_ID);
		//String xml = service.create(logger, declarationData, new Date());

		Map<String, Object> exchangeParams = new HashMap<String, Object>();
		exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, new Date());
		StringWriter writer = new StringWriter();
		exchangeParams.put(DeclarationDataScriptParams.XML, writer);

		service.executeScript(null, declarationData, FormDataEvent.CREATE, logger, exchangeParams);
		
		String xml = XML_HEADER.concat(writer.toString());

		String correctXml = IOUtils.toString(getClass().getResourceAsStream("createDeclaration.xml"), "UTF-8");
		XMLUnit.setIgnoreWhitespace(true);
		Diff xmlDiff = new Diff(xml, correctXml);
		assertTrue(xmlDiff.similar());		
		
		assertFalse(logger.getEntries().isEmpty());
		assertFalse(logger.containsLevel(LogLevel.ERROR));
	}
	
	@Test(expected = ServiceLoggerException.class)
	public void executeScriptError() {
		Logger logger = new Logger();
		DeclarationData declarationData = mockDeclarationData(1l, DEPARTMENT_ID, false, REPORT_TEMPLATE_ID2, REPORT_PERIOD_ID);
		
		Map<String, Object> exchangeParams = new HashMap<String, Object>();
		exchangeParams.put(DeclarationDataScriptParams.DOC_DATE, new Date());
		StringWriter writer = new StringWriter();
		exchangeParams.put(DeclarationDataScriptParams.XML, writer);

		service.executeScript(null, declarationData, FormDataEvent.CREATE, logger, exchangeParams);
	}

    @Test(expected = ServiceLoggerException.class)
    public void checkScript1() throws IOException {
        Logger logger = new Logger();

        DeclarationType declarationType = mock(DeclarationType.class);
        // Шаблон содержит синтаксическую ошибку в скрипте
        DeclarationTemplate template = new DeclarationTemplate();
        InputStream stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("checkDeclarationException.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        template.setType(declarationType);
        template.setCreateScript(script);

        service.executeScriptInNewReadOnlyTransaction(null, template, null, FormDataEvent.CHECK_SCRIPT, logger, null);
    }

    @Test
    public void checkScript2() throws IOException {
        Logger logger = new Logger();

        DeclarationType declarationType = mock(DeclarationType.class);
        // Шаблон содержит логическую ошибку в скрипте
        DeclarationTemplate template = new DeclarationTemplate();
        InputStream stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclarationException.groovy");
        String script = IOUtils.toString(stream, "UTF-8");
        template.setType(declarationType);
        template.setCreateScript(script);

        service.executeScriptInNewReadOnlyTransaction(null, template, null, FormDataEvent.CHECK_SCRIPT, logger, null);
    }
}
