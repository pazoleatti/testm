package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigFetchingAction;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermission;
import com.aplana.sbrf.taxaccounting.permissions.DepartmentConfigPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с настройками подразделений
 */
@RestController
public class DepartmentConfigController {

    @Autowired
    private DepartmentConfigService departmentConfigService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DepartmentConfigPermissionSetter departmentConfigPermissionSetter;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DepartmentConfigFetchingAction.class, new RequestParamEditor(DepartmentConfigFetchingAction.class));
        binder.registerCustomEditor(DepartmentConfig.class, new RequestParamEditor(DepartmentConfig.class));
        binder.registerCustomEditor(RefBookDepartment.class, new RequestParamEditor(DepartmentConfig.class));
        binder.registerCustomEditor(DepartmentType.class, new RequestParamEditor(DepartmentConfig.class));
    }

    /**
     * Получает настройки подразделений для отображения
     *
     * @param filter       фильтр поиска
     * @param pagingParams параметры пагинации
     * @return возвращает список настроек подразделений
     */
    @GetMapping(value = "/rest/departmentConfig")
    public JqgridPagedList<DepartmentConfig> fetchDepartmentConfig(@RequestParam DepartmentConfigFetchingAction filter, @RequestParam PagingParams pagingParams) {
        if (filter.getDepartmentId() == null) {
            return new JqgridPagedList<>();
        }
        PagingResult<DepartmentConfig> result = departmentConfigService.fetchDepartmentConfigs(filter, pagingParams);

        setDepartmentConfigsPermission(result);

        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.getTotalCount(),
                pagingParams
        );
    }

    private void setDepartmentConfigsPermission(PagingResult<DepartmentConfig> departmentConfigs) {
        for (DepartmentConfig departmentConfig : departmentConfigs) {//noinspection unchecked
            departmentConfigPermissionSetter.setPermissions(departmentConfig,
                    DepartmentConfigPermission.UPDATE, DepartmentConfigPermission.DELETE);
        }
    }

    /**
     * Создание записи настроек подразделений
     */
    @PostMapping(value = "/actions/departmentConfig/create")
    public ActionResult create(@RequestBody DepartmentConfig departmentConfig) {
        return departmentConfigService.create(departmentConfig, securityService.currentUserInfo());
    }

    /**
     * Создание записи настроек подразделений
     */
    @PostMapping(value = "/actions/departmentConfig/update")
    public ActionResult update(@RequestBody DepartmentConfig departmentConfig) {
        return departmentConfigService.update(departmentConfig, securityService.currentUserInfo());
    }

    /**
     * Создание записи настроек подразделений
     */
    @PostMapping(value = "/actions/departmentConfig/delete")
    public ActionResult delete(@RequestBody List<Long> ids) {
        return departmentConfigService.delete(ids, securityService.currentUserInfo());
    }
}
