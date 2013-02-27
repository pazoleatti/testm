package com.aplana.sbrf.taxaccounting.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

public class DeclarationDataScriptingServiceImplTest {
	private static DeclarationDataScriptingServiceImpl service;
	
	@BeforeClass
	public static void tearUp() throws IOException {
		service = new DeclarationDataScriptingServiceImpl();
		
		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansWithAnnotation(ScriptExposed.class)).thenReturn(new HashMap<String, Object>());
		service.setApplicationContext(ctx);
		
		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao, DepartmentFormTypeDao.class);
		
		FormDataDao formDataDao = mock(FormDataDao.class);
		ReflectionTestUtils.setField(service, "formDataDao", formDataDao, FormDataDao.class);
		
		DeclarationType declarationType = mock(DeclarationType.class);
		when(declarationType.getId()).thenReturn(1);
		
		// Этот шаблон генерирует демо-XML
		DeclarationTemplate template1 = mock(DeclarationTemplate.class);
		InputStream stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclaration.groovy");
		String createScript = IOUtils.toString(stream, "UTF-8");
		when(template1.getCreateScript()).thenReturn(createScript);
		when(template1.getDeclarationType()).thenReturn(declarationType);
		
		// Этот шаблон содержит ошибку в скрипте
		DeclarationTemplate template2 = mock(DeclarationTemplate.class);
		stream = DeclarationDataScriptingServiceImplTest.class.getResourceAsStream("createDeclarationException.groovy");
		createScript = IOUtils.toString(stream, "UTF-8");
		when(template2.getCreateScript()).thenReturn(createScript);
		when(template2.getDeclarationType()).thenReturn(declarationType);

		DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);		
		when(declarationTemplateDao.get(1)).thenReturn(template1);
		when(declarationTemplateDao.get(2)).thenReturn(template2);
		ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao, DeclarationTemplateDao.class);
	}
	
	@Test
	public void testCreate() throws IOException, SAXException {
		Logger logger = new Logger();
		String xml = service.create(logger, 1, 1, 1);

		String correctXml = IOUtils.toString(getClass().getResourceAsStream("createDeclaration.xml"), "UTF-8");
		XMLUnit.setIgnoreWhitespace(true);
		Diff xmlDiff = new Diff(xml, correctXml);
		assertTrue(xmlDiff.similar());		
		
		assertFalse(logger.getEntries().isEmpty());
		assertFalse(logger.containsLevel(LogLevel.ERROR));
	}
	
	@Test
	public void testCreateWithException() {
		Logger logger = new Logger();
		String xml = service.create(logger, 1, 2, 1);
		
		assertNull(xml);
		assertFalse(logger.getEntries().isEmpty());
		assertTrue(logger.containsLevel(LogLevel.ERROR));
	}
	
}
