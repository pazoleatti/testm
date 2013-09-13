package com.aplana.sbrf.taxaccounting.service.impl;

import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;
import static com.aplana.sbrf.taxaccounting.test.DeclarationTemplateMockUtils.mockDeclarationTemplate;
import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static com.aplana.sbrf.taxaccounting.test.ReportPeriodMockUtils.mockReportPeriod;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.SourceService;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;

public class DeclarationDataAccessServiceImplTest {
	private static DeclarationDataAccessServiceImpl service;

	private final static int DEPARTMENT_TB1_ID = 2;
	private final static int DEPARTMENT_TB2_ID = 3;

	private final static int USER_CONTROL_BANK_ID = 10;
	private final static int USER_CONTROL_UNP_ID = 11;
	private final static int USER_CONTROL_TB1_ID = 12;
	private final static int USER_OPERATOR_ID = 14;

	private final static int DECLARATION_TYPE_1_ID = 101;
	private final static int DECLARATION_TYPE_2_ID = 102;
	
	private final static int DECLARATION_TEMPLATE_1_ID = 111;
	private final static int DECLARATION_TEMPLATE_2_ID = 112;

	private final static int DECLARATION_CREATED_BANK_ID = 119;
	private final static int DECLARATION_ACCEPTED_BANK_ID = 120;
	private final static int DECLARATION_CREATED_TB1_ID = 121;
	private final static int DECLARATION_ACCEPTED_TB1_ID = 122;
	private final static int DECLARATION_CREATED_TB2_ID = 123;
	private final static int DECLARATION_ACCEPTED_TB2_ID = 124;

	private final static int REPORT_PERIOD_ID = 1;

	private final static String LOCAL_IP = "127.0.0.1";

