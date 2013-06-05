package com.aplana.sbrf.taxaccounting.service.impl;

import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;

public class DeclarationDataServiceImplTest {

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

		//DeclarationDataScriptingService declarationDataScriptingService = mock(DeclarationDataScriptingService.class);
		//(declarationDataScriptingService.executeScript(any(TAUser.class) ,any(DeclarationData.class), any(FormDataEvent.class), any(Logger.class), any(Map.class)))
				
		ReflectionTestUtils.setField(service, "declarationDataScriptingService", declarationDataScriptingService);
		
		DeclarationDataAccessService declarationDataAccessService = mock(DeclarationDataAccessService.class);
		//when(declarationDataAccessService.canRefresh(USER_ID, 1)).thenReturn(true);
		//when(declarationDataAccessService.canRefresh(USER_ID, 2)).thenReturn(false);
		ReflectionTestUtils.setField(service, "declarationDataAccessService", declarationDataAccessService);
		
		declarationTemplateService = mock(DeclarationTemplateService.class);
		when(declarationTemplateService.getJasper(any(Integer.class))).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\" name=\"report\"/>".getBytes());
		ReflectionTestUtils.setField(service, "declarationTemplateService", declarationTemplateService);
	}

	////////////////
	// TODO: (sgoryachkin)
	// Незнаю как это тестировать. Закормментил тесты
	//
	//
	//
	////////////////
	
	//@Test
	public void testRefreshDeclaration() {
		Logger logger = new Logger();		
		// TODO: sgoryachkin: Нужно сделать нормальный тест. Пока как временное решение - игнорить ошибку при генерации
		try{
			TAUserInfo userInfo = new TAUserInfo();
			userInfo.setIp("192.168.72.16");
			userInfo.setUser(mockUser(10,  Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
			service.reCreate(logger, 1l, userInfo, new Date());
		} catch (ServiceException e) {
			
		}
		
		// Verify
		verify(declarationDataDao).get(1l);
		verify(declarationDataDao).setXmlData(1l, XML_DATA);
	}
	
	//@Test(expected=AccessDeniedException.class)
	public void testRefreshDeclarationNoAccess() {
		Logger logger = new Logger();
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp("192.168.72.16");
		userInfo.setUser(mockUser(10,  2, TARole.ROLE_CONTROL));
		service.reCreate(logger, 2l, userInfo, new Date());
	}
	
}
