package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DepartmentController {

    private final DepartmentService departmentService;


    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(Department.class, new RequestParamEditor(Department.class));
    }

    /**
     * Проверка подразделения на наличие дочерних
     */
    @GetMapping(value = "rest/department/{departmentId}", params = "projection=checkHasChildDepartment")
    public Boolean checkHasChildDepartment(@PathVariable Integer departmentId){
        return !departmentService.getChildren(departmentId).isEmpty();
    }


}
