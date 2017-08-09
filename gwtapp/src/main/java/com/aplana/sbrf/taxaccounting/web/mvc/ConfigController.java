package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.ConfigModel;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

/**
 * Контроллер для работы с конфигурационными данными приложения
 */
@RestController
public class ConfigController {

    private SecurityService securityService;
    private Properties versionInfoProperties;
    private DepartmentService departmentService;

    public ConfigController(SecurityService securityService, Properties versionInfoProperties, DepartmentService departmentService) {
        this.securityService = securityService;
        this.versionInfoProperties = versionInfoProperties;
        this.departmentService = departmentService;
    }

    /**
     * Получения конфигурационных данных приложения
     *
     * @return конфигурационные данные
     */
    @GetMapping(value = "/rest/config")
    public ConfigModel fetchConfig() {
        //TODO https://jira.aplana.com/browse/SBRFNDFL-1688
        String gwtMode = PropertyLoader.isProductionMode() ? "" : "?gwt.codesvr=127.0.0.1:9997";
        TAUserInfo userInfo = securityService.currentUserInfo();
        TAUser user = userInfo.getUser();
        Department department = departmentService.getDepartment(user.getDepartmentId());

        return new ConfigModel(gwtMode, userInfo, versionInfoProperties, department.getName());
    }
}