	private boolean canAccept(TAUserInfo userInfo, int declarationDataId){
		try{
		    service.checkEvents(userInfo, Long.valueOf(declarationDataId),
				    FormDataEvent.MOVE_CREATED_TO_ACCEPTED);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canReject(TAUserInfo userInfo, int declarationDataId){
		try{
		    service.checkEvents(userInfo, Long.valueOf(declarationDataId),
				    FormDataEvent.MOVE_ACCEPTED_TO_CREATED);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canGet(TAUserInfo userInfo, int declarationDataId){
		try{
		    service.checkEvents(userInfo, Long.valueOf(declarationDataId),
				    FormDataEvent.GET_LEVEL0);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canGetXml(TAUserInfo userInfo, int declarationDataId){
		try{
		    service.checkEvents(userInfo, Long.valueOf(declarationDataId),
				    FormDataEvent.GET_LEVEL1);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canDelete(TAUserInfo userInfo, int declarationDataId){
		try{
		    service.checkEvents(userInfo, Long.valueOf(declarationDataId),
				    FormDataEvent.DELETE);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canRefresh(TAUserInfo userInfo, int declarationDataId){
		try{
		    service.checkEvents(userInfo, Long.valueOf(declarationDataId),
				    FormDataEvent.CALCULATE);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canCreate(TAUserInfo userInfo, int declarationTemplateId,
			int departmentId, int reportPeriodId) {
		return service.getPermittedEvents(userInfo, declarationTemplateId, departmentId, reportPeriodId).contains(FormDataEvent.CREATE);
	 }
	

	@BeforeClass
	public static void tearUp() {
		service = new DeclarationDataAccessServiceImpl();

		// На уровне Банка разрешена работа с декларациями DECLARATION_TYPE_1_ID
		Department departmentBank = mockDepartment(Department.ROOT_BANK_ID, Department.ROOT_BANK_ID, DepartmentType.ROOT_BANK);

		// В подразделении DEPARTMENT_TB1_ID разрешена работа с декларациями DECLARATION_TYPE_1_ID
		Department departmentTB1 = mockDepartment(DEPARTMENT_TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);

		// В подразделении DEPARTMENT_TB2_ID разрешена работа с декларациями DECLARATION_TYPE_2_ID
		Department departmentTB2 = mockDepartment(DEPARTMENT_TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);


		DepartmentDao departmentDao = mock(DepartmentDao.class);
		when(departmentDao.getDepartment(Department.ROOT_BANK_ID)).thenReturn(departmentBank);
		when(departmentDao.getDepartment(DEPARTMENT_TB1_ID)).thenReturn(departmentTB1);
		when(departmentDao.getDepartment(DEPARTMENT_TB2_ID)).thenReturn(departmentTB2);
		ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
		
		DeclarationTemplate declarationTemplate1 = mockDeclarationTemplate(DECLARATION_TEMPLATE_1_ID, DECLARATION_TYPE_1_ID);
		DeclarationTemplate declarationTemplate2 = mockDeclarationTemplate(DECLARATION_TEMPLATE_2_ID, DECLARATION_TYPE_2_ID);
		DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
		when(declarationTemplateDao.get(DECLARATION_TEMPLATE_1_ID)).thenReturn(declarationTemplate1);
		when(declarationTemplateDao.get(DECLARATION_TEMPLATE_2_ID)).thenReturn(declarationTemplate2);
		ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);

		ReportPeriod reportPeriod = mockReportPeriod(REPORT_PERIOD_ID);

		DeclarationData declarationCreatedBank = mockDeclarationData(DECLARATION_CREATED_BANK_ID, Department.ROOT_BANK_ID, false, DECLARATION_TEMPLATE_1_ID, REPORT_PERIOD_ID);
		DeclarationData declarationAcceptedBank = mockDeclarationData(DECLARATION_ACCEPTED_BANK_ID, Department.ROOT_BANK_ID, true, DECLARATION_TEMPLATE_1_ID, REPORT_PERIOD_ID);
		DeclarationData declarationCreatedTB1 = mockDeclarationData(DECLARATION_CREATED_TB1_ID, DEPARTMENT_TB1_ID, false, DECLARATION_TEMPLATE_1_ID, REPORT_PERIOD_ID);
		DeclarationData declarationAcceptedTB1 = mockDeclarationData(DECLARATION_ACCEPTED_TB1_ID, DEPARTMENT_TB1_ID, true, DECLARATION_TEMPLATE_1_ID, REPORT_PERIOD_ID);
		DeclarationData declarationCreatedTB2 = mockDeclarationData(DECLARATION_CREATED_TB2_ID, DEPARTMENT_TB2_ID, false, DECLARATION_TEMPLATE_2_ID, REPORT_PERIOD_ID);
		DeclarationData declarationAcceptedTB2 = mockDeclarationData(DECLARATION_ACCEPTED_TB2_ID, DEPARTMENT_TB2_ID, true, DECLARATION_TEMPLATE_2_ID, REPORT_PERIOD_ID);
		DeclarationDataDao declarationDataDao = mock(DeclarationDataDao.class);
		when(declarationDataDao.get(DECLARATION_CREATED_BANK_ID)).thenReturn(declarationCreatedBank);
		when(declarationDataDao.get(DECLARATION_ACCEPTED_BANK_ID)).thenReturn(declarationAcceptedBank);
		when(declarationDataDao.get(DECLARATION_CREATED_TB1_ID)).thenReturn(declarationCreatedTB1);
		when(declarationDataDao.get(DECLARATION_ACCEPTED_TB1_ID)).thenReturn(declarationAcceptedTB1);
		when(declarationDataDao.get(DECLARATION_CREATED_TB2_ID)).thenReturn(declarationCreatedTB2);
		when(declarationDataDao.get(DECLARATION_ACCEPTED_TB2_ID)).thenReturn(declarationAcceptedTB2);		
		ReflectionTestUtils.setField(service, "declarationDataDao", declarationDataDao);

		PeriodService reportPeriodService = mock(PeriodService.class);
		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_ID, DEPARTMENT_TB1_ID)).thenReturn(true);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_ID, DEPARTMENT_TB1_ID)).thenReturn(false);

		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_ID, DEPARTMENT_TB2_ID)).thenReturn(true);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_ID, DEPARTMENT_TB2_ID)).thenReturn(false);

		when(reportPeriodService.isActivePeriod(REPORT_PERIOD_ID, Department.ROOT_BANK_ID)).thenReturn(true);
		when(reportPeriodService.isBalancePeriod(REPORT_PERIOD_ID, Department.ROOT_BANK_ID)).thenReturn(false);
		ReflectionTestUtils.setField(service, "reportPeriodService", reportPeriodService);
		
		SourceService sourceService = mock(SourceService.class);
		List<DepartmentDeclarationType> bankDeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(Department.ROOT_BANK_ID, DECLARATION_TYPE_1_ID));
		when(sourceService.getDDTByDepartment(Matchers.eq(Department.ROOT_BANK_ID), Matchers.any(TaxType.class))).thenReturn(bankDeclarationTypes);
		
		List<DepartmentDeclarationType> departmentTB1DeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(DEPARTMENT_TB1_ID, DECLARATION_TYPE_1_ID));
		when(sourceService.getDDTByDepartment(Matchers.eq(DEPARTMENT_TB1_ID), Matchers.any(TaxType.class))).thenReturn(departmentTB1DeclarationTypes);
		
		List<DepartmentDeclarationType> departmentTB2DeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(DEPARTMENT_TB2_ID, DECLARATION_TYPE_2_ID));
		when(sourceService.getDDTByDepartment(Matchers.eq(DEPARTMENT_TB2_ID), Matchers.any(TaxType.class))).thenReturn(departmentTB2DeclarationTypes);
		ReflectionTestUtils.setField(service, "sourceService", sourceService);
	}

