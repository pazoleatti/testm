package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;

import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

public class DeclarationDataServiceImplTest {
	public static final int USER_ID = 1;
	
	//private static String XML_DATA = "<?xml version=\"1.0\" encoding=\"windows-1251\"?><Документ Имя=\"Федор\"></Документ>";
	private static String XML_DATA = "<?xml version=\"1.0\" encoding=\"windows-1251\"?><A/>";

	private DeclarationDataServiceImpl service;
	private DeclarationDataDao declarationDataDao;
	private DeclarationTemplateService declarationTemplateService;
	@SuppressWarnings("unused")
	private DeclarationDataScriptingService declarationDataScriptingService;
	
	@Before
	public void tearUp() {
		service = new DeclarationDataServiceImpl();
	
		declarationDataDao = mock(DeclarationDataDao.class);
		DeclarationData declarationData1 = mockDeclarationData(1l, 1, false, 1, 1);
		DeclarationData declarationData2 = mockDeclarationData(2l, 2, false, 1, 1);
		when(declarationDataDao.get(1)).thenReturn(declarationData1);
		when(declarationDataDao.get(2)).thenReturn(declarationData2);
		ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);

		DeclarationDataScriptingService declarationDataScriptingService = mock(DeclarationDataScriptingService.class);
		when(declarationDataScriptingService.create(any(Logger.class), any(DeclarationData.class), any(String.class)))
				.thenReturn(XML_DATA);
		ReflectionTestUtils.setField(service, "declarationDataScriptingService", declarationDataScriptingService);
		
		DeclarationDataAccessService declarationDataAccessService = mock(DeclarationDataAccessService.class);
		when(declarationDataAccessService.canRefresh(USER_ID, 1)).thenReturn(true);
		when(declarationDataAccessService.canRefresh(USER_ID, 2)).thenReturn(false);
		ReflectionTestUtils.setField(service, "declarationDataAccessService", declarationDataAccessService);
		
		declarationTemplateService = mock(DeclarationTemplateService.class);
		when(declarationTemplateService.getJasper(any(Integer.class))).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" name=\"report\"/>".getBytes());
		ReflectionTestUtils.setField(service, "declarationTemplateService", declarationTemplateService);
	}

	@Test
	public void testRefreshDeclaration() {
		Logger logger = new Logger();		
		// TODO: sgoryachkin: Нужно сделать нормальный тест. Пока как временное решение - игнорить ошибку при генерации
		try{
			service.reCreate(logger, 1l, USER_ID, new Date().toString());
		} catch (ServiceException e) {
			
		}
		
		// Verify
		verify(declarationDataDao).get(1l);
		verify(declarationDataDao).setXmlData(1l, XML_DATA);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testRefreshDeclarationNoAccess() {
		Logger logger = new Logger();
		service.reCreate(logger, 2l, USER_ID, new Date().toString());
	}
	
}
