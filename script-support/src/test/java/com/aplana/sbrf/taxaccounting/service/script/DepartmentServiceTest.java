package com.aplana.sbrf.taxaccounting.service.script;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.service.script.impl.DepartmentServiceImpl;

/**
 * Тест для сервиса работы с подразделениями
 * @author auldanov
 */
public class DepartmentServiceTest {
	
	 private static DepartmentService service = new DepartmentServiceImpl();

	@BeforeClass
    public static void tearUp() {
	 	DepartmentDao departmentDao = mock(DepartmentDao.class);
	 	
        when(departmentDao.getDepartmentByName("Банк1")).thenReturn(new Department());
	 	when(departmentDao.getDepartmentByName("")).thenReturn(null);

        when(departmentDao.getDepartmentBySbrfCode("123")).thenReturn(new Department());
	 	when(departmentDao.getDepartmentBySbrfCode("321")).thenReturn(null);

	 	ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
    }
	 
	
	@Test
	public void issetName() {
        assertTrue(service.issetName("Банк1"));
        assertFalse(service.issetName(""));
    }

	@Test
	public void issetSbrfCode() {
		assertTrue(service.issetSbrfCode("123"));
		assertFalse(service.issetSbrfCode("321"));
    }

}
