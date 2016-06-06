package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.service.script.impl.DepartmentServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        List<Integer> tempIds = new ArrayList<Integer>();
        tempIds.add(valid.getId());
        tempIds.add(validTB.getId());
        when(departmentDao.listDepartmentIds()).thenReturn(tempIds);

        when(departmentDao.getDepartmentByName("Банк1")).thenReturn(new Department());
        when(departmentDao.getDepartmentByName("")).thenReturn(null);

        when(departmentDao.getDepartmentBySbrfCode("123", true)).thenReturn(new Department());
        when(departmentDao.getDepartmentBySbrfCode("321", true)).thenReturn(null);

        ReflectionTestUtils.setField(service, "departmentDao", departmentDao);
    }

    @Test
    public void getDepartment() {
        Boolean exception;
        assertEquals(service.get(valid.getId()).getName(), valid.getName());
        exception = false;
        try {
            service.get(32134244);  // Must be invalid ID
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }
}
