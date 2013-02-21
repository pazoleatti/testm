package com.aplana.sbrf.taxaccounting.service.impl;

import static com.aplana.sbrf.taxaccounting.test.DeclarationMockUtils.mockDeclaration;
import static com.aplana.sbrf.taxaccounting.test.DeclarationTemplateMockUtils.mockDeclarationTemplate;
import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;

public class DeclarationAccessServiceImplTest {
	private static DeclarationAccessServiceImpl service;
	
	private final static int DEPARTMENT_TB1_ID = 1;
	private final static int DEPARTMENT_TB2_ID = 2;
	
	private final static int USER_CONTROL_UNP_ID = 11;
	private final static int USER_CONTROL_TB1_ID = 12;
	private final static int USER_CONTROL_TB2_ID = 13;
	private final static int USER_OPERATOR_ID = 14;

	private final static int DECLARATION_TYPE_1_ID = 101;
	private final static int DECLARATION_TYPE_2_ID = 102;
	
	private final static int DECLARATION_TEMPLATE_1_ID = 111;
	private final static int DECLARATION_TEMPLATE_2_ID = 112;
	
	private final static int DECLARATION_CREATED_TB1_ID = 121;
	private final static int DECLARATION_ACCEPTED_TB1_ID = 122;
	private final static int DECLARATION_CREATED_TB2_ID = 123;
	private final static int DECLARATION_ACCEPTED_TB2_ID = 124;
	
	@BeforeClass
	public static void tearUp() {
		service = new DeclarationAccessServiceImpl();
		
		TAUser controlUnp = mockUser(USER_CONTROL_UNP_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL_UNP);
		TAUser controlTB1 = mockUser(USER_CONTROL_TB1_ID, DEPARTMENT_TB1_ID, TARole.ROLE_CONTROL);
		TAUser controlTB2 = mockUser(USER_CONTROL_TB2_ID, DEPARTMENT_TB2_ID, TARole.ROLE_CONTROL);
		TAUser operator = mockUser(USER_OPERATOR_ID, DEPARTMENT_TB1_ID, TARole.ROLE_OPERATOR);
		TAUserDao userDao = mock(TAUserDao.class);
		when(userDao.getUser(USER_CONTROL_UNP_ID)).thenReturn(controlUnp);
		when(userDao.getUser(USER_CONTROL_TB1_ID)).thenReturn(controlTB1);
		when(userDao.getUser(USER_CONTROL_TB2_ID)).thenReturn(controlTB2);
		when(userDao.getUser(USER_OPERATOR_ID)).thenReturn(operator);
		ReflectionTestUtils.setField(service, "userDao", userDao);
		
		
		Department departmentTB1 = mockDepartment(DEPARTMENT_TB1_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);
		// В подразделении DEPARTMENT_TB1_ID разрешена работа с декларациями DECLARATION_TYPE_1_ID
		List<DepartmentDeclarationType> departmentTB1DeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(DEPARTMENT_TB1_ID, DECLARATION_TYPE_1_ID)); 
		when(departmentTB1.getDepartmentDeclarationTypes()).thenReturn(departmentTB1DeclarationTypes);
		// В подразделении DEPARTMENT_TB2_ID разрешена работа с декларациями DECLARATION_TYPE_2_ID
		Department departmentTB2 = mockDepartment(DEPARTMENT_TB2_ID, Department.ROOT_BANK_ID, DepartmentType.TERBANK);
		List<DepartmentDeclarationType> departmentTB2DeclarationTypes = Collections.singletonList(mockDepartmentDeclarationType(DEPARTMENT_TB2_ID, DECLARATION_TYPE_2_ID));
		when(departmentTB2.getDepartmentDeclarationTypes()).thenReturn(departmentTB2DeclarationTypes);
		DepartmentDao departmentDao = mock(DepartmentDao.class);
		when(departmentDao.getDepartment(DEPARTMENT_TB1_ID)).thenReturn(departmentTB1);
		when(departmentDao.getDepartment(DEPARTMENT_TB2_ID)).thenReturn(departmentTB2);
		ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
		
		DeclarationTemplate declarationTemplate1 = mockDeclarationTemplate(DECLARATION_TEMPLATE_1_ID, DECLARATION_TYPE_1_ID);
		DeclarationTemplate declarationTemplate2 = mockDeclarationTemplate(DECLARATION_TEMPLATE_2_ID, DECLARATION_TYPE_2_ID);
		DeclarationTemplateDao declarationTemplateDao = mock(DeclarationTemplateDao.class);
		when(declarationTemplateDao.get(DECLARATION_TEMPLATE_1_ID)).thenReturn(declarationTemplate1);
		when(declarationTemplateDao.get(DECLARATION_TEMPLATE_2_ID)).thenReturn(declarationTemplate2);
		ReflectionTestUtils.setField(service, "declarationTemplateDao", declarationTemplateDao);
		
