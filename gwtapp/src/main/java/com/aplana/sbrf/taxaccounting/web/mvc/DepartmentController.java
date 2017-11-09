package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DepartmentController {

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    private DepartmentService departmentService;

    @GetMapping(value = "rest/getBankDepartment")
    public Department getBankDepartment(){
        return departmentService.getBankDepartment();
    }

    @GetMapping(value = "rest/getDepartments")
    public List<Department> getDepartments(){
        return departmentService.listAll();
    }
}
