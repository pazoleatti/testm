package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;

import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class DeclarationDataServiceImplTest {
	public static final int USER_ID = 1;

	private DeclarationDataServiceImpl service;
	private DeclarationDataDao declarationDataDao;
	
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
		when(declarationDataScriptingService.create(any(Logger.class), any(Integer.class), any(Integer.class), any(Integer.class))).thenReturn("generated-xml");
		ReflectionTestUtils.setField(service, "declarationDataScriptingService", declarationDataScriptingService);
		
		DeclarationDataAccessService declarationDataAccessService = mock(DeclarationDataAccessService.class);
		when(declarationDataAccessService.canRefresh(USER_ID, 1)).thenReturn(true);
		when(declarationDataAccessService.canRefresh(USER_ID, 2)).thenReturn(false);
		ReflectionTestUtils.setField(service, "declarationDataAccessService", declarationDataAccessService);
	}

	@Test
	public void testRefreshDeclaration() {
		Logger logger = new Logger();		
		service.refreshDeclaration(logger, 1l, USER_ID );
		verify(declarationDataDao).get(1l);
		verify(declarationDataDao).setXmlData(1l, "generated-xml");
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testRefreshDeclarationNoAccess() {
		Logger logger = new Logger();
		service.refreshDeclaration(logger, 2l, USER_ID);
	}
	
}
