package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.test.DepartmentFormTypeMockUtils.mockDepartmentFormType;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormDataSearchServiceTest {
	private static FormDataSearchServiceImpl service;
	private final static List<FormType> FORM_TYPES_BY_TAX_TYPE = new ArrayList<FormType>();
	
	private static final int CONTROL_UNP_USER_ID = 1;
	private static final int CONTROL_USER_ID = 2;
	private static final int OPERATOR_USER_ID = 3;
	
	@BeforeClass
	public static void tearUp() {
		service = new FormDataSearchServiceImpl();
		
		TAUser controlUnpUser = mockUser(CONTROL_UNP_USER_ID, 1, TARole.ROLE_CONTROL_UNP),
			controlUser = mockUser(CONTROL_USER_ID, 1, TARole.ROLE_CONTROL),
			operatorUser = mockUser(OPERATOR_USER_ID, 1, TARole.ROLE_OPERATOR);
		
		TAUserDao userDao = mock(TAUserDao.class);
		when(userDao.getUser(CONTROL_UNP_USER_ID)).thenReturn(controlUnpUser);
		when(userDao.getUser(CONTROL_USER_ID)).thenReturn(controlUser);
		when(userDao.getUser(OPERATOR_USER_ID)).thenReturn(operatorUser);
		ReflectionTestUtils.setField(service, "taUserDao", userDao);
		
		FormTypeDao formTypeDao = mock(FormTypeDao.class);
		when(formTypeDao.listAllByTaxType(TaxType.TRANSPORT)).thenReturn(FORM_TYPES_BY_TAX_TYPE);
		ReflectionTestUtils.setField(service, "formTypeDao", formTypeDao);
		
		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		
		List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(1, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(1, 2, FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getByTaxType(1, TaxType.TRANSPORT)).thenReturn(dfts);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(2, 3, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(3, 2, FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getDepartmentSources(1, TaxType.TRANSPORT)).thenReturn(dfts);

		ReflectionTestUtils.setField(service, "departmentFormTypeDao", departmentFormTypeDao);
	}
	
	@Test
	public void testGetAvailableFilterValuesForControlUnp() {
		FormDataFilterAvailableValues values = service.getAvailableFilterValues(CONTROL_UNP_USER_ID, TaxType.TRANSPORT);
		assertTrue(values.getFormTypes() == FORM_TYPES_BY_TAX_TYPE);
		assertEquals(3, values.getKinds().size());
		assertNull(values.getDepartmentIds());
	}

	@Test
	public void testGetAvailableFilterValuesForOperator() {
		FormDataFilterAvailableValues values = service.getAvailableFilterValues(OPERATOR_USER_ID, TaxType.TRANSPORT);
		assertEquals(2, values.getFormTypes().size());
		assertEquals(0, values.getKinds().size());
		assertFalse(values.getKinds().contains(FormDataKind.SUMMARY));
		assertEquals(1, values.getDepartmentIds().size());
		assertTrue(values.getDepartmentIds().contains(1));
	}

	@Test
	public void testGetAvailableFilterValuesForControl() {
		FormDataFilterAvailableValues values = service.getAvailableFilterValues(CONTROL_USER_ID, TaxType.TRANSPORT);
		assertEquals(3, values.getFormTypes().size());
		assertEquals(1, values.getKinds().size());
		assertTrue(values.getKinds().contains(FormDataKind.SUMMARY));
		assertEquals(3, values.getDepartmentIds().size());
		assertTrue(values.getDepartmentIds().contains(1));
		assertTrue(values.getDepartmentIds().contains(2));
		assertTrue(values.getDepartmentIds().contains(3));
	}

	
}
