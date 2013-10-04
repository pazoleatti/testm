package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.SourceService;

import static com.aplana.sbrf.taxaccounting.test.DeclarationTypeMockUtils.mockDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentDeclarationTypeMockUtils.mockDepartmentDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.DepartmentMockUtils.mockDepartment;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author dsultanbekov
 */
public class DeclarationDataSearchServiceImplTest {
	private static DeclarationDataSearchServiceImpl service;
	
	private static final int CONTROL_UNP_USER_ID = 1;
	private static final int CONTROL_USER_ID = 2;
	private static final int OPERATOR_USER_ID = 3;
	
	private final static int INCOME_DECLARATION_TYPE_ID_1 = 21;
	private final static int INCOME_DECLARATION_TYPE_ID_2 = 22;

	private final static String LOCAL_IP = "127.0.0.1";

	@BeforeClass
	public static void tearUp() {
		 service = new DeclarationDataSearchServiceImpl();

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

		Department department1 = mockDepartment(1, null, DepartmentType.TERBANK);
		when(departmentDao.getDepartment(1)).thenReturn(department1);
		ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
		
		DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
		DeclarationType incomeDeclarationType1 = mockDeclarationType(INCOME_DECLARATION_TYPE_ID_1, TaxType.INCOME);
		DeclarationType incomeDeclarationType2 = mockDeclarationType(INCOME_DECLARATION_TYPE_ID_2, TaxType.INCOME);
		when(declarationTypeDao.get(INCOME_DECLARATION_TYPE_ID_1)).thenReturn(incomeDeclarationType1);
		when(declarationTypeDao.get(INCOME_DECLARATION_TYPE_ID_2)).thenReturn(incomeDeclarationType2);
		List<DeclarationType> incomeDeclarationTypes = new ArrayList<DeclarationType>(2);		
		incomeDeclarationTypes.add(incomeDeclarationType1);
		incomeDeclarationTypes.add(incomeDeclarationType2);
		when(declarationTypeDao.listAllByTaxType(TaxType.INCOME)).thenReturn(incomeDeclarationTypes);
		ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);
		
		SourceService sourceService = mock(SourceService.class);
		List<DepartmentDeclarationType> incomeDdts = new ArrayList<DepartmentDeclarationType>(1);
		incomeDdts.add(mockDepartmentDeclarationType(1, INCOME_DECLARATION_TYPE_ID_1));
		when(sourceService.getDDTByDepartment(Matchers.eq(1), Matchers.any(TaxType.class))).thenReturn(incomeDdts);
		ReflectionTestUtils.setField(service, "sourceService", sourceService);
	}

	// TODO: сделать тесты для остальных методов!
	
	@Test
	public void testGetAvailableFilterValuesControlUnp() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(CONTROL_UNP_USER_ID, 1, TARole.ROLE_CONTROL_UNP));

		DeclarationDataFilterAvailableValues valuesIncome = service.getFilterAvailableValues(userInfo, TaxType.INCOME);
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
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(CONTROL_USER_ID, 1, TARole.ROLE_CONTROL));

		DeclarationDataFilterAvailableValues valuesIncome = service.getFilterAvailableValues(userInfo, TaxType.INCOME);
		assertEquals(1, valuesIncome.getDeclarationTypes().size());
		assertEquals(INCOME_DECLARATION_TYPE_ID_1, valuesIncome.getDeclarationTypes().get(0).getId());
		assertEquals(1, valuesIncome.getDepartmentIds().size());
		assertTrue(valuesIncome.getDepartmentIds().contains(1));
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testGetAvailableFilterValuesOperator() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(OPERATOR_USER_ID, 1, TARole.ROLE_OPER));

		service.getFilterAvailableValues(userInfo, TaxType.INCOME);
	}
}
