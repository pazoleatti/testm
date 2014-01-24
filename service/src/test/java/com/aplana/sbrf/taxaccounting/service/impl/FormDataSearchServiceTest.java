package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.test.DepartmentFormTypeMockUtils.mockDepartmentFormType;
import static com.aplana.sbrf.taxaccounting.test.UserMockUtils.mockUser;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
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

        DepartmentService departmentService = mock(DepartmentService.class);
        when(departmentService.getTaxFormDepartments(any(TAUser.class), anyListOf(TaxType.class))).thenReturn(asList(1, 2, 3));

		FormTypeDao formTypeDao = mock(FormTypeDao.class);
		when(formTypeDao.getByTaxType(TaxType.TRANSPORT)).thenReturn(FORM_TYPES_BY_TAX_TYPE);
		ReflectionTestUtils.setField(service, "formTypeDao", formTypeDao);
		
		DepartmentFormTypeDao departmentFormTypeDao = mock(DepartmentFormTypeDao.class);
		
		List<DepartmentFormType> dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(1, 1, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(1, 2, FormDataKind.SUMMARY));
		when(departmentFormTypeDao.getByTaxType(1, TaxType.TRANSPORT)).thenReturn(dfts);
		
		dfts = new ArrayList<DepartmentFormType>();
		dfts.add(mockDepartmentFormType(2, 3, FormDataKind.SUMMARY));
		dfts.add(mockDepartmentFormType(3, 2, FormDataKind.SUMMARY));
        dfts.add(mockDepartmentFormType(1, 2, FormDataKind.PRIMARY));
		when(departmentFormTypeDao.getDepartmentSources(1, TaxType.TRANSPORT)).thenReturn(dfts);
        ReflectionTestUtils.setField(service, "departmentService", departmentService);

        ReflectionTestUtils.setField(service, "formDataAccessService", new FormDataAccessServiceImpl());
	}

	@Test
	public void testGetAvailableFilterValuesForControlUnp() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setUser(mockUser(CONTROL_UNP_USER_ID, 1, TARole.ROLE_CONTROL_UNP));
		FormDataFilterAvailableValues values = service.getAvailableFilterValues(userInfo, TaxType.TRANSPORT);
		assertTrue(values.getFormTypes() == FORM_TYPES_BY_TAX_TYPE);
		assertEquals(4, values.getKinds().size());
        assertTrue(values.getKinds().containsAll(asList(FormDataKind.PRIMARY, FormDataKind.CONSOLIDATED,
                FormDataKind.SUMMARY, null)));
        assertEquals(3, values.getDepartmentIds().size());
        assertTrue(values.getDepartmentIds().containsAll(asList(1, 2, 3)));
	}

	@Test
	public void testGetAvailableFilterValuesForOperator() {
        try {
            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setUser(mockUser(OPERATOR_USER_ID, 1, TARole.ROLE_OPER));
            FormDataFilterAvailableValues values = service.getAvailableFilterValues(userInfo, TaxType.TRANSPORT);
            assertEquals(0, values.getFormTypes().size());
            assertEquals(2, values.getKinds().size());
            assertTrue(values.getKinds().containsAll(asList(FormDataKind.PRIMARY, null)));
            assertFalse(values.getKinds().contains(FormDataKind.SUMMARY));
            assertFalse(values.getKinds().contains(FormDataKind.CONSOLIDATED));
            assertEquals(3, values.getDepartmentIds().size());
            assertTrue(values.getDepartmentIds().containsAll(asList(1, 2, 3)));
            assertTrue(values.getDepartmentIds().contains(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Test
	public void testGetAvailableFilterValuesForControl() {
		TAUserInfo userInfo = new TAUserInfo();
		userInfo.setUser(mockUser(CONTROL_USER_ID, 1, TARole.ROLE_CONTROL));
		FormDataFilterAvailableValues values = service.getAvailableFilterValues(userInfo, TaxType.TRANSPORT);
		assertEquals(0, values.getFormTypes().size());
		assertEquals(4, values.getKinds().size());
        assertTrue(values.getKinds().containsAll(asList(FormDataKind.PRIMARY, FormDataKind.CONSOLIDATED,
                FormDataKind.SUMMARY, null)));
		assertEquals(3, values.getDepartmentIds().size());
        assertTrue(values.getDepartmentIds().containsAll(asList(1, 2, 3)));
	}
}