	@Test
	public void testCanRead() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может читать в любом подразделении и в любом статусе
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(canGet(userInfo, DECLARATION_CREATED_BANK_ID));
		assertTrue(canGet(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertTrue(canGet(userInfo, DECLARATION_CREATED_TB1_ID));
		assertTrue(canGet(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(canGet(userInfo, DECLARATION_CREATED_TB2_ID));
		assertTrue(canGet(userInfo, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может читать только в своём обособленном подразделении и в любом статусе
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canGet(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canGet(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(canGet(userInfo, DECLARATION_CREATED_TB1_ID));
		assertTrue(canGet(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canGet(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canGet(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может читать никаких деклараций
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canGet(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canGet(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canGet(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canGet(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canGet(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canGet(userInfo, DECLARATION_ACCEPTED_TB2_ID));
	}

	@Test
	public void testCanRefresh() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может обновлять непринятые декларации в любом подразделении
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(canRefresh(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertTrue(canRefresh(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(canRefresh(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может обновлять только непринятые декларации в своём обособленном подразделении
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canRefresh(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(canRefresh(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может обновлять никаких деклараций
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canRefresh(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canRefresh(userInfo, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanAccept() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может принимать непринятые декларации в любом подразделении
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(canAccept(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertTrue(canAccept(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(canAccept(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Контролёр может принимать непринятые декларации только в своём обособленном подразделении
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canAccept(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(canAccept(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canAccept(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может принимать никаких деклараций
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canAccept(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canAccept(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canAccept(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canAccept(userInfo, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanReject() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может отменять принятые декларации в любом подразделении
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_BANK_ID));
		assertTrue(canReject(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_TB1_ID));
		assertTrue(canReject(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_TB2_ID));
		assertTrue(canReject(userInfo, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может отменять принятые декларации только в своём обособленном подразделении
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canReject(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_TB1_ID));
		assertTrue(canReject(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canReject(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может отменять никаких деклараций
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canReject(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canReject(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canReject(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canReject(userInfo, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanDownloadXml() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может скачивать файл в формате законодателя у принятых деклараций в любом подразделении
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_BANK_ID));
		assertTrue(canGetXml(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(!service.getPermittedEvents(userInfo, Long.valueOf(DECLARATION_ACCEPTED_TB1_ID)).contains(FormDataEvent.GET_LEVEL1));
		assertTrue(service.getPermittedEvents(userInfo, Long.valueOf(DECLARATION_ACCEPTED_TB1_ID)).contains(FormDataEvent.GET_LEVEL1));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_TB2_ID));
		assertTrue(canGetXml(userInfo, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может скачивать файл в формате законодателя у принятых деклараций только в своём обособленном подразделении
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_TB1_ID));
		assertTrue(canGetXml(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не скачивать файл в формате законодателя ни у каких деклараций
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canGetXml(userInfo, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanDelete() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может удалять непринятые декларации в любом подразделении
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(canDelete(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertTrue(canDelete(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(canDelete(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Контролёр может удалять непринятые декларации только в своём обособленном подразделении
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canDelete(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(canDelete(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canDelete(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может удалять никаких деклараций
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canDelete(userInfo, DECLARATION_CREATED_BANK_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canDelete(userInfo, DECLARATION_CREATED_TB1_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canDelete(userInfo, DECLARATION_CREATED_TB2_ID));
		assertFalse(canDelete(userInfo, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanCreate() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);

		// Контролёр УНП может создавать декларации в любом подразделении, если они там разрешены
		userInfo.setUser(mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP));
		assertTrue(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, Department.ROOT_BANK_ID, 1));
		assertTrue(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertTrue(canCreate(userInfo, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));
		
		// Контролёр может создавать декларации в своём обособленном подразделении, если они там разрешены
		userInfo.setUser(mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, Department.ROOT_BANK_ID, 1));
		userInfo.setUser(mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL));
		assertTrue(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));

		// Оператор не может создавать декларации
		userInfo.setUser(mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPER));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, Department.ROOT_BANK_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(canCreate(userInfo, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));

	}

}
