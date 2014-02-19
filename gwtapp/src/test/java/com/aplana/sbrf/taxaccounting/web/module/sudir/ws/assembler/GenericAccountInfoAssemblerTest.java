package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.departmentendpoint.TaxAccDepartment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: avanteev
 */
public class GenericAccountInfoAssemblerTest {

    private GenericAccountInfoAssembler assembler = new GenericAccountInfoAssembler();
    ArrayList<Department> departments = new ArrayList<Department>();

    @Before
    public void init(){
        DepartmentService departmentService = mock(DepartmentService.class);
        ReflectionTestUtils.setField(assembler, "departmentService", departmentService);

        Department department1 = new Department();
        department1.setId(1);
        department1.setType(DepartmentType.ROOT_BANK);
        department1.setShortName("first");
        Department department2 = new Department();
        department2.setId(2);
        department2.setParentId(department1.getId());
        department2.setType(DepartmentType.CSKO_PCP);
        department2.setShortName("second");
        Department department3 = new Department();
        department3.setId(3);
        department3.setParentId(department2.getId());
        department3.setType(DepartmentType.CSKO_PCP);
        department3.setShortName("third");
        Department department4 = new Department();
        department4.setId(4);
        department4.setParentId(department3.getId());
        department4.setType(DepartmentType.MANAGEMENT);
        department4.setShortName("forth");
        departments.add(department1);
        departments.add(department2);
        departments.add(department3);
        departments.add(department4);

        when(departmentService.getDepartment(1)).thenReturn(department1);
        when(departmentService.getDepartment(2)).thenReturn(department2);
        when(departmentService.getDepartment(3)).thenReturn(department3);
        when(departmentService.getDepartment(4)).thenReturn(department4);
    }

    @Test
    public void testName(){
        List<TaxAccDepartment> accDepartments = assembler.desassembleDepartments(departments);
        for (TaxAccDepartment department : accDepartments)
            System.out.println(department.getName());
    }

}
