package com.aplana.sbrf.taxaccounting.service.impl;

import static com.aplana.sbrf.taxaccounting.test.DeclarationTypeMockUtils.mockDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.DeclarationFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;

public class DeclarationServiceImplTest {
	private static DeclarationServiceImpl service;
	
	private static final int CONTROL_UNP_USER_ID = 1;
	private static final int CONTROL_USER_ID = 2;
	private static final int OPERATOR_USER_ID = 3;
	
	private final static int INCOME_DECLARATION_TYPE_ID_1 = 21;
	private final static int INCOME_DECLARATION_TYPE_ID_2 = 22;
	
	@BeforeClass
	public static void tearUp() {
		 service = new DeclarationServiceImpl();
		 
		TAUser controlUnpUser = mockUser(CONTROL_UNP_USER_ID, 1, TARole.ROLE_CONTROL_UNP),
			controlUser = mockUser(CONTROL_USER_ID, 1, TARole.ROLE_CONTROL),
			operatorUser = mockUser(OPERATOR_USER_ID, 1, TARole.ROLE_OPERATOR);
			
		TAUserDao userDao = mock(TAUserDao.class);
		when(userDao.getUser(CONTROL_UNP_USER_ID)).thenReturn(controlUnpUser);
		when(userDao.getUser(CONTROL_USER_ID)).thenReturn(controlUser);
		when(userDao.getUser(OPERATOR_USER_ID)).thenReturn(operatorUser);
		ReflectionTestUtils.setField(service, "userDao", userDao);
		
		DepartmentDeclarationTypeDao departmentDeclarationTypeDao = mock(DepartmentDeclarationTypeDao.class);
		// Декларации по налогу на прибыть есть в подразделениях 1, 2, 3
		Set<Integer> incomeDepartmentIds = new HashSet<Integer>(3);
		incomeDepartmentIds.add(1);
		incomeDepartmentIds.add(2);
		incomeDepartmentIds.add(3);
		when(departmentDeclarationTypeDao.getDepartmentIdsByTaxType(TaxType.INCOME)).thenReturn(incomeDepartmentIds);
		ReflectionTestUtils.setField(service, "departmentDeclarationTypeDao", departmentDeclarationTypeDao);
		

		DepartmentDao departmentDao = mock(DepartmentDao.class);
		// В подразделении 1 есть только декларация INCOME_DECLARATION_TYPE_ID_1
		List<DepartmentDeclarationType> incomeDdts = new ArrayList<DepartmentDeclarationType>(1);
		incomeDdts.add(mockDepartmentDeclarationType(1, INCOME_DECLARATION_TYPE_ID_1));
		Department department1 = mockDepartment(1, null, DepartmentType.TERBANK);
		when(department1.getDepartmentDeclarationTypes()).thenReturn(incomeDdts);
		when(departmentDao.getDepartment(1)).thenReturn(department1);
		ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
		
		DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
		DeclarationType incomeDeclarationType1 = mockDeclarationType(INCOME_DECLARATION_TYPE_ID_1);
		DeclarationType incomeDeclarationType2 = mockDeclarationType(INCOME_DECLARATION_TYPE_ID_2);
		when(declarationTypeDao.get(INCOME_DECLARATION_TYPE_ID_1)).thenReturn(incomeDeclarationType1);
		when(declarationTypeDao.get(INCOME_DECLARATION_TYPE_ID_2)).thenReturn(incomeDeclarationType2);
		List<DeclarationType> incomeDeclarationTypes = new ArrayList<DeclarationType>(2);		
		incomeDeclarationTypes.add(incomeDeclarationType1);
		incomeDeclarationTypes.add(incomeDeclarationType2);
		when(declarationTypeDao.listAllByTaxType(TaxType.INCOME)).thenReturn(incomeDeclarationTypes);
		ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);
	}

	// TODO: сделать тесты для остальных методов!
	
	@Test
	public void testGetAvailableFilterValuesControlUnp() {
		DeclarationFilterAvailableValues valuesIncome = service.getFilterAvailableValues(CONTROL_UNP_USER_ID, TaxType.INCOME);
		assertEquals(2, valuesIncome.getDeclarationTypes().size());
		assertEquals(INCOME_DECLARATION_TYPE_ID_1, valuesIncome.getDeclarationTypes().get(0).getId());
		assertEquals(INCOME_DECLARATION_TYPE_ID_2, valuesIncome.getDeclarationTypes().get(1).getId());
		assertEquals(3, valuesIncome.getDepartmentIds().size());
		assertTrue(valuesIncome.getDepartmentIds().contains(1));
		assertTrue(valuesIncome.getDepartmentIds().contains(2));
		assertTrue(valuesIncome.getDepartmentIds().contains(3));
	}
	
	@Test
	public void testGetAvailableFilterValuesControl() {
		DeclarationFilterAvailableValues valuesIncome = service.getFilterAvailableValues(CONTROL_USER_ID, TaxType.INCOME);
		assertEquals(1, valuesIncome.getDeclarationTypes().size());
		assertEquals(INCOME_DECLARATION_TYPE_ID_1, valuesIncome.getDeclarationTypes().get(0).getId());
		assertEquals(1, valuesIncome.getDepartmentIds().size());
		assertTrue(valuesIncome.getDepartmentIds().contains(1));
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testGetAvailableFilterValuesOperator() {
		service.getFilterAvailableValues(OPERATOR_USER_ID, TaxType.INCOME);
	}
	
}
