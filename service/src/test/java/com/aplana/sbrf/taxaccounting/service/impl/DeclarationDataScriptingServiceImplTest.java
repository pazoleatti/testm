package com.aplana.sbrf.taxaccounting.service.impl;

import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

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
		DeclarationTemplate template1 = mock(DeclarationTemplate.class);
		InputStream stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclaration.groovy");
		String createScript = IOUtils.toString(stream, "UTF-8");
		when(template1.getId()).thenReturn(REPORT_TEMPLATE_ID1);
		when(template1.getCreateScript()).thenReturn(createScript);
		when(template1.getDeclarationType()).thenReturn(declarationType);

		// Этот шаблон содержит ошибку в скрипте
		DeclarationTemplate template2 = mock(DeclarationTemplate.class);
		stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclarationException.groovy");
		createScript = IOUtils.toString(stream, "UTF-8");
		when(template2.getId()).thenReturn(REPORT_TEMPLATE_ID2);
		when(template2.getCreateScript()).thenReturn(createScript);
		when(template2.getDeclarationType()).thenReturn(declarationType);

		DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);		
		when(declarationTemplateDao.get(REPORT_TEMPLATE_ID1)).thenReturn(template1);
		when(declarationTemplateDao.get(REPORT_TEMPLATE_ID2)).thenReturn(template2);
		ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao, DeclarationTemplateDao.class);

		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		List<DepartmentFormType> sourcesInfo = new ArrayList<DepartmentFormType>();
		DepartmentFormType dft = new DepartmentFormType();
		dft.setFormTypeId(1);
		dft.setKind(FormDataKind.SUMMARY);
		dft.setDepartmentId(DEPARTMENT_ID);
		sourcesInfo.add(dft);
		when(departmentFormTypeDao.getDeclarationSources(DEPARTMENT_ID, template1.getDeclarationType().getId())).thenReturn(sourcesInfo);
		//ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao, DepartmentFormTypeDao.class);

		FormDataDao formDataDao = mock(FormDataDao.class);
		FormData formData = new FormData();
		formData.setState(WorkflowState.ACCEPTED);
		when(formDataDao.find(1, FormDataKind.SUMMARY, DEPARTMENT_ID, REPORT_PERIOD_ID)).thenReturn(formData);
		//ReflectionTestUtils.setField(service, "formDataDao", formDataDao, FormDataDao.class);
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
	
}
