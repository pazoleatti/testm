package com.aplana.sbrf.taxaccounting.service.script;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.service.script.impl.DepartmentServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Тест для сервиса работы с подразделениями
 *
 * @author auldanov
 */
public class DepartmentServiceTest {

    private static DepartmentService service = new DepartmentServiceImpl();
    private static Department valid = new Department();
    private static Department validTB = new Department();

    @BeforeClass
    public static void tearUp() {
        DepartmentDao departmentDao = mock(DepartmentDao.class);

        valid.setId(1);
        valid.setName("Банк");
        valid.setTbIndex("1");

        validTB.setId(2);
        validTB.setName("ТерБанк для Банк");
        validTB.setTbIndex("1");
        validTB.setType(DepartmentType.TERR_BANK);

        List<Department> temp = new ArrayList<Department>();
        temp.add(valid);
        temp.add(validTB);
        when(departmentDao.listDepartments()).thenReturn(temp);

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

    @Test
    public void getDepartment() {
        assertEquals(service.get(valid.getName()).getId(), valid.getId());
        Boolean exception = false;
        try {
            service.get("NOT VALID NAME");
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
        assertEquals(service.get(valid.getId()).getName(), valid.getName());
        exception = false;
        try {
            service.get(32134244);  // Must be invalid ID
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testGetTB() {
        assertEquals(service.getTB(valid.getTbIndex()).getId(), validTB.getId());   // Должен находить валидный ТерБанк
        Boolean exception = false;
        try {
            service.getTB("NOT VALID tbIndex"); // Must be invalid tbIndex
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

}
