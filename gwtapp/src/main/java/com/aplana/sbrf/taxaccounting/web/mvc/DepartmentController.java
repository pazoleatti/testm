package com.aplana.sbrf.taxaccounting.web.mvc;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;


@RestController
public class DepartmentController {

    private final DepartmentService departmentService;


    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(Department.class, new RequestParamEditor(Department.class));
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Проверка подразделения на наличие дочерних
     *
     * @param departmentId идентификатор проверяемого подразделения
     * @return признак наличия
     */
    @GetMapping(value = "rest/department/{departmentId}", params = "projection=checkHasChildDepartment")
    public Boolean checkHasChildDepartment(@PathVariable Integer departmentId) {
        return !departmentService.getChildren(departmentId).isEmpty();
    }

    /**
     * Получает родительский ТБ для подразделения.
     *
     * @param departmentId иденетификатор подразделения, для которого надо получить терр. банк.
     * @return терр. банк. Возвращает null, если departmentId корневое подразделение.
     * Возвращает переданное подразделение, если оно и есть террбанк.
     */
    @GetMapping(value = "/rest/department/{departmentId}", params = "projection=fetchParentTB")
    public Department fetchParentTB(@PathVariable Integer departmentId) {
        return departmentService.getParentTB(departmentId);
    }

    /**
     * Получение списка названий подразделений с поиском.
     *
     * @param name строка поиска
     */
    @GetMapping(value = "/rest/departments", params = "projection=name")
    public JqgridPagedList<DepartmentName> fetchDepartmentNames(@RequestParam(required = false) String name,
                                                                @RequestParam(required = false) PagingParams pagingParams) {
        PagingResult<DepartmentName> departmentNames = departmentService.searchDepartmentNames(name, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(departmentNames, departmentNames.getTotalCount(), pagingParams);
    }

    /**
     * Выгрузка краткой информации о всех тербанках.
     */
    @GetMapping(value = "/rest/departments", params = "projection=tb")
    public JqgridPagedList<DepartmentShortInfo> fetchAllTBNames(@RequestParam(name = "name", required = false) String filter,
                                                                @RequestParam(required = false) PagingParams pagingParams) {
        PagingResult<DepartmentShortInfo> tbs = departmentService.fetchAllTBShortInfo(filter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(tbs, tbs.getTotalCount(), pagingParams);
    }
}
