package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.permissions.UserPermission;
import com.aplana.sbrf.taxaccounting.permissions.UserPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.UserDataModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы с данными о пользователе
 */
@RestController
public class UserDataController {
    private SecurityService securityService;
    private DepartmentService departmentService;
    private UserPermissionSetter userPermissionSetter;

    public UserDataController(SecurityService securityService, DepartmentService departmentService, UserPermissionSetter userPermissionSetter) {
        this.securityService = securityService;
        this.departmentService = departmentService;
        this.userPermissionSetter = userPermissionSetter;
    }

    /**
     * Получения данных о пользователе
     *
     * @return данные о пользователе
     */
    @GetMapping(value = "/rest/userData")
    public UserDataModel fetchUserData() {
        TAUserInfo userInfo = securityService.currentUserInfo();
        TAUser user = userInfo.getUser();
        Department department = departmentService.getDepartment(user.getDepartmentId());
        userPermissionSetter.setPermissions(user, UserPermission.VIEW_TAXES_NDFL, UserPermission.VIEW_TAXES_NDFL_SETTINGS,
                UserPermission.VIEW_TAXES_NDFL_REPORTS, UserPermission.VIEW_TAXES_GENERAL,
                UserPermission.VIEW_ADMINISTRATION_BLOCK_AND_AUDIT, UserPermission.VIEW_ADMINISTRATION_USERS,
                UserPermission.VIEW_ADMINISTRATION_CONFIG, UserPermission.VIEW_ADMINISTRATION_SETTINGS,
                UserPermission.VIEW_MANUAL_USER, UserPermission.VIEW_MANUAL_DESIGNER);

        return new UserDataModel(userInfo, department.getName());
    }
}
