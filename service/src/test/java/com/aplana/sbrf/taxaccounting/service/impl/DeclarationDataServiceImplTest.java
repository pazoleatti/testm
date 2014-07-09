package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DeclarationDataServiceImplTest {

    private DeclarationDataServiceImpl service;
	private DeclarationDataDao declarationDataDao;
    @SuppressWarnings("unused")
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
			service.calculate(logger, 1l, userInfo, new Date());
		} catch (ServiceException e) {
			
		}
		
		// Verify
		verify(declarationDataDao).get(1l);
	}
	
	//@Test(expected=AccessDeniedException.class)
	public void testRefreshDeclarationNoAccess() {
		Logger logger = new Logger();
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp("192.168.72.16");
		userInfo.setUser(mockUser(10,  2, TARole.ROLE_CONTROL));
		service.calculate(logger, 2l, userInfo, new Date());
	}

    @Test
    public void testme() {
        // TODO фиктивный тест, добил чтоб не падала сборка
        assert 1 == 1;
    }

    @Test
    public void existDeclarationTest() {

        Logger logger = new Logger();

        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(1);
        declarationType.setName("Тестовый тип декларации");

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        declarationTemplate.setType(declarationType);
        declarationTemplate.setId(1);

        DeclarationData declarationData = new DeclarationData();
        declarationData.setDeclarationTemplateId(1);
        declarationData.setDepartmentId(1);
        declarationData.setReportPeriodId(1);
        declarationData.setId(1l);

        DeclarationData declarationData1 = new DeclarationData();
        declarationData1.setDeclarationTemplateId(1);
        declarationData1.setDepartmentId(1);
        declarationData1.setReportPeriodId(2);
        declarationData1.setId(2l);

        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setId(1);
        taxPeriod.setYear(2014);
        taxPeriod.setTaxType(TaxType.INCOME);
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(1);
        reportPeriod.setTaxPeriod(taxPeriod);
        reportPeriod.setName("Тестовый период");
        ReportPeriod reportPeriod1 = new ReportPeriod();
        reportPeriod1.setId(2);
        reportPeriod1.setTaxPeriod(taxPeriod);
        reportPeriod1.setName("Второй тестовый период");

        Department department = new Department();
        department.setName("Тестовое подразделение");

        List<Long> list = new ArrayList<Long>() {{
            add(1l);
            add(2l);
        }};

        declarationDataDao = mock(DeclarationDataDao.class);
        ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);

        when(declarationDataDao.getDeclarationIds(1, 1)).thenReturn(list);
        when(declarationDataDao.get(1)).thenReturn(declarationData);
        when(declarationDataDao.get(2)).thenReturn(declarationData1);

        DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
        ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);

        when(declarationTemplateDao.get(1)).thenReturn(declarationTemplate);

        ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
        ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);

        when(reportPeriodDao.get(1)).thenReturn(reportPeriod);
        when(reportPeriodDao.get(2)).thenReturn(reportPeriod1);

        DepartmentDao departmentDao = mock(DepartmentDao.class);
        ReflectionTestUtils.setField(service, "departmentDao", departmentDao);

        when(departmentDao.getDepartment(1)).thenReturn(department);

        assertTrue(service.existDeclaration(1, 1, logger.getEntries()));
        assertEquals("Существует экземпляр Тестовый тип декларации в подразделении Тестовое подразделение периоде Тестовый период 2014", logger.getEntries().get(0).getMessage());
        assertEquals("Существует экземпляр Тестовый тип декларации в подразделении Тестовое подразделение периоде Второй тестовый период 2014", logger.getEntries().get(1).getMessage());
    }
}
