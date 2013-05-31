package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.test.DeclarationDataMockUtils.mockDeclarationData;
import static com.aplana.sbrf.taxaccounting.test.DeclarationTemplateMockUtils.mockDeclarationTemplate;
import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.ReportPeriodMockUtils.mockReportPeriod;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тест для периода с флагом "Ввод остатков". Все работы с декларациями запрещены
 */
public class DeclarationDataAccessServiceImplBalancePeriodTest {
	private static DeclarationDataAccessServiceImpl service;

	private final static int DEPARTMENT_TB1_ID = 2;
	private final static int DEPARTMENT_TB2_ID = 3;

	private final static int USER_CONTROL_BANK_ID = 10;
	private final static int USER_CONTROL_UNP_ID = 11;
	private final static int USER_CONTROL_TB1_ID = 12;
	private final static int USER_CONTROL_TB2_ID = 13;
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
	
	private boolean canAccept(int userId, int declarationDataId){
		try{
		    service.checkEvents(userId, Long.valueOf(declarationDataId),
				    FormDataEvent.MOVE_CREATED_TO_ACCEPTED);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}
	
	private boolean canReject(int userId, int declarationDataId){
		try{
		    service.checkEvents(userId, Long.valueOf(declarationDataId),
				    FormDataEvent.MOVE_ACCEPTED_TO_CREATED);
		} catch (AccessDeniedException e){
			return false;
		}
		return true;
	}

	@BeforeClass
	public static void tearUp() {
		service = new DeclarationDataAccessServiceImpl();

		TAUser controlBank = mockUser(USER_CONTROL_BANK_ID, Department.ROOT_BANK_ID, TARole.ROLE_CONTROL);
		TAUser controlUnp = mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP);
		TAUser controlTB1 = mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL);
		TAUser controlTB2 = mockUser(USER_CONTROL_TB2_ID, DEPARTMENT_TB2_ID, TARole.ROLE_CONTROL);
		TAUser operator = mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPERATOR);
		TAUserDao userDao = mock(TAUserDao.class);
		when(userDao.getUser(USER_CONTROL_BANK_ID)).thenReturn(controlBank);
		when(userDao.getUser(USER_CONTROL_UNP_ID)).thenReturn(controlUnp);
		when(userDao.getUser(USER_CONTROL_TB1_ID)).thenReturn(controlTB1);
		when(userDao.getUser(USER_CONTROL_TB2_ID)).thenReturn(controlTB2);
		when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(operator);
		ReflectionTestUtils.setField(service, "userDao", userDao);

		// На уровне Банка разрешена работа с декларациями DECLARATION_TYPE_1_ID
		Department departmentBank = mockDepartment(Department.ROOT_BANK_ID, Department.ROOT_BANK_ID, DepartmentType.ROOT_BANK);
		List<DepartmentDeclarationType> bankDeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(Department.ROOT_BANK_ID, DECLARATION_TYPE_1_ID));
		when(departmentBank.getDepartmentDeclarationTypes()).thenReturn(bankDeclarationTypes);
		// В подразделении DEPARTMENT_TB1_ID разрешена работа с декларациями DECLARATION_TYPE_1_ID
		Department departmentTB1 = mockDepartment(DEPARTMENT_TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);
		List<DepartmentDeclarationType> departmentTB1DeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(DEPARTMENT_TB1_ID, DECLARATION_TYPE_1_ID));
		when(departmentTB1.getDepartmentDeclarationTypes()).thenReturn(departmentTB1DeclarationTypes);
		// В подразделении DEPARTMENT_TB2_ID разрешена работа с декларациями DECLARATION_TYPE_2_ID
		Department departmentTB2 = mockDepartment(DEPARTMENT_TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);
		List<DepartmentDeclarationType> departmentTB2DeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(DEPARTMENT_TB2_ID, DECLARATION_TYPE_2_ID));
		when(departmentTB2.getDepartmentDeclarationTypes()).thenReturn(departmentTB2DeclarationTypes);

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

		ReportPeriod reportPeriod = mockReportPeriod(REPORT_PERIOD_ID, true, true);

		ReportPeriodDao reportPeriodDao = mock(ReportPeriodDao.class);
		when(reportPeriodDao.get(REPORT_PERIOD_ID)).thenReturn(reportPeriod);
		ReflectionTestUtils.setField(service, "reportPeriodDao", reportPeriodDao);

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
	}
	
	@Test
	public void testCanRead() {
		// Контролёр УНП может читать в любом подразделении и в любом статусе
		assertFalse(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может читать только в своём обособленном подразделении и в любом статусе
		assertFalse(service.canRead(USER_CONTROL_BANK_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canRead(USER_CONTROL_BANK_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может читать никаких деклараций
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanRefresh() {
		// Контролёр УНП может обновлять непринятые декларации в любом подразделении
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может обновлять только непринятые декларации в своём обособленном подразделении
		assertFalse(service.canRefresh(USER_CONTROL_BANK_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canRefresh(USER_CONTROL_BANK_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может обновлять никаких деклараций
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanAccept() {
		// Контролёр УНП может принимать непринятые декларации в любом подразделении
		assertFalse(canAccept(USER_CONTROL_UNP_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(canAccept(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canAccept(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(canAccept(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canAccept(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(canAccept(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может принимать непринятые декларации только в своём обособленном подразделении
		assertFalse(canAccept(USER_CONTROL_BANK_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(canAccept(USER_CONTROL_BANK_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canAccept(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(canAccept(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canAccept(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(canAccept(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может принимать никаких деклараций
		assertFalse(canAccept(USER_OPERATOR_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(canAccept(USER_OPERATOR_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canAccept(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(canAccept(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canAccept(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(canAccept(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanReject() {
		// Контролёр УНП может отменять принятые декларации в любом подразделении
		assertFalse(canReject(USER_CONTROL_UNP_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(canReject(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canReject(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(canReject(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canReject(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(canReject(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может отменять принятые декларации только в своём обособленном подразделении
		assertFalse(canReject(USER_CONTROL_BANK_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(canReject(USER_CONTROL_BANK_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canReject(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(canReject(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canReject(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(canReject(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может отменять никаких деклараций
		assertFalse(canReject(USER_OPERATOR_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(canReject(USER_OPERATOR_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(canReject(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(canReject(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(canReject(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(canReject(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanDownloadXml() {
		// Контролёр УНП может скачивать файл в формате законодателя у принятых деклараций в любом подразделении
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может скачивать файл в формате законодателя у принятых деклараций только в своём обособленном подразделении
		assertFalse(service.canDownloadXml(USER_CONTROL_BANK_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_BANK_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не скачивать файл в формате законодателя ни у каких деклараций
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanDelete() {
		// Контролёр УНП может удалять непринятые декларации в любом подразделении
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может удалять непринятые декларации только в своём обособленном подразделении
		assertFalse(service.canDelete(USER_CONTROL_BANK_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canDelete(USER_CONTROL_BANK_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может удалять никаких деклараций
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_CREATED_BANK_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_ACCEPTED_BANK_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanCreate() {
		// Контролёр УНП может создавать декларации в любом подразделении, если они там разрешены
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_1_ID, Department.ROOT_BANK_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));
		
		// Контролёр может создавать декларации в своём обособленном подразделении, если они там разрешены
		assertFalse(service.canCreate(USER_CONTROL_BANK_ID, DECLARATION_TEMPLATE_1_ID, Department.ROOT_BANK_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));

		// Оператор не может создавать декларации
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_1_ID, Department.ROOT_BANK_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));
	}
	
}
