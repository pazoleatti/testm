package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.test.DeclarationTypeMockUtils.mockDeclarationType;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

//		DepartmentDeclarationTypeDao departmentDeclarationTypeDao = mock(DepartmentDeclarationTypeDao.class);
//		// Декларации по налогу на прибыть есть в подразделениях 1, 2, 3
//		Set<Integer> incomeDepartmentIds = new HashSet<Integer>(3);
//		incomeDepartmentIds.add(1);
//		incomeDepartmentIds.add(2);
//		incomeDepartmentIds.add(3);
//		when(departmentDeclarationTypeDao.getDepartmentIdsByTaxType(TaxType.INCOME)).thenReturn(incomeDepartmentIds);
//		ReflectionTestUtils.setField(service, "departmentDeclarationTypeDao", departmentDeclarationTypeDao);

		// В подразделении 1 есть только декларация INCOME_DECLARATION_TYPE_ID_1
		
		DeclarationTypeDao declarationTypeDao = mock(DeclarationTypeDao.class);
		DeclarationType incomeDeclarationType1 = mockDeclarationType(INCOME_DECLARATION_TYPE_ID_1, TaxType.INCOME);
		DeclarationType incomeDeclarationType2 = mockDeclarationType(INCOME_DECLARATION_TYPE_ID_2, TaxType.INCOME);
		when(declarationTypeDao.get(INCOME_DECLARATION_TYPE_ID_1)).thenReturn(incomeDeclarationType1);
		when(declarationTypeDao.get(INCOME_DECLARATION_TYPE_ID_2)).thenReturn(incomeDeclarationType2);
		List<DeclarationType> incomeDeclarationTypes = new ArrayList<DeclarationType>(2);		
		incomeDeclarationTypes.add(incomeDeclarationType1);
		incomeDeclarationTypes.add(incomeDeclarationType2);
		when(declarationTypeDao.listAllByTaxType(TaxType.NDFL)).thenReturn(incomeDeclarationTypes);
		ReflectionTestUtils.setField(service, "declarationTypeDao", declarationTypeDao);

        DepartmentService departmentService = mock(DepartmentService.class);
        when(departmentService.getTaxFormDepartments(any(TAUser.class), any(TaxType.class), any(Date.class), any(Date.class))).thenReturn(Arrays.asList(1));
        Department dep1 = new Department();
        dep1.setId(1);
        Department dep2 = new Department();
        dep2.setId(2);
        Department dep3 = new Department();
        dep3.setId(3);
        when(departmentService.listAll()).thenReturn(Arrays.asList(dep1, dep2, dep3));
        ReflectionTestUtils.setField(service, "departmentService", departmentService);
    }

	// TODO: сделать тесты для остальных методов!
	
	@Test
	public void testGetAvailableFilterValuesControlUnp() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(CONTROL_UNP_USER_ID, 1, TARole.N_ROLE_CONTROL_UNP));

		DeclarationDataFilterAvailableValues valuesIncome = service.getFilterAvailableValues(userInfo, TaxType.NDFL);
		assertEquals(2, valuesIncome.getDeclarationTypes().size());
		assertEquals(INCOME_DECLARATION_TYPE_ID_1, valuesIncome.getDeclarationTypes().get(0).getId());
		assertEquals(INCOME_DECLARATION_TYPE_ID_2, valuesIncome.getDeclarationTypes().get(1).getId());
		assertEquals(1, valuesIncome.getDepartmentIds().size());
        System.out.println(valuesIncome.getDepartmentIds());

		assertTrue(valuesIncome.getDepartmentIds().containsAll(Arrays.asList(1)));
	}
	
    @Test
	public void testGetAvailableFilterValuesOperator() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setIp(LOCAL_IP);
		userInfo.setUser(mockUser(OPERATOR_USER_ID, 1, TARole.N_ROLE_OPER));
		service.getFilterAvailableValues(userInfo, TaxType.NDFL);
	}
}
