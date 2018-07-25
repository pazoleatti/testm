package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.permissions.UserPermission;
import com.aplana.sbrf.taxaccounting.permissions.UserPermissionSetter;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
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
    private UserPermissionSetter userPermissionSetter;
    private RefBookDepartmentDataService refBookDepartmentDataService;
    private DepartmentService departmentService;

    public UserDataController(SecurityService securityService, RefBookDepartmentDataService refBookDepartmentDataService,
                              UserPermissionSetter userPermissionSetter, DepartmentService departmentService) {
        this.securityService = securityService;
        this.userPermissionSetter = userPermissionSetter;
        this.refBookDepartmentDataService = refBookDepartmentDataService;
        this.departmentService = departmentService;
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
        RefBookDepartment userDepartment = refBookDepartmentDataService.fetchUserDepartment(user);
        Department userTB = departmentService.getParentTB(userDepartment.getId());
        userPermissionSetter.setPermissions(user, UserPermission.VIEW_TAXES, UserPermission.VIEW_TAXES_NDFL, UserPermission.VIEW_TAXES_NDFL_SETTINGS,
                UserPermission.VIEW_TAXES_NDFL_REPORTS, UserPermission.VIEW_TAXES_GENERAL, UserPermission.VIEW_NSI,
                UserPermission.VIEW_ADMINISTRATION_BLOCK, UserPermission.VIEW_ADMINISTRATION_CONFIG, UserPermission.VIEW_ADMINISTRATION_SETTINGS,
                UserPermission.VIEW_MANUAL_USER, UserPermission.VIEW_MANUAL_DESIGNER, UserPermission.VIEW_JOURNAL,
                UserPermission.CREATE_DECLARATION_REPORT, UserPermission.CREATE_DECLARATION_PRIMARY, UserPermission.CREATE_DECLARATION_CONSOLIDATED,
                UserPermission.CREATE_UPLOAD_REPORT, UserPermission.HANDLING_FILE, UserPermission.UPLOAD_FILE, UserPermission.EDIT_GENERAL_PARAMS,
                UserPermission.VIEW_REF_BOOK, UserPermission.EDIT_REF_BOOK, UserPermission.VIEW_ADMINISTRATION_USERS, UserPermission.EDIT_DECLARATION_TYPES_ASSIGNMENT,
                UserPermission.OPEN_DEPARTMENT_REPORT_PERIOD, UserPermission.VIEW_TAXES_SERVICE, UserPermission.VIEW_TAXES_CREATE_APPLICATION_2);

        return new UserDataModel(userInfo, userDepartment, userTB);
    }
}