		Declaration declarationCreatedTB1 = mockDeclaration(DECLARATION_CREATED_TB1_ID, DEPARTMENT_TB1_ID, false, DECLARATION_TEMPLATE_1_ID);
		Declaration declarationAcceptedTB1 = mockDeclaration(DECLARATION_ACCEPTED_TB1_ID, DEPARTMENT_TB1_ID, true, DECLARATION_TEMPLATE_1_ID);
		Declaration declarationCreatedTB2 = mockDeclaration(DECLARATION_CREATED_TB2_ID, DEPARTMENT_TB2_ID, false, DECLARATION_TEMPLATE_2_ID);
		Declaration declarationAcceptedTB2 = mockDeclaration(DECLARATION_ACCEPTED_TB2_ID, DEPARTMENT_TB2_ID, true, DECLARATION_TEMPLATE_2_ID);
		DeclarationDao declarationDao = mock(DeclarationDao.class);
		when(declarationDao.get(DECLARATION_CREATED_TB1_ID)).thenReturn(declarationCreatedTB1);
		when(declarationDao.get(DECLARATION_ACCEPTED_TB1_ID)).thenReturn(declarationAcceptedTB1);
		when(declarationDao.get(DECLARATION_CREATED_TB2_ID)).thenReturn(declarationCreatedTB2);
		when(declarationDao.get(DECLARATION_ACCEPTED_TB2_ID)).thenReturn(declarationAcceptedTB2);		
		ReflectionTestUtils.setField(service, "declarationDao", declarationDao);
	}
	
	@Test
	public void testCanRead() {
		// Контролёр УНП может читать в любом подразделении и в любом статусе
		assertTrue(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertTrue(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertTrue(service.canRead(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может читать только в своём подразделении и в любом статусе
		assertTrue(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertTrue(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRead(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может читать никаких деклараций
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRead(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanRefresh() {
		// Контролёр УНП может обновлять непринятые декларации в любом подразделении
		assertTrue(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRefresh(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может обновлять только непринятые декларации в своём подразделении
		assertTrue(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRefresh(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может обновлять никаких деклараций
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canRefresh(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanAccept() {
		// Контролёр УНП может принимать непринятые декларации в любом подразделении
		assertTrue(service.canAccept(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canAccept(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(service.canAccept(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canAccept(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может принимать непринятые декларации только в своём подразделении
		assertTrue(service.canAccept(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canAccept(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canAccept(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canAccept(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может принимать никаких деклараций
		assertFalse(service.canAccept(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canAccept(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canAccept(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canAccept(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanReject() {
		// Контролёр УНП может отменять принятые декларации в любом подразделении
		assertFalse(service.canReject(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertTrue(service.canReject(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canReject(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertTrue(service.canReject(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может отменять принятые декларации только в своём подразделении
		assertFalse(service.canReject(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertTrue(service.canReject(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canReject(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canReject(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может отменять никаких деклараций
		assertFalse(service.canReject(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canReject(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canReject(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canReject(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanDownloadXml() {
		// Контролёр УНП может скачивать файл в формате законодателя у принятых деклараций в любом подразделении
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertTrue(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertTrue(service.canDownloadXml(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может скачивать файл в формате законодателя у принятых деклараций только в своём подразделении
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertTrue(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDownloadXml(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не скачивать файл в формате законодателя ни у каких деклараций 
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDownloadXml(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanDelete() {
		// Контролёр УНП может удалять непринятые декларации в любом подразделении
		assertTrue(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertTrue(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDelete(USER_CONTROL_UNP_ID, DECLARATION_ACCEPTED_TB2_ID));
		
		// Контролёр может удалять непринятые декларации только в своём подразделении
		assertTrue(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDelete(USER_CONTROL_TB1_ID, DECLARATION_ACCEPTED_TB2_ID));

		// Оператор не может удалять никаких деклараций
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_CREATED_TB1_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB1_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_CREATED_TB2_ID));
		assertFalse(service.canDelete(USER_OPERATOR_ID, DECLARATION_ACCEPTED_TB2_ID));
	}
	
	@Test
	public void testCanCreate() {
		// Контролёр УНП может создавать декларации в любом подразделении, если они там разрешены
		assertTrue(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertTrue(service.canCreate(USER_CONTROL_UNP_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));
		
		// Контролёр может создавать декларации в своём подразделении, если они там разрешены
		assertTrue(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(service.canCreate(USER_CONTROL_TB1_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));

		// Оператор не может создавать декларации
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB1_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_1_ID, DEPARTMENT_TB2_ID, 1));
		assertFalse(service.canCreate(USER_OPERATOR_ID, DECLARATION_TEMPLATE_2_ID, DEPARTMENT_TB2_ID, 1));
	}
	
}
